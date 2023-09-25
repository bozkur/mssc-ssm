package guru.springframework.msscssm.repository;

import guru.springframework.msscssm.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author cevher
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
