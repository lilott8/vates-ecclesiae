package us.cedarfarm.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger
import us.cedarfarm.config.DbConfig
import java.sql.Connection
import javax.sql.DataSource

object Database {
    private val log = logger()
    private var _dataSource: DataSource? = null

    fun initialize(config: DbConfig) {
        if (_dataSource == null) {
            log.info("jdbc url: ${config.getJdbcUrl()}\t user: ${config.user}, password: ${config.password}")
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = config.getJdbcUrl()
                username = config.user
                password = config.password
                maximumPoolSize = config.maximumPoolSize
                // Apply other configuration properties from DatabaseConfig
            }
            _dataSource = HikariDataSource(hikariConfig)
        }
    }

    val dataSource: DataSource
        get() {
            return _dataSource ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
        }

}