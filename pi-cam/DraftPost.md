# Raspberry Pi Rabbit-cam

*This post describes how to setup a Rasberry Pi with a Pi camera board and a (wireless) internet connection as a webcam serving a latest image (updated every 15 seconds) and a short timelapse containing the most recent hour of images in a 20 second movie (updated once per hour). The website has basic password protection. I've set it up to monitor a rabbit hutch, but obviously there are other potential applications. There isn't anything very novel here - this post serves mainly to document my setup in case I ever need to set it up again, which seems likely, as we've just ordered a Pi Noir camera for additional night-time monitoring...*

## Introduction

My kids got a rabbit this summer. It lives in a hutch in the back garden, placed so that we can see into the hutch easily from the kitchen window. So, the kids can easily check on the rabbit when in the kitchen, but not when they are in the lounge watching TV, and certainly not when they are out-and-about. So my Xmas Pi project was to set up Joey-cam (the rabbit is called "Joey", because kid-reasons), so that the kids can check on the rabbit from their smartphones wherever they are....

However, the hutch has closed (opaque) compartments, so in addition to a live image, the kids also wanted to be able to look at a timelapse of recent images, to be able to quickly and easily check for any sign of movement in the last hour.

The final requirement was that Joey-cam should be accessible from anywhere over the internet, but to have some very basic password protection, so that it wouldn't be completely public.

## Pre-requisites

I'm assuming a Pi (doesn't really matter which), with a Pi camera board attached, a clean Raspbian install, and some kind of internet connection. Mine has wifi (via a small USB wifi dongle), which is very convenient given it's location. It also needs to have an SSH server enabled, so that you can log into it from another machine. I'm assuming that the reader understands how to do all this already. This post is about the camera and web server setup.

## Set up







