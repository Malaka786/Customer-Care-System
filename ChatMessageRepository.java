package lk.sliit.customer_care.repository;

import lk.sliit.customer_care.modelentity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ✅ Fetch all messages in a chat session (includes sender details)
    @Query("""
        SELECT cm FROM ChatMessage cm 
        JOIN FETCH cm.sender 
        WHERE cm.chatSessionId = :sessionId 
        ORDER BY cm.createdAt ASC
        """)
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(
            @Param("sessionId") String sessionId);

    // ✅ Fetch only non-deleted messages for a session
    @Query("""
        SELECT cm FROM ChatMessage cm 
        JOIN FETCH cm.sender 
        WHERE cm.chatSessionId = :sessionId 
          AND cm.isDeleted = false 
        ORDER BY cm.createdAt ASC
        """)
    List<ChatMessage> findByChatSessionIdAndIsDeletedFalseOrderByCreatedAtAsc(
            @Param("sessionId") String sessionId);

    // ✅ Fetch messages for a specific sender (non-deleted only)
    @Query("""
        SELECT cm FROM ChatMessage cm 
        JOIN FETCH cm.sender 
        WHERE cm.chatSessionId = :sessionId 
          AND cm.sender.id = :senderId 
          AND cm.isDeleted = false 
        ORDER BY cm.createdAt ASC
        """)
    List<ChatMessage> findByChatSessionIdAndSenderIdOrderByCreatedAtAsc(
            @Param("sessionId") String sessionId,
            @Param("senderId") Long senderId);

    // ✅ Fetch messages filtered by sender type (USER / AGENT / SYSTEM)
    @Query("""
        SELECT cm FROM ChatMessage cm 
        JOIN FETCH cm.sender 
        WHERE cm.chatSessionId = :sessionId 
          AND cm.senderType = :senderType 
          AND cm.isDeleted = false 
        ORDER BY cm.createdAt ASC
        """)
    List<ChatMessage> findByChatSessionIdAndSenderTypeOrderByCreatedAtAsc(
            @Param("sessionId") String sessionId,
            @Param("senderType") ChatMessage.SenderType senderType);

    // ✅ Fetch a single message with sender (to avoid LazyInitialization issues)
    @Query("""
        SELECT cm FROM ChatMessage cm 
        JOIN FETCH cm.sender 
        WHERE cm.id = :id
        """)
    Optional<ChatMessage> findByIdWithSender(@Param("id") Long id);
}
