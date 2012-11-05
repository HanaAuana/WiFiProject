package wifi;
import java.io.PrintWriter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


import rf.RF;

/**
 * Use this layer as a starting point for your project code.  See {@link Dot11Interface} for more
 * details on these routines.
 * @author 
 */





public class LinkLayer implements Dot11Interface, Runnable {
	private RF theRF;           // You'll need one of these eventually
	private short ourMAC;       // Our MAC address
	private PrintWriter output; // The output stream we'll write to


	private BlockingQueue<Packet> in = new ArrayBlockingQueue(4);
	private BlockingQueue<Packet> out = new ArrayBlockingQueue(4);

	public BlockingQueue<Packet> getIn() {
		return in;
	}

	public BlockingQueue<Packet> getOut() {
		return out;
	}




	/**
	 * Constructor takes a MAC address and the PrintWriter to which our output will
	 * be written.
	 * @param ourMAC  MAC address
	 * @param output  Output stream associated with GUI
	 */
	public LinkLayer(short ourMAC, PrintWriter output) {
		this.ourMAC = ourMAC;
		this.output = output;      
		theRF = new RF(null, null);
		output.println("LinkLayer: Constructor ran.");
	}



	public void run() {

		Receiver theReceiver = new Receiver(this,theRF);
		Sender theSender = new Sender(this,theRF);
		
		new Thread(theReceiver).start();
		new Thread(theSender).start();

		while(true){



		}


	}




	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending "+len+" bytes to "+dest);

		byte[] fakeCRC = new byte[4];

		fakeCRC[0] = 8;
		fakeCRC[1] = 8;
		fakeCRC[2] = 8;
		fakeCRC[3] = 8;



		Packet p = new Packet(0, (short)0, dest, ourMAC, data, fakeCRC);

		try {
			out.put(p);
			theRF.transmit(data);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return len;
	}

	/**
	 * Recv method blocks until data arrives, then writes it an address info into
	 * the Transmission object.  See docs for full description.
	 */
	public int recv(Transmission t) {
		output.println("LinkLayer: Pretending to block on recv()");
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Packet p;
		try {
			p = in.take();
			byte[] data = p.getData();
		    t.setSourceAddr((short) p.getSrcAddr());
		    t.setDestAddr((short) p.getDestAddr());
		    t.setBuf(data);
		    return data.length;
		    
		} catch (InterruptedException e) {	
			e.printStackTrace();
		}
		return -1;
	      
	}

	/**
	 * Returns a current status code.  See docs for full description.
	 */
	public int status() {
		output.println("LinkLayer: Faking a status() return value of 0");
		return 0;
	}

	/**
	 * Passes command info to your link layer.  See docs for full description.
	 */
	public int command(int cmd, int val) {
		switch(cmd){
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
		output.println("LinkLayer: Sending command "+cmd+" with value "+val);
		return 0;
	}


	class Sender implements Runnable {

		private RF theRF;
		private LinkLayer theLinkLayer;

		public Sender(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;
			theLinkLayer = thisLink;
		}

		public void run() {


		}

	}

	class Receiver implements Runnable {

		private RF theRF;
		private LinkLayer theLinkLayer;

		public Receiver(LinkLayer thisLink, RF thisRF) {

			theRF = thisRF;
			theLinkLayer = thisLink;

		}

		public void run() {

			while (true)
			{

				Packet p = new Packet(theRF.receive());

				try {
					theLinkLayer.getIn().put(p);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				




			}

		}

	}


}
