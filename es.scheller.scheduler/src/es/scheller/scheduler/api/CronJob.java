package es.scheller.scheduler.api;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface CronJob<T> {
	String CRON = "cron";
    
	public void run(T data) throws Exception;
}