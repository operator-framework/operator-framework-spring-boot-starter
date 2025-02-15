package io.javaoperatorsdk.operator.springboot.starter.dependentresourceconfigurator;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResourceConfig;
import io.javaoperatorsdk.operator.springboot.starter.model.TestResource;

public class DependentResourceConfigMap
    extends CRUDKubernetesDependentResource<ConfigMap, TestResource> {

  public static final String NAME = "configMapDR";

  public DependentResourceConfigMap() {
    super(ConfigMap.class, NAME);
  }

  @Override
  public void configureWith(KubernetesDependentResourceConfig<ConfigMap> config) {
    if (config instanceof ConfigMapDRConfigurator drc) {
      drc.getsCalled();
    }
  }
}
