package us.cedarfarm.scraping

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
    supervisorScope {
        repeat(config.maxConcurrentTasks) { workerId ->
            launch(Dispatchers.IO + CoroutineName("Consumer-${workerId}")) {
                for (record in channel) {
                    kotlin.runCatching {
                        log.info("Processing: ${record.url}")
                        consume(client, record)
                    }
                }
            }
        }
    }
}

fun consume(client: HttpClient, record: CorpusDao): suspend (CorpusDao) -> Unit {
    return { _ ->
        log.info("in consume for: ${record.url}")
        scrape(client, record)
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



fun fetchPage(record:CorpusDao): CrawlerState {
    return CrawlerState.EXTRACT_LINKS
}

fun extractLinks(record: CorpusDao): CrawlerState {
    return CrawlerState.COMPLETE
}

fun complete(record: CorpusDao): CrawlerState {
    return CrawlerState.COMPLETE
}