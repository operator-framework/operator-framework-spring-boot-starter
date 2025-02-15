package io.javaoperatorsdk.operator.springboot.starter.test;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.config.ConfigurationService;
import io.javaoperatorsdk.operator.api.config.ConfigurationServiceOverrider;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.springboot.starter.properties.OperatorConfigurationProperties;
import io.javaoperatorsdk.operator.springboot.starter.properties.ReconcilerProperties;
import io.javaoperatorsdk.operator.springboot.starter.sample.CustomService;
import io.javaoperatorsdk.operator.springboot.starter.sample.CustomServiceReconciler;
import io.javaoperatorsdk.operator.springboot.starter.sample.ServiceSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@EnableMockOperator(crdPaths = "classpath:crd.yml")
class EnableMockOperatorTests {

  @Autowired
  KubernetesClient client;

  @Autowired
  ApplicationContext applicationContext;

  @Test
  void testCrdLoaded() {
    assertThat(applicationContext.getBean(Operator.class)).isNotNull();
    assertThat(
        client
            .apiextensions()
            .v1()
            .customResourceDefinitions()
            .withName("customservices.sample.javaoperatorsdk")
            .get())
        .isNotNull();
    assertThat(
        client
            .apiextensions()
            .v1()
            .customResourceDefinitions()
            .withName("customservices.global.sample.javaoperatorsdk")
            .get())
        .isNotNull();
  }

  @Autowired
  private OperatorConfigurationProperties operatorConfigurationProperties;

  @Test
  void customServiceReconciler() {
    ReconcilerProperties properties =
        operatorConfigurationProperties.getReconcilers().get("customservicereconciler");
    assertNotNull(properties);
    String testNS = "test-ns";
    assertTrue(properties.getNamespaces().contains(testNS));
    // create a namespace for testing
    client.namespaces().resource(namespace(testNS)).create();
    assertNotNull(client.namespaces().withName(testNS).get());
    // create a CR
    String name = "test-name";
    client.resources(CustomService.class).inNamespace(testNS)
        .resource(customService(name, "test-label"))
        .create();
    assertNotNull(client.resources(CustomService.class).inNamespace(testNS)
        .withName("test-name").get());
    // test if a service was created by the CustomServiceReconciler
    await()
        .atMost(15, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          assertNotNull(
              client.resources(Service.class).inNamespace(testNS).withName("test-name").get());
          assertThat(client.configMaps().inNamespace(testNS).withName(name + "-config").get())
              .isNotNull().satisfies(configMap -> {
                assertThat(configMap.getData()).isNotNull();
                assertThat(configMap.getData()).containsEntry("foo", "bar");
              });
        });
  }

  private Namespace namespace(String ns) {
    return new NamespaceBuilder()
        .withMetadata(
            new ObjectMetaBuilder().withName(ns).build())
        .build();
  }

  private CustomService customService(String name, String label) {
    CustomService resource = new CustomService();
    resource.setMetadata(
        new ObjectMetaBuilder()
            .withName(name)
            .withResourceVersion("v1")
            .build());
    resource.setSpec(new ServiceSpec());
    resource.getSpec().setName(name);
    resource.getSpec().setLabel(label);
    return resource;
  }

  @SpringBootApplication
  @ComponentScan(
      includeFilters = {
          @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerConfiguration.class)
      },
      basePackages = {
          "io.javaoperatorsdk"
      })
  static class SpringBootTestApplication {
    // Need to override the CustomServiceReconciler Bean to set KubernetesClient to the mocked one.
    @Bean
    @Primary
    CustomServiceReconciler customServiceReconciler(KubernetesClient kubernetesClient) {
      return new CustomServiceReconciler(kubernetesClient);
    }

    /**
     * Since operator-framework 4.4 the default for SSA (Server-Side-Apply) is set to true, which
     * causes a problem when working with dependent resources. To disable SSA, we have to provide a
     * custom ConfigurationServiceOverrider.
     *
     * @see ConfigurationService#ssaBasedCreateUpdateMatchForDependentResources()
     */
    @Bean
    public Consumer<ConfigurationServiceOverrider> additionalConfigServiceOverrider() {
      return overrider -> overrider.withSSABasedCreateUpdateMatchForDependentResources(false);
    }
  }
}
