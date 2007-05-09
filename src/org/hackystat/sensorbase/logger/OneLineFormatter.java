package org.hackystat.sensorbase.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

  /**
   * Provides a one line formatter for use with Hackystat logging. Supports optional date stamp
   * prefix. If the date stamp prefix is enabled, then a cr is also added.
   *
   * @author Philip Johnson
   */
class OneLineFormatter extends Formatter {

    /**
     * Formats the passed log string as a single line. Prefixes the log string with a date stamp.
     *
     * @param record  A log record.
     * @return The message string.
     */
    public String format(LogRecord record) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
      StringBuffer buff = new StringBuffer();
      buff.append(dateFormat.format(new Date()));
      buff.append(" ");
      buff.append(record.getMessage());
      buff.append(System.getProperty("line.separator"));
      return buff.toString();
    }
  }
  


