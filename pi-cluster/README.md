# Raspberry Pi 2 cluster with NAT routing head node

Scripts and config files associated with my blog post "Raspberry Pi 2 cluster with NAT routing" currently in preparation. A link to the post will be provided here when it is complete.

The brief summary is as follows:

Create a cluster by connecting a bunch of Pis to a switch via the Pis ethernet port. Pick one of the Pis to be a head node and NAT router. Connect a USB ethernet dongle to this Pi, and use the dongle port as the internet uplink.

Stick Raspbian on each node, with SSH server enabled.

Boot up the head node.

```bash
wget https://github.com/darrenjw/blog/archive/master.zip
unzip master.zip
cd blog-master/pi-cluster
sudo sh install-packages
```

will reboot when done. On reboot, re-enter same directory, and then do:

```sudo sh setup-network```

when done, will reboot.

On reboot, re-enter same directory. Boot up the other nodes and then run 

```sh setup-cluster```

on the head node.


