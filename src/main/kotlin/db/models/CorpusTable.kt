package us.cedarfarm.db.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.Timestamp
import java.time.Instant

object CorpusTable : IntIdTable("corpus") {
    val url = text("url")
    val domain = varchar("domain", 2048).index()
    val path = varchar("path", 1024)
    val pageTitle = varchar("page_title", 2048)
    val hash = varchar("hash", 1024).index()
    val state = enumerationByName<CrawlerState>("state", 255, CrawlerState::class)
    val timesCrawled = integer("times_crawled").default(0)
    val firstSeen = timestamp("first_crawled").clientDefault { Instant.now() }
    val lastUpdated = timestamp("last_updated").clientDefault { Instant.now() }
}