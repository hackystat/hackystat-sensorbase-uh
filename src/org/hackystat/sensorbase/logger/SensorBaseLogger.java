package org.hackystat.sensorbase.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Supports logging of informational and error messages by this SensorBase.
 * @author Philip Johnson
 */
public class SensorBaseLogger {
  /** The string used to identify the logger and log files associated with this logger. */
  private static String loggerName = "sensorbase-uh";
  
  /**
   * Private constructor disables default public no-arg constructor. 
   */
  private SensorBaseLogger() {
    // Does nothing.
  }

  /**
   * Returns a logger for this service, creating it if it couldn't be found.
   *
   * @return   The logger.
   */
  public static Logger getLogger() {
    Logger logger = LogManager.getLogManager().getLogger(loggerName);
    if (logger == null) {
        logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);

        // Define a file handler that writes to the ~/.hackystat/logs directory, creating it if nec.
        File logDir = new File(System.getProperty("user.home") + "/.hackystat/logs/");
        logDir.mkdirs();
        String fileName = logDir + "/" + loggerName + ".%u.log";
        FileHandler fileHandler;
        try {
          fileHandler = new FileHandler(fileName, 500000, 1, true);
          fileHandler.setFormatter(new OneLineFormatter());
          logger.addHandler(fileHandler);
        }
        catch (IOException e) {
          throw new RuntimeException("Could not open the log file for the SensorBase", e);
        }

        // Define a console handler to also write the message to the console.
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new OneLineFormatter());
        logger.addHandler(consoleHandler);
        setLoggingLevel(Level.INFO);
      }
    return logger;
  }

  /**
   * Sets the logging level to be used for the Hackystat logger. 
   * @param level The new Level.
   */
  public static void setLoggingLevel(Level level) {
    Logger logger = LogManager.getLogManager().getLogger(loggerName);
    logger.setLevel(level);
    logger.getHandlers()[0].setLevel(level);
    logger.getHandlers()[1].setLevel(level);
  }
}

