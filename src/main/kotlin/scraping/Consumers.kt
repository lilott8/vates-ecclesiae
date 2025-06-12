package us.cedarfarm.scraping

import db.dal.CorpusDal
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.io.files.Path
import org.apache.logging.log4j.kotlin.logger
import org.jsoup.Jsoup
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.db.models.StateContext
import us.cedarfarm.states.crawlerFSM
import us.cedarfarm.utils.getHostFromUrl
import us.cedarfarm.utils.toSHA256
import java.io.File
import java.util.UUID

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
                                val context = StateContext(record, config, client, CorpusDal())
                                log.info("router has ${record.url} in state: ${record.state}")
                                crawlerFSM.run(context)
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

suspend fun handlePending(context: StateContext): StateContext {
    log.info("handlePending ${context.record.url}'s state: ${context.record.state}")
    val updated = context.dal.updateAndGet(context.record.id.value, context.record.timesCrawled, CrawlerState.FETCH_PAGE)!!
    log.info("handlePending changed ${updated.url} to state: ${updated.state}")
    return context.copy(record = updated)
}

suspend fun handleFetchPage(context: StateContext): StateContext {
    log.info("handleFetchPage for ${context.record.url}")
    val path = Path(context.config.corpusDir, "${UUID.randomUUID()}.html")
    var newRecord: CorpusDao = context.record
    try {
        val response = context.client.get(context.record.url)

        if(response.status.isSuccess()) {
            val body = response.bodyAsText()

            // Extract the page title from HTML (simple regex example)
            val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
            val pageTitle = titleRegex.find(body)?.groups?.get(1)?.value ?: ""

            val hash = body.toSHA256()
            log.info("Writing ${context.record.url} to $path")
            File(path.toString()).writeText(body)

            newRecord = context.dal.updateAndGet(context.record.id.value,
                state = CrawlerState.EXTRACT_LINKS,
                timesCrawled = context.record.timesCrawled+1,
                documentPath = path.toString(),
                pageTitle = pageTitle,
                documentHash = hash)!!
        }

    } catch(e: Exception) {
        log.error("Error fetching page, updating record, or writing results to $path")
        log.error(e)
    }
    return context.copy(record = newRecord)
}

suspend fun handleExtractLinks(context: StateContext): StateContext {
    log.info("handleExtractLinks for ${context.record.url}")
    val document = Jsoup.parse(File(context.record.documentPath))
    var newRecord = context.record
    var insertedUrlCount = 0
    var skippedUrlCount = 0

    // Extract and normalize links
    val links = document.select("a[href]")
        .mapNotNull { it.absUrl("href").takeIf { href -> href.isNotBlank() } }

    val totalUrlCount = links.size
    links.forEach {
        val result = context.dal.conditionalCreate(it, domain = getHostFromUrl(it) )
        result?.let {
            insertedUrlCount++
        } ?: skippedUrlCount++
    }
    log.info("Total links: $totalUrlCount, Inserted: $insertedUrlCount, Skipped: $skippedUrlCount")

    if (skippedUrlCount + insertedUrlCount != totalUrlCount) {
        log.warn("Skipped Count($skippedUrlCount) + Inserted($insertedUrlCount) != Total($totalUrlCount)")
    }

    newRecord = context.dal.updateAndGet(context.record.id.value, state = CrawlerState.COMPLETE)!!
    return context.copy(record = newRecord)
}

suspend fun handleComplete(context: StateContext): StateContext {
    log.info("handleComplete for ${context.record.url}")
    return context
}

suspend fun handleFailed(context: StateContext): StateContext {
    log.info("handleFailed for ${context.record.url}")
    return context
}