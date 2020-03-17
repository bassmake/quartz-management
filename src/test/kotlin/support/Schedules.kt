package support

import org.quartz.SimpleScheduleBuilder

object Schedules {

    val instant: SimpleScheduleBuilder = SimpleScheduleBuilder
            .simpleSchedule()
            .withRepeatCount(0)
            .withIntervalInSeconds(0)
}