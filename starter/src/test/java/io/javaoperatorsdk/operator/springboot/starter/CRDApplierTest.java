package io.javaoperatorsdk.operator.springboot.starter;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDApplier;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDTransformer;
import io.javaoperatorsdk.operator.springboot.starter.crd.DefaultCRDApplier;
import io.javaoperatorsdk.operator.springboot.starter.model.TestResource;
import io.javaoperatorsdk.operator.springboot.starter.properties.CrdProperties;
import io.javaoperatorsdk.operator.springboot.starter.properties.OperatorConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CRDApplierTest {

  private final List<CRDTransformer> crdTransformers = List.of(t -> {
    t.getMetadata().setLabels(Map.of("transformed", "true"));
    return t;
  });
  @Mock
  private KubernetesClient kubernetesClient;
  @Mock
  private NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> loadedResource;
  private String crdSuffix = "-v2.yml";
  private final String crdPath = "/META-INF/fabric8/";

  private CRDApplier applier() {
    var props = new OperatorConfigurationProperties();
    var crd = new CrdProperties();
    crd.setPath(crdPath);
    crd.setSuffix(crdSuffix);
    props.setCrd(crd);
    return new DefaultCRDApplier(kubernetesClient, props, crdTransformers);
  }

  @Test
  void shouldUploadAndApplyTransformer() {
    var testResource = new TestResource();
    var crds = List.<HasMetadata>of(testResource);
    when(loadedResource.items()).thenReturn(crds);
    when(kubernetesClient.load(any())).thenReturn(loadedResource);
    when(kubernetesClient.resourceList(crds)).thenReturn(loadedResource);

    applier().apply();

    assertThat(testResource.getMetadata().getLabels().get("transformed"))
        .isEqualTo("true");
  }

  @Test
  void shouldNotUploadWhenNoCrdsFound() {
    crdSuffix = "not-found-suffix";

    applier().apply();

    verifyNoInteractions(kubernetesClient);
  }
}
