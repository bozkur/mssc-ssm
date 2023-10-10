package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.security.SecureRandom;
import java.util.EnumSet;

/**
 * @author cevher
 */
@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .states(EnumSet.allOf(PaymentState.class))
                .initial(PaymentState.NEW)
                .end(PaymentState.AUTH)
                .end(PaymentState.AUTH_ERROR)
                .end(PaymentState.PRE_AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction()).guard(idExistanceGuard())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE).target(PaymentState.PRE_AUTH).action(authAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).event(PaymentEvent.AUTH_APPROVED).target(PaymentState.AUTH)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED);

    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {

        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State is changed from {} to {}", from == null? from : from.getId(), to.getId());
            }
        };
        config.withConfiguration().listener(adapter);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            System.out.println("Pre auth is called");
            int random = new SecureRandom().nextInt(10);
            if (random < 9) {
                System.out.println("Approved.");
                Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build();
                context.getStateMachine().sendEvent(message);
            } else {
                Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build();
                context.getStateMachine().sendEvent(message);
            }
        };
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
          System.out.println("Auth is called.");
          int random = new SecureRandom().nextInt(10);

          if (random < 8) {
              System.out.println("Credit OK!");
              Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                      .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                      .build();
              context.getStateMachine().sendEvent(message);
          } else {
              System.out.println("Credit is low!!!");
              Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                      .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                      .build();
              context.getStateMachine().sendEvent(message);
          }
        };
    }

    private Guard<PaymentState, PaymentEvent> idExistanceGuard() {
        return context -> {
            Object paymentIdHeader = context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER);
            if (paymentIdHeader == null) {
                return false;
            }
            return paymentIdHeader instanceof Long;
        };
    }
}
