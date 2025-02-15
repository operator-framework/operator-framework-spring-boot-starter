package io.javaoperatorsdk.operator.springboot.starter;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.RegisteredController;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatorStarterTest {

  @Mock
  private Operator operator;
  @Mock
  private io.javaoperatorsdk.operator.springboot.starter.crd.CRDApplier CRDApplier;
  @Mock
  private ApplicationReadyEvent event;
  @Mock
  private ConfigurableApplicationContext context;
  @InjectMocks
  private OperatorStarter starter;

  @Test
  void shouldShutdownOnApplyFailure() {
    when(event.getApplicationContext()).thenReturn(context);
    when(operator.getRegisteredControllers()).thenReturn(Set.of(mock(RegisteredController.class)));
    doThrow(new IllegalStateException("False Knees")).when(CRDApplier).apply();

    starter.start(event);

    verify(CRDApplier).apply();
    verify(operator, never()).start();
    verify(context).close();
  }

  @Test
  void shouldShutdownOnStartFailure() {
    when(event.getApplicationContext()).thenReturn(context);
    when(operator.getRegisteredControllers()).thenReturn(Set.of(mock(RegisteredController.class)));
    doThrow(new IllegalStateException("False Knees")).when(operator).start();

    starter.start(event);

    verify(CRDApplier).apply();
    verify(operator).start();
    verify(context).close();
  }

  @Test
  void shouldNotStartWithoutReconcilers() {
    starter.start(event);

    verify(operator, never()).start();
    verify(CRDApplier, never()).apply();
  }

}
