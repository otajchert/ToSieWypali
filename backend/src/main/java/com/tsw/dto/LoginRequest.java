package com.tsw.dto;

import com.tsw.validation.Utf8ByteLength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        @Size(max = 254, message = "EMAIL_TOO_LONG") String email,
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Utf8ByteLength(max = 72, message = "PASSWORD_TOO_LONG") String password
) {
}
