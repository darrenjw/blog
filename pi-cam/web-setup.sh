# web-setup.sh
# run-once web setup script

sudo apt-get update
sudo apt-get upgrade
sudo apt-get install lighttpd

sudo chown www-data:www-data /var/www
sudo chmod 775 /var/www
sudo usermod -a -G www-data pi

cp index.html /var/www



# eof


