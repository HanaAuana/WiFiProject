package wifi;

public class Main {

	public static void main(String[] args){
		
		testPacketInOut_C2();
	}
	
	public static void testPacketInOut_C1(){
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
		System.out.println(test.toString());
	}
	
	public static void testPacketInOut_C2(){
		int type = 7;
		short seq = 1;
		short dest = 1;
		short src = 1;
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
		
		Packet test_1 = new Packet(type, seq, dest, src, data, crc);
		byte[] fullframe = test_1.getFrame();
		Packet test_2 = new Packet(fullframe);

		System.out.println("Data Out: ");
		System.out.println("\t Type: " + test_2.getFrameType());
		test_2.setRetry(true);
		System.out.println("\t Retry: " + test_2.isRetry());
		System.out.println("\t SeqNum: " + test_2.getSeqNum());
		System.out.println("\t DestAddr: " + test_2.getDestAddr());
		System.out.println("\t SrcAddr: " + test_2.getSrcAddr());
		System.out.print("\t Data: ");
		for(int i = 0;i <test_2.getData().length; i++){
			System.out.print(test_2.getData()[i] + " ");
		}
		System.out.println();
		System.out.print("\t Crc: ");
		for(int i = 0;i <test_2.getCrc().length; i++){
			System.out.print(test_2.getCrc()[i] + " ");
		}
		System.out.println();
		System.out.print("\t Full Frame: ");
		System.out.println(test_2.toString());
	}
}
