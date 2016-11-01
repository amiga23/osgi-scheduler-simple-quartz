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
  
  @Activate
  public void activate() {
    List<ConfigurationAttribute> attributesLogging = new ArrayList<ConfigurationAttribute>();
//  attributesLogging.add( new ConfigurationAttribute( "org.ops4j.pax.logging.DefaultServiceLog.level", "WARN" ) );
//  attributesLogging.add( new ConfigurationAttribute( "DefaultServiceLog.level", "WARN" ) );
    attributesLogging.add( new ConfigurationAttribute( "log4j.rootLogger", "WARN, A1" ) );
    attributesLogging.add( new ConfigurationAttribute( "log4j.appender.A1", "org.apache.log4j.ConsoleAppender" ) );
    attributesLogging.add( new ConfigurationAttribute( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" ) );
    attributesLogging.add( new ConfigurationAttribute( "log4j.appender.A1.layout.ConversionPattern", "%d{ISO8601} | %-5p | %-32.32c{1} | %m%n" ) );
    attributesLogging.add( new ConfigurationAttribute( "log4j.logger.es.scheller", "DEBUG, osgi:*" ) );  
//    attributesLogging.add( new ConfigurationAttribute( "log4j.additivity.es.scheller","false" ) );
    ConfigurationData configLogging = new ConfigurationData( "org.ops4j.pax.logging", attributesLogging );
    applyConfig( configLogging );
  }
  
  public boolean applyConfig( ConfigurationData configurationData ) {
    try {
      org.osgi.service.cm.Configuration config = cm.getConfiguration( configurationData.getPid(), null );
      Hashtable< String, String > properties = new Hashtable< String, String >();
      configurationData.getAttributes().forEach( new Consumer< ConfigurationAttribute >() {

        @Override
        public void accept( ConfigurationAttribute t ) {
          properties.put( t.getName(), t.getValue() );
        }
      } );
      config.update( properties );
      config.update();
      return true;
    } catch( IOException e ) {
      System.err.println( "Can't set logger configuration." );
      return false;
    }
  }
}
