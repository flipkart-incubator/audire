package com.flipkart.audire.stream.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStoreSinkConfiguration {

    @NotBlank
    private String documentKey;
}
