package es.scheller.air.config.logging.development;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Consumer;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import es.scheller.air.config.api.ConfigurationAttribute;
import es.scheller.air.config.api.ConfigurationData;

@Component( service = LogConfigurator.class, immediate = true)
public class LogConfigurator {

  @Reference
  ConfigurationAdmin cm;

  public static final String PAX_LOGGING_PID = "org.ops4j.pax.logging";

  @Activate
  public void activate() {
    try {
      Hashtable< String, String > properties = new Hashtable< String, String >();

//  properties.put( "org.ops4j.pax.logging.DefaultServiceLog.level", "WARN" );
//  properties.put( "DefaultServiceLog.level", "WARN" );
      properties.put( "log4j.rootLogger", "WARN, A1" );
      properties.put( "log4j.appender.A1", "org.apache.log4j.ConsoleAppender" );
      properties.put( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" );
      properties.put( "log4j.appender.A1.layout.ConversionPattern", "%d{ISO8601} | %-5p | %-32.32c{1} | %m%n" );
      properties.put( "log4j.logger.es.scheller", "DEBUG, osgi:*" );
//    properties.put( "log4j.putitivity.es.scheller","false" );
      org.osgi.service.cm.Configuration config = cm.getConfiguration( PAX_LOGGING_PID, null );
      config.update( properties );
      config.update();
    } catch( IOException e ) {
      System.err.println( "Can't set logger configuration." );
    }
  }
}
