#!/bin/bash
# camera-script.sh

while [ "true" != "false" ]
do
  DATE=$(date +"%Y-%m-%d_%H:%M:%S")
  raspistill -w 640 -o now.jpg
  cp now.jpg ~/timelapse/tl-$DATE.jpg # time-stamped photo
  cp now.jpg /var/www/latest.jpg # copy latest image for serving via web
  # raspistill takes around 8 seconds
  # add any extra delay (in seconds) below:
  sleep 7
done

exit 0

#eof

