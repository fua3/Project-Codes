package org.pitt.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress; 
import java.net.MulticastSocket;
/**
 * CC read UDP Multicast info from HPi 
 */
public class UDPMulticastSocketClient {

	public static void main(String[] args) {
		MulticastSocket multicastSocket;
		try {
			    multicastSocket = new MulticastSocket(7404);
			    InetAddress address = InetAddress.getByName("239.1.1.171");
		        multicastSocket.joinGroup(address);
		        byte[] buf = new byte[1024];
		        System.out.println("UDP Multicast Client Start Success");
		        while (true) {
		            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		            multicastSocket.receive(datagramPacket);
		            
		            byte[] message = new byte[datagramPacket.getLength()]; 
		            System.arraycopy(buf, 0, message, 0, datagramPacket.getLength());
		            System.out.println(datagramPacket.getAddress());
		            System.out.println(new String(message));
		        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
       
	}

}
