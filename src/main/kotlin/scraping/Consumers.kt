package us.cedarfarm.scraping

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jsoup.Jsoup
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.utils.toSHA256

suspend fun startConsumers(channel: Channel<CorpusDao>, client: HttpClient, config: ScraperConfig) {
    supervisorScope {
        repeat(config.maxConcurrentTasks) { workerId ->
            launch(Dispatchers.IO + CoroutineName("Consumer-${workerId}")) {
                for (record in channel) {
                    kotlin.runCatching {
                        consume(client, )
                    }
                }
            }
        }
    }
}

fun consume(client: HttpClient): suspend (CorpusDao) -> Unit {
    return { record -> scrape(client, record) }
}

suspend fun scrape(client: HttpClient, record: CorpusDao) {
    runCatching {
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