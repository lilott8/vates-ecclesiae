package us.cedarfarm.db

import org.flywaydb.core.Flyway
import us.cedarfarm.config.DbConfig
import java.sql.Connection

class Migrator(config: DbConfig) {

    val connection: Connection

    init {
        Database.initialize(config)
        connection = Database.dataSource.connection
    }

    fun runMigration(wipeDb: Boolean = false) {
        val flyway: Flyway = Flyway.configure().dataSource(Database.dataSource).cleanDisabled(!wipeDb).load()
        if (wipeDb) {
            flyway.clean()
        }
        flyway.migrate()
    }
}