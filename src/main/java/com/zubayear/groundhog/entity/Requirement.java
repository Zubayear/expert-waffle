package com.zubayear.groundhog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(value = "requirements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Requirement {
    @Id
    private UUID id;
    private String title, description;
    @Column(value = "scholarships_id")
    private UUID scholarshipsId;
}
