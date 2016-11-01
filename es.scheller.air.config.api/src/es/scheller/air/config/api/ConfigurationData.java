package es.scheller.air.config.api;

import java.util.List;


public class ConfigurationData {

  private String pid;
  private List< ConfigurationAttribute > attributes;

  public ConfigurationData( String pid, List< ConfigurationAttribute > attributes ) {
    this.pid = pid;
    this.attributes = attributes;
  }

  public String getPid() {
    return pid;
  }

  public List< ConfigurationAttribute > getAttributes() {
    return attributes;
  }
}
