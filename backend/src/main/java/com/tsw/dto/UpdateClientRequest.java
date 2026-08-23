package com.tsw.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "EMAIL_REQUIRED")
        @Email(message = "INVALID_EMAIL")
        @Size(max = 254, message = "EMAIL_TOO_LONG") String email,
        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "FIRST_NAME_REQUIRED")
        @Size(max = 100, message = "FIRST_NAME_TOO_LONG") String firstName,
        @Pattern(regexp = ".*\\S.*", flags = Pattern.Flag.DOTALL, message = "LAST_NAME_REQUIRED")
        @Size(max = 100, message = "LAST_NAME_TOO_LONG") String lastName,
        @Size(max = 20, message = "PHONE_NUMBER_TOO_LONG") String phoneNumber
) {
}
