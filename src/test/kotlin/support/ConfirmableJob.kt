package support

import mu.KotlinLogging
import org.quartz.*
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

private val log = KotlinLogging.logger {}

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
class ConfirmableJob : Job {

    companion object {

        const val RUNNING_TIME = "RUNNING_TIME"

        val started: ConcurrentHashMap<JobKey, Int> = ConcurrentHashMap()

        fun startedCalls(jobKey: JobKey): Int {
            log.debug { "$jobKey - retrieving started calls" }
            return started.getOrElse(jobKey, {
                log.warn { "$jobKey - no start found" }
                return 0
            })
        }

        val confirmations: ConcurrentMap<JobKey, Int> = ConcurrentHashMap()

        fun endedCalls(jobKey: JobKey): Int {
            log.debug { "$jobKey - retrieving confirmed calls" }
            return confirmations.getOrElse(jobKey, {
                log.warn { "$jobKey - no confirmation found" }
                return 0
            })
        }

        fun create(name: String, runningTimeInSeconds: Int): JobDetail {
            val jobKey = JobKey(name)
            val jobDetail = JobBuilder.newJob(ConfirmableJob::class.java)
                .withIdentity(jobKey)
                .build()

            jobDetail.jobDataMap[RUNNING_TIME] = runningTimeInSeconds
            return jobDetail
        }

    }

    override fun execute(context: JobExecutionContext?) {
        val dataMap = context?.mergedJobDataMap
        log.info { "Executing with $dataMap" }

        val key = context?.jobDetail?.key
        val runningTime = dataMap?.getInt(RUNNING_TIME)
        if (key != null && runningTime != null) {

            log.debug { "$key - Started working for $runningTime seconds" }
            started.compute(key) { _, oldValue -> if (oldValue == null) 1 else oldValue + 1}
            Thread.sleep(runningTime * 1000L)
            confirmations.compute(key) { _, oldValue -> if (oldValue == null) 1 else oldValue + 1}
            log.debug { "$key - Stopped working" }
        } else {
           throw IllegalArgumentException("key=$key or runningTime=$runningTime")
        }
    }

}