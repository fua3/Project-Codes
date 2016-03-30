package org.pitt.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * CC send UDP Multicast info to HPi
 */
		
public class UDPMulticastSocketServer {

	public static void main(String[] args) {
		
		String FormatedData = "";	

		MulticastSocket multicastSocket;
		try {
			multicastSocket = new MulticastSocket();
			InetAddress address = InetAddress.getByName("239.1.1.171"); 
	        multicastSocket.joinGroup(address); 
	        System.out.println("UDP Multicast Server Start Success");
	        while (true) {
	        	//if(args!=null && args.length>0)
	        		 FormatedData = "23;1;0;199;01;1,2,3,4;5";//args[0];//"23;1;0;199;01;1,2,3,4;5";
	            byte[] buf = FormatedData.getBytes();
	            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
	            datagramPacket.setAddress(address);  
	            datagramPacket.setPort(7404);
	            
	            multicastSocket.send(datagramPacket);
	            Thread.sleep(1000);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

}
