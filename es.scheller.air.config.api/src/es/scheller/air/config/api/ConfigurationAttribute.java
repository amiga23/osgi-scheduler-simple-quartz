package es.scheller.air.config.api;

public class ConfigurationAttribute {

  private String name;
  private String value;

  public ConfigurationAttribute( String name, String value ) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
