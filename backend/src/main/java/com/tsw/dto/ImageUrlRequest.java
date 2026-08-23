package com.tsw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ImageUrlRequest(
        @NotBlank(message = "IMAGE_URL_REQUIRED")
        @Size(max = 500, message = "IMAGE_URL_TOO_LONG")
        @Pattern(regexp = "https://\\S+", message = "INVALID_IMAGE_URL") String url
) {
}
