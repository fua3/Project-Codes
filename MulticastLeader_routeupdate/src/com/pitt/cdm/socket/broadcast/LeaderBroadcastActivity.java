package com.pitt.cdm.socket.broadcast;

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
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;



import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pitt.cdm.arcgis.android.maps.R;

public class LeaderBroadcastActivity extends Activity {

	private static String LOG_TAG = "Smart Phone App";
	EditText TxttoSend;
	TextView Label;
	Button StartButton;
	Button StopButton;
	boolean start;
	MulticastSocket Sndmcast =null;
	MulticastSocket Rcvcast =null;
	DataStruct InputData=null;
	String broadcastinfo="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broadcastactivity_main);
		
		TxttoSend = (EditText) this.findViewById(R.id.editText1);
		StartButton = (Button) this.findViewById(R.id.button1);
		StopButton = (Button) this.findViewById(R.id.button2);
		Label = (TextView) this.findViewById(R.id.textView3);
		
		Intent intent=getIntent();  
		broadcastinfo=intent.getStringExtra("info");  
		TxttoSend.setText(broadcastinfo);
		
		StartButton.setOnClickListener(listener);
		StopButton.setOnClickListener(listener);
		
		ListMcast MCastListen = new ListMcast();
		Thread ListCastThread = new Thread(MCastListen);
		InputData=new DataStruct();
		
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
	   	WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
	   	multicastLock.acquire(); 
	   	
	   	ListCastThread.start();
	   	Label.setText("");
		
		
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.s, menu);
		return true;
	}
*/	
	private View.OnClickListener listener = new View.OnClickListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			
			MCast MCastRun = new MCast();
			Thread MCastThread = new Thread(MCastRun);
			
			String IPaddr=null;
			String[] Segment;
			try{
				if (v == StartButton) {
					start = true;
				//	StartButton.setEnabled(false);
				//	Label.setText(TxttoSend.getText().toString());
					
					StopButton.setEnabled(true);
					IPaddr=getLocalIPAddress(); 
					InputData.addr=new String(IPaddr);
					InputData.SegID=new String[(TxttoSend.getText().toString().split(";")[0].split(",")).length];
					InputData.SegID=TxttoSend.getText().toString().split(";")[0].split(",");
					InputData.Port=22220;
					InputData.com=1;
					InputData.ShellID= Integer.parseInt(TxttoSend.getText().toString().split(";")[1]);
				//	Segment=new String[(TxttoSend.getText().toString().split(",")).length];
				//	Segment=TxttoSend.getText().toString().split(",");
					//This is only example with command =2;
					MCastRun.GetInputData(InputData);
				//	GetInputData(String addr, int Port, int com, String[] SegID, int ShellID)
					MCastThread.start();
					
				} else if (v == StopButton) {
					start = false;
					StartButton.setEnabled(true);
					StopButton.setEnabled(false);
					Label.setText(" ");
					MCastThread.stop();
					//MCastThread.interrupt();
					
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class MCast implements Runnable{
		
		private String addr;
		private int Port;
		private int com;
		private String[] SegID;
		private int ShellID;
		public void GetInputData(DataStruct input){
			this.addr=input.addr;
			this.Port=input.Port;
			this.com=input.com;
			this.SegID=input.SegID;
			this.ShellID=input.ShellID;
			
			
		}
		
		private String ConstructData(){
			String Frame = null;
			String Segments=null;
			int i;
			
			if(com==2){
				addr="#";
				Frame=new String (Integer.toString(com)+";#;#;#;###");
				return Frame;
			}
			if(com==1){
				Segments=new String((SegID[0]+","));
				for(i=1; i<SegID.length;i++)
				{
					if(i==(SegID.length)-1){
						Segments=Segments.concat(SegID[i]);
					}
					else{
						Segments=Segments.concat(SegID[i]+",");
					}
					
					
				}
				Frame=new String (Integer.toString(com)+";"+addr+":"+Integer.toString(Port)+
						";"+Segments+";"+Integer.toString(ShellID)+";###");
				return Frame;
			}
			if(com==0){
				Frame=new String (Integer.toString(com)+";"+addr+":"+Integer.toString(Port)+
						";#;#;###");
			}
			else{
				Frame=new String("###");
				return Frame;
			}
			return Frame;
				
					
			
		}
		public void run(){
			
			DatagramPacket dataPacket = null;
			
			InetAddress OutIFaddr=null;
			 
			NetworkInterface Wlan;
			String address=null;
			String DatatoSend =null;
			
			address = getLocalIPAddress();
			SocketAddress dest= new InetSocketAddress("150.212.122.228",22220);
			
			try {
				Sndmcast=new MulticastSocket(22220);
				OutIFaddr=InetAddress.getByName(address);
				Sndmcast.setInterface(OutIFaddr);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			
			try {
				
				
				byte[] senddata = ConstructData().getBytes();
				dataPacket = new DatagramPacket(senddata, senddata.length,dest);
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				Sndmcast.send(dataPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Label.post(new Runnable() {
			
			final String Disp=ConstructData();
			@Override
			public void run() {
				
				Label.append("Sent : " + Disp + "\n" );
			
			}
		});
		//while(true);
		Sndmcast.close();
		}
	}
	
	class ListMcast implements Runnable{
		String address=null;
		DatagramPacket IncdataPacket =null;
		private DataStruct ReceivedData=new DataStruct();
		
		private void ParseData(String Data)
		{
			
			String[] Field;
			//String[] IPPort = new String[2];
			String[] SegID;
			Field=new String[5];
			Field=Data.split(";");
			ReceivedData.com=Integer.parseInt(Field[0]);
			ReceivedData.addr=(Field[1]).split(":")[0];
			ReceivedData.Port=Integer.parseInt((Field[1]).split(":")[1]);
			SegID=new String[Field[2].length()];
			SegID=Field[2].split(",");
			ReceivedData.SegID=SegID;
			ReceivedData.ShellID=Integer.parseInt(Field[3]);
			
		}
		public DataStruct getReceivedData(){
			return ReceivedData;
		}
		public void run(){
			
			byte[] incdata = new byte[500];
			
			address = getLocalIPAddress();
			try {
				Rcvcast=new MulticastSocket(22220);
				Rcvcast.setReuseAddress(true);
				Rcvcast.joinGroup(InetAddress.getByName("150.212.122.228"));
				IncdataPacket = new DatagramPacket(incdata,incdata.length);
				while(true){
					Rcvcast.receive(IncdataPacket);
					
					if (null != IncdataPacket.getAddress()&& 
							!(IncdataPacket.getAddress().toString().equals("/"+ address))){
						
						final String codeString = new String(incdata, 0, IncdataPacket.getLength());
						final String ip=IncdataPacket.getAddress().toString();
						ParseData(codeString);
						Label.post(new Runnable() {
							
							
							@Override
							public void run() {
								
								Label.append("Received from : "+ ip + "\n" );
								Label.append("Content : "+ codeString + "\n" );
							
							}
						});
					
					}
					
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public class DataStruct{
		
		public String addr;
		public int Port;
		public int com;
		public String[] SegID;
		public int ShellID;
		
	}
	private String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() && 
								InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))    
					{
					
							return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		return null;
	}
	
}
