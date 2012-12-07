package wifi;

import java.io.PrintWriter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	public static final int SIFS = RF.aSIFSTime;
	public static final int SLOT = RF.aSlotTime;
	public static final int DIFS = RF.aSIFSTime + (2*RF.aSlotTime);
	
	private int cWindow = RF.aCWmin;

	private static final int QUEUE_SIZE = 4;
	private static final int FULL_DEBUG = -1;

	private int debug = FULL_DEBUG; // SET TO 0 BEFORE TURNING IN!

	private boolean randomSlots = true;

	private int beaconDelay = 5;

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
				output.println("Queueing  " + p.getFrame().length + " bytes for " + dest);
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
				byte[] data = p.getData(); 	// Extracts the necessary parts from the packet and puts them into the
											// supplied transmission object
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

	public long roundUp(long input){
		return (long) Math.ceil((input % 50)/ 50.0);
	}
	
	private void waitUntil(long time){
		long cTime = theRF.clock();
		while(cTime < time){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private long nearestWait(long waitTime){
		
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
				if (theLinkLayer.getOut().isEmpty() == false && needNextPacket == true){
					//System.err.println("getting next packet");
					try {
						macPacket = theLinkLayer.getOut().take();
					} catch (InterruptedException e) {
						currentStatus = UNSPECIFIED_ERROR;
						e.printStackTrace();
					}
					//System.err.println("got packet: "+ macPacket.toString());
					needNextPacket = false;
				}
				
				switch(macState){
				
				
				
				case DEFAULT_WAIT:	//1
//					if(debug == FULL_DEBUG){
//						output.println("Moved to DEFAULT_WAIT.");
//					}
					
					if(!theLinkLayer.getOut().isEmpty()){
						if(!theRF.inUse() ){
							macState = IDLE_WAIT;
						}else{
							macState = BUSY_WAIT;
						}
					}
					break;
				case WAIT_ACK:	//2
					if(debug == FULL_DEBUG){
						output.println("Moved to WAIT_ACK.");
					}


					try {
						Thread.sleep((long) (2615+SIFS+SLOT)); //TODO add slop
					} catch (InterruptedException e) {
						currentStatus = UNSPECIFIED_ERROR;
						e.printStackTrace();
					}

					int counter = 0;

					while (counter < RF.dot11RetryLimit
							&& (theLinkLayer.recievedACKS.containsKey(macPacket.getDestAddr())
							&& theLinkLayer.recievedACKS.get(macPacket.getDestAddr()).contains(macPacket.getSeqNum()) == false)) {

						Packet retryPacket = new Packet(macPacket.getFrameType(), macPacket.getSeqNum(), macPacket.getDestAddr(), macPacket.getSrcAddr(),macPacket.getData());
						retryPacket.setRetry(true);

						if (debug == FULL_DEBUG) {
							output.println("Resending packet with sequence "
									+ retryPacket.getSeqNum()
									+ ". Attempt number: " + counter);
						}

						// output.println("RESENDING PACKET: "+ retryPacket.getSeqNum()+" Attempt number: "+ counter);
						theRF.transmit(retryPacket.getFrame()); // Send the first packet out on the RF layer
						
						if (debug == FULL_DEBUG) {
							output.println("Resending packet with sequence "+ retryPacket.getSeqNum()+ ". Attempt number: " + counter);
						}

						try {                                                     //TODO still need to round to nearest 50 ms
							Thread.sleep((long) RF.aSIFSTime + (2*RF.aSlotTime)); //Waiting DIFS after send 
						} catch (InterruptedException e) {
							currentStatus = UNSPECIFIED_ERROR;
							e.printStackTrace();
						}

						counter++;
					}
					if (counter == RF.dot11RetryLimit){
						currentStatus = TX_FAILED;
						macState = BUSY_WAIT;
					}
					else{
						currentStatus = TX_DELIVERED;
						needNextPacket = true;
						macState = DEFAULT_WAIT;
					}
					
					break;
				case BUSY_WAIT:	//3
					if(debug == FULL_DEBUG){
						output.println("Moved to BUSY_WAIT.");
					}
					//EXP BACKOFF
					
					macState = SLOT_WAIT;
					break;
				case SLOT_WAIT:	//4
					if(debug == FULL_DEBUG){
						output.println("Moved to SLOT_WAIT.");
					}
					//Look at current time, align to slots. while our slot is not the current slot, loop through and wait a slot time each time. 
					//Reduce our slot by 1 each time, until our slot is the current slot.
					if(macPacket.getFrameType() == 0){
						
						theRF.transmit(macPacket.getFrame());
						if (debug == FULL_DEBUG) {
							output.println("Sent packet with sequence number "
									+ macPacket.getSeqNum() + " to MAC address "
									+ macPacket.getDestAddr());
						}
					}
					else{
						
						
					}
					
					if(macPacket.getSrcAddr() == -1){
						// Broadcast packet, dont need to look for an ack
						needNextPacket = true;
						macState = DEFAULT_WAIT;
					}
					else{
						macState = WAIT_ACK;
					}
					
					
					break;
				case IDLE_WAIT:	//5
					if(debug == FULL_DEBUG){
						output.println("Moved to IDLE_WAIT.");
					}
					
					waitUntil(nearestWait(DIFS));
					if(debug == FULL_DEBUG){
						output.println("DIFS wait is over. Starting slot countdown");
					}
					macState = SLOT_WAIT;
					break;

				default:
					currentStatus = UNSPECIFIED_ERROR;
				}
				
//				try {
//					// Dont forget about exponential backoff!
//					Thread.sleep(10); // Sleeps each time through, in order to not monopolize the CPU
//				} catch (InterruptedException e) {
//					currentStatus = UNSPECIFIED_ERROR;
//					e.printStackTrace();
//				}
//
//				if (theLinkLayer.getOut().isEmpty() == false && theRF.inUse() == false) { // If there are Packets to be sent in the LinkLayer's outbound queue
//					                                                                      // Also makes sure the RF is not in use
//
//					//Packet p = null;
//
//					try {
//						p = theLinkLayer.getOut().take();
//					} catch (InterruptedException e) {
//						currentStatus = UNSPECIFIED_ERROR;
//						e.printStackTrace();
//					}
//
//					theRF.transmit(p.getFrame()); // Send the first packet out on the RF layer
//					// output.println("SENT PACKET with SEQ NUM: "+ p.getSeqNum());
//
//					if (debug == FULL_DEBUG) {
//						output.println("Sent packet with sequence number "
//								+ p.getSeqNum() + " to MAC address "
//								+ p.getDestAddr());
//					}
//					
//					
//					//ACK timeout should be around 2615 ms + whatever from the IEEE spec
//
//					try {
//						Thread.sleep((long) 1000);
//					} catch (InterruptedException e) {
//						currentStatus = UNSPECIFIED_ERROR;
//						e.printStackTrace();
//					}
//
//					int counter = 0;
//
//					while (counter < RF.dot11RetryLimit
//							&& (theLinkLayer.recievedACKS.containsKey(p.getDestAddr())
//							&& theLinkLayer.recievedACKS.get(p.getDestAddr()).contains(p.getSeqNum()) == false)) {
//
//						Packet retryPacket = new Packet(p.getFrameType(),
//								p.getSeqNum(), p.getDestAddr(), p.getSrcAddr(),
//								p.getData());
//						retryPacket.setRetry(true);
//
//						if (debug == FULL_DEBUG) {
//							output.println("Resending packet with sequence "
//									+ retryPacket.getSeqNum()
//									+ ". Attempt number: " + counter);
//						}
//
//						// output.println("RESENDING PACKET: "+ retryPacket.getSeqNum()+" Attempt number: "+ counter);
//						theRF.transmit(retryPacket.getFrame()); // Send the first packet out on the RF layer
//
//						try {                                                     //TODO still need to round to nearest 50 ms
//							Thread.sleep((long) RF.aSIFSTime + (2*RF.aSlotTime)); //Waiting DIFS after send 
//						} catch (InterruptedException e) {
//							currentStatus = UNSPECIFIED_ERROR;
//							e.printStackTrace();
//						}
//
//						counter++;
//					}
//					if (counter == RF.dot11RetryLimit){
//						currentStatus = TX_FAILED;
//					}
//					else{
//						currentStatus = TX_DELIVERED;
//					}
//				}
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
				
				if(debug == FULL_DEBUG && recvPacket.getFrameType() == 0){
					output.println("Tx starting from " + recvPacket.getSrcAddr() + " at local time " + theRF.clock() );
				}
				
				short destAddr = recvPacket.getDestAddr();

				if ((destAddr & 0xffff) == ourMAC || (destAddr & 0xffff) == 65535) {
					// output.println("Packet for us: "+ recvPacket.getSeqNum());

					if (debug == FULL_DEBUG) {
						output.println("Packet for us arrived from "
								+ recvPacket.getSrcAddr()
								+ " with sequence number "
								+ recvPacket.getSeqNum());
					}

					if ((destAddr & 0xffff) == ourMAC
							&& recvPacket.getFrameType() == 0
							&& theLinkLayer.getIn().size() < QUEUE_SIZE) {

						short nextSeq = gotRecvSeqNum(recvPacket.getSrcAddr());
						if (recvPacket.getSeqNum() > nextSeq) {

							output.println("Sequence out of order, expected: "
									+ nextSeq + " got: "
									+ recvPacket.getSeqNum());
						}
						try {
							theLinkLayer.getIn().put(recvPacket); // Puts them new Packet into the LinkLayer's inbound queue
						} catch (InterruptedException e) {
							currentStatus = UNSPECIFIED_ERROR;
							e.printStackTrace();
						}

						output.println("Got: " + recvPacket.getSeqNum()
								+ " next is " + nextSeq);

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
						// output.println("Sent an ACK: " + ack.getSeqNum());
					} else if ((destAddr & 0xffff) == ourMAC
							&& recvPacket.getFrameType() == 1) {
						// output.println("Saw an ACK: " + recvPacket.getSeqNum());

						if (theLinkLayer.recievedACKS.containsKey(recvPacket.getSrcAddr())) {

							if (theLinkLayer.recievedACKS.get(
									recvPacket.getSrcAddr()).contains(
									recvPacket.getSeqNum())) {
								// output.println("Already got this ACK: "+ recvPacket.getSeqNum());
							} else {

								theLinkLayer.recievedACKS.get(recvPacket.getSrcAddr()).add(recvPacket.getSeqNum());
								// output.println("Added an ACK for "+ recvPacket.getSeqNum()+ " from "+recvPacket.getSrcAddr());
							}

						} else {
							ArrayList<Short> newHost = new ArrayList<Short>();
							newHost.add(recvPacket.getSeqNum());
							theLinkLayer.recievedACKS.put(
									recvPacket.getSrcAddr(), newHost);
							// output.println("Added an ACK for "+ recvPacket.getSeqNum()+ " from "+recvPacket.getSrcAddr());
						}

					} else {
						// output.println("Saw a packet of type: "+ recvPacket.getFrameType() + " from address "+ (destAddr&0xffff));
					}
				} else {

				}
			}
		}
	}
}
