# Raspberry Pi Rabbit-cam

*This post describes how to setup a Rasberry Pi with a Pi camera board and a (wireless) internet connection as a webcam serving a latest image (updated every 15 seconds) and a short timelapse containing the most recent hour of images in a 20 second movie (updated once per hour). The website has basic password protection. I've set it up to monitor a rabbit hutch, but obviously there are other potential applications. There isn't anything very novel here - this post serves mainly to document my setup in case I ever need to set it up again, which seems likely, as we've just ordered a Pi Noir camera for additional night-time monitoring...*

## Introduction

My kids got a rabbit this summer. It lives in a hutch in the back garden, placed so that we can see into the hutch easily from the kitchen window. So, the kids can easily check on the rabbit when in the kitchen, but not when they are in the lounge watching TV, and certainly not when they are out-and-about. So my Xmas Pi project was to set up Joey-cam (the rabbit is called "Joey", because kid-reasons), so that the kids can check on the rabbit from their smartphones wherever they are....

However, the hutch has closed (opaque) compartments, so in addition to a live image, the kids also wanted to be able to look at a timelapse of recent images, to be able to quickly and easily check for any sign of movement in the last hour.

The final requirement was that Joey-cam should be accessible from anywhere over the internet, but to have some very basic password protection, so that it wouldn't be completely public.

## Pre-requisites

I'm assuming a Pi (doesn't really matter which), with a Pi camera board attached, a clean Raspbian install, and some kind of internet connection. Mine has wifi (via a small USB wifi dongle), which is very convenient given it's location. It also needs to have an SSH server enabled, so that you can log into it from another machine. I'm assuming that the reader understands how to do all this already. This post is about the camera and web server setup.

## Set up

First log in to the Pi from another machine (eg. `ssh pi@ip-address`, replacing *ip-address* appropriately) and then download this repo with:
```bash
cd
wget https://github.com/darrenjw/blog/archive/master.zip
unzip master.zip
cd blog-master/pi-cam/
```
Run the camera setup with:
```bash
./camera-setup.sh
```
The script finishes by running raspi-config. If you already have the camera enabled, just exit. If not, enable it, but don't yet reboot - you will need to reboot soon anyway. But if you haven't set a sensible hostname yet, it's probably worth doing that, too.

The camera scripts can be enabled by running:
```bash
crontab -e
```
and add the following lines to the end of the file:
```
@reboot /home/pi/blog-master/pi-cam/camera-script.sh 2>&1
20 * * * * /home/pi/blog-master/pi-cam/hourly.sh 2>&1
```
Save and exit and check with `crontab -l`.

Next set up the web-site by running:
```bash
./web-setup.sh
```

Reboot with `sudo reboot` and log back in again after 30 seconds and then return to the pi-cam directory with:
```bash
cd blog-master/pi-cam/
```
First check that photos start to appear every 15 seconds or so:
```bash
ls -l ~/timelapse/
```
Assuming so, try pointing a web-browser at your Pi (`http://ip-address/` - replace *ip-address* appropriately). You should get a basic page containing the latest image, and the page should update every 20 seconds. There will also be a link to the "latest movie", but it won't work straight away - wait a couple of hours before trying that.

If you aren't going to open up your Pi to the world, then you should be done.

## Adding password protection

If you are intending to open up your cam to the internet, it's probably worth adding some basic password protection. The very basic protection I describe here is probably OK for something as mundane as a rabbit hutch, but if you are monitoring anything more sensitive, then you should google how to lock down your site properly.

You can find more detailed instructions [here](https://www.cyberciti.biz/tips/lighttpd-setup-a-password-protected-directory-directories.html), but the tl;dr is to
```bash
sudo nano /etc/lighttpd/lighttpd.conf
```
and paste the following at the end of the file:
```

server.modules += ( "mod_auth" )

auth.debug = 2
auth.backend = "plain"
auth.backend.plain.userfile = "/home/pi/lighttpdpwd"

auth.require = ( "/" =>
(
"method" => "basic",
"realm" => "Rabbit-cam",
"require" => "user=joey"
)
)
```
Replace the required username (here, "joey"), with something appropriate, and create the file `/home/pi/lighttpdpwd` containing a single line in the form `username:password`. Then restart the server with
```bash
/etc/init.d/lighttpd restart
```
Test it out and find diagnostic information in `/var/log/lighttpd/`.


## Opening up

You need to open up ports on your router to allow access to the Pi from outside your home network. This is router-specific, so you may want to just google for instructions for you router. Note that I have another public-facing web server on my home network, so I map port 81 on my router to port 80 on the Pi to have a public facing webcam on port 81 of my home network address (`http://my.home.address:81`, where *my.home.address* is replaced appropriately).

## Customisation

You will almost certainly want to customise the very basic web page in `/var/www/index.html` appropriately for your application. You can also tweak various things in `camera-script.sh` and `hourly.sh`, particularly settings such as resolutions, qualities, frame-rates, etc.


