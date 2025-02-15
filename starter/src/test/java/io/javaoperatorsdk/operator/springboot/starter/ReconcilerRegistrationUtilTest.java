package io.javaoperatorsdk.operator.springboot.starter;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig
class ReconcilerRegistrationUtilTest {

  Map<String, List<DependentResourceConfigurator>> configurators = Map.of(
      "SingleEntry", List.of(() -> "SingleEntry"),
      "SecondEntry", List.of(() -> "SecondEntry"),
      "DoubleEntry", List.of(() -> "DoubleEntry", () -> "DoubleEntry"));

  @Test
  void testFilterConfiguratorsNoWorkflow() {
    Reconciler<?> test = (resource, context) -> null;

    List<DependentResourceConfigurator> result = assertDoesNotThrow(
        () -> ReconcilerRegistrationUtil.filterConfigurators(test, configurators));
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void testFilterConfiguratorsWithEmptyWorkflow() {
    Reconciler<?> test = new TestEmptyReconciler();

    List<DependentResourceConfigurator> result = assertDoesNotThrow(
        () -> ReconcilerRegistrationUtil.filterConfigurators(test, configurators));
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void testFilterConfiguratorsWithSingleEntryWorkflow() {
    Reconciler<?> test = new TestSingleEntryReconciler();

    List<DependentResourceConfigurator> result = assertDoesNotThrow(
        () -> ReconcilerRegistrationUtil.filterConfigurators(test, configurators));
    assertThat(result)
        .isNotNull()
        .hasSize(1)
        .element(0)
        .matches(e -> "SingleEntry".equals(e.getName()));
  }

  @Test
  void testFilterConfiguratorsWithSecondEntryWorkflow() {
    Reconciler<?> test = new TestSecondEntryReconciler();

    List<DependentResourceConfigurator> result = assertDoesNotThrow(
        () -> ReconcilerRegistrationUtil.filterConfigurators(test, configurators));
    assertThat(result)
        .isNotNull()
        .hasSize(2)
        .anyMatch(e -> "SingleEntry".equals(e.getName()))
        .anyMatch(e -> "SecondEntry".equals(e.getName()));
  }

  @Test
  void testFilterConfiguratorsWithDoubleEntryWorkflow() {
    Reconciler<?> test = new TestDoubleEntryReconciler();

    Throwable th = assertThrows(IllegalStateException.class,
        () -> ReconcilerRegistrationUtil.filterConfigurators(test, configurators));
    assertThat(th)
        .isNotNull()
        .hasMessageContaining("more than one config for Dependent Resource")
        .hasMessageContaining("DoubleEntry");
  }

  @Workflow(dependents = {})
  private static class TestEmptyReconciler implements Reconciler<HasMetadata> {

    @Override
    public UpdateControl<HasMetadata> reconcile(HasMetadata resource,
        Context<HasMetadata> context) {
      return null;
    }
  }

  @Workflow(dependents = {
      @Dependent(name = "SingleEntry", type = DependentResource.class)
  })
  private static class TestSingleEntryReconciler implements Reconciler<HasMetadata> {

    @Override
    public UpdateControl<HasMetadata> reconcile(HasMetadata resource,
        Context<HasMetadata> context) {
      return null;
    }
  }

  @Workflow(dependents = {
      @Dependent(name = "SingleEntry", type = DependentResource.class),
      @Dependent(name = "SecondEntry", type = DependentResource.class)
  })
  private static class TestSecondEntryReconciler implements Reconciler<HasMetadata> {

    @Override
    public UpdateControl<HasMetadata> reconcile(HasMetadata resource,
        Context<HasMetadata> context) {
      return null;
    }
  }

  @Workflow(dependents = {
      @Dependent(name = "DoubleEntry", type = DependentResource.class)
  })
  private static class TestDoubleEntryReconciler implements Reconciler<HasMetadata> {

    @Override
    public UpdateControl<HasMetadata> reconcile(HasMetadata resource,
        Context<HasMetadata> context) {
      return null;
    }
  }
}
