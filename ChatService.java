package lk.sliit.customer_care.service;

import lk.sliit.customer_care.dto.ChatMessageDTO;
import lk.sliit.customer_care.dto.ChatSessionDTO;
import lk.sliit.customer_care.modelentity.*;
import lk.sliit.customer_care.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ChatSessionRepository chatSessionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // --- Send message ---
    public void sendMessage(String sessionId, Long senderId, String content, ChatMessage.SenderType senderType) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found"));

        ChatMessage message = new ChatMessage(content, sender, senderType, sessionId);
        // No need to set chatSession relationship - we use chatSessionId string
        message.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);

        // ✅ Broadcast message to all clients in the chat session
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, new ChatMessageDTO(message));
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        logger.info("Loading chat history for session: {}", sessionId);
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdAndIsDeletedFalseOrderByCreatedAtAsc(sessionId);
        logger.info("Found {} messages for session {}", messages.size(), sessionId);
        return messages;
    }

    public ChatSession getOrCreateActiveSession(User user) {
        var existing = chatSessionRepository.findByUserAndStatusOrderByCreatedAtDesc(user, ChatSession.ChatStatus.ACTIVE);
        if (!existing.isEmpty()) return existing.get(0);

        ChatSession session = new ChatSession("session-" + System.currentTimeMillis(), user);
        session.setStatus(ChatSession.ChatStatus.WAITING);
        chatSessionRepository.save(session);

        // ✅ Broadcast new waiting session to all agents
        broadcastNewSession(session);

        return session;
    }

    public void broadcastNewSession(ChatSession session) {
        messagingTemplate.convertAndSend("/topic/sessions", new ChatSessionDTO(session));
    }

    public List<ChatSession> getWaitingSessions() {
        return chatSessionRepository.findWaitingForAgentSessions();
    }

    public List<ChatSession> getAgentSessions(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return chatSessionRepository.findByAgentOrderByCreatedAtDesc(agent);
    }

    public List<ChatSession> getAllActiveSessions() {
        // Get all waiting and active sessions for agents to see
        return chatSessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.WAITING || 
                           s.getStatus() == ChatSession.ChatStatus.ACTIVE)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public void deleteSession(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // Delete all messages in this session
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);
        chatMessageRepository.deleteAll(messages);
        
        // Delete the session
        chatSessionRepository.delete(session);
    }

    public ChatSession assignAgentToSession(String sessionId, Long agentId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        session.setAgent(agent);
        session.setStatus(ChatSession.ChatStatus.ACTIVE);
        chatSessionRepository.save(session);

        // Notify this session's topic (optional)
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId,
                new ChatMessageDTO("System", "Agent joined the chat", ChatMessage.SenderType.SYSTEM, sessionId));

        return session;
    }

    // --- Edit message ---
    public ChatMessage editMessage(Long messageId, String newContent, Long userId) {
        logger.info("Editing message {} by user {}", messageId, userId);
        ChatMessage message = chatMessageRepository.findByIdWithSender(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify the user owns this message
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own messages");
        }
        
        // Update content
        message.setContent(newContent);
        message.setIsEdited(true);
        message.setUpdatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
        
        logger.info("Message {} edited successfully", messageId);
        
        // Broadcast update
        ChatMessageDTO dto = new ChatMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatSessionId(), dto);
        
        return message;
    }

    // --- Delete message (soft delete) ---
    public ChatMessage deleteMessage(Long messageId, Long userId) {
        logger.info("Deleting message {} by user {}", messageId, userId);
        ChatMessage message = chatMessageRepository.findByIdWithSender(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify the user owns this message
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own messages");
        }
        
        // Soft delete
        message.setIsDeleted(true);
        message.setUpdatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
        
        logger.info("Message {} deleted successfully", messageId);
        
        // Broadcast deletion
        ChatMessageDTO dto = new ChatMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatSessionId(), dto);
        
        return message;
    }
}
