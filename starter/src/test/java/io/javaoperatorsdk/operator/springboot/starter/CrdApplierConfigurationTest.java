package io.javaoperatorsdk.operator.springboot.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.springboot.starter.crd.DefaultCRDApplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CrdApplierConfigurationTest {

  private static final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withUserConfiguration(OperatorAutoConfiguration.class)
      .withBean(DefaultCRDApplier.class)
      .withBean(Operator.class, () -> mock(Operator.class));

  @Test
  void shouldNotCreateByDefault() {
    runner.run(ctx -> assertThat(ctx)
        .doesNotHaveBean("defaultCRDApplier")
        .hasBean("disabledCrdApplier"));
  }

  @Test
  void shouldNotCreateWhenDisabled() {
    runner.withPropertyValues("javaoperatorsdk.crd.apply-on-startup=false")
        .run(ctx -> assertThat(ctx)
            .doesNotHaveBean("defaultCRDApplier")
            .hasBean("disabledCrdApplier"));
  }

  @Test
  void shouldCreateWhenEnabled() {
    runner.withPropertyValues("javaoperatorsdk.crd.apply-on-startup=true")
        .run(ctx -> assertThat(ctx)
            .hasBean("defaultCRDApplier")
            .doesNotHaveBean("disabledCrdApplier"));
  }
}
