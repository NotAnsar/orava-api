package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.*;
import org.example.api.model.Order.OrderStatus;
import org.example.api.model.UserRole;
import org.example.api.payload.response.DefaultResponse;
import org.example.api.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * Search products with flexible parameters
     */
    @GetMapping("/products/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GUEST')")
    public ResponseEntity<DefaultResponse<List<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID colorId,
            @RequestParam(required = false) UUID sizeId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        List<ProductDTO> products = chatService.searchProducts(
                name, categoryId, colorId, sizeId, archived, featured,
                minPrice, maxPrice, minStock, maxStock,
                limit, offset, sortBy, sortDirection);

        return ResponseEntity.ok(
                new DefaultResponse<>("Products retrieved successfully", true, products)
        );
    }

    /**
     * Search orders with flexible parameters
     */
    @GetMapping("/orders/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GUEST')")
    public ResponseEntity<DefaultResponse<List<OrderDTO>>> searchOrders(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        new DefaultResponse<>("Invalid order status", false, null)
                );
            }
        }

        List<OrderDTO> orders = chatService.searchOrders(
                userId, userEmail, userName, orderStatus, minTotal, maxTotal,
                startDate, endDate, limit, offset, sortBy, sortDirection);

        return ResponseEntity.ok(
                new DefaultResponse<>("Orders retrieved successfully", true, orders)
        );
    }

    /**
     * Search users with flexible parameters
     */
    @GetMapping("/users/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DefaultResponse<List<UserDTO>>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        UserRole userRole = null;
        if (role != null && !role.isEmpty()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        new DefaultResponse<>("Invalid user role", false, null)
                );
            }
        }

        List<UserDTO> users = chatService.searchUsers(
                firstName, lastName, email, userRole, startDate, endDate,
                limit, offset, sortBy, sortDirection);

        return ResponseEntity.ok(
                new DefaultResponse<>("Users retrieved successfully", true, users)
        );
    }

    /**
     * Multi-query endpoint for complex queries
     */
    @PostMapping("/query")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'GUEST')")
    public ResponseEntity<DefaultResponse<Map<String, Object>>> multiQuery(
            @RequestBody Map<String, Object> queryParams) {
        try {
            Map<String, Object> result = chatService.processMultiQuery(queryParams);
            return ResponseEntity.ok(
                    new DefaultResponse<>("Multi-query processed successfully", true, result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new DefaultResponse<>("Error processing multi-query: " + e.getMessage(), false, null)
            );
        }
    }
}