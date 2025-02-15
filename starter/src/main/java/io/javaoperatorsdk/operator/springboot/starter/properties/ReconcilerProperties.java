package io.javaoperatorsdk.operator.springboot.starter.properties;


import java.time.Duration;
import java.util.Objects;
import java.util.Set;


public class ReconcilerProperties {
  private String name;
  private String finalizerName;
  private Boolean generationAware;
  private Boolean clusterScoped;
  private Set<String> namespaces;
  private RetryProperties retry;
  private String labelSelector;
  private Duration reconciliationMaxInterval;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFinalizerName() {
    return finalizerName;
  }

  public void setFinalizerName(String finalizerName) {
    this.finalizerName = finalizerName;
  }

  public Boolean getGenerationAware() {
    return generationAware;
  }

  public void setGenerationAware(Boolean generationAware) {
    this.generationAware = generationAware;
  }

  public Boolean getClusterScoped() {
    return clusterScoped;
  }

  public void setClusterScoped(Boolean clusterScoped) {
    this.clusterScoped = clusterScoped;
  }

  public Set<String> getNamespaces() {
    return namespaces;
  }

  public void setNamespaces(Set<String> namespaces) {
    this.namespaces = namespaces;
  }

  public RetryProperties getRetry() {
    return retry;
  }

  public void setRetry(RetryProperties retry) {
    this.retry = retry;
  }

  public String getLabelSelector() {
    return labelSelector;
  }

  public void setLabelSelector(String labelSelector) {
    this.labelSelector = labelSelector;
  }

  public Duration getReconciliationMaxInterval() {
    return reconciliationMaxInterval;
  }

  public void setReconciliationMaxInterval(Duration reconciliationMaxInterval) {
    this.reconciliationMaxInterval = reconciliationMaxInterval;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ReconcilerProperties that))
      return false;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getFinalizerName(), that.getFinalizerName())
        && Objects.equals(getGenerationAware(), that.getGenerationAware())
        && Objects.equals(getClusterScoped(), that.getClusterScoped())
        && Objects.equals(getNamespaces(), that.getNamespaces())
        && Objects.equals(getRetry(), that.getRetry())
        && Objects.equals(getLabelSelector(), that.getLabelSelector())
        && Objects.equals(getReconciliationMaxInterval(), that.getReconciliationMaxInterval());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getFinalizerName(), getGenerationAware(), getClusterScoped(),
        getNamespaces(), getRetry(), getLabelSelector(), getReconciliationMaxInterval());
  }
}
