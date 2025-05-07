package us.cedarfarm.config

import kotlinx.serialization.Serializable

@Serializable
data class DbConfig(
    val host: String,
    val user: String,
    val password: String,
    val port: Int,
    val db_name: String
)

@Serializable
data class ScraperConfig(
    val seed: List<String>,
    val scrape_interval: Long,
    val corpus_dir: String,
    val max_concurrent_tasks: Int,
    val request_delay: Double,
    val per_domain_delay: Double
)

@Serializable
data class AppConfig(
    val db: DbConfig,
    val scraper: ScraperConfig
)