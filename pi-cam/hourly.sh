# hourly.sh
# hourly cron
# NB. Can take over half an hour to run, so shouldn't run more than once per hour

# cron:
# 20 * * * * /home/pi/hourly.sh

cd ~/timelapse

# Delete images older than an hour
find . -name tl-\*.jpg -type f -mmin +60 -delete

# Make stills into a movie:
ls *.jpg > stills.txt
rm -f timelapse.avi
mencoder -nosound -ovc lavc -lavcopts vcodec=mpeg4:aspect=16/9:vbitrate=8000000 -vf scale=640:-1 -o timelapse.avi -mf type=jpeg:fps=12 mf://@stills.txt

# Turn into a movie that will view on android devices:
avconv -i timelapse.avi -vf scale=640:-1 timelapse.mp4

# move to web for serving:
mv timelapse.mp4 /var/www/


# eof 


