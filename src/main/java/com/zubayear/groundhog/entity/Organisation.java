package com.zubayear.groundhog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(value = "organisations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Organisation {
    @Id
    private UUID id;

    @Column(value = "org_name")
    private String orgName;

    private String description;

    private Integer founded;

    @Column(value = "origin_country")
    private String originCountry;
}
