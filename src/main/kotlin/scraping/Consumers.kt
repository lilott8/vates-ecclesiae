package us.cedarfarm.scraping

import db.dal.CorpusDal
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.logging.log4j.kotlin.logger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jsoup.Jsoup
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.utils.toSHA256

private val log = logger("Consumer")

suspend fun startConsumers(channel: Channel<CorpusDao>, client: HttpClient, config: ScraperConfig) {
    val terminals = setOf(CrawlerState.COMPLETE, CrawlerState.FAILED)
    supervisorScope {
        repeat(config.maxConcurrentTasks) { workerId ->
            launch(Dispatchers.IO + CoroutineName("Consumer-${workerId}")) {
                for (c in channel) {
                    var record = c
                    kotlin.runCatching {
                        try {
                            log.info("Processing: ${record.url}")
                            while (record.state !in terminals) {
                                log.info("router has ${record.url} in state: ${record.state}")
                                record = record.state.getHandler()(record, CorpusDal(), client)
                            }
                        } catch(e: Exception) {
                            log.error(e)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

fun consume(client: HttpClient, record: CorpusDao): suspend (CorpusDao) -> Unit {
    return { _ ->
        log.info("in consume for: ${record.url}")

//        scrape(client, record)
    }
}

suspend fun scrape(client: HttpClient, record: CorpusDao) {
    runCatching {
        log.info("Issuing request for: ${record.url}")
        val html = client.get(record.url).bodyAsText()
        val document = Jsoup.parse(html)
        val title = document.title()

        val host = Url(record.url).host
        val hash = record.url.toSHA256()

        // get links on page
        val links = document.select("a[href]")
            .mapNotNull { it.attr("abs:href").takeIf { href -> href.isNotBlank() } }
            .distinct()
        // insert links into db


        newSuspendedTransaction {
            // Update record with new information.

        }

    }
}

fun handlePending(record: CorpusDao, dal: CorpusDal, client: HttpClient): CorpusDao {
    log.info("handlePending ${record.url}'s state: ${record.state}")
    dal.update(record.id.value, record.timesCrawled, CrawlerState.FETCH_PAGE)
    val updated = dal.findById(record.id.value)!!
    log.info("handlePending changed ${updated.url} to state: ${updated.state}")
    return updated
}

fun handleFetchPage(record: CorpusDao, dal: CorpusDal, client: HttpClient): CorpusDao {
    log.info("handleFetchPage for ${record.url}")
    record.state = CrawlerState.EXTRACT_LINKS
    return record
}

fun handleExtractLinks(record: CorpusDao, dal: CorpusDal, client: HttpClient): CorpusDao {
    log.info("handleExtractLinks for ${record.url}")
    record.state = CrawlerState.COMPLETE
    return record
}

fun handleComplete(record: CorpusDao, dal: CorpusDal, client: HttpClient): CorpusDao {
    log.info("handleComplete for ${record.url}")
    return record
}

fun handleFailed(record: CorpusDao, dal: CorpusDal, client: HttpClient): CorpusDao {
    log.info("handleFailed for ${record.url}")
    return record
}