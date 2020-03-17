package support

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import java.sql.Connection
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

class SchedulerJunitExtension : BeforeAllCallback {

    private var scheduler: Scheduler? = null

    fun scheduler(): Scheduler {
        return scheduler!!
    }

    override fun beforeAll(context: ExtensionContext?) {
        val rootStore = context?.root?.getStore(ExtensionContext.Namespace.GLOBAL)
        if (rootStore != null) {
            rootStore.getOrComputeIfAbsent("scheduler", {

                logger.info { "Retrieving connection" }
                val url = "jdbc:hsqldb:file:build/db/quartz-mgmt;hsqldb.lock_file=false"
                DriverManager.getConnection(url, "SA", null).use { createQuartzTables(it) }

                logger.info { "Starting scheduler" }
                scheduler = StdSchedulerFactory.getDefaultScheduler()!!
                scheduler!!.start()
                SchedulerCloseableResource(scheduler!!)
            })
        } else {
            throw IllegalStateException("Unable to initialize scheduler")
        }
    }


    private fun createQuartzTables(connection: Connection) {
        logger.info { "Applying DB migrations" }
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        val liquibase = Liquibase("liquibase/changelog.xml", ClassLoaderResourceAccessor(), database)
        val contexts  = Contexts()
        liquibase.update(contexts)
        logger.info { "DB migrations applied" }
    }

}

class SchedulerCloseableResource(private val scheduler: Scheduler) : ExtensionContext.Store.CloseableResource {

    override fun close() {
        logger.debug { "Shutting down scheduler" }
        scheduler.shutdown()
    }

}