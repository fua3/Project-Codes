package org.pitt.server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
/**
 * @ CC read UDP Unicast info from Hpi by leader, report risk
 */
public class UdpUnicastSocketServer {
	public static void main(String[] args) {
		try {
			init();
			while(true){
				try {
					byte[] buffer = new byte[1024 * 64]; 
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					receive(packet);
					new Thread(new ServiceImpl(packet)).start();
				} catch (Exception e) {
				}
				Thread.sleep(1 * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Data receive package, this means will cause thread block
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public static DatagramPacket receive(DatagramPacket packet) throws Exception {
		try {
			datagramSocket.receive(packet);
			return packet;
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * send response package to request
	 * @param bt
	 * @throws IOException
	 */
	public static void response(DatagramPacket packet) {
		try {
			datagramSocket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * initialization
	 * @throws SocketException
	 */
	public static void init(){
		try {
			socketAddress = new InetSocketAddress("130.49.219.87", 7385);
			datagramSocket = new DatagramSocket(socketAddress);
			datagramSocket.setSoTimeout(5 * 1000);
			System.out.println("Receiver start success");
		} catch (Exception e) {
			datagramSocket = null;
			System.err.println("Receiver start fail");
			e.printStackTrace();
		}
	}
	private static InetSocketAddress socketAddress = null; // service listening address
	private static DatagramSocket datagramSocket = null; // link object
}
/**
 * @Print received package, return original data, Set sleep to show time-consuming 
 */
class ServiceImpl implements Runnable {
	private DatagramPacket packet;
	public ServiceImpl(DatagramPacket packet){
		this.packet = packet;
	}
	public void run() {
		try {
			byte[] bt = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
			String res = new String(bt);
			System.out.println(packet.getAddress().getHostAddress() + "：" + packet.getPort() + "：" + Arrays.toString(bt)+":"+res);
			Thread.sleep(5 * 1000); 
			// return original data
			packet.setData(bt);
			UdpUnicastSocketServer.response(packet);
			
			//process the format info
			//*************************
			String[] tt=res.split(";");
			if(tt[0].equals("23")){
			    BufferedReader in = null;
			    PrintWriter out = null;
			    Socket sk = null;
			    Exception mException = null;
				String url="http://130.49.219.87:8081/kvs/keyvaluestore.do?action=cleardefaultleadershelter";
			 	String result="";
		        try{
		        	InetAddress hosaddresss=InetAddress.getByName("130.49.219.87");
		        	int port=22225;
		        	
		        	sk = new Socket (hosaddresss, port);
		        	InputStreamReader isr;
		        	isr = new InputStreamReader (sk.getInputStream ());
		        	in = new BufferedReader (isr);

		        	out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sk.getOutputStream())), true);			   											   
		        	out.println (url);
				   
		        	String line;
		        	while ((line = in.readLine()) != null) {
				       result += line;
		        	}			   													   																									   
		        } 
		        catch (IOException e) {
			
		        	e.printStackTrace();
		        } catch (Exception e) {
		        	mException = e;
		        }
		        finally
		        {
		        	try{ 
		        		if (in != null)
		        			in.close ();
		        		if (out != null)
		        			out.close ();
		        		if (sk != null)
		        			sk.close ();       
		        	}
		        	catch (IOException e)
		        	{ 
		        		System.out.println("close err"+e);       
		        	}
		        }		 
		        System.out.println("Result: "+result); 
		        //***********************
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} 