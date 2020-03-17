package support

import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import java.util.*

object Triggers {

    fun instant(id: UUID, prefix: String = "instant-trigger"): Trigger {
        val name = "$prefix-$id"
        return TriggerBuilder.newTrigger()
            .withIdentity(name)
            .startNow()
            .withSchedule(
                Schedules.instant
            )
            .build()
    }

    fun inIntervals(id: UUID, intervalInSeconds: Int): Trigger {
        val name = "interval-trigger-$id"
        return TriggerBuilder.newTrigger()
            .withIdentity(name)
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(intervalInSeconds)
                    .repeatForever())
            .build()
    }

}