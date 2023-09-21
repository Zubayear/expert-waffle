package com.zubayear.groundhog.model;

import java.time.LocalDate;

public record Scholarship(Long id, String title, String description, String purpose, LocalDate last_date, Integer days_left, Integer total, Long organisations_id) {
}
