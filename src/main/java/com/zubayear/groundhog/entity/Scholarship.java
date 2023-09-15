package com.zubayear.groundhog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Table(value = "scholarships")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Scholarship {
    @Id
    private UUID id;
    private String title, description, purpose;
    @Column(value = "last_date")
    private LocalDate lastDate;
    @Column(value = "days_left")
    private Integer daysLeft;
    private Integer total;
    @Column(value = "organisations_id")
    private UUID organisationsId;
}
