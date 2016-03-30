package org.pitt.server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
/**
 * @UDP CC send UDP Unicast info to HPi and receive  response from HPi
 */
public class UdpUnicastSocketClient {
	public static void main(String[] args) {
		try {
			 for(int i=0;i<10;i++){//while (true) {
				new Thread(new ClientImpl()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
/**
 * @thread create own udp connect, dynamic port,send one package then receive response from server
 */
class ClientImpl implements Runnable{
	private Random random = new Random();
	private String uuid = UUID.randomUUID().toString();
	public void run() {
		try {
			init();
			byte[] buffer = new byte[1024 * 64]; 
			// send random data
			String formantdata="23;1;0;199;01;1,2,3,4,5:6";
			byte[] btSend =formantdata.getBytes("UTF-8");//new byte[]{(byte)random.nextInt(127), (byte)random.nextInt(127), (byte)random.nextInt(127)};
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("130.49.219.87"), 7385);
			packet.setData(btSend);
			System.out.println(uuid + ":send:" + Arrays.toString(btSend));
			try {
				sendDate(packet);
			} catch(Exception e){
				e.printStackTrace();
			}
			receive(packet);
			byte[] bt = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
			if(null != bt && bt.length > 0){
				System.out.println(uuid + ":receive:" + Arrays.toString(bt));
			}
			Thread.sleep(1 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * receive package, this means can cause thread block
	 * @return
	 * @throws IOException
	 */
	public void receive(DatagramPacket packet) throws Exception {
		try {
			datagramSocket.receive(packet);
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * send package to designated destination
	 * @param bt
	 * @throws IOException
	 */
	public void sendDate(DatagramPacket packet) {
		try {
			datagramSocket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * initialization client connection
	 * @throws SocketException
	 */
	public void init() throws SocketException{
		try {
			datagramSocket = new DatagramSocket(random.nextInt(9999));
			datagramSocket.setSoTimeout(10 * 1000);
			System.out.println("Client start success");
		} catch (Exception e) {
			datagramSocket = null;
			System.out.println("Client start fail");
			e.printStackTrace();
		}
	}
	private DatagramSocket datagramSocket = null; 
}