package us.cedarfarm.utils

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import org.apache.logging.log4j.kotlin.Logging
import us.cedarfarm.config.ScraperConfig
import java.nio.file.Files
import java.nio.file.Paths


object HttpClientSingleton {
    val instance: HttpClient by lazy {
        HttpClient(CIO) {
            // Default Ktor client configuration (customize as needed)
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(DefaultRequest) {
                headers.append("Content-Type", "application/json")
                // Add other default headers if needed
            }
//            install(Logging) {
                // Configure logging if needed (e.g., level = LogLevel.ALL)
//            }
            install(UserAgent) {
                agent = "YourAppName/1.0" // Replace with your application's user agent
            }
            install(HttpCache) {
                val cacheFile = Files.createDirectories(Paths.get("")).toFile()
                publicStorage(FileStorage(cacheFile))
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                delayMillis { retry -> retry * 1000L }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000 // 15 seconds
                connectTimeoutMillis = 5000  // 5 seconds
                socketTimeoutMillis = 10000   // 10 seconds
            }
        }
    }

}