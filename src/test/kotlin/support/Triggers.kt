package support

import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder

object Triggers {

    fun instant(name: String): Trigger {
        return TriggerBuilder.newTrigger()
            .withIdentity(name)
            .startNow()
            .withSchedule(
                Schedules.instant
            )
            .build()
    }

    fun inIntervals(name: String, intervalInSeconds: Int): Trigger {
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