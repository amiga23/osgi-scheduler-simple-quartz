package es.scheller.scheduler.config.development;

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
import osgi.enroute.debug.api.Debug;

@Component( immediate = true, service = Object.class, property = { Debug.COMMAND_SCOPE + "=cron",
		Debug.COMMAND_FUNCTION + "=add" } )
public class CronConfigurator{
  @Reference
  ConfigurationAdmin cm;
  
  @Activate
  public void activate() {

  }
  
  public void add( String id, String cron ) {
	  List<ConfigurationAttribute> attributesJdbc = new ArrayList<ConfigurationAttribute>();
	    attributesJdbc.add( new ConfigurationAttribute( "id", id ) );
	    attributesJdbc.add( new ConfigurationAttribute( "cron", cron ) );
	    
	    ConfigurationData configJdbc = new ConfigurationData( "es.scheller.scheduler.examplejob.ExampleJob", attributesJdbc );
	    applyConfig( configJdbc );
  }
  
  public boolean applyConfig( ConfigurationData configurationData ) {
    try {
      org.osgi.service.cm.Configuration config = cm.createFactoryConfiguration( configurationData.getPid(), null );
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
//      log.error( "Can't set config.", e );
      return false;
    }
  }
}
