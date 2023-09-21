package com.zubayear.groundhog.model.ginput;

import java.time.LocalDate;
import java.util.List;

public record ScholarshipInput(
        String title, String description, String purpose,
        LocalDate lastDate,
        Integer daysLeft,
        Integer total,
        List<SortInput> sorts) {
}
