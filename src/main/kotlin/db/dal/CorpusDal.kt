package db.dal

import org.apache.logging.log4j.kotlin.logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CorpusTable
import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.utils.toSHA256
import java.time.Instant

class CorpusDal {

    private val log = logger()
    private val crawlable: List<CrawlerState> = listOf(CrawlerState.PENDING)

    fun create(url: String, domain: String, pageTitle: String, path: String): CorpusDao = transaction {
        CorpusDao.new {
            this.url = url
            this.urlHash = url.toSHA256()
            this.domain = domain
            this.documentPath = path
            this.documentHash = ""
            this.pageTitle = pageTitle
            this.state = CrawlerState.PENDING
            this.timesCrawled = 0
        }
    }

    fun getByState(state: CrawlerState): List<CorpusDao> = transaction {
        CorpusDao.find { CorpusTable.state eq state}.toList()
    }

    fun update(id: Int, timesCrawled: Int? = null, state: CrawlerState? = null, urlHash: String? = null, documentHash: String? = null, documentPath: String? = null, pageTitle: String? = null): Boolean = transaction {
        val corpus = CorpusDao.findById(id) ?: return@transaction false
        timesCrawled?.let{corpus.timesCrawled = timesCrawled}
        urlHash?.let{corpus.urlHash = urlHash}
        state?.let{corpus.state = state}
        documentHash?.let{corpus.documentHash = documentHash}
        documentPath?.let{corpus.documentPath = documentPath}
        pageTitle?.let{corpus.pageTitle = pageTitle}
        true
    }

    fun updateAndGet(id: Int, timesCrawled: Int? = null, state: CrawlerState? = null, urlHash: String? = null, documentHash: String? = null, documentPath: String? = null, pageTitle: String? = null): CorpusDao? = transaction {
        update(id, timesCrawled, state, urlHash, documentHash, documentPath, pageTitle)
        CorpusDao.findById(id)
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

    fun findByUrlHash(hash: String): CorpusDao? = transaction {
        CorpusDao.find{ CorpusTable.url_hash eq hash}.firstOrNull()
    }

    fun findCrawlable(window: Instant): List<CorpusDao> = transaction {
        CorpusDao.find{ CorpusTable.state inList crawlable or (CorpusTable.lastUpdated greater window) }.toList()
    }

    fun findById(entityId: Int): CorpusDao? = transaction {
        CorpusDao.find{CorpusTable.id eq entityId}.firstOrNull()
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
