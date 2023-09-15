package com.zubayear.groundhog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRequirementInput {
    private String title, description;
    private UUID scholarshipsId;
}
