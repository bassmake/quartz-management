import org.junit.jupiter.api.extension.RegisterExtension
import support.SchedulerJunitExtension

abstract class QuartzManagementTest {

    companion object {

        @JvmField
        @RegisterExtension
        val schedulerExtension = SchedulerJunitExtension()

    }

    val scheduler = schedulerExtension.scheduler()

}
