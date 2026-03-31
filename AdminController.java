package lk.sliit.customer_care.controller;

import lk.sliit.customer_care.modelentity.ChatSession;
import lk.sliit.customer_care.modelentity.FAQ;
import lk.sliit.customer_care.modelentity.Ticket;
import lk.sliit.customer_care.modelentity.User;
import lk.sliit.customer_care.repository.ChatSessionRepository;
import lk.sliit.customer_care.repository.FAQRepository;
import lk.sliit.customer_care.repository.TicketRepository;
import lk.sliit.customer_care.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private FAQRepository faqRepository;

    // Create new agent
    @PostMapping("/create-agent")
    public String createAgent(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              @RequestParam String phoneNumber,
                              @RequestParam String address,
                              Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "redirect:/admin/dashboard?error=PasswordsDoNotMatch";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "redirect:/admin/dashboard?error=UserExists";
        }

        User agent = new User();
        agent.setUsername(username);
        agent.setPassword(passwordEncoder.encode(password));
        agent.setRole("ROLE_AGENT");
        agent.setPhoneNumber(phoneNumber);
        agent.setAddress(address);

        userRepository.save(agent);

        return "redirect:/admin/dashboard?success=AgentCreated";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> allUsers = userRepository.findAll();
        
        // Separate users by role
        List<User> users = allUsers.stream()
                .filter(u -> "ROLE_USER".equals(u.getRole()))
                .collect(Collectors.toList());
        
        List<User> agents = allUsers.stream()
                .filter(u -> "ROLE_AGENT".equals(u.getRole()))
                .collect(Collectors.toList());
        
        List<User> admins = allUsers.stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());
        
        model.addAttribute("users", users);
        model.addAttribute("agents", agents);
        model.addAttribute("admins", admins);
        
        return "admin-users";
    }
    
    @DeleteMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Prevent deletion of admin accounts
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Cannot delete admin accounts"));
            }
            
            userRepository.delete(user);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("address", user.getAddress());
            userData.put("role", user.getRole());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", userData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PutMapping("/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update user details
            String phoneNumber = (String) requestBody.get("phoneNumber");
            String address = (String) requestBody.get("address");
            String password = (String) requestBody.get("password");
            
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }
            
            if (address != null && !address.trim().isEmpty()) {
                user.setAddress(address);
            }
            
            // Only update password if provided
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/analytics")
    public String getAnalytics(Model model) {
        // User Statistics
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        long totalAgents = allUsers.stream().filter(u -> "ROLE_AGENT".equals(u.getRole())).count();
        long totalAdmins = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        
        // Ticket Statistics
        List<Ticket> allTickets = ticketRepository.findAll();
        long totalTickets = allTickets.size();
        long openTickets = allTickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgressTickets = allTickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long resolvedTickets = allTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        long closedTickets = allTickets.stream().filter(t -> "CLOSED".equals(t.getStatus())).count();
        
        // Ticket Category Breakdown
        Map<String, Long> ticketsByCategory = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCategory, Collectors.counting()));
        
        // Chat Session Statistics
        List<ChatSession> allSessions = chatSessionRepository.findAll();
        long totalChatSessions = allSessions.size();
        long activeChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.ACTIVE)
                .count();
        long waitingChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.WAITING)
                .count();
        long closedChatSessions = allSessions.stream()
                .filter(s -> s.getStatus() == ChatSession.ChatStatus.CLOSED)
                .count();
        
        // Add all statistics to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAgents", totalAgents);
        model.addAttribute("totalAdmins", totalAdmins);
        
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("closedTickets", closedTickets);
        
        model.addAttribute("ticketsByCategory", ticketsByCategory);
        
        model.addAttribute("totalChatSessions", totalChatSessions);
        model.addAttribute("activeChatSessions", activeChatSessions);
        model.addAttribute("waitingChatSessions", waitingChatSessions);
        model.addAttribute("closedChatSessions", closedChatSessions);
        
        return "admin-analytics";
    }
    
    // Manage pending FAQ approvals
    @GetMapping("/faq/pending")
    public String managePendingFAQs(Model model) {
        List<FAQ> pendingFaqs = faqRepository.findByIsApprovedFalseOrderByCreatedAtDesc();
        model.addAttribute("pendingFaqs", pendingFaqs);
        return "admin-faq-approval";
    }
    
    // Approve FAQ
    @PostMapping("/faq/approve/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveFAQ(@PathVariable Long id) {
        try {
            System.out.println("Approve FAQ called for ID: " + id);
            
            FAQ faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));
            
            System.out.println("FAQ found: " + faq.getQuestion());
            
            // Get current admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current authenticated user: " + auth.getName());
            System.out.println("User authorities: " + auth.getAuthorities());
            
            // Find admin user - optional, approval can work without linking to admin
            User admin = userRepository.findByUsername(auth.getName()).orElse(null);
            
            if (admin != null) {
                System.out.println("Admin found: " + admin.getUsername() + " (ID: " + admin.getId() + ")");
                faq.setApprovedBy(admin);
            } else {
                System.out.println("Warning: User not found in database, but proceeding with approval");
                faq.setApprovedBy(null); // Approval without linking to user
            }
            
            faq.setIsApproved(true);
            faq.setApprovedAt(LocalDateTime.now());
            faqRepository.save(faq);
            
            System.out.println("FAQ approved successfully!");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FAQ approved successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error approving FAQ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Reject FAQ
    @DeleteMapping("/faq/reject/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectFAQ(@PathVariable Long id) {
        try {
            System.out.println("Reject FAQ called for ID: " + id);
            
            FAQ faq = faqRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));
            
            System.out.println("FAQ found, deleting: " + faq.getQuestion());
            
            faqRepository.delete(faq);
            
            System.out.println("FAQ deleted successfully!");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FAQ rejected and deleted"
            ));
        } catch (Exception e) {
            System.err.println("Error rejecting FAQ: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
