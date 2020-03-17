import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import support.ConfirmableJob
import support.Triggers
import java.time.Duration
import java.util.*

class LongerRunningJobInstantlyTest : QuartzManagementTest() {

    @Test
    fun `that job will run twice with one updated instant trigger`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 2)

        val trigger = Triggers.instant(id)


        scheduler.scheduleJob(jobDetail, trigger)

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofMillis(500))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.startedCalls(jobDetail.key))
            }

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        scheduler.scheduleJob(jobDetail, setOf(trigger), true)

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        Thread.sleep(3_000)
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)
        Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))

        await
            .atMost(Duration.ofSeconds(5))
            .untilAsserted {
                Assertions.assertEquals(2, ConfirmableJob.startedCalls(jobDetail.key))
            }

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(5))
            .untilAsserted {
                Assertions.assertEquals(2, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)
    }

    @Test
    fun `that job will run twice with two instant triggers`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 2)

        val trigger1 = Triggers.instant(id, "instant-1-")
        val trigger2 = Triggers.instant(id, "instant-2-")

        scheduler.scheduleJob(jobDetail, setOf(trigger1, trigger2), false)
        Assertions.assertEquals(2, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(3))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(5))
            .untilAsserted {
                Assertions.assertEquals(2, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)
    }

    @Test
    fun `that not running job will run only twice even if multiple reschedules are created`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("job-$id", 2)

        val trigger = Triggers.instant(id, "instant-")

        scheduler.scheduleJob(jobDetail, trigger)

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(10))
            .untilAsserted {
                Assertions.assertEquals(1, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)

        val anotherTrigger = Triggers.instant(id, "another-instant-")

        scheduler.scheduleJob(jobDetail, setOf(anotherTrigger), true)
        scheduler.scheduleJob(jobDetail, setOf(anotherTrigger), true)
        scheduler.scheduleJob(jobDetail, setOf(anotherTrigger), true)

        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        await
            .atMost(Duration.ofSeconds(5))
            .untilAsserted {
                Assertions.assertEquals(2, ConfirmableJob.endedCalls(jobDetail.key))
            }

        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)

        Thread.sleep(5_000)
        Assertions.assertEquals(2, ConfirmableJob.endedCalls(jobDetail.key))

        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)
    }

}