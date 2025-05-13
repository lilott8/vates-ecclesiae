package us.cedarfarm

import com.github.ajalt.clikt.core.main
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger
import org.jetbrains.exposed.dao.id.EntityID
import us.cedarfarm.cli.Cli
import us.cedarfarm.config.AppConfig
import us.cedarfarm.config.ScraperConfig
import us.cedarfarm.db.Database
import us.cedarfarm.db.Migrator
import us.cedarfarm.db.dao.CorpusDao
import us.cedarfarm.scraping.runScrapers

val log = logger("main")

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    log.info("Hello World!")
    val cli = Cli()
    cli.main(args)

    val hocon = ConfigFactory.parseFile(cli.config)
    val config = Hocon.decodeFromConfig<AppConfig>(AppConfig.serializer(), hocon)
    log.info("db ${config.db.host}")

    Database.initialize(config.db)
//    Migrator(config.db).apply{runMigration()}
    runBlocking {
        runScrapers(config.scraper, null)
    }


}