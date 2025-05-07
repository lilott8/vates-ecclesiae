package us.cedarfarm.cli

//import com.github.ajalt.clikt.core.CliktCommand
//import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import org.apache.logging.log4j.kotlin.Logging


class Cli : CoreCliktCommand(), Logging {
    val migrate by option("-m", "--migrate", help="run migrations").flag(default = false)
    val config by option("-c", "--config", help="path to config").file(mustExist = true, mustBeReadable = true, canBeFile = true, canBeDir = false).required()

    override fun run() {
        logger.info("migration is set to: ${migrate}, config path is: ${config}")
        logger.info("hello, world.")
    }
}