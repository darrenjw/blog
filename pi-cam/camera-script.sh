#!/bin/bash
# camera-script.sh

while [ "true" != "false" ]
do
  DATE=$(date +"%Y-%m-%d_%H:%M:%S")
  raspistill -o now.jpg # default settings
  # raspistill -ex night -ss 3500000 -o now.jpg # night settings
  avconv -i now.jpg -vf scale=640:-1 now-small.jpg
  mv now-small.jpg now.jpg
  cp now.jpg ~/timelapse/tl-$DATE.jpg # time-stamped photo
  cp now.jpg /var/www/html/latest.jpg # copy latest image for serving via web
  # raspistill takes around 8 seconds
  # add any extra delay (in seconds) below:
  sleep 7
done

exit 0

#eof

