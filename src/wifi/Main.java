package wifi;

public class Main {

	public static void main(String[] args){
		
		//testPacketInOut_C1();
		//testPacketInOut_C2();
		//bitShift();
		//testPacket_3();
		testRound();
	}
	
	public static void testRound(){
		long test = 750;
		System.out.println(test + (50L - test % 50L));
	}
	
	public static void testPacketInOut_C1(){
		int type = 7;
		short seq = 9;
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
		
		Packet test = new Packet(type, seq, dest, src, data);
		//test.setRetry(true);
		
		System.out.println("Data Out: ");
		System.out.println("\t Type: " + test.getFrameType());
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
//		for(int i = 0;i <test.getCrc().length; i++){
//			System.out.print(test.getCrc()[i] + " ");
//		}
		System.out.println();
		System.out.print("\t Full Frame: ");
		System.out.println(test.toString());
	}
	
	public static void testPacketInOut_C2(){
		int type = 0;
		short seq = 0;
		short dest = (short)30000;
		short src = (short)0;
		int retry = 0;
		byte[] data = {0, 0, 0, 0};
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
//		for(int i = 0;i <crc.length; i++){
//			System.out.print(crc[i] + " ");
//		}
		System.out.println();
		Packet test_1 = new Packet(type, seq, dest, src, data);
		//test_1.setRetry(true);
		
		byte[] fullframe = test_1.getFrame();
		
		System.out.println("Packet Two");
		Packet test_2 = new Packet(fullframe);
		
		System.out.println("Data Out: ");
		System.out.println("\t Type: " + test_2.getFrameType());
		System.out.println("\t Retry: " + test_2.isRetry());
		System.out.println("\t SeqNum: " + test_2.getSeqNum());
		System.out.println("\t DestAddr: " + test_2.getDestAddr());
		System.out.println("\t SrcAddr: " + test_2.getSrcAddr());
		System.out.print("\t Data: ");
		for(int i = 0;i <test_2.getData().length; i++){
			System.out.print(test_2.getData()[i] + " ");
		}
		System.out.println();
		System.out.print("\t CRC: " + test_2.getCRC());
		System.out.println();
		System.out.print("\t Full Frame: ");
		System.out.println(test_2.toString());
	}
	
	public static void bitShift(){
		byte[] seq = new byte[2];
		seq[0]=-1;
		seq[1]=7;
		System.out.println(seq[0] +" " + seq[1]);
		System.out.println((seq[0]<<8 | seq[1]) & 0xFFF);
		System.out.println(((seq[0]<<12)>>>4) | seq[1]);
	}
	
	public static void testPacket_3(){
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 1;
		data[2] = 1;
		data[3] = 1;
		data[4] = 1;
		Packet test = new Packet(3, (short)1792, (short)(10), (short)(10), data);
		System.out.println(test.toString());
		
		
		//---------------Data Testing-----------------------
//		for(int i = 0;i <test.getData().length; i++){
//			System.out.print(test.getData()[i] + " ");
//		}
//		System.out.println();
		//---------------SeqNum Testing-----------------------
//		System.out.println("Frame[0]: " + test.getFrame()[0]);
//		System.out.println("SeqNum: " + test.getSeqNum());
		
		//---------------Retry Testing-----------------------
//		System.out.println("Frame[0]: " + test.getFrame()[0]);
//		System.out.println("Retry: " + test.isRetry());
//		test.setRetry(true);
//		System.out.println("Frame[0]: " + test.getFrame()[0]);
//		System.out.println("Retry: " + test.isRetry());
//		test.setRetry(false);
//		System.out.println("Frame[0]: " + test.getFrame()[0]);
//		System.out.println("Retry: " + test.isRetry());
		
		
		//---------------Frame Testing-----------------------
		System.out.println("Frame[0]: " + test.getFrame()[0]);
		System.out.println("getFrameType(): " + test.getFrameType());
		
		test.setFrameType(1);
		System.out.println("Frame[0]: " + test.getFrame()[0]);
		System.out.println("getFrameType(): " + test.getFrameType());
	}
}
