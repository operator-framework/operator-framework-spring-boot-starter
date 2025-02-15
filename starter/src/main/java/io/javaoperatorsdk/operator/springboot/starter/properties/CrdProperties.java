package io.javaoperatorsdk.operator.springboot.starter.properties;

public class CrdProperties {

  private boolean applyOnStartup;
  /**
   * path to the resource folder where CRDs are located
   */
  private String path = "/META-INF/fabric8/";
  /**
   * file suffix to filter out CRDs
   */
  private String suffix = "-v1.yml";

  public boolean isApplyOnStartup() {
    return applyOnStartup;
  }

  public void setApplyOnStartup(boolean applyOnStartup) {
    this.applyOnStartup = applyOnStartup;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }
}
