import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import support.*
import java.time.Duration
import java.util.*

class FastRunningJobInstantlyTest : QuartzManagementTest() {

    @Test
    fun `that non-running job can be triggered instantly via changing original trigger`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 1)

        // very long interval so job is not triggered
        val trigger = Triggers.inIntervals(id, 100)

        scheduler.scheduleJob(jobDetail, trigger)

        Thread.sleep(5_000)
        Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))

        val changedTrigger = TriggerBuilder.newTrigger()
            .withIdentity(trigger.key)
            .forJob(jobDetail)
            .startNow()
            .withSchedule(Schedules.instant)
            .build()

        scheduler.rescheduleJob(trigger.key, changedTrigger)

        await
            .atMost(Duration.ofSeconds(2))
            .untilAsserted {
                Assertions.assertEquals(2, ConfirmableJob.endedCalls(jobDetail.key))
            }
    }

    @Test
    fun `that non-running job can run instantly via new trigger`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 1)

        val trigger = Triggers.inIntervals(id, 5_000)

        scheduler.scheduleJob(jobDetail, trigger)

        Thread.sleep(2_000)
        Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))

        val instantTrigger = TriggerBuilder.newTrigger()
            .withIdentity("instant-trigger-$id")
            .forJob(jobDetail.key)
            .startNow()
            .build()

        // triggers are replaced
        scheduler.scheduleJob(jobDetail, setOf(instantTrigger, trigger), true)

        await
            .atMost(Duration.ofSeconds(7))
            .untilAsserted {
                Assertions.assertEquals(3, ConfirmableJob.endedCalls(jobDetail.key))
            }
    }

}