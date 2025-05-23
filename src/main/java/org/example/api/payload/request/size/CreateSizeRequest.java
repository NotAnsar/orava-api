package org.example.api.payload.request.size;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSizeRequest {
    @NotBlank(message = "Size name is required")
    @Size(max = 50, message = "Size name must be less than 50 characters")
    private String name;

    @Size(max = 100, message = "Fullname must be less than 100 characters")
    private String fullname;
}