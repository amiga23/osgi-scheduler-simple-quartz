package es.scheller.scheduler.config.development;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( immediate = true, service = Object.class, property = {
  CommandProcessor.COMMAND_SCOPE
                                                                   + "=cron",
  CommandProcessor.COMMAND_FUNCTION + "=add"
} )
public class CronConfigurator {
  
  Logger log = LoggerFactory.getLogger( CronConfigurator.class );

  @Reference
  ConfigurationAdmin cm;

  @Activate
  public void activate() {
  }

  public void add( String id, String cron ) {
    try {
      org.osgi.service.cm.Configuration config = cm
        .createFactoryConfiguration( "es.scheller.scheduler.examplejob.ExampleJob", null );
      Hashtable< String, String > properties = new Hashtable< String, String >();
      properties.put( "id", id );
      properties.put( "cron", cron );
      config.update( properties );
      config.update();
    } catch( IOException e ) {
      log.error( "Can't set config.", e );
    }
  }
}
