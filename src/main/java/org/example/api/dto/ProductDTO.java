package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.api.model.Product;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private CategoryDTO category;
    private ZonedDateTime createdAt;
    private String description;
    private Boolean archived;
    private Boolean featured;
    private ColorDTO color;
    private SizeDTO size;

    public static ProductDTO fromEntity(Product product) {
        // Create nested CategoryDTO with createdAt
        CategoryDTO categoryDTO = null;
        if (product.getCategory() != null) {
            categoryDTO = new CategoryDTO(
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCategory().getCreatedAt() // Include createdAt
            );
        }

        // Create nested ColorDTO with createdAt
        ColorDTO colorDTO = null;
        if (product.getColor() != null) {
            colorDTO = new ColorDTO(
                    product.getColor().getId(),
                    product.getColor().getName(),
                    product.getColor().getValue(),
                    product.getColor().getCreatedAt() // Include createdAt
            );
        }

        // Create nested SizeDTO with createdAt
        SizeDTO sizeDTO = null;
        if (product.getSize() != null) {
            sizeDTO = new SizeDTO(
                    product.getSize().getId(),
                    product.getSize().getName(),
                    product.getSize().getFullname(),
                    product.getSize().getCreatedAt() // Include createdAt
            );
        }

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                categoryDTO,
                product.getCreatedAt(),
                product.getDescription(),
                product.getArchived(),
                product.getFeatured(),
                colorDTO,
                sizeDTO
        );
    }
}