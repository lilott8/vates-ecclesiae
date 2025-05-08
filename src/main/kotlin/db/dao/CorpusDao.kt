package us.cedarfarm.db.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import us.cedarfarm.db.models.CorpusTable

class CorpusDao(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<CorpusDao>(CorpusTable)

    var url by CorpusTable.url
    var domain  by CorpusTable.domain
    var path by CorpusTable.path
    var pageTitle by CorpusTable.pageTitle
    var hash by CorpusTable.hash
    var state by CorpusTable.state
    var timesCrawled by CorpusTable.timesCrawled
    var firstSeen by CorpusTable.firstSeen
    var lastUpdate by CorpusTable.lastUpdated
}