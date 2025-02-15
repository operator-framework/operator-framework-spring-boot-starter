package io.javaoperatorsdk.operator.springboot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.javaoperatorsdk.operator.Operator;

@Component
public class OperatorHealthIndicator extends AbstractHealthIndicator {

  private final static Logger log = LoggerFactory.getLogger(OperatorHealthIndicator.class);
  private final Operator operator;

  public OperatorHealthIndicator(final Operator operator) {
    super("OperatorSDK health check failed");
    Assert.notNull(operator, "OperatorSDK Operator not initialized");
    this.operator = operator;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    final var runtimeInfo = operator.getRuntimeInfo();
    log.debug("Executing health check for {}", runtimeInfo);
    if (runtimeInfo.isStarted()) {
      final boolean[] healthy = {true};
      runtimeInfo.getRegisteredControllers().forEach(rc -> {
        final var name = rc.getConfiguration().getName();
        final var unhealthy = rc.getControllerHealthInfo().unhealthyEventSources();
        if (unhealthy.isEmpty()) {
          builder.withDetail(name, "OK");
        } else {
          healthy[0] = false;
          builder.withDetail(name, "unhealthy: " + String.join(", ", unhealthy.keySet()));
        }
      });
      log.debug("Healthy: {}", healthy[0]);
      if (healthy[0]) {
        builder.up();
      } else {
        builder.down();
      }
    } else {
      log.debug("Healthy: unknown");
      builder.unknown();
    }
  }
}
