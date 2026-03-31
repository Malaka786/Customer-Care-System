package lk.sliit.customer_care.service;

import lk.sliit.customer_care.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Singleton pattern implementation for managing service instances
 * Ensures only one instance of service manager exists throughout the application
 */
@Component
public class SingletonServiceManager {
    
    private static volatile SingletonServiceManager instance;
    private static final Object lock = new Object();
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private FAQRepository faqRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private AgentResponseRepository agentResponseRepository;
    
    // Private constructor to prevent instantiation
    private SingletonServiceManager() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    
    /**
     * Thread-safe singleton implementation using double-checked locking
     * @return SingletonServiceManager instance
     */
    public static SingletonServiceManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SingletonServiceManager();
                }
            }
        }
        return instance;
    }
    
    // Getters for repositories
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    public TicketRepository getTicketRepository() {
        return ticketRepository;
    }
    
    public FeedbackRepository getFeedbackRepository() {
        return feedbackRepository;
    }
    
    public FAQRepository getFAQRepository() {
        return faqRepository;
    }
    
    public ChatSessionRepository getChatSessionRepository() {
        return chatSessionRepository;
    }
    
    public ChatMessageRepository getChatMessageRepository() {
        return chatMessageRepository;
    }
    
    public AgentResponseRepository getAgentResponseRepository() {
        return agentResponseRepository;
    }
    
    /**
     * Prevent cloning of singleton
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Singleton cannot be cloned");
    }
    
    /**
     * Prevent deserialization of singleton
     */
    protected Object readResolve() {
        return getInstance();
    }
}

