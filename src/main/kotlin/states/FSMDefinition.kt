package us.cedarfarm.states

import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.scraping.*

val crawlerFSM = stateMachine {
    state(CrawlerState.PENDING) {
        transitionsTo(CrawlerState.FETCH_PAGE, CrawlerState.FAILED)
        handle(::handlePending)
    }
    state(CrawlerState.FETCH_PAGE) {
        transitionsTo(CrawlerState.EXTRACT_LINKS, CrawlerState.FAILED)
        handle(::handleFetchPage)
    }
    state(CrawlerState.EXTRACT_LINKS) {
        transitionsTo(CrawlerState.COMPLETE, CrawlerState.FAILED)
        handle(::handleExtractLinks)
    }
    state(CrawlerState.COMPLETE) {
        handle(::handleComplete)
    }
    state(CrawlerState.FAILED) {
        handle(::handleFailed)
    }
}