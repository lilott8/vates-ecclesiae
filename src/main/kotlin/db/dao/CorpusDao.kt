package us.cedarfarm.db.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import us.cedarfarm.db.models.CorpusTable

class CorpusDao(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<CorpusDao>(CorpusTable)

    var url by CorpusTable.url
    var urlHash by CorpusTable.url_hash
    var domain  by CorpusTable.domain
    var pageTitle by CorpusTable.pageTitle
    var documentPath by CorpusTable.document_path
    var documentHash by CorpusTable.document_hash
    var state by CorpusTable.state
    var timesCrawled by CorpusTable.timesCrawled
    var firstSeen by CorpusTable.firstSeen
    var lastUpdate by CorpusTable.lastUpdated
}