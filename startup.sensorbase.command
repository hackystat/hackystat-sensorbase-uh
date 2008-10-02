echo -n -e "\033]0;Sensorbase $HACKYSTAT_VERSION\007"
java -Xmx512M -jar $HACKYSTAT_SERVICE_DIST/hackystat-sensorbase-uh/sensorbase.jar
