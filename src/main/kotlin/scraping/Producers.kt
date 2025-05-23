package us.cedarfarm.scraping

import kotlinx.coroutines.channels.Channel
import us.cedarfarm.db.dao.CorpusDao
import db.dal.CorpusDal
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.logger
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.utils.calculateWindow

private val log = logger("Producer")

suspend fun produceRecords(channel: Channel<CorpusDao>, config: ScraperConfig) {
    val dal = CorpusDal()
    supervisorScope {
        launch(Dispatchers.IO + CoroutineName("Producer")) {
            val records = findRecords(dal, config)
            if (records.isEmpty()) {
                records.forEach { state ->
                    log.info("Publishing ${state.url} to channel")
                    channel.send(state)
                }
            }
            delay(1000)
        }
    }
}

fun findRecords(dal: CorpusDal, config: ScraperConfig): List<CorpusDao> {
    return dal.findCrawlable(calculateWindow(config.scrapeInterval))
}