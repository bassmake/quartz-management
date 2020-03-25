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
        const val SHOULD_FAIL = "SHOULD_FAIL"

        val startedExecutions: ConcurrentHashMap<JobKey, Int> = ConcurrentHashMap()

        fun startedCalls(jobKey: JobKey): Int {
            log.debug { "$jobKey - retrieving started calls" }
            return startedExecutions.getOrElse(jobKey, {
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

        fun create(name: String, runningTimeInSeconds: Int = 0, shouldFail: Boolean = false): JobDetail {
            val jobKey = JobKey(name)
            val jobDetail = JobBuilder.newJob(ConfirmableJob::class.java)
                .withIdentity(jobKey)
                .build()

            jobDetail.jobDataMap[RUNNING_TIME] = runningTimeInSeconds
            jobDetail.jobDataMap[SHOULD_FAIL] = shouldFail
            return jobDetail
        }
    }

    override fun execute(context: JobExecutionContext?) {

        val key = context?.jobDetail?.key
        log.info { "Executing job $key" }
        if (key != null) {
            log.debug { "$key - Started initializing" }

            startedExecutions.compute(key) { _, oldValue -> if (oldValue == null) 1 else oldValue + 1}

            val dataMap = context.mergedJobDataMap
            val runningTime = dataMap?.getInt(RUNNING_TIME)?:0
            val fail = dataMap?.getBoolean(SHOULD_FAIL)?:false
            log.debug { "$key - Started working for $runningTime seconds" }
            Thread.sleep(runningTime * 1000L)
            if (fail) {
                val jobException = JobExecutionException("$key - failed because it should")
                jobException.setRefireImmediately(false)
                throw jobException
            }
            confirmations.compute(key) { _, oldValue -> if (oldValue == null) 1 else oldValue + 1}
            log.debug { "$key - Stopped working" }
        } else {
           throw IllegalArgumentException("key is null")
        }
    }

}
