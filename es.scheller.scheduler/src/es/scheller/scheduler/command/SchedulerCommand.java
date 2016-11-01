package es.scheller.scheduler.command;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.scheller.scheduler.api.Scheduler;
import osgi.enroute.debug.api.Debug;

@Component( service = SchedulerCommand.class, property = {
  Debug.COMMAND_SCOPE + "=cron",
  Debug.COMMAND_FUNCTION + "=list",
  Debug.COMMAND_FUNCTION + "=pauseAll",
  Debug.COMMAND_FUNCTION + "=pauseJob",
  Debug.COMMAND_FUNCTION + "=resumeAll",
  Debug.COMMAND_FUNCTION + "=resumeJob",
  Debug.COMMAND_FUNCTION + "=triggerJob",
  Debug.COMMAND_FUNCTION + "=enable",
  Debug.COMMAND_FUNCTION + "=standby"
} )
public class SchedulerCommand {
  
  Logger log = LoggerFactory.getLogger( SchedulerCommand.class );

  @Reference
  private Scheduler scheduler;
  
  public String list() {
    try {
      return scheduler.status();
    } catch ( Exception e ) {
      log.error("Error in cron:list command", e);
      return e.getMessage();
    }
  }
  
  public String pauseAll() {
    if ( scheduler.pauseAll() ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String pauseJob( String id ) {
    if ( scheduler.pauseJob( id ) ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String resumeAll() {
    if ( scheduler.resumeAll() ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String resumeJob( String id ) {
    if ( scheduler.resumeJob( id ) ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String triggerJob( String id ) {
    if ( scheduler.triggerJob( id ) ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String enable() {
    if ( scheduler.standby( false ) ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
  
  public String standby() {
    if ( scheduler.standby( true ) ) {
      return "SUCESS";
    } else {
      return "FAILED";
    }
  }
}
