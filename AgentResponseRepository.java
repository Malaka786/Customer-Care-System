package lk.sliit.customer_care.repository;

import lk.sliit.customer_care.modelentity.AgentResponse;
import lk.sliit.customer_care.modelentity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentResponseRepository extends JpaRepository<AgentResponse, Long> {

    // Return a list of responses for a ticket (even if there is only one)
    List<AgentResponse> findByTicket(Ticket ticket);
}
