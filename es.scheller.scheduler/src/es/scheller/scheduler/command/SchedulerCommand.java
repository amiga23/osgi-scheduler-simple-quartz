package es.scheller.scheduler.command;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.scheller.scheduler.api.Scheduler;

@Component( service = SchedulerCommand.class, property = {
  CommandProcessor.COMMAND_SCOPE + "=cron",
  CommandProcessor.COMMAND_FUNCTION + "=list",
  CommandProcessor.COMMAND_FUNCTION + "=pauseAll",
  CommandProcessor.COMMAND_FUNCTION + "=pauseJob",
  CommandProcessor.COMMAND_FUNCTION + "=resumeAll",
  CommandProcessor.COMMAND_FUNCTION + "=resumeJob",
  CommandProcessor.COMMAND_FUNCTION + "=triggerJob",
  CommandProcessor.COMMAND_FUNCTION + "=enable",
  CommandProcessor.COMMAND_FUNCTION + "=standby"
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
