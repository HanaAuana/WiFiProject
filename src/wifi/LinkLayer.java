package wifi;
import java.io.PrintWriter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;


import rf.RF;

/**
 * Use this layer as a starting point for your project code.  See {@link Dot11Interface} for more
 * details on these routines.
 * @author richards
 */
public class LinkLayer implements Dot11Interface {
	private RF theRF;           // You'll need one of these eventually
	private short ourMAC;       // Our MAC address
	private PrintWriter output; // The output stream we'll write to
	
	
	private BlockingQueue<Packet> in = new ArrayBlockingQueue(4);
	private BlockingQueue<Packet> out = new ArrayBlockingQueue(4);

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

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending "+len+" bytes to "+dest);
		Packet p = new Packet(data);
		
		try {
			out.put(p);
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
		while(true); // <--- This is a REALLY bad way to wait.  Sleep a little each time through.
		//return 0;
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
}
