package com.zubayear.groundhog.model.ginput;

import java.time.LocalDate;

public record CreateScholarshipInput(String title, String description, String purpose, LocalDate last_date, Integer days_left, Integer total, Long organisations_id) {}
