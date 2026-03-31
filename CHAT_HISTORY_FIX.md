# Chat History Loading Fix

## Problem
Previous chat messages were not displaying when agents opened a chat session in the agent dashboard.

## Root Cause
The issue was in the `ChatMessageDTO` constructor. It was trying to access the `ChatSession` relationship using lazy loading:

```java
this.sessionId = message.getChatSession() != null ? message.getChatSession().getSessionId() : null;
```

Since `ChatSession` is marked with `@ManyToOne(fetch = FetchType.LAZY)` in the `ChatMessage` entity, the relationship was not loaded when converting to DTO, causing the `sessionId` to be null in the response.

## Solution Applied

### 1. Fixed ChatMessageDTO Constructor
**File**: `src/main/java/lk/sliit/customer_care/dto/ChatMessageDTO.java`

Changed from:
```java
this.sessionId = message.getChatSession() != null ? message.getChatSession().getSessionId() : null;
```

To:
```java
this.sessionId = message.getChatSessionId();
```

This directly uses the `chatSessionId` string field from the `ChatMessage` entity, avoiding the lazy loading issue.

### 2. Added Logging to ChatService
**File**: `src/main/java/lk/sliit/customer_care/service/ChatService.java`

- Added SLF4J logger
- Added logging in `getChatHistory()` method to track:
  - When chat history is requested
  - How many messages are found
  - Session ID being queried

### 3. Enhanced ChatController Error Handling
**File**: `src/main/java/lk/sliit/customer_care/controller/ChatController.java`

- Added SLF4J logger
- Added try-catch block in `getHistory()` endpoint
- Added detailed logging for debugging
- Returns proper HTTP 500 error if something fails

## Changes Summary

### ChatMessageDTO.java
```diff
  public ChatMessageDTO(ChatMessage message) {
      this.content = message.getContent();
      this.senderUsername = message.getSender() != null ? message.getSender().getUsername() : "System";
      this.senderType = message.getSenderType();
-     this.sessionId = message.getChatSession() != null ? message.getChatSession().getSessionId() : null;
+     // Use chatSessionId directly to avoid lazy loading issues
+     this.sessionId = message.getChatSessionId();
      this.createdAt = message.getCreatedAt();
  }
```

### ChatService.java
```diff
+ import org.slf4j.Logger;
+ import org.slf4j.LoggerFactory;

  @Service
  public class ChatService {
  
+     private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
  
      public List<ChatMessage> getChatHistory(String sessionId) {
+         logger.info("Loading chat history for session: {}", sessionId);
-         return chatMessageRepository.findByChatSessionIdAndIsDeletedFalseOrderByCreatedAtAsc(sessionId);
+         List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdAndIsDeletedFalseOrderByCreatedAtAsc(sessionId);
+         logger.info("Found {} messages for session {}", messages.size(), sessionId);
+         return messages;
      }
```

### ChatController.java
```diff
+ import org.slf4j.Logger;
+ import org.slf4j.LoggerFactory;

  @Controller
  public class ChatController {
  
+     private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
  
      @GetMapping("/api/chat/history/{sessionId}")
      @ResponseBody
      public ResponseEntity<List<ChatMessageDTO>> getHistory(@PathVariable String sessionId) {
+         try {
+             logger.info("REST API: Fetching chat history for session: {}", sessionId);
              var messages = chatService.getChatHistory(sessionId);
              var dto = messages.stream().map(ChatMessageDTO::new).toList();
+             logger.info("REST API: Returning {} messages for session {}", dto.size(), sessionId);
              return ResponseEntity.ok(dto);
+         } catch (Exception e) {
+             logger.error("Error fetching chat history for session {}: {}", sessionId, e.getMessage(), e);
+             return ResponseEntity.internalServerError().build();
+         }
      }
```

## Testing Instructions

### Step 1: Restart the Application
Restart your Spring Boot application to apply all changes.

### Step 2: Create Test Data
1. **Login as a customer** (or create a new user account)
2. **Navigate to Live Chat** (e.g., `/live-chat`)
3. **Send several test messages** like:
   - "Hello, I need help"
   - "Can you assist me?"
   - "Is anyone there?"
4. **Keep the chat open**

### Step 3: Test Agent Chat
1. **Open a new browser** (or incognito window)
2. **Login as an agent**
3. **Navigate to Agent Chat Dashboard** (e.g., `/agent/chat`)
4. **Open the browser console** (F12 or Right-click → Inspect → Console)
5. **Click on the customer's chat session** in the sidebar

### Step 4: Verify in Console
You should see detailed logs like:
```
🔍 Selecting session: session-1234567890 for user: customerName
📖 Loading chat history for session: session-1234567890
🌐 Fetching from URL: /api/chat/history/session-1234567890
📡 Response status: 200 OK
📨 Received messages: 3 messages
Messages data: [{...}, {...}, {...}]
✅ Displaying 3 messages
Message 1: {...}
💬 Displaying message: {...}
  → Customer message (left)
  → Sender: customerName
  → Content: Hello, I need help
...
```

### Step 5: Verify in Chat Window
You should see:
- ✅ All previous messages loaded
- ✅ Customer name above their messages (on the left)
- ✅ Agent name above their messages (on the right)
- ✅ Proper alignment and styling

## Debugging

### Browser Console Logs
The enhanced version now includes extensive console logging. Check your browser console (F12) for:

**Connection logs:**
- ✅ `WebSocket Connected: ...` - WebSocket is working
- ❌ `WebSocket connection error: ...` - Connection failed

**Session selection logs:**
- 🔍 `Selecting session: session-xxxxx for user: username`
- 📖 `Loading chat history for session: session-xxxxx`
- 🌐 `Fetching from URL: /api/chat/history/session-xxxxx`

**Response logs:**
- 📡 `Response status: 200 OK` - API call succeeded
- 📨 `Received messages: X messages` - Messages retrieved
- ✅ `Displaying X messages` - Messages being rendered

**Message display logs:**
- 💬 `Displaying message: {...}` - Each message details
- → `Customer message (left)` or `Agent message (right)`
- → `Sender: username`
- → `Content: message text`

### Server Console Logs
Check your Spring Boot application logs for:
- `INFO ChatService - Loading chat history for session: session-xxxxx`
- `INFO ChatService - Found X messages for session session-xxxxx`
- `INFO ChatController - REST API: Fetching chat history for session: session-xxxxx`
- `INFO ChatController - REST API: Returning X messages for session session-xxxxx`

### Common Issues

#### Issue 1: "Error loading chat history" in chat window
**Check console for:**
- Response status (should be 200)
- Error message details

**Possible causes:**
- Session ID mismatch
- Authentication issue
- Database connection problem

**Solution:**
- Verify session ID is correct in the URL
- Check that agent is logged in
- Check database connectivity

#### Issue 2: "No messages yet" but messages exist
**Check console for:**
- `Received messages: 0 messages` - No messages found in database

**Possible causes:**
- Messages saved with different session ID
- Messages marked as deleted (`isDeleted = true`)

**Solution:**
- Check database: `SELECT * FROM chat_messages WHERE chat_session_id = 'session-xxxxx'`
- Verify `chatSessionId` field is being set correctly when saving messages

#### Issue 3: Messages load but don't display
**Check console for:**
- `Received messages: X messages` (X > 0)
- Display logs for each message

**Possible causes:**
- JavaScript error in `displayMessage()` function
- CSS hiding the messages

**Solution:**
- Check console for JavaScript errors
- Inspect the DOM to see if message elements are created

#### Issue 4: WebSocket not connecting
**Check console for:**
- ❌ `WebSocket connection error`

**Possible causes:**
- WebSocket endpoint not configured
- STOMP server not running

**Solution:**
- Verify WebSocketConfig is loaded
- Check application logs for WebSocket initialization
- Try accessing `/chat` endpoint directly

## Technical Notes

### Why This Fix Works
- The `chatSessionId` field is a direct column in the `ChatMessage` table
- No relationship traversal is needed
- Avoids JPA lazy loading initialization exceptions
- Provides consistent behavior regardless of Hibernate session state

### Performance Impact
- **Positive**: Eliminates unnecessary join to ChatSession table
- No additional database queries needed
- Faster DTO conversion

### Related Files
- `src/main/java/lk/sliit/customer_care/modelentity/ChatMessage.java` - Entity with chatSessionId field
- `src/main/java/lk/sliit/customer_care/repository/ChatMessageRepository.java` - Query methods
- `src/main/resources/templates/agent-chat.html` - Frontend display logic
