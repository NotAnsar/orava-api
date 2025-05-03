package org.example.api.security;

import lombok.RequiredArgsConstructor;
import org.example.api.model.Order;
import org.example.api.repository.OrderRepository;
import org.example.api.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {
    private final OrderRepository orderRepository;

    public boolean isOrderOwner(Authentication authentication, UUID orderId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return false;
        }

        return orderOpt.get().getUser().getId().equals(userDetails.getId());
    }

    public boolean isUser(Authentication authentication, UUID userId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId().equals(userId);
    }
}