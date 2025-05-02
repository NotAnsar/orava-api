package org.example.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorDTO {
    private UUID id;
    private String name;
    private String value;
    private ZonedDateTime createdAt;

    public static ColorDTO fromEntity(Color color) {
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getValue(),
                color.getCreatedAt()
        );
    }
}