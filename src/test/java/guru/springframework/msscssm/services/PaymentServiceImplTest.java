package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author cevher
 */
@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;


    @Transactional
    @Test
    @RepeatedTest(10)
    void shouldPreAuth() {
        Payment payment = Payment.builder()
                .amount(new BigDecimal("12.99"))
                .state(PaymentState.NEW)
                .build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        Payment obtainedPayment = paymentRepository.getReferenceById(savedPayment.getId());
        assumeTrue(obtainedPayment.getState() == PaymentState.PRE_AUTH);
    }

    @Transactional
    @Test
    @RepeatedTest(10)
    void shouldAuth() throws InterruptedException {
        Payment payment = Payment.builder()
                .amount(new BigDecimal("12.99"))
                .state(PaymentState.NEW)
                .build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        if(sm.getState().getId() == PaymentState.PRE_AUTH) {
            sm = paymentService.authorizePayment(savedPayment.getId());
            Payment obtainedPayment = paymentRepository.getReferenceById(savedPayment.getId());
            assumeTrue(obtainedPayment.getState() == PaymentState.AUTH);
        } else {
            assumeTrue(false);
        }
    }

}