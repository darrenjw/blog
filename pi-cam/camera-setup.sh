# camera-setup.sh
# script to run once to set up camera

sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y install libav-tools

mkdir ~/timelapse

sudo raspi-config

# eof
