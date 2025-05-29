package us.cedarfarm.db

import org.apache.logging.log4j.kotlin.logger
import org.flywaydb.core.Flyway
import us.cedarfarm.config.DbConfig
import java.sql.Connection

class Migrator(config: DbConfig) {
    private val log = logger()
    private val connection: Connection

    init {
        log.info("attempting migration init")
        Database.initialize(config)
        connection = Database.dataSource.connection
    }

    fun runMigration(wipeDb: Boolean = false) {
        log.info("Attempting migration now")
        val flyway: Flyway = Flyway.configure()
            .dataSource(Database.dataSource)
            .cleanDisabled(!wipeDb)
            .load()
        if (wipeDb) {
            log.warn("Warning: cleaning DB!!!")
            flyway.clean()
        }
        flyway.migrate()
    }
}