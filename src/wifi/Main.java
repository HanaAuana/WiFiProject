package wifi;

public class Main {

	public static void main(String[] args){
		
		testPacketInOut();
	}
	
	public static void testPacketInOut(){
		int type = 7;
		short seq = 1;
		short dest = 534;
		short src = 256;
		int retry = 1;
		byte[] data = {1, 1, 0, 1};
		byte[] crc = {1,2,3,1};
		System.out.println("Data In:");
		System.out.println("\t Type:" + type);
		System.out.println("\t Retry:" + retry);
		System.out.println("\t SeqNum:" + seq);
		System.out.println("\t DestAddr:" + dest);
		System.out.println("\t SrcAddr:" + src);
		System.out.print("\t Data: ");
		for(int i = 0;i <data.length; i++){
			System.out.print(data[i] + " ");
		}
		System.out.println();
		System.out.print("\t Crc: ");
		for(int i = 0;i <crc.length; i++){
			System.out.print(crc[i] + " ");
		}
		System.out.println();
		
		Packet test = new Packet(type, seq, dest, src, data, crc);
		
		System.out.println("Data Out: ");
		System.out.println("\t Type: " + test.getFrameType());
		test.setRetry(true);
		System.out.println("\t Retry: " + test.isRetry());
		System.out.println("\t SeqNum: " + test.getSeqNum());
		System.out.println("\t DestAddr: " + test.getDestAddr());
		System.out.println("\t SrcAddr: " + test.getSrcAddr());
		System.out.print("\t Data: ");
		for(int i = 0;i <test.getData().length; i++){
			System.out.print(test.getData()[i] + " ");
		}
		System.out.println();
		System.out.print("\t Crc: ");
		for(int i = 0;i <test.getCrc().length; i++){
			System.out.print(test.getCrc()[i] + " ");
		}
		System.out.println();
		System.out.print("\t Full Frame: ");
		for(int i = 0;i <test.getFrame().length; i++){
			System.out.print(test.getFrame()[i] + " ");
		}
		System.out.println();
	}
}
