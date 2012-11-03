package wifi;

import java.nio.ByteBuffer;

/**
 * @author Alexander  King & Michael Lim
 * 
 * The Packet class wraps all neccesary information that is required for 
 * a transmission for the 802.11~ spec.
 * 
 */

public class Packet {

	//byte[] frame;
	
	int frameType;
	int retry;
	int seqNum;
	int destAddr;
	int srcAddr;
	byte[] data;
	byte[] crc;
	ByteBuffer buf;
	
	public Packet(byte[] frame){
		
	}
	
	public Packet(int frameType, boolean retry, int seqNum, int destAddr, int srcAddr, byte[] data, byte[] crc){
		
		buf = ByteBuffer.allocate(2048);
		
		//Check frameType
		if(frameType < 0 || frameType > 7){
			System.err.println("Error: Invalid frameType. Initialized to 0.");
			frameType = 0;
		}else{
			this.frameType = frameType;
		}
		
		if(retry == true){
			this.retry = 1;
		}else{
			this.retry = 0;
		}
		
		//Check seqNum
		if(seqNum < 0 || seqNum > 4095){
			System.err.println("Error: Invalid seqNum. Initialized to 0.");
			seqNum = 0;
		}else{
			this.seqNum = seqNum;
		}
		
		//Check destAddr
		if(destAddr < 0 || destAddr > 65535){
			System.err.println("Error: Invalid destAddr. Initialized to 0.");
			destAddr = 0;
		}else{
			this.destAddr = destAddr;
		}
		
		//Check srcAddr
		if(srcAddr < 0 || srcAddr > 65535){
			System.err.println("Error: Invalid srcAddr. Initialized to 0.");
			srcAddr = 0;
		}else{
			this.srcAddr = srcAddr;
		}
		
		//Check data
		if(data == null || data.length != 2038){
			System.err.println("Error: Invalid data. Initialized to empty byte[2038].");
			data = new byte[2038];
		}else{
			this.data = data;
		}
		
		//Check CRC
		if(crc == null ||crc.length != 4){
			System.err.println("Error: Invalid CRC. Initialized to empty byte[4].");
			crc = new byte[4];
		}else{
			this.crc = crc;
		}
		
		//build control byte
		byte control = makeControl(frameType,this.retry, seqNum);
		
		//fill Packet bytes
		buf.put(0, control); //put control bytes
		buf.putInt(2, destAddr); // put destAddr bytes
		buf.putInt(4, srcAddr); // put srcAddr bytes
		for(int i=0;i<data.length;i++){ //put data bytes
			buf.put(i+5,data[i]);
		}
		for(int i=0;i<crc.length;i++){ //put crc bytes
			buf.put(i+2044,crc[i]);
		}
	}
	
	private byte makeControl(int frameType, int retry, int seqNum){
		int temp = 0;
		temp = (frameType << 13) | (retry << 12) | seqNum;
		//Test Shifting
		//System.out.println("frameType: " + (frameType << 5));
		//System.out.println("retry: " + retry);
		//System.out.println("seqNum: " + seqNum);
		//System.out.println("byte: " + temp);
		return (byte)temp;
	}
	
	public int getFrameType(){
		return frameType;
	}
	
	public boolean isRetry(){
		if(retry == 1){
			return true;
		}
		else{
		return false;
		}
	}
	
	public int getSeqNum(){
		return seqNum;
	}
	
	public int getDestAddr(){
		return destAddr;
	}
	
	public int getSrcAddr(){
		return srcAddr;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public byte[] getCrc(){
		return crc;
	}
	
	public byte[] getFrame(){
		return buf.array();
	}
}
