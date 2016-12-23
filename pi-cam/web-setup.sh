# web-setup.sh
# run-once web setup script

sudo apt-get -y update
sudo apt-get -y install lighttpd

sudo chown www-data:www-data /var/www/html
sudo chmod 775 /var/www/html
sudo usermod -a -G www-data pi

sudo cp index.html /var/www/html/
sudo chown www-data:www-data /var/www/html/index.html
sudo chmod g+w /var/www/html/index.html

# eof


