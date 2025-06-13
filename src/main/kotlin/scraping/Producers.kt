package us.cedarfarm.scraping

import kotlinx.coroutines.channels.Channel
import us.cedarfarm.db.dao.CorpusDao
import db.dal.CorpusDal
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import org.apache.logging.log4j.kotlin.logger
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.utils.calculateWindow

private val log = logger("Producer")

suspend fun produceRecords(channel: SendChannel<CorpusDao>, config: ScraperConfig) {
    val dal = CorpusDal()
    supervisorScope {
        launch(Dispatchers.IO + CoroutineName("Producer")) {
            while (isActive) {
                log.info("Loading producer.")
                val records = findRecords(dal, config)

                log.info("Producer found: ${records.size}")
                records.forEach { state ->
                    log.info("Publishing ${state.url} to channel")
                    channel.send(state)
                }

                val delayTime = if (records.isEmpty()) { 10000L } else { 1000L }
                log.info("Producer sleeping for $delayTime ms")
                delay(delayTime)
            }
        }
    }
}

fun findRecords(dal: CorpusDal, config: ScraperConfig): List<CorpusDao> {
    return dal.findCrawlable(calculateWindow(config.scrapeInterval))
}