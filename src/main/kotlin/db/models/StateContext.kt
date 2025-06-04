package us.cedarfarm.db.models

import db.dal.CorpusDal
import io.ktor.client.*
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.dao.CorpusDao

data class StateContext(
    val record: CorpusDao,
    val config: ScraperConfig,
    val client: HttpClient,
    val dal: CorpusDal
)
