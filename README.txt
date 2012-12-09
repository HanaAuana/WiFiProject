WifiProject
-----------
@author Alex King and Michael Lim
@date 12.8.2012

Checkpoint 1 (4/5):

	Implemented two-threaded approach to simulating Wifi. Diagram mostly correct.

Checkpoint 2 (5/5):

	Complete

Checkpoint 3 (4/5):

	Implemented address selectivity. Didn't implement MAC rules. ACKs mostly worked. Sequences were out of order due to a structural
	issue in our Packet() constructor.

Overall Project:

MAC rules

	Implemented mainly in the form of a switch statement in our "Sender" class. Should work
	according to all required MAC rules.

Address Selectivity

	Accurately accepts and acknowledges packets with correct addresses.

ACKs

	Should work as expected. Sends and waits for ACKs according to MAC rules.

Sequence Numbers

	Packets are sent and received with accurate sequences.

Control Interface

	All controls are implemented. Options prints out all possible options. Debug implements FULL_DEBUG. Slot
	selection and Beacon intervals set correctly.

Status

	Status is updated during pertinent action items.

Output
	
	All output correctly prints in the output module.

Limited Buffering

	BlockingQueues are initialized with size QUEUE_SIZE which determines the size of the buffer. ACKs and BEACONs ignore this size and send
	according to MAC rules.

Timing Issues

	All rounding to nearest 50 is performed. Whenever printing the time sent, the wait value (waited) is stored and then printed to retain
	accurate time.

Clock Synchronization

	Beacons are set and correct intervals while adhering to MAC rules. Clock offset is adjusted according to incoming Beacons. Clock
	adjustment is slightly off on occasion. Often, it will adjust correctly, remaining within a few milliseconds of the Beacons sent from
	wifi.jar. Depending on other beacons sent on the network, sometimes adjusts with larger values.

CRCs

	CRCs are fully implemented using the CRC32 class.
	

