#!/bin/bash

# Wait until DISPLAY is accessible
mkdir -p /root/.droid6/profile_templates/schema\ 6.7.0/
export DISPLAY=:99
Xvfb $DISPLAY -screen 0 1920x1080x24 &
XVFB_PID=$!

echo "Waiting for X to be available..."
for i in {1..30}; do
    if xdpyinfo -display "$DISPLAY" > /dev/null 2>&1; then
        echo "X display $DISPLAY is ready."
        break
    fi
    sleep 1
done

mvn clean install -q -DskipTests -Dcheckstyle.skip=true -Ddependency-check.skip=true
DISPLAY=:99.0 mvn test -Dtest="uk.gov.nationalarchives.droid.gui.ui.SwingUiTest" -pl droid-swing-ui -Djava.awt.headless=false
