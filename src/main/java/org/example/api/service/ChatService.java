package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.*;
import org.example.api.model.Order.OrderStatus;
import org.example.api.model.UserRole;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final HomeService homeService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final CategoryService categoryService;

    /**
     * Unified product search with all parameters
     */
    public List<ProductDTO> searchProducts(String name, UUID categoryId, UUID colorId, UUID sizeId,
                                           Boolean archived, Boolean featured, Double minPrice, Double maxPrice,
                                           Integer minStock, Integer maxStock, int limit, int offset,
                                           String sortBy, String sortDirection) {
        // Start with all products
        List<ProductDTO> products = productService.getAllProducts();

        // Apply name filter
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            products = filterList(products, p -> p.getName().toLowerCase().contains(lowerName));
        }

        // Apply category filter
        if (categoryId != null) {
            products = filterList(products, p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()));
        }

        // Apply color filter
        if (colorId != null) {
            products = filterList(products, p -> p.getColor() != null && colorId.equals(p.getColor().getId()));
        }

        // Apply size filter
        if (sizeId != null) {
            products = filterList(products, p -> p.getSize() != null && sizeId.equals(p.getSize().getId()));
        }

        // Apply archived filter
        if (archived != null) {
            products = filterList(products, p -> archived.equals(p.getArchived()));
        }

        // Apply featured filter
        if (featured != null) {
            products = filterList(products, p -> featured.equals(p.getFeatured()));
        }

        // Apply price filters
        if (minPrice != null) {
            products = filterList(products, p -> p.getPrice().doubleValue() >= minPrice);
        }

        if (maxPrice != null) {
            products = filterList(products, p -> p.getPrice().doubleValue() <= maxPrice);
        }

        // Apply stock filters
        if (minStock != null) {
            products = filterList(products, p -> p.getStock() >= minStock);
        }

        if (maxStock != null) {
            products = filterList(products, p -> p.getStock() <= maxStock);
        }

        // Apply sorting and pagination
        return sortAndPaginate(products, getComparator(sortBy, ProductDTO.class), sortDirection, offset, limit);
    }

    /**
     * Unified order search with all parameters
     */
    public List<OrderDTO> searchOrders(UUID userId, String userEmail, String userName,
                                       OrderStatus status, BigDecimal minTotal, BigDecimal maxTotal,
                                       ZonedDateTime startDate, ZonedDateTime endDate,
                                       int limit, int offset, String sortBy, String sortDirection) {
        // Start with all orders
        List<OrderDTO> orders = orderService.getAllOrders();

        // Apply user ID filter
        if (userId != null) {
            orders = filterList(orders, o -> userId.equals(o.getUserId()));
        }

        // Apply user email filter
        if (userEmail != null && !userEmail.isEmpty()) {
            String lowerEmail = userEmail.toLowerCase();
            orders = filterList(orders, o -> o.getUserEmail() != null && o.getUserEmail().toLowerCase().contains(lowerEmail));
        }

        // Apply user name filter
        if (userName != null && !userName.isEmpty()) {
            String lowerName = userName.toLowerCase();
            orders = filterList(orders, o -> o.getUserName() != null && o.getUserName().toLowerCase().contains(lowerName));
        }

        // Apply status filter
        if (status != null) {
            orders = filterList(orders, o -> status.name().equals(o.getStatus()));
        }

        // Apply total filters
        if (minTotal != null) {
            orders = filterList(orders, o -> o.getTotal().compareTo(minTotal) >= 0);
        }

        if (maxTotal != null) {
            orders = filterList(orders, o -> o.getTotal().compareTo(maxTotal) <= 0);
        }

        // Apply date filters
        if (startDate != null) {
            orders = filterList(orders, o -> !o.getCreatedAt().isBefore(startDate));
        }

        if (endDate != null) {
            orders = filterList(orders, o -> !o.getCreatedAt().isAfter(endDate));
        }

        // Apply sorting and pagination
        return sortAndPaginate(orders, getComparator(sortBy, OrderDTO.class), sortDirection, offset, limit);
    }

    /**
     * Unified user search with all parameters
     */
    public List<UserDTO> searchUsers(String firstName, String lastName, String email,
                                     UserRole role, ZonedDateTime startDate, ZonedDateTime endDate,
                                     int limit, int offset, String sortBy, String sortDirection) {
        // Start with all users
        List<UserDTO> users = userService.getAllUsers();

        // Apply first name filter
        if (firstName != null && !firstName.isEmpty()) {
            String lowerFirstName = firstName.toLowerCase();
            users = filterList(users, u -> u.getFirstName() != null &&
                    u.getFirstName().toLowerCase().contains(lowerFirstName));
        }

        // Apply last name filter
        if (lastName != null && !lastName.isEmpty()) {
            String lowerLastName = lastName.toLowerCase();
            users = filterList(users, u -> u.getLastName() != null &&
                    u.getLastName().toLowerCase().contains(lowerLastName));
        }

        // Apply email filter
        if (email != null && !email.isEmpty()) {
            String lowerEmail = email.toLowerCase();
            users = filterList(users, u -> u.getEmail().toLowerCase().contains(lowerEmail));
        }

        // Apply role filter
        if (role != null) {
            users = filterList(users, u -> role.equals(u.getRole()));
        }

        // Apply date filters
        if (startDate != null) {
            users = filterList(users, u -> !u.getCreatedAt().isBefore(startDate));
        }

        if (endDate != null) {
            users = filterList(users, u -> !u.getCreatedAt().isAfter(endDate));
        }

        // Apply sorting and pagination
        return sortAndPaginate(users, getComparator(sortBy, UserDTO.class), sortDirection, offset, limit);
    }

    /**
     * Process multi-query request
     */
    public Map<String, Object> processMultiQuery(Map<String, Object> queryParams) {
        Map<String, Object> result = new HashMap<>();

        // Process products
        if (queryParams.containsKey("products")) {
            Map<String, Object> params = toMap(queryParams.get("products"));

            // Extract parameters
            String name = getStringParam(params, "name", null);
            UUID categoryId = getUuidParam(params, "categoryId");
            UUID colorId = getUuidParam(params, "colorId");
            UUID sizeId = getUuidParam(params, "sizeId");
            Boolean archived = getBooleanParam(params, "archived");
            Boolean featured = getBooleanParam(params, "featured");
            Double minPrice = getDoubleParam(params, "minPrice");
            Double maxPrice = getDoubleParam(params, "maxPrice");
            Integer minStock = getIntegerParam(params, "minStock");
            Integer maxStock = getIntegerParam(params, "maxStock");
            int limit = getIntParam(params, "limit", 10);
            int offset = getIntParam(params, "offset", 0);
            String sortBy = getStringParam(params, "sortBy", "name");
            String sortDirection = getStringParam(params, "sortDirection", "asc");

            result.put("products", searchProducts(
                    name, categoryId, colorId, sizeId, archived, featured,
                    minPrice, maxPrice, minStock, maxStock,
                    limit, offset, sortBy, sortDirection));
        }

        // Process orders
        if (queryParams.containsKey("orders")) {
            Map<String, Object> params = toMap(queryParams.get("orders"));

            // Extract parameters
            UUID userId = getUuidParam(params, "userId");
            String userEmail = getStringParam(params, "userEmail", null);
            String userName = getStringParam(params, "userName", null);
            OrderStatus status = getEnumParam(params, "status", OrderStatus.class);
            BigDecimal minTotal = getBigDecimalParam(params, "minTotal");
            BigDecimal maxTotal = getBigDecimalParam(params, "maxTotal");
            ZonedDateTime startDate = getDateParam(params, "startDate");
            ZonedDateTime endDate = getDateParam(params, "endDate");
            int limit = getIntParam(params, "limit", 10);
            int offset = getIntParam(params, "offset", 0);
            String sortBy = getStringParam(params, "sortBy", "createdAt");
            String sortDirection = getStringParam(params, "sortDirection", "desc");

            result.put("orders", searchOrders(
                    userId, userEmail, userName, status, minTotal, maxTotal,
                    startDate, endDate, limit, offset, sortBy, sortDirection));
        }

        // Process users
        if (queryParams.containsKey("users")) {
            Map<String, Object> params = toMap(queryParams.get("users"));

            // Extract parameters
            String firstName = getStringParam(params, "firstName", null);
            String lastName = getStringParam(params, "lastName", null);
            String email = getStringParam(params, "email", null);
            UserRole role = getEnumParam(params, "role", UserRole.class);
            ZonedDateTime startDate = getDateParam(params, "startDate");
            ZonedDateTime endDate = getDateParam(params, "endDate");
            int limit = getIntParam(params, "limit", 10);
            int offset = getIntParam(params, "offset", 0);
            String sortBy = getStringParam(params, "sortBy", "createdAt");
            String sortDirection = getStringParam(params, "sortDirection", "asc");

            result.put("users", searchUsers(
                    firstName, lastName, email, role, startDate, endDate,
                    limit, offset, sortBy, sortDirection));
        }

        // Process inventory alerts (special case of product search)
        if (queryParams.containsKey("inventoryAlerts")) {
            Map<String, Object> params = toMap(queryParams.get("inventoryAlerts"));
            int limit = getIntParam(params, "limit", 10);
            Integer threshold = getIntegerParam(params, "threshold");
            if (threshold == null) {
                // If no threshold specified, use low stock threshold from configuration
                threshold = 10; // Default value, could be configurable
            }

            // Use product search with stock filter
            List<ProductDTO> lowStockProducts = searchProducts(
                    null, null, null, null, false, null,
                    null, null, null, threshold,
                    limit, 0, "stock", "asc");

            result.put("inventoryAlerts", lowStockProducts);
        }

        return result;
    }

    // HELPER METHODS

    /**
     * Filter a list using the given predicate
     */
    private <T> List<T> filterList(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Sort and paginate a list
     */
    private <T> List<T> sortAndPaginate(List<T> list, Comparator<T> comparator,
                                        String sortDirection, int offset, int limit) {
        // Apply sort direction
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        // Apply sorting, pagination
        return list.stream()
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get a type-specific comparator
     */
    @SuppressWarnings("unchecked")
    private <T> Comparator<T> getComparator(String sortBy, Class<T> clazz) {
        if (ProductDTO.class.equals(clazz)) {
            return (Comparator<T>) getProductComparator(sortBy);
        } else if (OrderDTO.class.equals(clazz)) {
            return (Comparator<T>) getOrderComparator(sortBy);
        } else if (UserDTO.class.equals(clazz)) {
            return (Comparator<T>) getUserComparator(sortBy);
        }

        // Default to comparing toString if no specific comparator is available
        return Comparator.comparing(Object::toString);
    }

    /**
     * Get a comparator for ProductDTO
     */
    private Comparator<ProductDTO> getProductComparator(String sortBy) {
        if ("price".equals(sortBy)) {
            return Comparator.comparing(ProductDTO::getPrice);
        } else if ("createdAt".equals(sortBy)) {
            return Comparator.comparing(ProductDTO::getCreatedAt);
        } else if ("stock".equals(sortBy)) {
            return Comparator.comparing(ProductDTO::getStock);
        } else if ("categoryName".equals(sortBy)) {
            return Comparator.comparing(p -> p.getCategory() != null ? p.getCategory().getName() : "");
        } else {
            // Default to name
            return Comparator.comparing(ProductDTO::getName);
        }
    }

    /**
     * Get a comparator for OrderDTO
     */
    private Comparator<OrderDTO> getOrderComparator(String sortBy) {
        if ("total".equals(sortBy)) {
            return Comparator.comparing(OrderDTO::getTotal);
        } else if ("status".equals(sortBy)) {
            return Comparator.comparing(OrderDTO::getStatus);
        } else if ("userName".equals(sortBy)) {
            return Comparator.comparing(OrderDTO::getUserName);
        } else {
            // Default to createdAt
            return Comparator.comparing(OrderDTO::getCreatedAt);
        }
    }

    /**
     * Get a comparator for UserDTO
     */
    private Comparator<UserDTO> getUserComparator(String sortBy) {
        if ("firstName".equals(sortBy)) {
            return Comparator.comparing(u -> u.getFirstName() != null ? u.getFirstName() : "");
        } else if ("lastName".equals(sortBy)) {
            return Comparator.comparing(u -> u.getLastName() != null ? u.getLastName() : "");
        } else if ("email".equals(sortBy)) {
            return Comparator.comparing(UserDTO::getEmail);
        } else if ("role".equals(sortBy)) {
            return Comparator.comparing(u -> u.getRole() != null ? u.getRole().name() : "");
        } else {
            // Default to createdAt
            return Comparator.comparing(UserDTO::getCreatedAt);
        }
    }

    /**
     * Convert an object to a map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return new HashMap<>();
    }

    // PARAMETER EXTRACTION HELPERS

    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params.containsKey(key) && params.get(key) instanceof String) {
            return (String) params.get(key);
        }
        return defaultValue;
    }

    private Boolean getBooleanParam(Map<String, Object> params, String key) {
        if (params.containsKey(key) && params.get(key) instanceof Boolean) {
            return (Boolean) params.get(key);
        }
        return null;
    }

    private Integer getIntegerParam(Map<String, Object> params, String key) {
        if (params.containsKey(key) && params.get(key) instanceof Number) {
            return ((Number) params.get(key)).intValue();
        }
        return null;
    }

    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        Integer value = getIntegerParam(params, key);
        return value != null ? value : defaultValue;
    }

    private Double getDoubleParam(Map<String, Object> params, String key) {
        if (params.containsKey(key) && params.get(key) instanceof Number) {
            return ((Number) params.get(key)).doubleValue();
        }
        return null;
    }

    private BigDecimal getBigDecimalParam(Map<String, Object> params, String key) {
        if (params.containsKey(key)) {
            if (params.get(key) instanceof BigDecimal) {
                return (BigDecimal) params.get(key);
            } else if (params.get(key) instanceof Number) {
                return BigDecimal.valueOf(((Number) params.get(key)).doubleValue());
            }
        }
        return null;
    }

    private UUID getUuidParam(Map<String, Object> params, String key) {
        if (params.containsKey(key)) {
            if (params.get(key) instanceof UUID) {
                return (UUID) params.get(key);
            } else if (params.get(key) instanceof String) {
                try {
                    return UUID.fromString((String) params.get(key));
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format
                }
            }
        }
        return null;
    }

    private ZonedDateTime getDateParam(Map<String, Object> params, String key) {
        if (params.containsKey(key) && params.get(key) instanceof String) {
            try {
                return ZonedDateTime.parse((String) params.get(key));
            } catch (Exception e) {
                // Invalid date format
            }
        }
        return null;
    }

    private <T extends Enum<T>> T getEnumParam(Map<String, Object> params, String key, Class<T> enumClass) {
        if (params.containsKey(key) && params.get(key) instanceof String) {
            try {
                return Enum.valueOf(enumClass, ((String) params.get(key)).toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid enum value
            }
        }
        return null;
    }
}