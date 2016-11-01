package es.scheller.scheduler.provider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import javax.management.ServiceNotFoundException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.enroute.scheduler.api.CronJob;


public class InternalJob implements Job {

  private Logger log = LoggerFactory.getLogger( InternalJob.class );
  final DateFormat dateFormat;

  public InternalJob() {
    try {
      dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss zzz");
      dateFormat.setTimeZone( new SimpleTimeZone( 0, "UTC" ) );
    } catch( Throwable t ) {
      throw t;
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void execute( JobExecutionContext arg0 ) throws JobExecutionException {
    String id = arg0.getJobDetail().getKey().getName();
    try {
      log.debug( dateFormat.format( new Date() )
                 + " Hi i am job "
                 + id );
      // Try to get job with attribute id
      @SuppressWarnings( "rawtypes" )
      CronJob cronJob = getService( CronJob.class, "id=" + id );
      // If not found, try to find with toString method
      if ( cronJob == null ) {
        CronJob<?>[] cronJobs = getService( CronJob.class );
        for( int i = 0; i < cronJobs.length; i++ ) {
          if ( cronJobs[ i ].toString().equals( id ) ) {
            cronJob = cronJobs[ i ];
            break;
          }
        }
      }
      // If still null, log error and return
      if ( cronJob == null ) {
        log.error( "No CronJob found with id=" + id);
        return;
      }
      cronJob.run( cronJob );
    } catch( Exception e ) {
      log.error( "Job with id=" + id + " failed.", e );
    }
  }

  /**
   * Get a service from the OSGi Service Registry
   * 
   * @param clazz The Interface of the required Service.
   * @param filter Some filter
   * @return Returns null if service was not resolved after 5000 ms.
   * @throws ServiceNotFoundException
   */
  public < T > T getService( Class< T > clazz, String filter ) throws ServiceNotFoundException {
    BundleContext context = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
    String filterString = "(&("
                          + Constants.OBJECTCLASS
                          + "="
                          + clazz.getName()
                          + ")"
                          + "("
                          + filter
                          + "))";
    Filter filterFilter;
    try {
      filterFilter = context.createFilter( filterString );
    } catch( InvalidSyntaxException ise ) {
      log.error( "Can't get service because of invalid filter syntax. filter=" + filter, ise );
      return null;
    }
    ServiceTracker< T, T > st = new ServiceTracker<>( context, filterFilter, null );
    st.open();
    try {
      T service = st.waitForService( 5000 );
      if( service != null ) {
        return service;
      } else {
        throw new ServiceNotFoundException();
      }
    } catch( InterruptedException e ) {
      return null;
    }
  }
  
  /**
   * Get a service from the OSGi Service Registry
   * 
   * @param clazz The Interface of the required Service.
   * @return Returns null if service was not resolved after 5000 ms.
   * @throws ServiceNotFoundException
   */
  public < T > T[] getService( Class< T > clazz ) throws ServiceNotFoundException {
    BundleContext context = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
    ServiceTracker< T, T > st = new ServiceTracker<>( context, clazz, null );
    st.open();
    @SuppressWarnings( "unchecked" )
    T[] services = ( T[] )st.getServices();
    if( services != null ) {
      return services;
    } else {
      throw new ServiceNotFoundException();
    }
  }
}
