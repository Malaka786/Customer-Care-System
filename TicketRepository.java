package lk.sliit.customer_care.repository;

import lk.sliit.customer_care.modelentity.Ticket;
import lk.sliit.customer_care.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUser(User user);  // ✅ get tickets for logged-in user
    long countByStatus(String status);   // ✅ count tickets by status
}