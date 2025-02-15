package io.javaoperatorsdk.operator.springboot.starter.crd;

import static org.slf4j.LoggerFactory.getLogger;

@FunctionalInterface
public interface CRDApplier {

  CRDApplier NOOP = () -> getLogger(CRDApplier.class).debug("Not searching for CRDs to apply");

  void apply();
}
