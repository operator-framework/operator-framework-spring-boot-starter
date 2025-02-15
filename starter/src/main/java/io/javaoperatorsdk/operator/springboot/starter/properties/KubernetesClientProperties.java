package io.javaoperatorsdk.operator.springboot.starter.properties;

import java.util.Objects;
import java.util.Optional;

public class KubernetesClientProperties {

  private boolean openshift = false;
  private String context;
  private String username;
  private String password;
  private String oauthToken;
  private String masterUrl;
  private boolean trustSelfSignedCertificates = false;


  public boolean isOpenshift() {
    return openshift;
  }

  public KubernetesClientProperties setOpenshift(boolean openshift) {
    this.openshift = openshift;
    return this;
  }

  public boolean isTrustSelfSignedCertificates() {
    return trustSelfSignedCertificates;
  }

  public KubernetesClientProperties setTrustSelfSignedCertificates(
      boolean trustSelfSignedCertificates) {
    this.trustSelfSignedCertificates = trustSelfSignedCertificates;
    return this;
  }

  public Optional<String> getContext() {
    return Optional.ofNullable(context);
  }

  public KubernetesClientProperties setContext(String context) {
    this.context = context;
    return this;
  }

  public Optional<String> getUsername() {
    return Optional.ofNullable(username);
  }

  public KubernetesClientProperties setUsername(String username) {
    this.username = username;
    return this;
  }

  public Optional<String> getPassword() {
    return Optional.ofNullable(password);
  }

  public KubernetesClientProperties setPassword(String password) {
    this.password = password;
    return this;
  }

  public Optional<String> getOauthToken() {
    return Optional.ofNullable(oauthToken);
  }

  public KubernetesClientProperties setOauthToken(String oauthToken) {
    this.oauthToken = oauthToken;
    return this;
  }

  public Optional<String> getMasterUrl() {
    return Optional.ofNullable(masterUrl);
  }

  public KubernetesClientProperties setMasterUrl(String masterUrl) {
    this.masterUrl = masterUrl;
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof KubernetesClientProperties that))
      return false;
    return isOpenshift() == that.isOpenshift()
        && isTrustSelfSignedCertificates() == that.isTrustSelfSignedCertificates()
        && Objects.equals(getContext(), that.getContext())
        && Objects.equals(getUsername(), that.getUsername())
        && Objects.equals(getPassword(), that.getPassword())
        && Objects.equals(getOauthToken(), that.getOauthToken())
        && Objects.equals(getMasterUrl(), that.getMasterUrl());
  }

  @Override
  public int hashCode() {
    return Objects.hash(isOpenshift(), getContext(), getUsername(), getPassword(), getOauthToken(),
        getMasterUrl(), isTrustSelfSignedCertificates());
  }
}
