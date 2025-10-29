#!/bin/sh
CONFIG_URL=${CONFIG_SERVER_URI:-http://config-server:8888}

echo "Waiting for Config Server at $CONFIG_URL..."
while ! curl -s $CONFIG_URL >/dev/null; do
  echo "Config Server not ready yet..."
  sleep 3
done

echo "Waiting for Eureka at $EUREKA_URI..."
while ! curl -s $EUREKA_URI >/dev/null; do
  echo "Eureka not ready yet..."
  sleep 3
done

echo "Config Server and Eureka is up! Starting application..."
exec java -jar app.jar
