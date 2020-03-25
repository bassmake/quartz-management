import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import support.ConfirmableJob
import support.Triggers
import java.time.Duration
import java.util.*

class PeriodicJobAndInstantTriggerTest : QuartzManagementTest() {

    // TODO finish mix of periodic jobs with instant scheduling

    @Test
    fun `that periodically running job cannot be triggered instantly`() {

        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 1)

        val trigger = Triggers.inIntervals(id, 3)

        scheduler.scheduleJob(jobDetail, trigger)

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)
        scheduler.getTriggersOfJob(jobDetail.key).forEach {
            Assertions.assertNull(it.previousFireTime)
        }

        await
            .atMost(Duration.ofSeconds(2))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)
        scheduler.getTriggersOfJob(jobDetail.key).forEach {
            Assertions.assertNotNull(it.previousFireTime)
        }

    }

}