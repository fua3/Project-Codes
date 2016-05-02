package org.pitt.server.console.controller;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map; 
import java.util.Random;




public class CommunicationController extends MultiActionController {

    private static final String Serviceip="10.130.10.58";
    private static final int Serviceport=6379;
    BufferedReader in = null;
    PrintWriter out = null;
    Socket sk = null;
    Exception mException = null;

	public CommunicationController() {
		
    }
	
	/*
	 * warning
	 * Function: From CC to HPi--warning with dynamic region id
	 * cmd=1
	 */
	    public void udpmulticastwarning(HttpServletRequest request,
	            HttpServletResponse response)  throws Exception {
	    	   	        
	        String regionids = ServletRequestUtils.getStringParameter(request,
	                "regionid");
	    	
	    	OutputStream outputStream = response.getOutputStream();
	    	String respstr="OK";
	    	 
			String FormatedData = "";	

			MulticastSocket multicastSocket;
			try {
				multicastSocket = new MulticastSocket();
				InetAddress address = InetAddress.getByName("239.1.1.171"); 
		        multicastSocket.joinGroup(address); 
		        System.out.println("UDP Multicast Warning");
		        
		            //"23;1;0;199;01;1,2,3,4;5";
					String tempstr="1;0;199;"+regionids+";#";
					int strlen=tempstr.length();
					FormatedData=strlen+";"+tempstr;
					System.out.println(FormatedData);				
					out.println(FormatedData); 
					
		            byte[] buf = FormatedData.getBytes();
		            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		            datagramPacket.setAddress(address); 
		            datagramPacket.setPort(7404);
		            
		            multicastSocket.send(datagramPacket);
		            Thread.sleep(1000);
		        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        byte[] resp = respstr.getBytes("UTF-8");
	        outputStream.write(resp);  
	        }
	  
	/**
	 * Function: send notification to effected leader when report allocated shelter is unavailalbe 
	 * cmd=6
	 * @param request
	 * @param response
	 * @throws Exception
	 */
    public void notificationleadersbyshelterid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null;        
        String shelterid = ServletRequestUtils.getStringParameter(request,
                "shelterid");

    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs = jedis.hmget("leadershelter", shelterid); 
        
        if(rs != null){//sample: 41:1,000000000000,localhost:22223,136.142.186.102:22227    	
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i)=="" ||rs.get(i)==null) continue;
        			String[] leaderids= rs.get(i).split(",");
        				if(leaderids.length==4){
        					String ip = leaderids[2].split(":")[0];
        					int port = Integer.parseInt(leaderids[2].split(":")[1]);
        		        
        					try
        					{             		    		        		    		       						
            					outputSocket = new Socket(ip, port);
            					//outputSocket.setKeepAlive(true);  
            					//outputSocket.setSoTimeout(2*1000);
            					System.out.println("Connected on Pi: " +ip+":"+ port);

            				    out = new PrintWriter(new BufferedWriter(
            							new OutputStreamWriter(outputSocket.getOutputStream())),
            							true);		    
            				 
            				    String tempstr="6;"+ip+":"+port+";#;###";
            				    int strlen=tempstr.length();
            				    out.println(strlen+";6;"+ip+":"+port+";#;###");   
            				    
            				    respstr="success";
            				                				    
        					}catch (Exception e) {
        						
        					}         				
        				       			       			        		        			
        			}       		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }    	
    }  
    public void udpunicastnewpath(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	  
		Random random = new Random();
		DatagramSocket datagramSocket = null;       
        String shelterid = ServletRequestUtils.getStringParameter(request,
                "shelterid");

   	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs = jedis.hmget("leadershelter", shelterid); 
        
        if(rs != null){   	
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i)=="" ||rs.get(i)==null) continue;
        			String[] leaderids= rs.get(i).split(",");
        				if(leaderids.length==4){
        					String regionid=leaderids[0];
        					String[] temppi=leaderids[3].split(":");
        					String tempstr="7;0;199;"+shelterid+","+leaderids[0]+";0,0,0,-1";
							int strlen=tempstr.length();	                							
							System.out.println(strlen+";"+tempstr);
							
							try {
								datagramSocket = new DatagramSocket(random.nextInt(9999));
								datagramSocket.setSoTimeout(10 * 1000);
								System.out.println("Client start success");
							} catch (Exception e) {
								datagramSocket = null;
								System.out.println("Client start fail");
								e.printStackTrace();
							}
							
							byte[] buffer = new byte[1024 * 64]; 
							
							String formantdata=strlen+";"+tempstr;
							byte[] btSend =formantdata.getBytes("UTF-8");
							DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(temppi[0]), Integer.parseInt(temppi[1]));
							packet.setData(btSend);
							System.out.println("Connected on " +temppi[0]+":"+ temppi[1]);
							System.out.println("CC send:" + Arrays.toString(btSend));
							try {
								datagramSocket.send(packet);
							} catch(Exception e){
								e.printStackTrace();
							}	                							
							
							respstr="success";                				                				    
						}
        		        
        				
        				       			       			        		        			
        			}       		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }    	
 
    /*
     * UDP Multicast
     * Function: send warning notification to affected leaders
     */
    public void notificationleadersbysegmentid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid");
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  
        List<String> rs = jedis.hmget("leadersegment", segmentid); 
        
        if(rs != null){        	
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i)=="" ||rs.get(i)==null) continue;
        			String[] leaderids= rs.get(i).split(";");
        			for(int j=0;j<leaderids.length;j++){
        				String[] templeader=leaderids[j].split(":");
        				if(templeader.length==2){
        					String ip = templeader[0];
        					int port = Integer.parseInt(templeader[1]);
        		        
        					try
        					{             		    		        		    		        						
            					outputSocket = new Socket(ip, port);
            					//outputSocket.setKeepAlive(true);  
            					//outputSocket.setSoTimeout(2*1000);
            					System.out.println("Connected on " +ip+":"+ port);

            				    out = new PrintWriter(new BufferedWriter(
            							new OutputStreamWriter(outputSocket.getOutputStream())),
            							true);		    
            				    //out.println(str);
            				    String tempstr="0;"+ip+":"+port+";#;###";
            				    int strlen=tempstr.length();
            				    out.println("0;"+ip+":"+port+";#;###");   
            				    
            				    respstr="success";
            				                				    
        					}catch (Exception e) {
        						
        					}         				
        				}        			       			        		        			
        			}       		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }    	
    }  
    
    public void notificationpiandleadersbysegmentid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
    	List<String> templeaderids= new ArrayList<String>();
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid");
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs0 = jedis.hmget("leadersegment", segmentid); 
        
        if(rs0 != null){        	
        	for(int i=0;i<rs0.size();i++){
        		if(rs0.get(i)=="" ||rs0.get(i)==null) continue;
        		String[] leaderids= rs0.get(i).split(";");
        		for(int k=0;k<leaderids.length;k++){
            		//List<String> rs = jedis.hmget("piandleaderipaddress", leaderids[k]); 
            		
        			String[] leaderips= leaderids[k].split(",");
        			if(leaderips.length==6){
        				//String[] temppi=leaderips[4].split(":");
        				String[] temppi=leaderips[3].split(":");
        				String[] templeader=leaderips[3].split(":");
        				if(temppi.length==2){
        					String piip = temppi[0];
        					int piport = Integer.parseInt(temppi[1]);
        					String leaderip=templeader[0];
        					int leaderport=Integer.parseInt(templeader[1]);
    					    if(templeaderids.contains(leaderip)) continue;
    					    
    					    else
    					    {
    					    	 templeaderids.add(leaderip);
        				    try
        						{             		    		        		    		
        						
        							//outputSocket = new Socket(piip, piport);
        							outputSocket = new Socket(leaderip, leaderport);
        							
        							System.out.println("Connected on " +piip+":"+ piport);

        							out = new PrintWriter(new BufferedWriter(
            							new OutputStreamWriter(outputSocket.getOutputStream())),
            							true);		    
        							String tempstr="7;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";"+leaderips[5]+";"+leaderips[6]+";###";
        							int strlen=tempstr.length();          							
        							out.println(strlen+";7;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";"+leaderips[5]+";"+leaderips[6]+";###");            				    
        							respstr="success";
            				                				    
        						}catch (Exception e) {        						
        					}
    					    }
        				}   
        			}          			
        		
        		}
      		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }    	
    } 
  
    
    public void notificationbywarning(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
        String[] regionids = ServletRequestUtils.getStringParameter(request,
                "regionid").split(",");
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	 
    	 List<String> templeaderids= new ArrayList<String>();
    	 int tempis=0;
    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        for(int i=0;i<regionids.length;i++){
        Map<String,String> map1 = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map1.entrySet()) {
        	
            String segmentid =  entry.getKey().toString();           
            String[] leaderids =entry.getValue().toString().split(";");
            
            if(leaderids != null){        	            	
            		for(int k=0;k<leaderids.length;k++){
                		//List<String> rs = jedis.hmget("piandleaderipaddress", leaderids[k]); 
                		
            			String[] leaderips= leaderids[k].split(",");//41,3,88708ca4537d,192.168.19.12:22223,192.168.1.111:22227
            				if(leaderips.length==7 && leaderips[1].equals(regionids[i])){
            					//when localhost test, use do not connect to pi, use localhost:22223
            					String[] temppi=leaderips[4].split(":");//leaderips[3].split(":");
            					String[] templeader=leaderips[3].split(":");
            					if(temppi.length==2){
            						String piip = temppi[0];
            						int piport = Integer.parseInt(temppi[1]);
            					    String leaderip=templeader[0];
            					    int leaderport=Integer.parseInt(templeader[1]);
            					    
            					    if(templeaderids.contains(leaderip)) continue;
            					    

            					    else
            					    {
            					    	 templeaderids.add(leaderip);
            						try
            						{             		    		        		    		
                    						
            							outputSocket = new Socket(piip, piport);

            							System.out.println("Connected on " +piip+":"+ piport);

            							out = new PrintWriter(new BufferedWriter(
                							new OutputStreamWriter(outputSocket.getOutputStream())),
                							true);	
            							
            							String tempstr="1;0;199;"+regionids[i]+";###";
            							int strlen=tempstr.length();
            							
            							System.out.println(strlen+";"+tempstr);
            							out.println(strlen+";1;0;199;"+regionids[i]+";###"); 
            							
            							tempis=0;
            							respstr="success";                				                				    
            						}catch (Exception e) {        						
            					} 
            					    
            					}
            				}   
            			}          			
            		
            		}       		                 	           	   
              } 

       } 
    }
       
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);  
        }
    
  
 
	 public String getCCinfo(String url, InetAddress hosaddresss, int port ){
		 
		 	String result="";
	        try{
	        	//InetAddress.getByName(tip)
			   sk = new Socket (hosaddresss, port);
			   InputStreamReader isr;
			   isr = new InputStreamReader (sk.getInputStream ());
			   in = new BufferedReader (isr);
			   
			   //create output
	
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

	//at last, release resources
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
return result;
		 
}
	 
public void udpmulticastwarningtest(HttpServletRequest request,
	       HttpServletResponse response)  throws Exception {
		 
		 
			String FormatedData = "";	
			MulticastSocket multicastSocket;
			try {
				multicastSocket = new MulticastSocket();
				InetAddress address = InetAddress.getByName("239.1.1.171"); 
		        multicastSocket.joinGroup(address); 
		        System.out.println("UDP Multicast Sender Start Success");
		      
		        FormatedData = "23;1;0;199;01;1,2,3,4;5";
		        byte[] buf = FormatedData.getBytes();
		        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		        datagramPacket.setAddress(address);  
		        datagramPacket.setPort(7404);
		            
		        multicastSocket.send(datagramPacket);
		        Thread.sleep(1000);		            
		        multicastSocket.close();
		       
			} catch (IOException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}								 
}

/*
 * Detour
 * Function: from CC to affected HPi 
 */
public void udpunicastdetour(HttpServletRequest request,
	        HttpServletResponse response)  throws Exception {
	
		DatagramSocket datagramSocket = null; 
		Random random = new Random();
		//String uuid = UUID.randomUUID().toString();
		List<String> templeaderids= new ArrayList<String>();
        
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid");
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	
		datagramSocket = new DatagramSocket(random.nextInt(9999));
		datagramSocket.setSoTimeout(10 * 1000);		
		byte[] buffer = new byte[1024 * 64];  	
		
		try {	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs0 = jedis.hmget("leadersegment", segmentid); 
        
        if(rs0 != null){        	
        	for(int i=0;i<rs0.size();i++){
        		if(rs0.get(i)=="" ||rs0.get(i)==null) continue;
        		String[] leaderids= rs0.get(i).split(";");
        		for(int k=0;k<leaderids.length;k++){
            		
        			String[] leaderips= leaderids[k].split(",");
        			if(leaderips.length==7){
        				String[] temppi=leaderips[4].split(":");
        				String[] templeader=leaderips[3].split(":");
        				if(temppi.length==2){
        					
        					String leaderip=templeader[0];

    					    if(templeaderids.contains(leaderip)) continue;
    					    
    					    else
    					    {
    					    	 templeaderids.add(leaderip);
        				    try
        						{             		    		        		    		
        						
        						String tempstr="7;0;199;"+leaderips[2]+","+leaderips[1]+";"+leaderips[2]+","+leaderips[3]+","+leaderips[5]+","+leaderips[6];
        						int strlen=tempstr.length();						
        						String formantdata=strlen+";"+tempstr;
        						byte[] btSend =formantdata.getBytes("UTF-8");
        						DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName( temppi[0]), Integer.parseInt(temppi[1]));
        						packet.setData(btSend);
        						System.out.println("CC send:" + Arrays.toString(btSend));
        						//CC send detour to HPi
        						try {
        							datagramSocket.send(packet);
        						} catch(Exception e){
        							e.printStackTrace();
        						}
        						
        						datagramSocket.receive(packet);
        						byte[] bt = new byte[packet.getLength()];
        						System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
        						if(null != bt && bt.length > 0){
        							System.out.println("CC receive:" + Arrays.toString(bt));
        						}
        						Thread.sleep(1 * 1000);
        						datagramSocket.close();
        						respstr="success";
            				                				    
        						}catch (Exception e) {        						
        					}
    					    }
        				}   
        			}          			
        		
        		}
      		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }  								
		} catch (Exception e) {
			datagramSocket = null;
			System.out.println("UDP Unicast Sender start fail");
			e.printStackTrace();
		}
}
/*
 * Warning
 * UDP Unicast
 * Function: from CC to affected HPi 
 */
public void udpunicastwarning(HttpServletRequest request,
	        HttpServletResponse response)  throws Exception {
	
				Random random = new Random();
				DatagramSocket datagramSocket = null; 
	        	Socket outputSocket = null;
	        	PrintWriter out = null; 
	            String str = ServletRequestUtils.getStringParameter(request,
	                    "str");         
	            String[] regionids = ServletRequestUtils.getStringParameter(request,
	                    "regionid").split(",");
	        	
	        	OutputStream outputStream = response.getOutputStream();
	        	String respstr="failure";
	        	 
	        	 List<String> templeaderids= new ArrayList<String>();
	        	 int tempis=0;
	        	
	        	   	
	            Jedis jedis = new Jedis(Serviceip,Serviceport);  

	            for(int i=0;i<regionids.length;i++){
	            Map<String,String> map1 = jedis.hgetAll("leadersegment");   
	            for(Map.Entry entry: map1.entrySet()) {
	            	
	                String segmentid =  entry.getKey().toString();           
	                String[] leaderids =entry.getValue().toString().split(";");
	                
	                if(leaderids != null){        	            	
	                		for(int k=0;k<leaderids.length;k++){
	                    		//List<String> rs = jedis.hmget("piandleaderipaddress", leaderids[k]); 
	                    		
	                			String[] leaderips= leaderids[k].split(",");//41,3,88708ca4537d,192.168.19.12:22223,192.168.1.111:22227
	                				if(leaderips.length==7 && Integer.parseInt(leaderips[1])==(Integer.parseInt(regionids[i]))){
	                					//when localhost test, use do not connect to pi, use localhost:22223
	                					String[] temppi=leaderips[4].split(":");//leaderips[3].split(":");
	                					String[] templeader=leaderips[3].split(":");
	                					if(temppi.length==2){
	                						String piip = temppi[0];
	                						int piport = Integer.parseInt(temppi[1]);
	                					    String leaderip=templeader[0];
	                					    int leaderport=Integer.parseInt(templeader[1]);
	                					    
	                					    if(templeaderids.contains(leaderip)) continue;
	                					    

	                					    else
	                					    {
	                					    	 templeaderids.add(leaderip);
	                						try
	                						{             		    		        		    		
	                							//change 99 to real pi id
	                							String tempstr="1;0;199;"+regionids[i]+";###";
	                							int strlen=tempstr.length();	                							
	                							System.out.println(strlen+";"+tempstr);
	                							
	                							try {
	                								datagramSocket = new DatagramSocket(random.nextInt(9999));
	                								datagramSocket.setSoTimeout(10 * 1000);
	                								System.out.println("Client start success");
	                							} catch (Exception e) {
	                								datagramSocket = null;
	                								System.out.println("Client start fail");
	                								e.printStackTrace();
	                							}
	                							
	                							byte[] buffer = new byte[1024 * 64]; 
	                							
	                							String formantdata=strlen+";"+tempstr;
	                							byte[] btSend =formantdata.getBytes("UTF-8");
	                							DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(temppi[0]), Integer.parseInt(temppi[1]));
	                							packet.setData(btSend);
	                							System.out.println("Connected on " +piip+":"+ piport);
	                							System.out.println("CC send:" + Arrays.toString(btSend));
	                							try {
	                								datagramSocket.send(packet);
	                							} catch(Exception e){
	                								e.printStackTrace();
	                							}	                							
	                							tempis=0;
	                							respstr="success";                				                				    
	                						}catch (Exception e) {        						
	                					} 
	                					    
	                					}
	                				}   
	                			}          			
	                		
	                		}       		                 	           	   
	                  } 

	           } 
	        }
	           
	            byte[] resp = respstr.getBytes("UTF-8");
	            outputStream.write(resp);  
	            }

}
