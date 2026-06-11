package com.mentorx.api.feature.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProofLinkItem {

    @NotBlank(message = "Proof link label is required")
    @Size(max = 80, message = "Proof link label must not exceed 80 characters")
    private String label;

    @NotBlank(message = "Proof link URL is required")
    @Size(max = 1000, message = "Proof link URL must not exceed 1000 characters")
    private String url;
}
