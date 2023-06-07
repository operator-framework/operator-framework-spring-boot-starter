package io.javaoperatorsdk.operator.springboot.starter;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.javaoperatorsdk.operator.api.config.ConfigurationService;

@ConfigurationProperties(prefix = "javaoperatorsdk")
public class OperatorConfigurationProperties {

  private KubernetesClientProperties client = new KubernetesClientProperties();
  private Map<String, ReconcilerProperties> reconcilers = Collections.emptyMap();
  private boolean checkCrdAndValidateLocalModel = true;
  private int concurrentReconciliationThreads =
      ConfigurationService.DEFAULT_RECONCILIATION_THREADS_NUMBER;
  private Integer minConcurrentReconciliationThreads;
  private Integer concurrentWorkflowExecutorThreads;
  private Integer minConcurrentWorkflowExecutorThreads;
  private Boolean closeClientOnStop;
  private Boolean stopOnInformerErrorDuringStartup;
  private Duration cacheSyncTimeout;

  public KubernetesClientProperties getClient() {
    return client;
  }

  public void setClient(KubernetesClientProperties client) {
    this.client = client;
  }

  public Map<String, ReconcilerProperties> getReconcilers() {
    return reconcilers;
  }

  public void setReconcilers(Map<String, ReconcilerProperties> reconcilers) {
    this.reconcilers = reconcilers;
  }

  public boolean getCheckCrdAndValidateLocalModel() {
    return checkCrdAndValidateLocalModel;
  }

  public void setCheckCrdAndValidateLocalModel(boolean checkCrdAndValidateLocalModel) {
    this.checkCrdAndValidateLocalModel = checkCrdAndValidateLocalModel;
  }

  public int getConcurrentReconciliationThreads() {
    return concurrentReconciliationThreads;
  }

  public void setConcurrentReconciliationThreads(int concurrentReconciliationThreads) {
    this.concurrentReconciliationThreads = concurrentReconciliationThreads;
  }

  public Integer getMinConcurrentReconciliationThreads() {
    return minConcurrentReconciliationThreads;
  }

  public void setMinConcurrentReconciliationThreads(Integer minConcurrentReconciliationThreads) {
    this.minConcurrentReconciliationThreads = minConcurrentReconciliationThreads;
  }

  public Integer getConcurrentWorkflowExecutorThreads() {
    return concurrentWorkflowExecutorThreads;
  }

  public void setConcurrentWorkflowExecutorThreads(Integer concurrentWorkflowExecutorThreads) {
    this.concurrentWorkflowExecutorThreads = concurrentWorkflowExecutorThreads;
  }

  public Integer getMinConcurrentWorkflowExecutorThreads() {
    return minConcurrentWorkflowExecutorThreads;
  }

  public void setMinConcurrentWorkflowExecutorThreads(
      Integer minConcurrentWorkflowExecutorThreads) {
    this.minConcurrentWorkflowExecutorThreads = minConcurrentWorkflowExecutorThreads;
  }

  public Boolean getCloseClientOnStop() {
    return closeClientOnStop;
  }

  public void setCloseClientOnStop(Boolean closeClientOnStop) {
    this.closeClientOnStop = closeClientOnStop;
  }

  public Boolean getStopOnInformerErrorDuringStartup() {
    return stopOnInformerErrorDuringStartup;
  }

  public void setStopOnInformerErrorDuringStartup(Boolean stopOnInformerErrorDuringStartup) {
    this.stopOnInformerErrorDuringStartup = stopOnInformerErrorDuringStartup;
  }

  public Duration getCacheSyncTimeout() {
    return cacheSyncTimeout;
  }

  public void setCacheSyncTimeout(Duration cacheSyncTimeout) {
    this.cacheSyncTimeout = cacheSyncTimeout;
  }
}
