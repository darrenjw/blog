# Rabbit-cam notes

## Basic setup

Basic camera setup in camera-setup.sh

But need to enable camera in raspi-config and reboot

Basic web setup in web-setup.sh

Also need to add relevant lines to `crontab -e`:
```
@reboot /home/pi/camera-script.sh 2>&1
20 * * * * /home/pi/hourly.sh 2>&1
```
Check with `crontab -l`


## Adding a password

Details at: https://www.cyberciti.biz/tips/lighttpd-setup-a-password-protected-directory-directories.html

Essentially, add: "mod_auth" to the list of server.modules and then:
```
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
to the end of /etc/lighttpd/lighttpd.conf 

Then restart server with: `/etc/init.d/lighttpd restart`

Password file just contains: `username:password` on a single line.




