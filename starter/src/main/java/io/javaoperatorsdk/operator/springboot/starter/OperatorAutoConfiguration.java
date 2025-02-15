package io.javaoperatorsdk.operator.springboot.starter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.http.HttpClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.config.Cloner;
import io.javaoperatorsdk.operator.api.config.ConfigurationServiceOverrider;
import io.javaoperatorsdk.operator.api.config.DefaultResourceClassResolver;
import io.javaoperatorsdk.operator.api.config.ResourceClassResolver;
import io.javaoperatorsdk.operator.api.monitoring.Metrics;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.springboot.starter.crd.CRDApplier;
import io.javaoperatorsdk.operator.springboot.starter.properties.OperatorConfigurationProperties;

@Configuration
@EnableConfigurationProperties(OperatorConfigurationProperties.class)
public class OperatorAutoConfiguration {

  private final static Logger log = LoggerFactory.getLogger(OperatorAutoConfiguration.class);

  @Autowired
  private OperatorConfigurationProperties configurationProperties;

  @Bean
  @ConditionalOnMissingBean
  public KubernetesClient kubernetesClient(
      @Autowired(required = false) HttpClient.Factory httpClientFactory, Config config) {
    KubernetesClientBuilder clientBuilder = new KubernetesClientBuilder().withConfig(config);

    if (httpClientFactory != null) {
      clientBuilder = clientBuilder.withHttpClientFactory(httpClientFactory);
    }
    KubernetesClient client = clientBuilder.build();

    if (configurationProperties.getClient().isOpenshift()) {
      return client.adapt(OpenShiftClient.class);
    }
    return client;
  }

  @Bean
  @ConditionalOnMissingBean(ResourceClassResolver.class)
  public ResourceClassResolver resourceClassResolver() {
    return new DefaultResourceClassResolver();
  }

  @Bean
  @ConditionalOnMissingBean(CRDApplier.class)
  public CRDApplier disabledCrdApplier() {
    if (log.isDebugEnabled()) {
      log.debug("no CRDApplier loaded, using NOOP");
    }
    return CRDApplier.NOOP;
  }

  @Bean(destroyMethod = "stop")
  @ConditionalOnMissingBean(Operator.class)
  public Operator operator(List<Consumer<ConfigurationServiceOverrider>> configServiceOverriders,
      List<Reconciler<?>> reconcilers,
      List<DependentResourceConfigurator> dependentResourceConfigurators) {
    var chainedOverriders = configServiceOverriders.stream()
        .reduce(Consumer::andThen)
        .orElseThrow(
            () -> new IllegalStateException("Default Config Service Overrider Not Created"));

    var dependentResourceConfiguratorsMap = dependentResourceConfigurators == null ? null
        : dependentResourceConfigurators.stream()
            .collect(Collectors.groupingBy(DependentResourceConfigurator::getName));

    var operator = new Operator(chainedOverriders);
    reconcilers.forEach(reconciler -> {
      var name = ReconcilerUtils.getNameFor(reconciler);
      var props = configurationProperties.getReconcilers().get(name);
      operator.register(reconciler, o -> {
        ReconcilerRegistrationUtil.overrideFromProps(o, props);
        if (dependentResourceConfiguratorsMap != null) {
          var drsForReconciler = ReconcilerRegistrationUtil.filterConfigurators(reconciler,
              dependentResourceConfiguratorsMap);
          drsForReconciler.forEach(dr -> o.replacingNamedDependentResourceConfig(dr.getName(), dr));
        }
      });
    });
    return operator;
  }

  @Bean
  @Order(0)
  public Consumer<ConfigurationServiceOverrider> defaultConfigServiceOverrider(
      @Autowired(required = false) Cloner cloner, Metrics metrics,
      KubernetesClient kubernetesClient) {
    return overrider -> {
      ReconcilerRegistrationUtil.doIfPresent(cloner, overrider::withResourceCloner);
      ReconcilerRegistrationUtil.doIfPresent(
          configurationProperties.getStopOnInformerErrorDuringStartup(),
          overrider::withStopOnInformerErrorDuringStartup);
      ReconcilerRegistrationUtil.doIfPresent(
          configurationProperties.getConcurrentWorkflowExecutorThreads(),
          overrider::withConcurrentWorkflowExecutorThreads);
      ReconcilerRegistrationUtil.doIfPresent(configurationProperties.getCloseClientOnStop(),
          overrider::withCloseClientOnStop);
      ReconcilerRegistrationUtil.doIfPresent(configurationProperties.getCacheSyncTimeout(),
          overrider::withCacheSyncTimeout);

      overrider
          .withConcurrentReconciliationThreads(
              configurationProperties.getConcurrentReconciliationThreads())
          .withMetrics(metrics)
          .checkingCRDAndValidateLocalModel(
              configurationProperties.isCheckCrdAndValidateLocalModel())
          .withKubernetesClient(kubernetesClient);
    };
  }

  @Bean
  @ConditionalOnMissingBean(name = "reconciliationExecutorService")
  public ExecutorService reconciliationExecutorService() {
    return Executors
        .newFixedThreadPool(configurationProperties.getConcurrentReconciliationThreads());
  }

  @Bean
  @ConditionalOnMissingBean(Metrics.class)
  public Metrics metrics() {
    return Metrics.NOOP;
  }

  @Bean
  public Config getClientConfiguration(
      @Autowired(required = false) KubernetesConfigCustomizer configCustomizer) {
    return configurationProperties.getClient().getContext()
        .map(Config::autoConfigure)
        .map(it -> {
          if (configCustomizer != null) {
            final var builder = new ConfigBuilder(it);
            configCustomizer.customize(builder);
            return builder.build();
          } else {
            return it;
          }
        })
        .orElseGet(() -> {
          final var clientCfg = configurationProperties.getClient();
          ConfigBuilder config = new ConfigBuilder();
          config.withTrustCerts(clientCfg.isTrustSelfSignedCertificates());
          clientCfg.getMasterUrl().ifPresent(config::withMasterUrl);
          clientCfg.getUsername().ifPresent(config::withUsername);
          clientCfg.getPassword().ifPresent(config::withPassword);

          if (configCustomizer != null) {
            configCustomizer.customize(config);
          }

          return config.build();
        });
  }

}
