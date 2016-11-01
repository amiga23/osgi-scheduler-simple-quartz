package es.scheller.scheduler.examplejob;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.enroute.scheduler.api.CronJob;

@ObjectClassDefinition( name = "Scheduler Test" )
@interface CronConfiguration {

  @AttributeDefinition( description = "The cron.", type = AttributeType.STRING, name = "cron" )
  String cron() default "23 * * * * ? *";

  @AttributeDefinition( description = "ID.", type = AttributeType.STRING, name = "id" )
  String id() default "1";
  
  @AttributeDefinition( description = "Description.", type = AttributeType.STRING, name = "description" )
  String description() default "An Example Job for testing.";
}
@Designate( ocd = CronConfiguration.class, factory = true )

@Component( immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ExampleJob implements CronJob< ExampleJob > {
  
  private Logger log = LoggerFactory.getLogger( ExampleJob.class );

  private CronConfiguration config;

  @Activate
  public void activate( CronConfiguration config ) {
    log.info( this + " Here I am " + config.id() );
    this.config = config;
  }

  @Deactivate
  public void deactivate() {
    log.info( this + " Goodbye I have been " + config.id() );
  }
  
  @Override
  public void run( ExampleJob data ) throws Exception {
    log.info( this + " JUHU, but I am just an example job, so I am not really doing something!" );
  }
}
