# Raspberry Pi 2 cluster with NAT routing head node

Scripts and config files associated with my blog post "Raspberry Pi 2 cluster with NAT routing" currently in preparation. A link to the post will be provided here when it is complete.

The brief summary is as follows:

Create a cluster by connecting a bunch of Pis to a switch via the Pis ethernet port. Pick one of the Pis to be a head node and NAT router. Connect a USB ethernet dongle to this Pi, and use the dongle port as the internet uplink.

Stick Raspbian on each node, with SSH server enabled.

Boot up the head node, and run "make-head-node".

Boot up the other nodes and then run "cluster-setup" on the head node.


