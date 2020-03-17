import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import support.ConfirmableJob
import support.Triggers
import java.time.Duration
import java.util.*

class SimpleTest : QuartzManagementTest() {

    @Test
    fun `that scheduled job runs`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("simple-scheduled-job-$id", 1)

        val trigger = Triggers.inIntervals(id, 1)

        scheduler.scheduleJob(jobDetail, trigger)
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(3))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)
    }

    @Test
    fun `that job can run with instant trigger that is removed afterwards`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("simple-instant-job-$id", 1)

        val trigger = Triggers.instant(id)

        scheduler.scheduleJob(jobDetail, trigger)
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(5))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
            }
        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)

        Thread.sleep(5_000)

        Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)
    }


}