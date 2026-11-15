package com.barberbooking.api.scheduling;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingHoursItem(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {
    public static WorkingHoursItem from(WorkingHours wh) {
        return new WorkingHoursItem(wh.getDayOfWeek(), wh.getStartTime(), wh.getEndTime());
    }
}