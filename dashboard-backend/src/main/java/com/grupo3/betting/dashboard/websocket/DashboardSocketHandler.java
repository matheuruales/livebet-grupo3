package com.grupo3.betting.dashboard.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DashboardSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions=ConcurrentHashMap.newKeySet();
    @Override public void afterConnectionEstablished(WebSocketSession session){sessions.add(session);}
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status){sessions.remove(session);}
    public void broadcast(String json){
        sessions.removeIf(s -> !s.isOpen());
        sessions.forEach(s -> { try { synchronized(s){s.sendMessage(new TextMessage(json));} } catch(Exception ignored){} });
    }
}
