package io.javaoperatorsdk.operator.springboot.starter.crd;

import java.util.List;
import java.util.function.UnaryOperator;

import io.fabric8.kubernetes.api.model.HasMetadata;

public interface CRDTransformer extends UnaryOperator<HasMetadata> {
  static CRDTransformer reduce(List<CRDTransformer> transformers) {
    return transformers.stream().reduce(t -> t, CRDTransformer::thenTransform);
  }

  default CRDTransformer thenTransform(CRDTransformer after) {
    return t -> after.apply(apply(t));
  }
}
