package io.javaoperatorsdk.operator.springboot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDApplier;


@Component
public class OperatorStarter {

  private static final Logger log = LoggerFactory.getLogger(OperatorStarter.class);

  private final Operator operator;
  private final CRDApplier crdApplier;


  public OperatorStarter(Operator operator, CRDApplier crdApplier) {
    this.operator = operator;
    this.crdApplier = crdApplier;
  }

  @EventListener
  public void start(ApplicationReadyEvent event) {
    if (operator.getRegisteredControllers().isEmpty()) {
      log.warn(
          "No Reconcilers found in the application context: Not starting the Operator, not looking for CRDs");
      return;
    }
    try {
      crdApplier.apply();
      operator.start();
    } catch (Exception ex) {
      log.error("Could not start operator", ex);
      SpringApplication.exit(event.getApplicationContext(), () -> 1);
    }
  }
}
