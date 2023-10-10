package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
        stateMachine.start();
    }

    @AfterEach
    void tearDown() {
        stateMachine.stop();
    }

    @Test
    @DisplayName("Test new -> new when incoming event is pre authorize")
    void shouldCorrectStateTransitionOccursWhenPreAuthorizeEventComes() {
        Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE)
                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, 5L)
                .build();
        stateMachine.sendEvent(message);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.PRE_AUTH));
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
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.PRE_AUTH_ERROR));
    }

    @Test
    void shouldTranslateFromPreAuthToAuth() {
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        stateMachine.sendEvent(PaymentEvent.AUTH_APPROVED);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.AUTH));
    }

    @Test
    void shouldTranslateFromPreAuthToAuthError() {
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        stateMachine.sendEvent(PaymentEvent.AUTH_DECLINED);
        assertThat(stateMachine.getState().getId(), Matchers.equalTo(PaymentState.AUTH_ERROR));
    }
}