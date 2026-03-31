package lk.sliit.customer_care.controller;

import lk.sliit.customer_care.modelentity.AgentResponse;
import lk.sliit.customer_care.modelentity.Ticket;
import lk.sliit.customer_care.modelentity.User;
import lk.sliit.customer_care.repository.AgentResponseRepository;
import lk.sliit.customer_care.repository.TicketRepository;
import lk.sliit.customer_care.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class AgentResponseController {

    private final AgentResponseRepository agentResponseRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public AgentResponseController(AgentResponseRepository agentResponseRepository,
                                   TicketRepository ticketRepository,
                                   UserRepository userRepository) {
        this.agentResponseRepository = agentResponseRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // Add or update a response for a ticket
    @PostMapping("/respond/{ticketId}")
    public String addOrUpdateResponse(@PathVariable Long ticketId,
                                      @RequestParam String responseText,
                                      @RequestParam String action,
                                      Authentication authentication,
                                      Model model) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            // Get the currently logged-in agent
            String username = authentication.getName();
            User agent = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            // Create a new response (allow multiple responses)
            AgentResponse newResponse = new AgentResponse();
            newResponse.setTicket(ticket);
            newResponse.setAgent(agent);
            newResponse.setResponseText(responseText);
            newResponse.setCreatedAt(LocalDateTime.now());
            agentResponseRepository.save(newResponse);

            // Update the ticket status based on the response action
            ticket.setStatus(action);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            model.addAttribute("message", "Response sent successfully!");
            return "redirect:/agent/tickets";
        } catch (Exception e) {
            model.addAttribute("error", "Error sending response: " + e.getMessage());
            return "redirect:/agent/tickets";
        }
    }

    // Show the response for a specific ticket (history view)
    @GetMapping("/responses/{ticketId}")
    public String getResponseForTicket(@PathVariable Long ticketId, Model model) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Fetch all responses for the ticket
        List<AgentResponse> responses = agentResponseRepository.findByTicket(ticket);

        // Display the first response if available, or null if not
        AgentResponse response = responses.isEmpty() ? null : responses.get(0);

        // Add the ticket and its response (if available)
        model.addAttribute("ticket", ticket);
        model.addAttribute("response", response); // Display the single response (null if none)

        return "ticket-response"; // Render the response view
    }

    // Debug/REST endpoint
    @ResponseBody
    @GetMapping("/responses/all")
    public Iterable<AgentResponse> getAllResponses() {
        return agentResponseRepository.findAll();
    }
}
