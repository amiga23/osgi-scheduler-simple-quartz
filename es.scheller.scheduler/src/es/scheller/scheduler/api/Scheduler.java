package es.scheller.scheduler.api;

public interface Scheduler {

  String status();

  boolean pauseAll();

  boolean resumeAll();

  boolean pauseJob( String id );

  boolean resumeJob( String id );

  boolean triggerJob( String id );

  boolean standby( boolean bool );
}
