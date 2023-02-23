package io.javaoperatorsdk.operator.springboot.starter;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.http.HttpClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.config.BaseConfigurationService;
import io.javaoperatorsdk.operator.api.config.Cloner;
import io.javaoperatorsdk.operator.api.config.ControllerConfigurationOverrider;
import io.javaoperatorsdk.operator.api.config.Utils;
import io.javaoperatorsdk.operator.api.monitoring.Metrics;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.processing.retry.GenericRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Configuration
@EnableConfigurationProperties(OperatorConfigurationProperties.class)
public class OperatorAutoConfiguration extends BaseConfigurationService {

  private final static Logger log = LoggerFactory.getLogger(OperatorAutoConfiguration.class);

  @Autowired
  private OperatorConfigurationProperties configuration;

  @Autowired(required = false)
  private Cloner cloner;

  @Autowired(required = false)
  private KubernetesConfigCustomizer configCustomizer;

  public OperatorAutoConfiguration() {
    super(Utils.loadFromProperties());
  }

  @Bean
  @ConditionalOnMissingBean
  public KubernetesClient kubernetesClient(Optional<HttpClient.Factory> httpClientFactory) {
    final var config = getClientConfiguration();
    return configuration.getClient().isOpenshift()
        ? httpClientFactory
            .map(it -> new KubernetesClientBuilder().withHttpClientFactory(it).withConfig(config)
                .build().adapt(OpenShiftClient.class))
            // new DefaultOpenShiftClient(it.createHttpClient(config),
            // new OpenShiftConfig(config)))
            .orElseGet(() -> new KubernetesClientBuilder().withConfig(config)
                .build().adapt(OpenShiftClient.class))
        : httpClientFactory
            .map(it -> new KubernetesClientBuilder().withHttpClientFactory(it).withConfig(config)
                .build())
            .orElseGet(() -> new KubernetesClientBuilder().withConfig(config)
                .build());
  }

  @Override
  public Config getClientConfiguration() {
    return configuration.getClient().getContext()
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
          final var clientCfg = configuration.getClient();
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

  @Override
  public boolean checkCRDAndValidateLocalModel() {
    return configuration.getCheckCrdAndValidateLocalModel();
  }

  @Bean
  @ConditionalOnMissingBean(ResourceClassResolver.class)
  public ResourceClassResolver resourceClassResolver() {
    return new NaiveResourceClassResolver();
  }

  @Bean(destroyMethod = "stop")
  @ConditionalOnMissingBean(Operator.class)
  public Operator operator(
      KubernetesClient kubernetesClient,
      List<Reconciler<?>> reconcilers,
      ResourceClassResolver resourceClassResolver) {
    Operator operator = new Operator(kubernetesClient, this);

    reconcilers.forEach(r -> operator.register(r,
        o -> setControllerOverrides(o, configuration, resourceClassResolver, r)));

    if (!reconcilers.isEmpty()) {
      operator.start();
    } else {
      log.warn("No Reconcilers found in the application context: Not starting the Operator");
    }

    return operator;
  }

  @SuppressWarnings("rawtypes")
  private void setControllerOverrides(ControllerConfigurationOverrider<?> o,
      OperatorConfigurationProperties configuration,
      ResourceClassResolver resourceClassResolver, Reconciler<?> reconciler) {
    final var reconcilerPropertiesMap = configuration.getReconcilers();
    final var name = ReconcilerUtils.getNameFor(reconciler);
    var props = reconcilerPropertiesMap.get(name);

    if (props != null) {
      Optional.ofNullable(props.getFinalizerName()).ifPresent(o::withFinalizer);
      Optional.ofNullable(props.getName()).ifPresent(o::withName);
      Optional.ofNullable(props.getNamespaces()).ifPresent(o::settingNamespaces);
      Optional.ofNullable(props.getRetry()).ifPresent(r -> {
        var retry = new GenericRetry();
        if (r.getInitialInterval() != null) {
          retry.setInitialInterval(r.getInitialInterval());
        }
        if (r.getMaxAttempts() != null) {
          retry.setMaxAttempts(r.getMaxAttempts());
        }
        if (r.getMaxInterval() != null) {
          retry.setMaxInterval(r.getMaxInterval());
        }
        if (r.getIntervalMultiplier() != null) {
          retry.setIntervalMultiplier(r.getIntervalMultiplier());
        }
        o.withRetry(retry);
      });
      Optional.ofNullable(props.isGenerationAware()).ifPresent(o::withGenerationAware);
      Optional.ofNullable(props.isClusterScoped()).ifPresent(clusterScoped -> {
        if (clusterScoped) {
          o.watchingAllNamespaces();
        }
      });
      Class resourceClass = resourceClassResolver.resolveCustomResourceClass(reconciler);
      o.withResourceClass(resourceClass);
    }
  }

  @Bean
  @ConditionalOnMissingBean(name = "reconciliationExecutorService")
  public ExecutorService reconciliationExecutorService() {
    return super.getExecutorService();
  }

  @Bean
  @ConditionalOnMissingBean(Metrics.class)
  public Metrics metrics() {
    return super.getMetrics();
  }

  @Override
  public Metrics getMetrics() {
    return metrics();
  }

  @Override
  public ExecutorService getExecutorService() {
    return reconciliationExecutorService();
  }

  @Override
  public int concurrentReconciliationThreads() {
    return configuration.getConcurrentReconciliationThreads();
  }

  @Override
  public Cloner getResourceCloner() {
    return cloner != null ? cloner : super.getResourceCloner();
  }
}
