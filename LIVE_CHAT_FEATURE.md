# Live Chat Feature Documentation

## Overview
The live chat feature provides real-time communication between customers and support agents with the ability to edit and delete messages. This feature is built using Spring WebSocket with STOMP protocol for real-time messaging.

## Features

### For Customers
- **Real-time messaging**: Send and receive messages instantly
- **Message editing**: Edit your own messages within 5 minutes of sending
- **Message deletion**: Delete your own messages
- **Chat history**: View previous messages when reconnecting
- **Auto-reconnection**: Automatically reconnects if connection is lost

### For Agents
- **Session management**: View waiting and active chat sessions
- **Real-time responses**: Respond to customers instantly
- **Session assignment**: Assign waiting sessions to yourself
- **Multiple sessions**: Handle multiple customer chats simultaneously
- **Message history**: View complete chat history for each session

## Technical Implementation

### Backend Components

#### 1. Entities
- **ChatMessage**: Stores individual chat messages with editing capabilities
- **ChatSession**: Manages chat sessions between users and agents

#### 2. Repositories
- **ChatMessageRepository**: Data access for chat messages
- **ChatSessionRepository**: Data access for chat sessions

#### 3. Services
- **ChatService**: Business logic for chat operations including:
  - Creating and managing chat sessions
  - Sending, editing, and deleting messages
  - Real-time message broadcasting
  - Agent assignment to sessions

#### 4. Controllers
- **ChatController**: Handles WebSocket messages and REST endpoints
  - `/live-chat`: Customer chat interface
  - `/agent/chat`: Agent chat dashboard
  - WebSocket endpoints for real-time communication

### Frontend Components

#### 1. Customer Interface (`live-chat.html`)
- Real-time WebSocket connection using SockJS and STOMP
- Message editing with inline textarea
- Message deletion with confirmation
- Auto-scroll to latest messages
- Responsive design for mobile devices

#### 2. Agent Interface (`agent-chat.html`)
- Session management sidebar
- Real-time chat window
- Session assignment functionality
- Multi-session support

### WebSocket Configuration
- **Endpoint**: `/chat` with SockJS fallback
- **Message Broker**: Simple broker on `/topic`
- **Application Prefix**: `/app`
- **CORS**: Configured for cross-origin requests

## Database Schema

### Chat Messages Table
```sql
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    content NVARCHAR(1000) NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_type NVARCHAR(20) NOT NULL,
    chat_session_id NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2,
    is_edited BIT NOT NULL DEFAULT 0,
    is_deleted BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (sender_id) REFERENCES users(id)
);
```

### Chat Sessions Table
```sql
CREATE TABLE chat_sessions (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    session_id NVARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    agent_id BIGINT,
    status NVARCHAR(20) NOT NULL,
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2,
    ended_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (agent_id) REFERENCES users(id)
);
```

## API Endpoints

### WebSocket Endpoints
- `/app/chat.send`: Send a new message
- `/app/chat.edit`: Edit an existing message
- `/app/chat.delete`: Delete a message
- `/topic/chat/{sessionId}`: Subscribe to session messages

### REST Endpoints
- `GET /live-chat`: Customer chat interface
- `GET /agent/chat`: Agent chat dashboard
- `POST /api/chat/assign-agent`: Assign agent to session
- `GET /api/chat/history/{sessionId}`: Get chat history

## Usage Instructions

### For Customers
1. Navigate to `/live-chat`
2. Start typing your message
3. Press Enter to send (Shift+Enter for new line)
4. Hover over your messages to see edit/delete options
5. Click edit to modify your message
6. Click delete to remove your message

### For Agents
1. Navigate to `/agent/chat`
2. View waiting sessions in the sidebar
3. Click "Assign to Me" to take a session
4. Select a session to start chatting
5. Type responses and press Enter to send
6. Switch between multiple sessions as needed

## Security Features
- Users can only edit/delete their own messages
- Message editing is limited to 5 minutes after sending
- Session validation ensures messages are sent to valid sessions
- Authentication required for both customer and agent interfaces

## Performance Considerations
- WebSocket connections are lightweight and efficient
- Message history is loaded on demand
- Auto-reconnection handles network interruptions
- Database queries are optimized with proper indexing

## Browser Support
- Modern browsers with WebSocket support
- SockJS provides fallback for older browsers
- Mobile-responsive design for all devices

## Troubleshooting
- Check browser console for WebSocket connection errors
- Verify database tables are created properly
- Ensure WebSocket configuration allows CORS
- Check that user authentication is working correctly

## Future Enhancements
- File sharing capabilities
- Typing indicators
- Message reactions/emojis
- Chat transcripts export
- Advanced session analytics
- Bot integration for automated responses
