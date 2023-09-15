package com.zubayear.groundhog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScholarshipInput {
    private String title, description, purpose;
    private LocalDate lastDate;
    private Integer daysLeft;
    private Integer total;
    private List<SortInput> sorts;
}
