package wifi;

import java.io.PrintWriter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

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

	private static final int QUEUE_SIZE = 10;

	private BlockingQueue<Packet> in = new ArrayBlockingQueue(QUEUE_SIZE);
	private BlockingQueue<Packet> out = new ArrayBlockingQueue(QUEUE_SIZE);

	public BlockingQueue<Packet> getIn() { //These Queues will facilitate communication between the LinkLayer and its Sender and Receiver helper classes
		return in;
	}

	public BlockingQueue<Packet> getOut() {
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
		output.println("LinkLayer: Constructor ran.");

		Receiver theReceiver = new Receiver(this, theRF); //Creates the sender and receiver instances
		Sender theSender = new Sender(this, theRF);

		Thread r = new Thread(theReceiver); //Threads them
		Thread s = new Thread(theSender);

		r.start(); //Starts the threads running
		s.start();
	}

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send. See docs for full description.
	 */
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending " + len + " bytes to " + dest);

		byte[] fakeCRC = new byte[4]; //Actual CRC stuff not implemented yet

		fakeCRC[0] = 15;
		fakeCRC[1] = 15;
		fakeCRC[2] = 15;
		fakeCRC[3] = 15;

		Packet p = new Packet(0, (short) 0, dest, ourMAC, data, fakeCRC); //Builds a packet using the supplied data
                                                                          //Some parts of the packet are fake for now
		try {
			out.put(p); //Puts the created packet into the outgoing queue
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return len;
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info
	 * into the Transmission object. See docs for full description.
	 */
	public int recv(Transmission t) { //Called by the above layer when it wants to receive data

		

		Packet p; 
		try {
			p = in.take();  //Grabs the next packet from the incoming queue
			byte[] data = p.getData();  //Extracts the necessary parts from the packet and puts them into the supplied transmission object
			t.setSourceAddr((short) p.getSrcAddr());
			t.setDestAddr((short) p.getDestAddr());
			t.setBuf(data);
			return data.length; //Returns the length of the data recieved

		} catch (InterruptedException e) {
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
			output.println("Command 0: View all options and settings.");
			output.println("Command 1: Set debug value.");
			output.println("Command 2: Set slot for link layer.");
			output.println("Command 3: Set desired wait time between start of beacon transmissions (in seconds).");
			output.println("-----------------------------------------");
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			output.println("Command " + cmd + " not recognized.");
		}
		output.println("LinkLayer: Sending command " + cmd + " with value "
				+ val);
		return 0;
	}

	class Sender implements Runnable { //Handles sending functions for the LinkLayer

		private RF theRF;
		private LinkLayer theLinkLayer;

		public Sender(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;
			theLinkLayer = thisLink;
		}

		public void run() {

			while (true) {
				
				
				try {
					Thread.sleep(10); //Sleeps each time through, in order to not monopolize the CPU
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (theLinkLayer.getOut().isEmpty() == false && theRF.inUse() == false) { // If there are Packets to be  sent in the  LinkLayer's  outbound queue
					try {                                                                 //Also makes sure the RF is not in use
						theRF.transmit(theLinkLayer.getOut().take().getFrame()); // Send the first packet out on the RF layer
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		}

	}

	class Receiver implements Runnable { //Handles receiving functions for the LinkLayer

		private RF theRF;
		private LinkLayer theLinkLayer;

		public Receiver(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;
			theLinkLayer = thisLink;

		}

		public void run() {
			
			

			while (true) {
				
				try {
					Thread.sleep(10); //Sleeps each time through, in order to not monopolize the CPU
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Packet p = new Packet(theRF.receive()); // Gets data from the RF layer, turns it into packet form
				
				short destAddr = p.getDestAddr();
				
				if((destAddr&0xffff) == ourMAC || (destAddr&0xffff) == 65535){
					
					try {
						theLinkLayer.getIn().put(p); // Puts the new Packet into the LinkLayer's inbound queue
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if((destAddr&0xffff) == ourMAC){
						
						byte[] fakeCRC = new byte[4]; //Actual CRC stuff not implemented yet

						fakeCRC[0] = 15;
						fakeCRC[1] = 15;
						fakeCRC[2] = 15;
						fakeCRC[3] = 15;
						
						Packet ack = new Packet(1, p.getSeqNum(), p.getSrcAddr(), ourMAC, null, fakeCRC);
						
						try {
							Thread.sleep((long)0.01); //Sleeps to wait SIFS
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						theRF.transmit(ack.getFrame());
						
					}
					
				}
				else{
					
					output.println("Addr: "+ destAddr);
				}



			}

		}

	}

}
