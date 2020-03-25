import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import support.ConfirmableJob
import support.Triggers
import java.util.*

class FailingJobTest : QuartzManagementTest() {

    @Test
    fun `that failed job cannot be identified by default`() {
        val id = UUID.randomUUID()
        val jobDetail = ConfirmableJob.create("simple-failing-job-$id", shouldFail = true)

        val trigger = Triggers.instant(id)

        scheduler.scheduleJob(jobDetail, trigger)
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobDetail.key).size)

        Thread.sleep(1000)

        Assertions.assertEquals(1, ConfirmableJob.startedCalls(jobDetail.key))
        Assertions.assertEquals(0, scheduler.getTriggersOfJob(jobDetail.key).size)

        // job instance does not exist either
        Assertions.assertNull(scheduler.getJobDetail(jobDetail.key))
    }

}