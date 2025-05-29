package db.dal

import org.apache.logging.log4j.kotlin.logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CorpusTable
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.utils.toSHA256
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class CorpusDal {

    private val log = logger()
    private val crawlable: List<CrawlerState> = listOf(CrawlerState.PENDING)

    fun create(url: String, domain: String, pageTitle: String, path: String): CorpusDao = transaction {
        CorpusDao.new {
            this.url = url
            this.domain = domain
            this.path = path
            this.hash = ""
            this.pageTitle = pageTitle
            this.state = CrawlerState.PENDING
            this.timesCrawled = 0
        }
    }

    fun getByState(state: CrawlerState): List<CorpusDao> = transaction {
        CorpusDao.find { CorpusTable.state eq state}.toList()
    }

    fun update(id: Int, timesCrawled: Int, state: CrawlerState?, hash: String?, path: String?, pageTitle: String?): Boolean = transaction {
        val corpus = CorpusDao.findById(id) ?: return@transaction false
        timesCrawled.let{corpus.timesCrawled = timesCrawled}
        state?.let{corpus.state = state}
        hash?.let{corpus.hash = hash}
        path?.let{corpus.path = path}
        pageTitle?.let{corpus.pageTitle = pageTitle}
        true
    }

    fun getAll(limit: Int = Integer.MAX_VALUE): List<CorpusDao> {
        return transaction {
            addLogger(MyQueryLogger)
            val result = CorpusDao.all().limit(limit).toList()
            log.info("return ${result.size} records")
            result
        }
    }

    fun delete(id: Int): Boolean = transaction {
        val corpus = CorpusDao.findById(id) ?: return@transaction false
        corpus.delete()
        true
    }

    fun findByHash(hash: String): CorpusDao? = transaction {
        CorpusDao.find{ CorpusTable.hash eq hash}.firstOrNull()
    }

    fun findCrawlable(window: Instant): List<CorpusDao> = transaction {
        CorpusDao.find{ CorpusTable.state inList crawlable or (CorpusTable.lastUpdated greater window) }.toList()
    }
}

object MyQueryLogger : SqlLogger {
    override
    fun log(context: StatementContext, transaction: Transaction) {
        //Customize how you want your logging, for example, using SLF4j
        val logger = logger()
        logger.info("SQL: ${context.expandArgs(transaction)}")
        logger.info(context.sql(transaction))
    }
}
