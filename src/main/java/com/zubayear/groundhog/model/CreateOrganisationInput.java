package com.zubayear.groundhog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrganisationInput {
    private String orgName, description, originCountry;
    private Integer founded;
}
