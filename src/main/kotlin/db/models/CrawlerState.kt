package us.cedarfarm.db.models

import io.ktor.util.*

enum class CrawlerState() {
    PENDING, IN_PROGRESS, COMPLETE, FAILED;

    companion object {
        fun fromString(name: String): CrawlerState {
            return when(name.toLowerCasePreservingASCIIRules()) {
                "pending" -> PENDING
                "in_progress" -> IN_PROGRESS
                "complete" -> COMPLETE
                "failed" -> FAILED
                else -> PENDING
            }
        }
    }
}