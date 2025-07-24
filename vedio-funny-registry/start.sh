#!/bin/bash

# Check if nacos directory exists
if [ ! -d "nacos" ]; then
    echo "Nacos not found. Downloading Nacos 3.0.2..."
    
    # Download Nacos
    curl -L -o nacos.zip https://github.com/alibaba/nacos/releases/download/3.0.2/nacos-server-3.0.2.zip
    
    # Extract Nacos
    unzip -q nacos.zip
    rm nacos.zip
    
    # Copy application.properties to conf directory
    cp application.properties nacos/conf/application.properties
    
    echo "Nacos setup completed."
else
    echo "Nacos directory found."
fi

# Start Nacos in standalone mode
cd nacos/bin
sh startup.sh -m standalone &
cd ../..

# Wait for Nacos to start (max 60 seconds)
echo "Waiting for Nacos to start..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    sleep 2
    if curl -s "http://localhost:8848/nacos/v1/console/health" > /dev/null 2>&1; then
        echo "Nacos is now running"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT+1))
    echo "Waiting... Attempt $RETRY_COUNT of $MAX_RETRIES"
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "Timeout waiting for Nacos to start"
    exit 1
fi

# Import configurations
echo "Starting configuration import..."
./import-configs.sh
