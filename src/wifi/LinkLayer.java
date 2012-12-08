package wifi;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import rf.RF;

/**
 * Use this layer as a starting point for your project code. See
 * {@link Dot11Interface} for more details on these routines.
 * 
 * @author
 */

public class LinkLayer implements Dot11Interface {
	private RF theRF; // Simulates a physical layer for us to send on
	private short ourMAC; // The address we are using
	private PrintWriter output; // The output stream we'll write to

	private int currentStatus;
	public static final int SUCCESS = 1;
	public static final int UNSPECIFIED_ERROR = 2;
	public static final int RF_INIT_FAILED = 3;
	public static final int TX_DELIVERED = 4;
	public static final int TX_FAILED = 5;
	public static final int BAD_BUF_SIZE = 6;
	public static final int BAD_ADDRESS = 7;
	public static final int BAD_MAC_ADDRESS = 8;
	public static final int ILLEGAL_ARGUMENT = 9;
	public static final int INSUFFICIENT_BUFFER_SPACE = 10;

	public static final int DEFAULT_WAIT = 1;
	public static final int WAIT_ACK = 2;
	public static final int BUSY_WAIT = 3;
	public static final int SLOT_WAIT = 4;
	public static final int IDLE_WAIT = 5;

	private int macState;

	private int ourSlot = 0;

	private boolean needNextPacket = true;

	private Packet macPacket;
	private Packet storePacket; //Used to hold onto a packet if a beacon has to cut in front of it

	public static final int SIFS = RF.aSIFSTime;
	public static final int SLOT = RF.aSlotTime;
	public static final int DIFS = RF.aSIFSTime + (2*RF.aSlotTime);

	private int cWindow = RF.aCWmin;

	private static final int QUEUE_SIZE = 4;
	private static final int FULL_DEBUG = -1;

	private int debug = FULL_DEBUG; // SET TO 0 BEFORE TURNING IN!
	boolean debugRound = false;
	boolean beaconDebug = true;

	private boolean randomSlots = true;

	private int beaconDelay = 5000;
	private long lastBeacon = 0;


	private int beaconOffset = 1900;

	int retryCounter = 0;

	private BlockingQueue<Packet> in = new ArrayBlockingQueue(QUEUE_SIZE);
	private BlockingQueue<Packet> out = new ArrayBlockingQueue(QUEUE_SIZE);

	private HashMap<Short, Short> sendSequences = new HashMap();
	private HashMap<Short, Short> recvSequences = new HashMap();

	private HashMap<Short, ArrayList<Short>> recievedACKS = new HashMap();

	public synchronized BlockingQueue<Packet> getIn() { // These Queues will facilitate communication between the LinkLayer and its
		// Sender and Receiver helper classes.
		return in;
	}

	public synchronized BlockingQueue<Packet> getOut() {
		return out;
	}

	/**
	 * Constructor takes a MAC address and the PrintWriter to which our output
	 * will be written.
	 * 
	 * @param ourMAC
	 *            MAC address
	 * @param output
	 *            Output stream associated with GUI
	 */
	public LinkLayer(short ourMAC, PrintWriter output) {
		this.ourMAC = ourMAC;
		this.output = output;
		theRF = new RF(null, null);

		macState = DEFAULT_WAIT;

		if(theRF == null){
			currentStatus = RF_INIT_FAILED;
		}

		output.println("LinkLayer initialized with MAC address of " + ourMAC);
		output.println("Send command 0 to see a list of supported commands");

		Receiver theReceiver = new Receiver(this, theRF); // Creates the sender and receiver instances
		Sender theSender = new Sender(this, theRF);

		Thread r = new Thread(theReceiver); // Threads them
		Thread s = new Thread(theSender);

		r.start(); // Starts the threads running
		s.start();
	}

	public short nextSeqNum(short addr) {
		short nextSeq;
		if (sendSequences.containsKey(addr)) {
			nextSeq = (short) (sendSequences.get(addr) + 1);
		} else {
			nextSeq = 0;
		}
		this.sendSequences.put(addr, (short) (nextSeq));
		return nextSeq;
	}

	public short gotRecvSeqNum(short addr) {
		short nextSeq;
		if (recvSequences.containsKey(addr)) {
			nextSeq = (short) (recvSequences.get(addr) + 1);
		} else {
			nextSeq = 0;
		}
		this.recvSequences.put(addr, (short) (nextSeq));
		return nextSeq;
	}

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send. See docs for full description.
	 */
	public int send(short dest, byte[] data, int len) {

		if(data.length < len){
			currentStatus = ILLEGAL_ARGUMENT;
		}

		short seqNum = nextSeqNum(dest);

		Packet p = new Packet(0, seqNum, dest, ourMAC, data); // Builds a packet using the supplied data

		if (out.size() < QUEUE_SIZE) {

			if (debug == FULL_DEBUG) {
				output.println("Queueing  " + p.getData().length + " bytes for " + dest);
			}

			try {
				out.put(p); // Puts the created packet into the outgoing queue
			} catch (InterruptedException e) {
				currentStatus = UNSPECIFIED_ERROR;
				e.printStackTrace();
			}
			currentStatus = SUCCESS;
			return len;

		} else {
			currentStatus = INSUFFICIENT_BUFFER_SPACE;
			return 0;
		}
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info
	 * into the Transmission object. See docs for full description.
	 */
	public int recv(Transmission t) {	// Called by the above layer when it wants to receive data

		Packet p;

		if(t == null){
			currentStatus = ILLEGAL_ARGUMENT;
		}

		try {
			p = in.take(); // Grabs the next packet from the incoming queue
			if (p.getSeqNum() < recvSequences.get(p.getSrcAddr())) {
				output.println("Already got this");
			} else {
				byte[] data = p.getData(); 	// Extracts the necessary parts from the packet and puts them into the supplied transmission object
				t.setSourceAddr((short) p.getSrcAddr());
				t.setDestAddr((short) p.getDestAddr());
				t.setBuf(data);
				currentStatus = SUCCESS;
				return data.length;	// Returns the length of the data recieved
			}

		} catch (InterruptedException e) {
			currentStatus = UNSPECIFIED_ERROR;
			e.printStackTrace();
		}
		return -1;

	}

	/**
	 * Returns a current status code. See docs for full description.
	 */
	public int status() {
		output.println("LinkLayer: Faking a status() return value of 0");
		return 0;
	}

	/**
	 * Passes command info to your link layer. See docs for full description.
	 */
	public int command(int cmd, int val) {
		switch (cmd) {
		case 0:
			output.println("Options & Settings:");
			output.println("-----------------------------------------");
			output.println("Cmd 0: \t View all options and settings.");
			output.println("Cmd 1: \t Set debug value. Debug currently at "
					+ debug);
			output.println("\t Use -1 for full debug output, 0 for no output.");
			output.println("Cmd 2: \t Set slot for link layer.");
			output.println("Cmd 3: \t Set desired wait time between start of beacon transmissions (in seconds).");
			output.println("-----------------------------------------");
			break;
		case 1:
			currentStatus = SUCCESS;
			if (val == FULL_DEBUG) {
				debug = FULL_DEBUG;
			}
			output.println("Setting debug to " + debug);
			break;
		case 2:
			currentStatus = SUCCESS;
			if (val == 0) {
				output.println("Using random slot times");
				randomSlots = true;
			} else {
				output.println("Using maximum slot times");
				randomSlots = false;
			}
			break;
		case 3:
			currentStatus = SUCCESS;
			if (val < 0) {
				beaconDelay = -1;
				output.println("Disabling beacons");
			} else {
				output.println("Using a beacon delay of " + val + " seconds");
				beaconDelay = val;
			}
			break;
		default:
			currentStatus = ILLEGAL_ARGUMENT;
			output.println("Command " + cmd + " not recognized.");
		}
		return 0;
	}

	private long roundUp(long input){

		if (Math.ceil(input % 50L / 50.0D) == 1.0D) //If already multiple of 50
		{
			return input + (50L - input % 50L);
		}else{
			return input;
		}
	}

	private void waitUntil(long time){
		long cTime = theRF.clock();

		while(cTime < time){
			if(debugRound == true){
				output.println("Time is: "+ cTime + " Need to wait till: " + time);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				currentStatus = UNSPECIFIED_ERROR;
				e.printStackTrace();
			}
			cTime+=10;
		}
	}

	private long nearestWait(long waitTime){

		if(debugRound == true){
			output.println("Time is: "+ theRF.clock() + " Need to wait: " + waitTime+ "  rounding to: " + roundUp(theRF.clock() + waitTime));
		}

		return roundUp(theRF.clock() + waitTime);
	}

	class Sender implements Runnable { // Handles sending functions for the LinkLayer

		private RF theRF;
		private LinkLayer theLinkLayer;


		public Sender(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;

			if(theRF == null){
				currentStatus = RF_INIT_FAILED;
			}

			if(thisLink == null){
				currentStatus = ILLEGAL_ARGUMENT;
			}

			theLinkLayer = thisLink;
		}

		public void run() {

			while (true) {				
				
				
				

				switch(macState){



				case DEFAULT_WAIT:	//1

					long cTime = theRF.clock();

					if(lastBeacon + beaconDelay <= cTime  ){//Time to send a Beacon
						lastBeacon = cTime;

						if(beaconDebug == true){
							output.println("Time for a BEACON");
						}

						ByteBuffer buf = ByteBuffer.allocate(8);
						buf.putLong(theRF.clock() + beaconOffset);
						byte[] timeStamp = buf.array();
						Packet beacon = new Packet(2, (short) 0, (short)-1, ourMAC, timeStamp );

						macPacket = beacon;
						if(!theRF.inUse() ){
							macState = IDLE_WAIT;
						}else{
							macState = BUSY_WAIT;
						}

					}

					else{
						
						if (theLinkLayer.getOut().isEmpty() == false && needNextPacket == true){
							try {
								macPacket = theLinkLayer.getOut().take();
							} catch (InterruptedException e) {
								currentStatus = UNSPECIFIED_ERROR;
								e.printStackTrace();
							}
							needNextPacket = false;
						}
						

						
						if(!theLinkLayer.getOut().isEmpty()){
							if(!theRF.inUse() ){
								macState = IDLE_WAIT;
							}else{
								macState = BUSY_WAIT;
							}
						}
					}


					break;
				case WAIT_ACK:	//2
					if(debug == FULL_DEBUG){
						output.println("Moved to WAIT_ACK.");
					}


					try {
						Thread.sleep((long) (2615+SIFS+SLOT)); //Average ACK transmission + SIFS + SLOT (IEEE Spec.)
					} catch (InterruptedException e) {
						currentStatus = UNSPECIFIED_ERROR;
						e.printStackTrace();
					}

					if((theLinkLayer.recievedACKS.containsKey(macPacket.getDestAddr())
							&& !theLinkLayer.recievedACKS.get(macPacket.getDestAddr()).contains(macPacket.getSeqNum()))) {

						retryCounter++;

						if(retryCounter == 1){

							cWindow = RF.aCWmin;

						}else if(cWindow < RF.aCWmax){
							cWindow = cWindow * 2;
						}else{
							cWindow = RF.aCWmax;
						}

						if(debug == FULL_DEBUG){
							output.println("Setting collision window to [0..." + cWindow + "]" );
						}

						if(retryCounter < RF.dot11RetryLimit){
							if(randomSlots){
								Random rand = new Random();
								ourSlot = rand.nextInt(cWindow);
							}else{
								ourSlot = cWindow;
							}
							macState = BUSY_WAIT;
							macPacket.setRetry(true);

							if(debug == FULL_DEBUG){
								output.println("Moving to BUSY_WAIT after ACK timeout");
							}

						}	

					}else{

						if(macPacket.getFrameType() == 2){


							retryCounter = 0;
							needNextPacket = true;

							if(debug == FULL_DEBUG){
								output.println("Moving to DEFAULT_WAIT.");
							}

							macState = DEFAULT_WAIT;
						}
						else{


							retryCounter = 0;
							needNextPacket = true;

							if(debug == FULL_DEBUG){
								output.println("Moving to DEFAULT_WAIT.");
							}

							macState = DEFAULT_WAIT;

						}
					}

					if (retryCounter == RF.dot11RetryLimit){					
						currentStatus = TX_FAILED;
					}
					else{
						currentStatus = TX_DELIVERED;
					}

					break;
				case BUSY_WAIT:	//3
					if(debug == FULL_DEBUG){
						output.println("Moved to BUSY_WAIT.");
						output.println("Waiting for DIFS to elapse after current Tx...");
					}

					waitUntil(nearestWait(DIFS));
					if(debug == FULL_DEBUG){
						output.println("DIFS wait is over, starting slot countdown (" + ourSlot + ")");
					}

					macState = SLOT_WAIT;
					break;
				case SLOT_WAIT:	//4
					if(debug == FULL_DEBUG){
						output.println("Moved to SLOT_WAIT.");
					}
					//Look at current time, align to slots. while our slot is not the current slot, loop through and wait a slot time each time. 
					//Reduce our slot by 1 each time, until our slot is the current slot.
					if(macPacket.getFrameType() == 0){

						//output.println("True Clock: " + theRF.clock() + " Rounded Clock: " +roundUp(theRF.clock()));
						waitUntil(roundUp(theRF.clock()));

						//Wait for our slot
						waitUntil(theRF.clock() + (ourSlot *SLOT));
						if(debug == FULL_DEBUG){
							output.println("Slot waited until " + theRF.clock());
						}

						if(theRF.inUse()){

							if(debug == FULL_DEBUG){
								output.println("RF is in use");
							}

							macState = BUSY_WAIT;
						}else{



							if(macPacket.getFrameType() == 2){


								if(beaconDebug == true){

									ByteBuffer buf = ByteBuffer.allocate(8);
									buf = ByteBuffer.wrap(macPacket.getData());
									long timeStamp = buf.getLong();
									output.println("Sending BEACON at: "+ theRF.clock()+ " built at " + timeStamp);
								}
							}

							theRF.transmit(macPacket.getFrame());
							if (debug == FULL_DEBUG) {
								output.println("Transmitting packet after waiting DIFS + SLOT(s) wait at " + theRF.clock());
							}

							if(macPacket.getFrameType() == 2){
								if(macPacket.getDestAddr() == -1){
									// Broadcast packet. Don't need to look for an ACK

									if(debug == FULL_DEBUG){
										output.println("Moving to DEFAULT_WAIT.");
									}

									needNextPacket = true;
									macState = DEFAULT_WAIT;
								}
								else{
									macState = WAIT_ACK;
								}

							}
							else{

								if(macPacket.getDestAddr() == -1){
									// Broadcast packet. Don't need to look for an ACK

									if(debug == FULL_DEBUG){
										output.println("Moving to DEFAULT_WAIT.");
									}

									needNextPacket = true;
									macState = DEFAULT_WAIT;
								}
								else{
									macState = WAIT_ACK;
								}
							}
						}
					}
					else{
						//Beacon Stuff

					}




					break;
				case IDLE_WAIT:	//5
					if(debug == FULL_DEBUG){
						output.println("Moved to IDLE_WAIT.");
					}

					waitUntil(nearestWait(DIFS));
					if(debug == FULL_DEBUG){
						output.println("Transmitting packet after DIFS wait at " + theRF.clock());
					}

					if( macPacket.getFrameType() == 2){


						if(beaconDebug == true){

							ByteBuffer buf = ByteBuffer.allocate(8);
							buf = ByteBuffer.wrap(macPacket.getData());
							long timeStamp = buf.getLong();
							output.println("Sending BEACON at: "+ theRF.clock()+ " built at " + timeStamp);
						}

					}

					theRF.transmit(macPacket.getFrame());

					if(macPacket.getFrameType() == 2){


						if(macPacket.getDestAddr() == -1){
							needNextPacket = true;

							if(debug == FULL_DEBUG){
								output.println("Moving to DEFAULT_WAIT.");
							}

							macState = DEFAULT_WAIT;
						}else{
							macState = WAIT_ACK;	
						}

					}
					else{
						if(macPacket.getDestAddr() == -1){
							needNextPacket = true;

							if(debug == FULL_DEBUG){
								output.println("Moving to DEFAULT_WAIT.");
							}

							macState = DEFAULT_WAIT;
						}else{
							macState = WAIT_ACK;	
						}
					}

					break;

				default:
					currentStatus = UNSPECIFIED_ERROR;
				}
			}
		}
	}

	class Receiver implements Runnable { // Handles receiving functions for the LinkLayer

		private RF theRF;
		private LinkLayer theLinkLayer;

		public Receiver(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;

			if(theRF == null){
				currentStatus = RF_INIT_FAILED;
			}

			if(thisLink == null){
				currentStatus = ILLEGAL_ARGUMENT;
			}

			theLinkLayer = thisLink;
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(10); // Sleeps each time through, in order to not monopolize the CPU
				} catch (InterruptedException e) {
					currentStatus = UNSPECIFIED_ERROR;
					e.printStackTrace();
				}

				Packet recvPacket = new Packet(theRF.receive()); // Gets data from the RF layer, turns it into packet form

				if(debug == FULL_DEBUG && recvPacket.getFrameType() != 1){
					output.println("Tx starting from " + recvPacket.getSrcAddr() + " at local time " + theRF.clock() );
				}

				short destAddr = recvPacket.getDestAddr();

				if(recvPacket.isGoodCRC()){

					if(debug == FULL_DEBUG){
						output.println("\tReceived packet with good CRC: " + recvPacket.toString());
					}

					if ((destAddr & 0xffff) == ourMAC || (destAddr & 0xffff) == 65535) {

						if ((destAddr & 0xffff) == ourMAC
								&& recvPacket.getFrameType() == 0
								&& theLinkLayer.getIn().size() < QUEUE_SIZE) {

							short nextSeq = gotRecvSeqNum(recvPacket.getSrcAddr());
							if (recvPacket.getSeqNum() > nextSeq) {

								output.println("Sequence out of order. Expected: "
										+ nextSeq + ". Received: "
										+ recvPacket.getSeqNum() + ".");
							}
							try {
								theLinkLayer.getIn().put(recvPacket); // Puts them new Packet into the LinkLayer's inbound queue
							} catch (InterruptedException e) {
								currentStatus = UNSPECIFIED_ERROR;
								e.printStackTrace();
							}

							Packet ack = new Packet(1, recvPacket.getSeqNum(),
									recvPacket.getSrcAddr(), ourMAC, null);

							try {
								Thread.sleep(RF.aSIFSTime); // Sleeps to wait SIFS
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if (debug == FULL_DEBUG) {
								output.println("Sending ACK with sequence number "
										+ ack.getSeqNum() + " to MAC address "
										+ ack.getDestAddr());
							}

							theRF.transmit(ack.getFrame());
						} 
						else if ((destAddr & 0xffff) == ourMAC && recvPacket.getFrameType() == 1) {
							output.println("Got a valid ACK: " + recvPacket.toString());

							if (theLinkLayer.recievedACKS.containsKey(recvPacket.getSrcAddr())) {

								if (theLinkLayer.recievedACKS.get(recvPacket.getSrcAddr()).contains(recvPacket.getSeqNum())) {
									output.println("Received duplicate ACK for sequence number "+ recvPacket.getSeqNum() + " from " + recvPacket.getSrcAddr());
								} else {

									theLinkLayer.recievedACKS.get(recvPacket.getSrcAddr()).add(recvPacket.getSeqNum());
								}

							} else {
								ArrayList<Short> newHost = new ArrayList<Short>();
								newHost.add(recvPacket.getSeqNum());
								theLinkLayer.recievedACKS.put(
										recvPacket.getSrcAddr(), newHost);
							}
						}
						else if(recvPacket.getFrameType() == 2){
							
						}
						else{
							output.println("Unexpected frame type");
							currentStatus = ILLEGAL_ARGUMENT; //Frame type was not 0,1, or 2
						}
					}
				}else{
					if(debug == FULL_DEBUG){
						output.println("\tIgnored packet with bad CRC: " + recvPacket.toString());
					}
				}
			}
		}
	}
}
