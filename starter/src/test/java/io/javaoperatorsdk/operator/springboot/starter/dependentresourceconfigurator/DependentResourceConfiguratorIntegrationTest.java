package io.javaoperatorsdk.operator.springboot.starter.dependentresourceconfigurator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.javaoperatorsdk.jenvtest.junit.EnableKubeAPIServer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDTransformer;
import io.javaoperatorsdk.operator.springboot.starter.model.TestResource;

import static org.mockito.Mockito.times;

@EnableKubeAPIServer(updateKubeConfigFile = true)
@SpringBootTest(properties = {
    "javaoperatorsdk.crd.apply-on-startup = true",
    "javaoperatorsdk.crd.path = META-INF/fabric8/deeper"
})
@SpringJUnitConfig
public class DependentResourceConfiguratorIntegrationTest {

  @MockitoSpyBean
  ConfigMapDRConfigurator dependentResourceConfigurator;

  @Test
  void testLoadingCustomDependentResourceConfigurator() {
    Mockito.verify(dependentResourceConfigurator, times(1)).getsCalled();
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public CRDTransformer transformer() {
      return crd -> {
        crd.getMetadata().getLabels().put("DRtest", "CRD");
        return crd;
      };
    }

    @Bean
    public ConfigMapDRConfigurator drConfigurator() {
      return new ConfigMapDRConfigurator();
    }

    @Bean
    public Reconciler<TestResource> drReconciler() {
      return new DependentResourceReconciler();
    }
  }
}
