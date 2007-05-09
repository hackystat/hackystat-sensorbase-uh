package org.hackystat.sensorbase.server;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * The Server class. 
 * @author Philip Johnson
 */
public class Server {
  
  /**
   * The main program that is invoked when running the SensorBase from the command line. 
   * @param args ignored. for now. 
   */
  public static void main(String[] args) {
    try {
        // Create a new Restlet component
        Component component = new Component();

        // Create the HTTP server connector, then add it as a server
        // connector to the Restlet component. Note that the component
        // is the call restlet.
        component.getServers().add(Protocol.HTTP, 9876);

        // Prepare and attach a test Handler
        Restlet handler = new Restlet(component.getContext()) {
            @Override
            public void handle(Request request, Response response) {
                if (request.getMethod().equals(Method.PUT)) {
                    System.out.println("Handling the call...");
                    System.out
                            .println("Trying to get the entity as a form...");
                    Form form = request.getEntityAsForm();

                    System.out.println("Trying to getParameters...");
                    StringBuffer sb = new StringBuffer("foo");
                    for (Parameter p : form) {
                        System.out.println(p);

                        sb.append("field name = ");
                        sb.append(p.getName());
                        sb.append("value = ");
                        sb.append(p.getValue());
                        sb.append("\n");
                        System.out.println(sb.toString());
                    }

                    response.setEntity(sb.toString(), MediaType.TEXT_PLAIN);
                    System.out.println("Done!");
                } else {
                    response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                }
            }
        };

        component.getDefaultHost().attach("/test", handler);

        // Now, start the component
        component.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
