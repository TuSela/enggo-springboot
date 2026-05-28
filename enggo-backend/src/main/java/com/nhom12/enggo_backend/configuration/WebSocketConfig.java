package com.nhom12.enggo_backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một broker đơn giản để gửi dữ liệu ngược về Client
        config.enableSimpleBroker("/topic", "/queue");
        // Các API Client gửi lên Server sẽ có tiền tố này (ví dụ: /app/find-match)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cổng thuần túy dành cho Postman / Android không dùng SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*").withSockJS();;

        // Cổng hỗ trợ SockJS dành cho file HTML/JS test trên trình duyệt
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}