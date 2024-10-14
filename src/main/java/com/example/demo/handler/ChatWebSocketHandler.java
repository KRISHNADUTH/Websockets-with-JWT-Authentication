package com.example.demo.handler;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        String username = (String) session.getAttributes().get("username");
        handleTextMessage(session, new TextMessage(username+" left the chat!"));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        handleTextMessage(session, new TextMessage(username + " joined chat"));
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession webSocketSession : sessions) {
            if (!webSocketSession.getId().equals(session.getId())) {
                if (sessions.contains(session)) {
                    String username = (String) session.getAttributes().get("username");
                    webSocketSession.sendMessage(new TextMessage(username + " :- " + message.getPayload()));
                } else {
                    webSocketSession.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Transport error occuerd for session {}, error cause:{}",session.getId(),exception.getMessage());
        // If an error occured on a session and which is still open means we can close it.
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }

        //remove the closed session.
        sessions.remove(session);

        // Notify other users this user has been disconected.
        for (WebSocketSession webSocketSession : sessions) {
            if (!webSocketSession.getId().equals(session.getId())) {
                webSocketSession.sendMessage(new TextMessage(session.getAttributes().get("username")+" closed the session"));
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
