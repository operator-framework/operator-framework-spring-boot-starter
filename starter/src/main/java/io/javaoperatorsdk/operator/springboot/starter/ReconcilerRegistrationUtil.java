package io.javaoperatorsdk.operator.springboot.starter;

import java.util.*;
import java.util.function.Consumer;

import io.javaoperatorsdk.operator.api.config.ControllerConfigurationOverrider;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.retry.GenericRetry;
import io.javaoperatorsdk.operator.springboot.starter.properties.ReconcilerProperties;


public class ReconcilerRegistrationUtil {

  public static List<DependentResourceConfigurator> filterConfigurators(Reconciler<?> reconciler,
      Map<String, List<DependentResourceConfigurator>> configuratorsMap) {
    var workflow = reconciler.getClass().getAnnotation(Workflow.class);
    if (workflow == null) {
      return Collections.emptyList();
    }

    var dependents = workflow.dependents();
    ArrayList<DependentResourceConfigurator> relevant = new ArrayList<>();
    for (Dependent dependent : dependents) {
      var name = dependent.name();
      var configurators = configuratorsMap.get(name);
      if (configurators == null || configurators.isEmpty()) {
        continue;
      }
      if (configurators.size() > 1) {
        throw new IllegalStateException("more than one config for Dependent Resource " + name
            + " - " + configurators.stream().map(o -> o.getClass().getName()).toList());
      }
      relevant.add(configurators.get(0));
    }
    return relevant;
  }

  public static void overrideFromProps(ControllerConfigurationOverrider<?> overrider,
      ReconcilerProperties props) {
    if (props != null) {
      doIfPresent(props.getFinalizerName(), overrider::withFinalizer);
      doIfPresent(props.getName(), overrider::withName);
      doIfPresent(props.getNamespaces(), overrider::settingNamespaces);
      doIfPresent(props.getRetry(), r -> {
        var retry = new GenericRetry();
        doIfPresent(r.getInitialInterval(), retry::setInitialInterval);
        doIfPresent(r.getMaxAttempts(), retry::setMaxAttempts);
        doIfPresent(r.getMaxInterval(), retry::setMaxInterval);
        doIfPresent(r.getIntervalMultiplier(), retry::setIntervalMultiplier);
        overrider.withRetry(retry);
      });
      doIfPresent(props.getGenerationAware(), overrider::withGenerationAware);
      doIfPresent(props.getClusterScoped(), clusterScoped -> {
        if (clusterScoped) {
          overrider.watchingAllNamespaces();
        }
      });
      doIfPresent(props.getLabelSelector(), overrider::withLabelSelector);
      doIfPresent(props.getReconciliationMaxInterval(), overrider::withReconciliationMaxInterval);
    }
  }

  public static <T> void doIfPresent(T prop, Consumer<T> action) {
    Optional.ofNullable(prop).ifPresent(action);
  }
}
