package us.cedarfarm.config

import kotlinx.serialization.Serializable

@Serializable
data class DbConfig(
    val host: String,
    val user: String,
    val password: String,
    val port: Int,
    val dbName: String,
    val maximumPoolSize: Int = 10
) {
    fun getJdbcUrl(): String {
        return "jdbc:postgresql://${host}:${port}/${dbName}"
    }
}

@Serializable
data class ScraperConfig(
    val seed: List<String>,
    val scrapeInterval: Long,
    val corpusDir: String,
    val maxConcurrentTasks: Int,
    val requestDelay: Double,
    val perDomainDelay: Double
)

@Serializable
data class AppConfig(
    val db: DbConfig,
    val scraper: ScraperConfig
)