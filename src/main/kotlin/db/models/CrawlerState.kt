package us.cedarfarm.db.models

import io.ktor.util.*

enum class CrawlerState {
    PENDING, FETCH_PAGE, EXTRACT_LINKS, COMPLETE, FAILED;

    companion object {
        fun fromString(name: String): CrawlerState {
            return when(name.toLowerCasePreservingASCIIRules()) {
                "pending" -> PENDING
                "fetch_page" -> FETCH_PAGE
                "extract_links" -> EXTRACT_LINKS
                "complete" -> COMPLETE
                "failed" -> FAILED
                else -> PENDING
            }
        }
    }
}