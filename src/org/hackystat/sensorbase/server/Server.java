package org.hackystat.sensorbase.server;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;


/**
 * Sets up the HTTP Server process and dispatching to the associated resources. 
 * @author Philip Johnson
 */
public class Server extends Application {
  
  /**
   * Starts up the SensorBase web service.  Control-c to exit. 
   * @param args Ignored. 
   * @throws Exception if problems occur.
   */
  public static void main(final String[] args) throws Exception {
    SensorBaseLogger.getLogger().warning("Starting SensorBase.");
    Component component = new Component();
    component.getServers().add(Protocol.HTTP, 9876);
    component.getDefaultHost().attach("/sensorbase", new Server() );
    component.start();
  }

  /**
   * Dispatch to the Projects, SensorData, SensorDataTypes, or Users Resource depending on the URL.
   * @return The router Restlet.
   */
  @Override
  public Restlet createRoot() {
    Router router = new Router(getContext());
    // This URI fragment specifies the "file" resource and a specific file name.
    // The router will dispatch to the FileResource class for URLs with this template 
    //router.attach("/file/{filename}", FileResource.class);
    return router;
  }
}

