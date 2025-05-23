package us.cedarfarm.scraping

import db.dal.CorpusDal
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.logging.log4j.kotlin.logger
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.utils.HttpClientSingleton
import us.cedarfarm.utils.getHostFromUrl
import us.cedarfarm.utils.getPathFromUrl

private val log = logger("Controller")

fun runScrapers(config: ScraperConfig, dal: CorpusDal?) = runBlocking {
    val channel = Channel<CorpusDao>(Channel.UNLIMITED)
    val job = SupervisorJob()

    val scope = CoroutineScope(Dispatchers.Default + job)

    val client = HttpClientSingleton.instance
    scope.launch {produceRecords(channel, config)}
    scope.launch { startConsumers(channel, client, config) }
    scope.coroutineContext[Job]?.join()
}

fun initializeCathedrales(config: ScraperConfig, dal: CorpusDal) {
    val list = dal.getAll(5)
    list.isEmpty().apply {
        log.info("No records found in DB, initializing DB with seeds")
        config.seed.forEach { seed ->
            log.info("Inserting seed $seed")
            val host = getHostFromUrl(seed)
            val path = getPathFromUrl(seed)
            dal.create(seed, host, "", path)
        }
        log.info("Completed initializing db with seed data")
    }
}