package us.cedarfarm.db.models

import db.dal.CorpusDal
import io.ktor.client.*
import io.ktor.util.*
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.scraping.*

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

        fun terminals(): List<CrawlerState> {
            return listOf(COMPLETE, FAILED)
        }
    }
}