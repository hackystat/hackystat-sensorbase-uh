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

  /** Holds the Restlet Component associated with this Server. */
  private Component component; 
  
  /**
   * Creates a new instance of a SensorBase HTTP server, listening on the supplied port.  
   * @param port The port number for this Server. 
   * @return The Server instance created. 
   * @throws Exception If problems occur starting up this server. 
   */
  public static Server newInstance(int port) throws Exception {
    Server server = new Server();
    SensorBaseLogger.getLogger().warning("Starting SensorBase.");
    server.component = new Component();
    server.component.getServers().add(Protocol.HTTP, port);
    server.component.getDefaultHost().attach("/sensorbase", server);
    server.component.start();
    return server;
  }

  /**
   * Starts up the SensorBase web service on port 9876.  Control-c to exit. 
   * @param args Ignored. 
   * @throws Exception if problems occur.
   */
  public static void main(final String[] args) throws Exception {
    Server.newInstance(9876);
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
  
  /**
   * Stops this server. 
   * @throws Exception If problems occur stopping the server. 
   */
  public void stop() throws Exception {
    this.component.stop();
  }
}

