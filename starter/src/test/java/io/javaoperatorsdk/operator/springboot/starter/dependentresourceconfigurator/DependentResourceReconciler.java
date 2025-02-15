package io.javaoperatorsdk.operator.springboot.starter.dependentresourceconfigurator;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.springboot.starter.model.TestResource;

@Workflow(dependents = @Dependent(type = DependentResourceConfigMap.class,
    name = DependentResourceConfigMap.NAME))
@ControllerConfiguration
public class DependentResourceReconciler implements Reconciler<TestResource> {

  @Override
  public UpdateControl<TestResource> reconcile(TestResource resource,
      Context<TestResource> context) {
    return UpdateControl.noUpdate();
  }
}
