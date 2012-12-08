WifiProject
-----------
@author Alex King and Michael Lim
@date 12.8.2012

Checkpoint 1 (4/5):

Implemented two-threaded approach to simulating Wifi

Checkpoint 2 (5/5):

Checkpoint 3 (4/5):

Alex King and Michael Lim

Just wanted to provide a little context, since we're pretty sure our code doesn't function completely up to the standards of the checkpoint.

MAC rules

Didn't even touch these. Laid out some theoretical groundwork though.

Address Selectivity

Should work fine

ACKs

We should be able to send ACKs correctly. In theory, our code will wait for the proper ACK before sending. However, there is an issue with our Packet class that prevents us from testing correctly.
When we receive data from the RF Layer, and pass into a constructor for the Packet class, our sequence numbers get messed up. We've isolated the problem to somewhere in the Packet class, but haven't been able to determine the correct bitwise operations to fix it.

Sequence Numbers

Our code sends out correct sequence numbers and updates them accordingly.
Receiving sequence numbers should work in theory as well, but the above mentioned ACK problem makes testing this difficult.