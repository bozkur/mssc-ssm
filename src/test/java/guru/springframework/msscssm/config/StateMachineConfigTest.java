package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author cevher
 */
@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    private StateMachine<PaymentState, PaymentEvent> stateMachine;
    @BeforeEach
    void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.startReactively().block();
    }

    @Test
    @DisplayName("Test new -> new when incoming event is pre authorize")
    void shouldCorrectStateTransitionOccursWhenPreAuthorizeEventComes() {
        stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.NEW));
    }

    @Test
    @DisplayName("Test new -> pre_auth when event is pre auth approved")
    void shouldCorrectStateTransitionOccursWhenPreAuthApprovedEventComes() {
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.PRE_AUTH));
    }

    @Test
    @DisplayName("Test new -> auth_error when event is pre auth declined")
    void shouldCorrectStateTransitionOccursWhenPreAuthDeclinedEventComes() {
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_DECLINED);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.AUTH_ERROR));
    }
}