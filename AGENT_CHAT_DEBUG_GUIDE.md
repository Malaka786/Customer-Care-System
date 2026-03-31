# Agent Chat - Debug & Testing Guide

## Quick Fix Summary

I've added **extensive logging** to help diagnose why previous messages aren't loading in the agent chat.

## What Changed

### 1. Enhanced Browser Console Logging
The agent-chat.html now logs **every step** of the process:
- WebSocket connection status
- Session selection
- API calls to load history
- Response status and data
- Each message being displayed

### 2. Fixed DTO Issue
The `ChatMessageDTO` was using lazy-loaded relationship. Now it uses the direct `chatSessionId` field.

### 3. Added Server-Side Logging
Both `ChatService` and `ChatController` now log:
- When history is requested
- How many messages are found
- Any errors that occur

## How to Test

### Step 1: Restart Application
```bash
# Stop the application and restart it
mvn spring-boot:run
```

### Step 2: Create Test Messages
1. Open browser → Login as **customer**
2. Go to **Live Chat** (`/live-chat`)
3. Send 3-4 test messages:
   - "Hello"
   - "I need help"
   - "Are you there?"

### Step 3: Test Agent View
1. Open **new browser/incognito** → Login as **agent**
2. Go to **Agent Chat** (`/agent/chat`)
3. **Open Developer Console** (Press F12)
4. Click on the customer's session in the sidebar

### Step 4: Check Console Logs

You should see something like this:

```
🔍 Selecting session: session-1761062803411 for user: sithira
📖 Loading chat history for session: session-1761062803411
🌐 Fetching from URL: /api/chat/history/session-1761062803411
📡 Response status: 200 OK
📨 Received messages: 3 messages
Messages data: [
  {content: "Hello", senderUsername: "sithira", senderType: "USER", ...},
  {content: "I need help", senderUsername: "sithira", senderType: "USER", ...},
  {content: "Are you there?", senderUsername: "sithira", senderType: "USER", ...}
]
✅ Displaying 3 messages
Message 1: {content: "Hello", senderUsername: "sithira", senderType: "USER"}
💬 Displaying message: {...}
  → Customer message (left)
  → Sender: sithira
  → Content: Hello
Message 2: {...}
...
```

## Troubleshooting

### Problem: "Error loading chat history" Shows in Chat

**Look for in console:**
```
❌ Error loading chat history: Failed to load chat history: 404
```

**This means:**
- The API endpoint is returning 404 (not found)
- Session ID might be incorrect

**Solution:**
1. Check the URL in console: `/api/chat/history/session-xxxxx`
2. Verify session ID exists in database
3. Check server logs for errors

---

### Problem: "No messages yet" But Messages Exist

**Look for in console:**
```
📨 Received messages: 0 messages
ℹ️ No messages found, showing empty state
```

**This means:**
- API call succeeded but returned empty array
- Messages might be in database with different session ID

**Solution:**
1. Check database directly:
```sql
SELECT * FROM chat_messages 
WHERE chat_session_id = 'session-1761062803411' 
AND is_deleted = false;
```

2. Check that messages are being saved with correct session ID
3. Verify `isDeleted` flag is false

---

### Problem: Console Shows "Response status: 500"

**Look for in server logs:**
```
ERROR ChatController - Error fetching chat history for session session-xxxxx: ...
```

**This means:**
- Server error occurred
- Check the full error message in server logs

**Common causes:**
- Database connection issue
- JPA query problem
- Null pointer exception

**Solution:**
- Check server console for full stack trace
- Verify database is running
- Check if `chatSessionId` field is null in messages

---

### Problem: WebSocket Not Connecting

**Look for in console:**
```
❌ WebSocket connection error: ...
```

**This means:**
- WebSocket endpoint `/chat` not accessible

**Solution:**
1. Verify `WebSocketConfig` is loaded
2. Check application startup logs for WebSocket configuration
3. Try accessing `http://localhost:8080/chat` in browser

---

### Problem: Messages Display But No Sender Names

**Look for in console:**
```
💬 Displaying message: {content: "Hello", senderUsername: null, ...}
  → Sender: Customer  (fallback used)
```

**This means:**
- `senderUsername` is null in the DTO
- Falling back to "Customer" or "Agent"

**Solution:**
- Check that `message.getSender()` is not null
- Verify User entity is properly loaded
- Check database for sender_id in chat_messages table

---

## Expected Behavior

### ✅ Success Indicators

**In Browser Console:**
- ✅ WebSocket Connected
- 📖 Loading chat history
- 📡 Response status: 200 OK
- 📨 Received messages: X messages (X > 0)
- ✅ Displaying X messages
- 💬 Multiple "Displaying message" logs

**In Chat Window:**
- Customer messages on the LEFT with customer name
- Agent messages on the RIGHT with agent name
- All previous messages visible
- Scrolled to bottom automatically

**In Server Console:**
```
INFO ChatService - Loading chat history for session: session-xxxxx
INFO ChatService - Found 3 messages for session session-xxxxx
INFO ChatController - REST API: Fetching chat history for session: session-xxxxx
INFO ChatController - REST API: Returning 3 messages for session session-xxxxx
```

## Test Checklist

- [ ] Application restarted
- [ ] Customer can send messages in live chat
- [ ] Agent can see customer session in sidebar
- [ ] Browser console open (F12)
- [ ] Clicked on customer session
- [ ] Console shows "Loading chat history"
- [ ] Console shows "Response status: 200 OK"
- [ ] Console shows "Received messages: X messages" (X > 0)
- [ ] Chat window shows all previous messages
- [ ] Each message shows sender name
- [ ] Customer messages on left, agent messages on right
- [ ] New messages appear in real-time

## Still Not Working?

If after following all steps above, messages still don't load:

1. **Share the browser console output** - Copy all console messages
2. **Share server console output** - Copy relevant server logs
3. **Check database** - Run this query:
```sql
SELECT cs.session_id, cs.status, u.username, 
       COUNT(cm.id) as message_count
FROM ChatSession cs
LEFT JOIN chat_messages cm ON cs.session_id = cm.chat_session_id
LEFT JOIN User u ON cs.user_id = u.id
WHERE cs.status IN ('WAITING', 'ACTIVE')
GROUP BY cs.session_id, cs.status, u.username;
```

This will show:
- Active/waiting sessions
- How many messages each has
- Which user owns each session

## Files Modified

1. ✅ `ChatMessageDTO.java` - Fixed lazy loading issue
2. ✅ `ChatService.java` - Added logging
3. ✅ `ChatController.java` - Added logging and error handling
4. ✅ `agent-chat.html` - Added extensive console logging

## Next Steps

After confirming messages load:
1. Test real-time messaging (send new message while agent viewing)
2. Test multiple concurrent sessions
3. Test message deletion
4. Test WebSocket reconnection after network issue
