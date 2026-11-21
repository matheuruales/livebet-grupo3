package com.grupo3.betting.dashboard.config;

import com.grupo3.betting.dashboard.websocket.DashboardSocketHandler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration @EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    private final DashboardSocketHandler handler;
    WebSocketConfig(DashboardSocketHandler handler){this.handler=handler;}
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){registry.addHandler(handler,"/ws").setAllowedOrigins("*");}
}
