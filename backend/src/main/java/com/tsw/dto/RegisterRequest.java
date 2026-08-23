package com.tsw.dto;

import com.tsw.validation.Utf8ByteLength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        @Size(max = 254, message = "EMAIL_TOO_LONG") String email,
        @NotBlank(message = "PASSWORD_REQUIRED")
        @Size(min = 8, message = "PASSWORD_TOO_SHORT")
        @Utf8ByteLength(max = 72, message = "PASSWORD_TOO_LONG") String password,
        @NotBlank(message = "FIRST_NAME_REQUIRED")
        @Size(max = 100, message = "FIRST_NAME_TOO_LONG") String firstName,
        @NotBlank(message = "LAST_NAME_REQUIRED")
        @Size(max = 100, message = "LAST_NAME_TOO_LONG") String lastName,
        @Size(max = 20, message = "PHONE_NUMBER_TOO_LONG") String phoneNumber
) {
}
