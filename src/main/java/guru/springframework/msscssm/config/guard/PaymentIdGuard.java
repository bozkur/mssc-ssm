package guru.springframework.msscssm.config.guard;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

/**
 * @author cevher
 */
@Component
public class PaymentIdGuard implements Guard<PaymentState, PaymentEvent> {
    @Override
    public boolean evaluate(StateContext<PaymentState, PaymentEvent> context) {
        Object paymentIdHeader = context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER);
        if (paymentIdHeader == null) {
            return false;
        }
        return paymentIdHeader instanceof Long;
    }
}
