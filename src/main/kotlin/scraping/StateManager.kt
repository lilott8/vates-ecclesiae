package us.cedarfarm.scraping

import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.db.models.CrawlerState

fun getState(state: CrawlerState, record: CorpusDao): (CorpusDao) -> CrawlerState {
    return when(state) {
        CrawlerState.PENDING -> ::fetchPage
        CrawlerState.EXTRACT_LINKS -> ::extractLinks
        else -> ::complete
    }
}