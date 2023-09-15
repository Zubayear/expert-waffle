package com.zubayear.groundhog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateScholarshipInput {
    private String title, description, purpose;
    private LocalDate lastDate;
    private Integer daysLeft, total;
    private UUID organisationsId;
}
