package us.cedarfarm

import com.github.ajalt.clikt.core.main
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger
import us.cedarfarm.cli.Cli
import us.cedarfarm.config.AppConfig
import us.cedarfarm.db.Database
import us.cedarfarm.db.Migrator

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
    Migrator(config.db).apply{runMigration()}
}