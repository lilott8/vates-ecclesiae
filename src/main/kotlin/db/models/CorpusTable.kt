package us.cedarfarm.db.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.Timestamp
import java.time.Instant

object CorpusTable : IntIdTable("corpus") {
    val url = text("url")
    val url_hash = varchar("url_hash", 1024).index()
    val domain = varchar("domain", 2048).index()
    val pageTitle = varchar("page_title", 2048)
    val document_path = varchar("document_path", 1024)
    val document_hash = varchar("document_hash", 1024).index()
    val state = enumerationByName<CrawlerState>("state", 255, CrawlerState::class)
    val timesCrawled = integer("times_crawled").default(0)
    val firstSeen = timestamp("first_crawled").clientDefault { Instant.now() }
    val lastUpdated = timestamp("last_updated").clientDefault { Instant.now() }
}