package io.javaoperatorsdk.operator.springboot.starter.dependentresourceconfigurator;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResourceConfig;
import io.javaoperatorsdk.operator.springboot.starter.DependentResourceConfigurator;

public class ConfigMapDRConfigurator extends KubernetesDependentResourceConfig<ConfigMap>
    implements DependentResourceConfigurator {

  public ConfigMapDRConfigurator() {
    super(null, true, InformerConfiguration.builder(ConfigMap.class).build());
  }

  @Override
  public String getName() {
    return DependentResourceConfigMap.NAME;
  }

  public void getsCalled() {}
}
