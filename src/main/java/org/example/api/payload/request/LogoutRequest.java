package org.example.api.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotNull
    private Long userId;
}