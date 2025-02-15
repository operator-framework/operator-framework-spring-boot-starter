package io.javaoperatorsdk.operator.springboot.starter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.jenvtest.junit.EnableKubeAPIServer;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDTransformer;

import static org.assertj.core.api.Assertions.assertThat;

@EnableKubeAPIServer(updateKubeConfigFile = true)
@SpringBootTest(properties = {"javaoperatorsdk.crd.apply-on-startup = true"})
public class AutoConfigurationIntegrationTest {

  @Autowired
  private KubernetesClient kubernetesClient;

  @Test
  void crdsUploadedAndTransformersApplied() {
    var list = kubernetesClient.apiextensions().v1()
        .customResourceDefinitions()
        .list(new ListOptionsBuilder().withLabelSelector("Glory = Glory, Man = United").build())
        .getItems();

    var crds = assertThat(list).hasSize(2);
    crds.filteredOn(it -> "testresource".equals(it.getSpec().getNames().getSingular()))
        .hasSize(1)
        .filteredOn(it -> "sample.javaoperatorsdk".equals(it.getSpec().getGroup()))
        .hasSize(1);
    crds.filteredOn(it -> "josdksecondresources".equals(it.getSpec().getNames().getPlural()))
        .hasSize(1)
        .filteredOn(it -> "josdk.io".equals(it.getSpec().getGroup()))
        .hasSize(1);
  }

  @TestConfiguration
  static class TestConfig {

    private static HasMetadata addLabel(HasMetadata crd, String k, String v) {
      crd.getMetadata().getLabels().put(k, v);
      return crd;
    }

    @Bean
    public CRDTransformer transformerOne() {
      return crd -> addLabel(crd, "Glory", "Glory");
    }

    @Bean
    public CRDTransformer transformerTwo() {
      return crd -> addLabel(crd, "Man", "United");
    }

  }

}
