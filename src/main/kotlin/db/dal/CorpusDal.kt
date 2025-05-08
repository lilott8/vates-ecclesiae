package us.cedarfarm.db.service

import org.jetbrains.exposed.sql.transactions.transaction
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CorpusTable
import us.cedarfarm.db.models.CrawlerState

class CorpusDal {
    fun create(url: String, domain: String, pageTitle: String): CorpusDao = transaction {
        CorpusDao.new {
            this.url = url
            this.domain = domain
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

    fun getAll(): List<CorpusDao> = transaction {
        CorpusDao.all().toList()
    }

    fun delete(id: Int): Boolean = transaction {
        val corpus = CorpusDao.findById(id) ?: return@transaction false
        corpus.delete()
        true
    }

    fun findByHash(hash: String): CorpusDao? = transaction {
        CorpusDao.find{ CorpusTable.hash eq hash}.firstOrNull()
    }
}