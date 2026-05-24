package org.example.protocol;

public enum MessageType {
    AUTH,           // Authentication request (login)
    AUTH_SUCCESS,   // Authentication success response
    AUTH_ERROR,     // Authentication error response
    CHAT,           // Broadcast chat message
    PRIVATE_CHAT,   // Private chat message
    USER_LIST,      // Update list of online users
    DELETE_MESSAGE, // Delete a message
    DISCONNECT      // Client disconnecting
}
