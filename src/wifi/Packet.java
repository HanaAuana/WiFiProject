package wifi;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author Alexander  King & Michael Lim
 * 
 * The Packet class wraps all neccesary information that is required for 
 * a transmission for the 802.11~ spec.
 * 
 */

public class Packet {

	byte[] data;
	int crc;
	ByteBuffer buf;

	public Packet(byte[] frame){
		if(frame == null){
			throw new IllegalArgumentException("Invalid packet. Packet is null.");
		}else if(frame.length > 2038){
			throw new IllegalArgumentException("Invalid packet size. Packet too big!");
		} else if( frame.length < 1 ){
			throw new IllegalArgumentException("Invalid packet size. No packet data.");
		}

		buf = ByteBuffer.allocate(frame.length);
		buf = ByteBuffer.wrap(frame);
	}

	public Packet(int frameType, short seqNum, short destAddr, short srcAddr, byte[] data){

		if(data != null){
			buf = ByteBuffer.allocate(10 + data.length);
		}
		else{
			buf = ByteBuffer.allocate(10);
		}

		setData(data); //set data first to short circuit
		setFrameType(frameType);
		setRetry(false);
		setSeqNum(seqNum);
		setDestAddr(destAddr);
		setSrcAddr(srcAddr);
		setCRC();
	}

	public void setFrameType(int type){

		//Check frame type
		if(type < 0 || type > 7){
			throw new IllegalArgumentException("Invalid frameType.");
		}else{
			//Clear bits
			buf.put(0,(byte)(buf.get(0) & 0x1F));
			//Set bits
			buf.put(0,(byte)(buf.get(0) | (type << 5)));
		}
	}

	public int getFrameType(){
		return ((buf.get(0) & 0xE0) >>> 5);
	}

	public void setRetry(boolean input){

		//Clear bit
		buf.put(0,(byte)(buf.get(0) & 0xFFFFFFEF));
		if (input){
			//Set bit
			buf.put(0,(byte)(buf.get(0) | 0x10));
		}
	}

	public boolean isRetry(){
		if(((buf.get(0)>>4)& 0x1) == 1){
			return true;
		}
		else{
			return false;
		}
	}

	public void setSeqNum(short seq){
		//Check seqNum
		if(seq < 0 || seq > 4095){
			throw new IllegalArgumentException("Invalid seqNum.");
		}else{
			//Clear bits
			buf.put(0,(byte)(buf.get(0) & 0xF0));
			//Set bits
			buf.put(0,(byte)(buf.get(0) | (seq >>> 8)));
			buf.put(1,(byte)(seq & 0xFF));
		}
	}

	public short getSeqNum(){
		return (short)(buf.getShort(0) & 0xFFF);
	}

	public void setDestAddr(short addr){
		//Check destAddr
		if((addr&0xff) < 0 || (addr&0xff) > 65535){
			throw new IllegalArgumentException("Invalid destAddr.");
		}else{
			buf.putShort(2, addr);
		}
	}

	public short getDestAddr(){
		return buf.getShort(2);
	}

	public void setSrcAddr(short addr){
		//Check srcAddr
		if((addr&0xff) < 0 || (addr&0xff) > 65535){
			throw new IllegalArgumentException("Invalid srcAddr.");
		}else{
			buf.putShort(4, addr); // put srcAddr bytes
		}
	}

	public short getSrcAddr(){
		return buf.getShort(4);
	}

	public void setData(byte[] inData){
		if(inData != null){

			//Check data
			if(inData.length > 2038){
				throw new IllegalArgumentException("Invalid data.");
			}else{
				for(int i=0;i<inData.length;i++){ //put data bytes
					buf.put(i+6,inData[i]);
				}
			}
		}
	}

	public byte[] getData(){
		byte[] temp = new byte[buf.limit() - 10];
		for(int i=0;i<temp.length;i++){ //put data bytes
			temp[i]= buf.get(i+6);
		}
		return temp;
	}

	public void setCRC(){
		CRC32 crc32 = new CRC32();
		crc32.update(buf.array(),0,buf.limit()-4);
		int crc = (int)crc32.getValue();
		buf.putInt(buf.limit()-4, crc);
	}

	public int getCRC(){
		int crc = buf.getInt(buf.limit()-4);
		return crc;
	}
	
	public boolean isGoodCRC(){
	int test = getCRC();
	
	CRC32 crc32 = new CRC32();
	crc32.update(buf.array(),0,buf.limit()-4);
	int goodCRC = (int)crc32.getValue();
	 
	
	if(test == goodCRC){
		return true;
	}else{
		return false;
	}
	
	}

	public byte[] getFrame(){
		setCRC();
		return buf.array();
	}

	public String toString(){
		String type;
		switch(getFrameType()){
		case 0:
			type = "DATA";
			break;
		case 1:
			type = "ACK";
			break;
		case 2:
			type = "BEACON";
			break;
		case 4:
			type = "CTS";
			break;
		case 5:
			type = "RTS";
			break;
		default:
			type = "UNKNOWN";
		}
		String out = "<" + type + " " + getSeqNum() + " " + getSrcAddr() + "-->" +
				getDestAddr() + " [" + getData().length + " bytes] (" + getCRC() + ")>"  ;
		
		return out;
	}
}