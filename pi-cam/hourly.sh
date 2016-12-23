# hourly.sh
# hourly cron
# NB. Can take over half an hour to run, so shouldn't run more than once per hour

cd ~/timelapse

# Delete images older than an hour (don't actually need)
# find . -name tl-\*.jpg -type f -mmin +60 -delete

# Make stills into a movie:
ls *.jpg | awk 'BEGIN{ a=0 }{ printf "mv %s tlsn-%04d.jpg\n", $0, a++ }' | bash
avconv -y -r 10 -i tlsn-%4d.jpg -r 10 -vcodec libx264 -q:v 3 -vf scale=640:480 timelapse.mp4;
rm -f tlsn-*.jpg

# move to web for serving:
mv timelapse.mp4 /var/www/html/

# eof 


