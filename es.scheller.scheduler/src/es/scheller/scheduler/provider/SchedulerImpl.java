package es.scheller.scheduler.provider;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.quartz.CronExpression;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.scheller.scheduler.api.Scheduler;
import osgi.enroute.scheduler.api.CronJob;

@ObjectClassDefinition( name = "Scheller :: Scheduler" )
@interface Configuration {

  @AttributeDefinition( description="Which host has to run the scheduler?", type = AttributeType.STRING, name = "Active host" )
  String activeHost() default "disabled";
}
@Designate( ocd = Configuration.class )

@Component( immediate = true, name = "es.scheller.scheduler" )
public class SchedulerImpl implements Scheduler {
  
  private Logger log = LoggerFactory.getLogger( SchedulerImpl.class );
  
  private static final String SIMPLE_JOB_GROUP = "simple";
  private static final String SIMPLE_TRIGGER_GROUP = "simple";
  
  private Map< String, CronJob< ? > > cronjobs = new ConcurrentHashMap<>();
  private org.quartz.Scheduler sched;
  
  private final DateFormat dateFormat;
  
  /**
   * We have to use a Constructor, because the addSchedule methods will be called,
   * before the activate method, if there are already CronJobs!
   * @throws SchedulerException
   */
  public SchedulerImpl() throws SchedulerException {
    dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss zzz");
    dateFormat.setTimeZone( new SimpleTimeZone(0, "UTC") );
    SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
    sched = schedFact.getScheduler();
  }
  
  @Activate
  public void activate( Configuration config ) {
    try {
      log.debug( "Activating Simple Quartz Scheduer." );
      String meMyselfAndI = InetAddress.getLocalHost().getHostName();
      if ( meMyselfAndI.equals( config.activeHost() ) ) {
        log.info( "I am " + meMyselfAndI + ": Starting the scheduler" );
        sched.start();
      } else {
        log.info( "I am " + meMyselfAndI + " but configured is " + config.activeHost() + " -> So I will not run the cronjob!" );
      }
    } catch( Throwable t ) {
      log.error( "Error activating iimple Quartz Scheduler.", t );
    }
  }

  @Reference( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
  < T > void addSchedule( CronJob< T > cronJob, Map< String, Object > map ) {
    try {
      log.debug( "A new cronjob appeared" );
      synchronized( cronjobs ) {
        String id = ( String )map.get( "id" );
        if( id == null || id.trim().isEmpty() ) {
          id = cronJob.toString();
        }
        Object cronobject = map.get( "cron" );
        if ( cronobject == null ) {
          log.error( "Cronjob does not have a cron attribute id=" + id );
          return;
        }
        String cron = ( String )cronobject;
        String description = null;
        Object descriptionObject = map.get( "description" );
        if ( descriptionObject != null ) {
          description = ((String) descriptionObject).trim();
        }
        if ( !CronExpression.isValidExpression( cron ) ) {
          log.error(  "Wrong syntax cron=" + cron );
          return;
        }
        if( cronjobs.containsKey( id )) {
          removeJob( id, cronJob );
        }
        cronjobs.put( id, cronJob );
        log.debug( "Adding job id=" + id + " to my list with cron=" + cron + "." );
        addJob( id, cron, description );
      }
    } catch( Exception e ) {
      log.error( "Can't add CronJob.", e );
    }
  }

  < T > void removeSchedule( CronJob< T > cronJob, Map< String, Object > map ) {
    try {
      synchronized( cronjobs ) {
        String id = ( String )map.get( "id" );
        if( id == null || id.trim().isEmpty() ) {
          id = cronJob.toString();
        }
        if( cronjobs.containsKey( id ) ) {
          log.debug( "Removing job id=" + id );
          removeJob( id, cronJob );
          cronjobs.remove( id );
        }
      }
    } catch( Exception e ) {
      log.error( "Can't remove CronJob.", e );
    }
  }

  @Deactivate
  public void deactivate( Map< String, Object > map ) {
    try {
      cronjobs.forEach( new BiConsumer< String, CronJob<?> >() {

        @Override
        public void accept( String id, CronJob<?> cronJob ) {
          try {
            removeJob( id, cronJob );
          } catch( Exception e ) {
            log.error( "Unabe to remove job id=" + id, e );
          }
        }} );
      sched.shutdown();
    } catch( SchedulerException e ) {
      log.error( "Can't deactivate scheduler.", e );
    }
    log.info( "Scheduler deativated" );
  }

  @Override
  public String status() {
    StringBuilder result = new StringBuilder();
    try {
      result.append( sched.getMetaData().getSummary() );
      
      Set< JobKey > jobKeys = sched.getJobKeys( GroupMatcher.jobGroupEquals( "simple" ) );
      result.append( "I do have " + jobKeys.size() + " jobs scheduled." + "\n" );
      result.append( "Job ID ; State ; NextFireTime ; PreviousFireTime ; FireAgain ; Prio ; StartTime ; Durable ; Description " + "\n" );
      for( JobKey jobKey : jobKeys ) {
        JobDetail jobDetail = sched.getJobDetail( jobKey );
        List< ? extends Trigger > triggers = sched.getTriggersOfJob( jobKey );
        Trigger trigger = triggers.get( 0 );
        result.append( jobKey.getName() );
        result.append( " ; " );
        result.append( sched.getTriggerState( trigger.getKey() ) );
        result.append( " ; " );
        if ( trigger.getNextFireTime() != null ) {
          result.append( dateFormat.format( trigger.getNextFireTime() )  );
        } else {
          result.append( "finished" );
        }
        result.append( " ; ");
        if ( trigger.getPreviousFireTime() != null ) {
          result.append( dateFormat.format( trigger.getPreviousFireTime() ) );
        } else {
          result.append( "not yet" );
        }
        result.append( " ; " );
        result.append( trigger.mayFireAgain() );
        result.append( " ; " );
        result.append( trigger.getPriority() );
        result.append( " ; " );
        result.append( dateFormat.format( trigger.getStartTime() ) );
        result.append( " ; " );
        result.append( jobDetail.isDurable() );
        result.append( " ; " );
        if ( jobDetail.getDescription() != null && !jobDetail.getDescription().isEmpty() ) {
          result.append( jobDetail.getDescription() );
        } else {
          result.append( "n/a" );
        }
        result.append( "\n" );
      }
      if ( sched.getCurrentlyExecutingJobs().size() == 0 ) {
        result.append( "Currently no jobs are running." );
      } else {
        result.append( "Currently " + sched.getCurrentlyExecutingJobs().size() + "jobs are running:" );
      }
      result.append( "\n" );
      for( JobExecutionContext jobExecutionContext : sched.getCurrentlyExecutingJobs() ) {
        result.append( jobExecutionContext.getJobDetail().getKey() );
      }
    } catch( Exception e ) {
      result.append( "\nERROR.\n" );
    }
    return result.toString();
  }

  private void addJob( String id, String cron, String description ) throws SchedulerException {
    JobBuilder jobBuilder = newJob( InternalJob.class ).withIdentity( id, SIMPLE_JOB_GROUP );
    if ( description != null && !description.isEmpty() ) {
      jobBuilder.withDescription( description );
    }
    JobDetail jobDetail = jobBuilder.build();
    Trigger trigger = newTrigger().withIdentity( id + "Trigger", SIMPLE_TRIGGER_GROUP )
      .startNow()
      .withSchedule( cronSchedule( cron ) ).build();
    Date date = sched.scheduleJob( jobDetail, trigger );
    if( date != null ) {
      log.info( "Job " + id + " scheduled for " + dateFormat.format( date ) + " with description: " + description );
    }
  }
  
  private void removeJob( String id, CronJob< ? > cronjob) throws SchedulerException {
    if ( cronjobs.containsValue( cronjob ) ) {
      sched.deleteJob( JobKey.jobKey( id, SIMPLE_JOB_GROUP ) );
      cronjobs.remove( id, cronjob );
    } else {
      log.error( "My list of cronjobs did not contain this job." );
    }
  }
  
  @Override
  public boolean pauseAll() {
    try {
      sched.pauseAll();
      return true;
    } catch( SchedulerException e ) {
      log.error( "Can't pause all jobs", e );
    }
    return false;
  }
  
  @Override
  public boolean pauseJob( String id ) {
    try {
      sched.pauseJob( new JobKey( id, SIMPLE_JOB_GROUP ) );;
      return true;
    } catch( SchedulerException e ) {
      log.error( "Can't pause job with id=" + id, e );
    }
    return false;
  }
  
  @Override
  public boolean resumeAll() {
    try {
      sched.resumeAll();
      return true;
    } catch( SchedulerException e ) {
      log.error( "Can't resume all jobs", e );
    }
    return false;
  }
  
  @Override
  public boolean resumeJob( String id ) {
    try {
      sched.resumeJob( new JobKey( id, SIMPLE_JOB_GROUP ) );;
      return true;
    } catch( SchedulerException e ) {
      log.error( "Can't resume job with id=" + id, e );
    }
    return false;
  }
  
  @Override
  public boolean triggerJob( String id ) {
    try {
      sched.triggerJob( new JobKey( id, SIMPLE_JOB_GROUP ) );;
      return true;
    } catch( SchedulerException e ) {
      log.error( "Can't trigger job with id=" + id, e );
    }
    return false;
  }
  
  @Override
  public boolean standby( boolean bool ) {
    if ( bool ) {
      try {
        sched.standby();
        return true;
      } catch( SchedulerException e ) {
        log.error( "Can't put scheduler in standby.", e );
        return false;
      }
    } else {
      try {
        sched.start();
        return true;
      } catch( SchedulerException e ) {
        log.error( "Can't activate scheduler from standby.", e );
        return false;
      } 
    }
  }
}
