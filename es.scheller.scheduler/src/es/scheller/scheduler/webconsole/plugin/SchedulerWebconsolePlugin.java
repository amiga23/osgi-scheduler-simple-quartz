package es.scheller.scheduler.webconsole.plugin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import es.scheller.scheduler.api.Scheduler;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;

@RequireWebServerExtender
@Component( service = javax.servlet.Servlet.class, property={"felix.webconsole.label=Scheduler"})
public class SchedulerWebconsolePlugin extends AbstractWebConsolePlugin {

  private static final long serialVersionUID = 7405422022745992690L;
  
  @Reference
  Scheduler schedulerService;
  
  @Activate
  public void activate(BundleContext context) {
    super.activate(context);
  }
  
  @Deactivate
  public void deactivate() {
    super.deactivate();
  }

  @Override
  public String getLabel() {
    return "Scheduler";
  }

  @Override
  public String getTitle() {
    return "Scheduler";
  }

  @Override
  protected void renderContent( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException
  {
    response.getWriter().println( "<br/><pre>" );
    response.getWriter().println( schedulerService.status() );
    response.getWriter().println( "</pre>" );
    response.flushBuffer();
  }


}
