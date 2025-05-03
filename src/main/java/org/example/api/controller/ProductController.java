package org.example.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.CategoryDTO;
import org.example.api.dto.ColorDTO;
import org.example.api.dto.ProductDTO;
import org.example.api.dto.SizeDTO;
import org.example.api.payload.request.product.CreateProductRequest;
import org.example.api.payload.request.product.UpdateProductRequest;
import org.example.api.payload.response.DefaultResponse;
import org.example.api.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID colorId,
            @RequestParam(required = false) UUID sizeId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Boolean featured) {

        // If no filters, return all products
        List<ProductDTO> products;
        if (name != null || categoryId != null || colorId != null || sizeId != null ||
                archived != null || featured != null) {
            products = productService.searchProducts(name, categoryId, colorId, sizeId, archived, featured);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(
                new DefaultResponse<>("Products retrieved successfully", true, products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
        Optional<ProductDTO> productOpt = productService.getProductById(id);

        if (productOpt.isPresent()) {
            return ResponseEntity.ok(
                    new DefaultResponse<>("Product retrieved successfully", true, productOpt.get())
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DefaultResponse<>("Product not found", false, null));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<ProductDTO> products = productService.findByCategoryId(categoryId);
        return ResponseEntity.ok(
                new DefaultResponse<>("Products retrieved successfully", true, products)
        );
    }

    @GetMapping("/color/{colorId}")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getProductsByColor(@PathVariable UUID colorId) {
        List<ProductDTO> products = productService.findByColorId(colorId);
        return ResponseEntity.ok(
                new DefaultResponse<>("Products retrieved successfully", true, products)
        );
    }

    @GetMapping("/size/{sizeId}")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getProductsBySize(@PathVariable UUID sizeId) {
        List<ProductDTO> products = productService.findBySizeId(sizeId);
        return ResponseEntity.ok(
                new DefaultResponse<>("Products retrieved successfully", true, products)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getActiveProducts() {
        List<ProductDTO> products = productService.getActiveProducts();
        return ResponseEntity.ok(
                new DefaultResponse<>("Active products retrieved successfully", true, products)
        );
    }

    @GetMapping("/featured")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> getFeaturedProducts() {
        List<ProductDTO> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(
                new DefaultResponse<>("Featured products retrieved successfully", true, products)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DefaultResponse<ProductDTO>> createProduct(@Valid @RequestBody CreateProductRequest createRequest) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(createRequest.getName());
        productDTO.setPrice(createRequest.getPrice());
        productDTO.setStock(createRequest.getStock());

        // Set category as a nested object
        if (createRequest.getCategoryId() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(createRequest.getCategoryId());
            productDTO.setCategory(categoryDTO);
        }

        productDTO.setDescription(createRequest.getDescription());
        productDTO.setArchived(createRequest.getArchived());
        productDTO.setFeatured(createRequest.getFeatured());

        // Set color as a nested object
        if (createRequest.getColorId() != null) {
            ColorDTO colorDTO = new ColorDTO();
            colorDTO.setId(createRequest.getColorId());
            productDTO.setColor(colorDTO);
        }

        // Set size as a nested object
        if (createRequest.getSizeId() != null) {
            SizeDTO sizeDTO = new SizeDTO();
            sizeDTO.setId(createRequest.getSizeId());
            productDTO.setSize(sizeDTO);
        }

        ProductDTO savedProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DefaultResponse<>("Product created successfully", true, savedProduct));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DefaultResponse<ProductDTO>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest updateRequest) {

        Optional<ProductDTO> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DefaultResponse<>("Product not found", false, null));
        }

        ProductDTO productDTO = productOpt.get();

        // Update only provided fields
        if (updateRequest.getName() != null) {
            productDTO.setName(updateRequest.getName());
        }

        if (updateRequest.getPrice() != null) {
            productDTO.setPrice(updateRequest.getPrice());
        }

        if (updateRequest.getStock() != null) {
            productDTO.setStock(updateRequest.getStock());
        }

        if (updateRequest.getCategoryId() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(updateRequest.getCategoryId());
            productDTO.setCategory(categoryDTO);
        }

        if (updateRequest.getDescription() != null) {
            productDTO.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getArchived() != null) {
            productDTO.setArchived(updateRequest.getArchived());
        }

        if (updateRequest.getFeatured() != null) {
            productDTO.setFeatured(updateRequest.getFeatured());
        }

        if (updateRequest.getColorId() != null) {
            ColorDTO colorDTO = new ColorDTO();
            colorDTO.setId(updateRequest.getColorId());
            productDTO.setColor(colorDTO);
        }

        if (updateRequest.getSizeId() != null) {
            SizeDTO sizeDTO = new SizeDTO();
            sizeDTO.setId(updateRequest.getSizeId());
            productDTO.setSize(sizeDTO);
        }

        ProductDTO updatedProduct = productService.updateProduct(productDTO);
        return ResponseEntity.ok(
                new DefaultResponse<>("Product updated successfully", true, updatedProduct)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DefaultResponse<Void>> deleteProduct(@PathVariable UUID id) {
        boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            return ResponseEntity.ok(
                    new DefaultResponse<>("Product deleted successfully", true, null)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DefaultResponse<>("Product not found", false, null));
        }
    }
}