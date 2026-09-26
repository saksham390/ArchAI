package com.archai.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r.path("/api/auth/**").uri("lb://AUTH-SERVICE"))
            .route("design-service", r -> r.path("/api/designs/**").uri("lb://DESIGN-SERVICE"))
            .route("knowledge-service", r -> r.path("/api/knowledge/**").uri("lb://KNOWLEDGE-SERVICE"))
            .route("ai-service", r -> r.path("/api/ai/**").uri("lb://AI-SERVICE"))
            .route("review-service", r -> r.path("/api/reviews/**").uri("lb://REVIEW-SERVICE"))
            .route("chat-service", r -> r.path("/api/chat/**").uri("lb://CHAT-SERVICE"))
            .route("notification-service", r -> r.path("/api/notifications/**").uri("lb://NOTIFICATION-SERVICE"))
            .build();
    }
}
