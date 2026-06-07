package com.nhom12.enggo_backend.configuration;

import com.nhom12.enggo_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
@Configuration
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userStatusService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Lấy thông tin User đã được xác thực qua token JWT trước đó
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();

        if (principal != null) {
            String userId = principal.getName(); // Thường là ID hoặc Username tùy cấu hình Security của nhóm
            userStatusService.userOnline(userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Gửi lệnh xóa khỏi danh sách Online
        userStatusService.userOffline(sessionId);
    }
}