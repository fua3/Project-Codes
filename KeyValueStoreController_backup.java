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

import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map; 
import java.util.HashMap;  
import java.util.Random;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class KeyValueStoreController_backup extends MultiActionController {

    private static final int List = 0;
    private static final String Serviceip="150.212.71.10";
    private static final int Serviceport=6379;
    BufferedReader in = null;
    PrintWriter out = null;
    Socket sk = null;
    Exception mException = null;

	public KeyValueStoreController_backup() {
    }
    public  void showallleadersegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	
        Map<String,String> map = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getKey().equals("999999") ||entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";
        	
        } 
                	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void showallaffectedsegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	
        Map<String,String> map = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getValue().equals("0") ||entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + ";";        	
        } 
        if(respstr!="") 
        	respstr=respstr.substring(0, respstr.length()-1);    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    
    public  void showalldefaultleadersegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	
        Map<String,String> map = jedis.hgetAll("defaultleadersegment");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getKey().equals("999999") ||entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";        	
        } 
                	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void showallleadershellter(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	
        Map<String,String> map = jedis.hgetAll("leadershelter");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getKey().equals("999999") ||entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";       	
        } 
                 	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void showpiandleaderipaddress(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	   	
    	
        Map<String,String> map = jedis.hgetAll("piandleaderipaddress");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";        	
        } 
                	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }  
    public  void showaffectedbarriers(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	   	
    	
        Map<String,String> map = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getValue().equals("")
        			||entry.getValue().equals(null)||entry.getValue().equals("0")) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             String[] coord=entry.getValue().toString().split(",");
             //respstr+="{\"geometry\":{\"paths\":[["+entry.getValue() +"]]}},";
             respstr+="%7B%22geometry%22%3A%7B%22paths%22%3A%5B%5B"+"%5B"+ coord[0]+ "%2C"+coord[1]+"%5D%2C%5B"+coord[2]+"%2C"+coord[3]+"%5D%5D%5D%7D%7D%2C";
             
             //respstr+="%7B%22geometry%22%3A%7B%22paths%22%3A%5B%5B"+"%5B"+ coord[0]+ "%2C"+40.44368508861808+"%5D%2C%5B"+-79.92124363637494+"%2C"+40.443994381224535+"%5D"+"%5D%5D%7D%7D%%2C";
             
             //respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";        	
        } 
        if(respstr!="")
        {
        	respstr=respstr.substring(0, respstr.length()-3);
        //String polylinebarriers="{\"spatialReference\":{\"wkid\":4326,\"latestWkid\":4326},\"features\":["+respstr+"]}";
        
        String polylinebarriers="%7B%22spatialReference%22%3A%7B%22wkid%22%3A4326%2C%22latestWkid%22%3A4326%7D%2C%22features%22%3A%5B"+respstr+"%5D%7D";;
        
        //String polylinebarriers="%7B%22spatialReference%22%3A%7B%22wkid%22%3A4326%2C%22latestWkid%22%3A4326%7D%2C%22features%22%3A%5B%7B%22geometry%22%3A%7B%22paths%22%3A%5B%5B%5B-79.92124363637494%2C40.44368508861808%5D%2C%5B-79.92124363637494%2C40.443994381224535%5D%5D%5D%7D%7D%5D%7D";
        
        //String polylinebarriers="{%22spatialReference%22:%20{%22wkid%22:%204326,%22latestWkid%22:%204326},%22features%22:%20[{%22geometry%22:%20{%22paths%22:%20["+respstr+"]}";
                	
        byte[] resp = polylinebarriers.getBytes("UTF-8");
        outputStream.write(resp);
        }
    }
    public  void showalldefaultleadershellter(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";   
    	
        Map<String,String> map = jedis.hgetAll("defaultleadershelter");   
        for(Map.Entry entry: map.entrySet()) { 
        	if(entry.getKey().equals("999999") ||entry.getValue().equals("")
        			||entry.getValue().equals(null)) continue;
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";        	
        } 
                	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void clearleadersegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
        Map<String,String> map = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "");             
        	jedis.hmset("leadersegment", affectedsegment);
        
        }
        
                
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void cleardefaultleadersegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
        Map<String,String> map = jedis.hgetAll("defaultleadersegment");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "");             
        	jedis.hmset("defaultleadersegment", affectedsegment);
        
        }
        
                
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void clearleadershelter(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
        Map<String,String> map = jedis.hgetAll("leadershelter");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "");             
        	jedis.hmset("leadershelter", affectedsegment);
        
        }
         
                
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }    
    public  void cleardefaultleadershelter(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
       	
        Map<String,String> map = jedis.hgetAll("defaultleadershelter");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "");             
        	jedis.hmset("defaultleadershelter", affectedsegment);
        
        }
    	 
                
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void clearaffectedsegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
        Map<String,String> map = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "0");             
        	jedis.hmset("affectedsegment", affectedsegment);
        
        }
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";   
    	
        Map<String,String> tmap = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: tmap.entrySet()) { 
        	
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n"); 
             //respstr=respstr+entry.getKey() + ":" + entry.getValue() + "\n";       	
        } 
                 	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
        
    }    
    public  void clearpiandleaderipadderss(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	Jedis jedis = new Jedis(Serviceip,Serviceport);
    	
    	
        Map<String,String> map = jedis.hgetAll("piandleaderipaddress");   
        for(Map.Entry entry: map.entrySet()) {   
        
        	String segmentid =  entry.getKey().toString();                   
        	Map<String,String> affectedsegment = new HashMap<String,String>();  
        	affectedsegment.put(segmentid, "");             
        	jedis.hmset("piandleaderipaddress", affectedsegment);
        
        }
        
                
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
        
    }   
    public  void getIpAddr(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    
    	String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
                        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr=ip;    	
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);
    }
    public  void addleaderbysegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true;
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String[] segmentids=ServletRequestUtils.getStringParameter(request,"segmentid").split(",");
        String[] segmentstartids=ServletRequestUtils.getStringParameter(request,"segmentstartpts").split(",");
        String templeaderid=leaderid;
        Jedis jedis = new Jedis(Serviceip,Serviceport);

        for(int k=0;k<segmentids.length;k++){
        
        String segmentid =  segmentids[k];
                       
        templeaderid=leaderid+","+segmentstartids[k]+","+String.valueOf(k);
        List<String> rs = jedis.hmget("leadersegment", segmentid);     
        if(rs==null||(rs.size()==1&&(rs.get(0)==null||rs.get(0)==""))) {}
        else{
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		String[] aa=rs.get(i).split(";");
        		
        		for(int j=0;j<aa.length;j++)
        		{
        			if(leaderid.equals(aa[j])){
        				temp=false;
        				templeaderid=rs.get(i);        				
        				break;        				
        			}        			
        		}
        		if(temp==true)
        		{ 
        			if(rs.get(i).equals(""))
        				templeaderid= leaderid+","+segmentstartids[k]+","+String.valueOf(k);
        			else
        				templeaderid= rs.get(i)+";"+leaderid+","+segmentstartids[k]+","+String.valueOf(k);
        		}
        	}	
        		
        	}       	
        
                                    
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put(segmentid, templeaderid);            
        jedis.hmset("leadersegment", leadersegment); 
       // templeaderid="";
  
        }
        Map<String,String> map = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    public  void addleaderbysegmentorder(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true;
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        int segmentorder=Integer.valueOf(ServletRequestUtils.getStringParameter(request,"affectedsegmentorder"));
        String[] segmentids=ServletRequestUtils.getStringParameter(request,"segmentid").split(",");
        String[] segmentstartids=ServletRequestUtils.getStringParameter(request,"segmentstartpts").split(",");
        String templeaderid=leaderid;
        Jedis jedis = new Jedis(Serviceip,Serviceport);

        for(int k=0;k<segmentids.length;k++){
        
        String segmentid =  segmentids[k];
                       
        templeaderid=leaderid+","+segmentstartids[k]+","+String.valueOf(k+segmentorder);
        List<String> rs = jedis.hmget("leadersegment", segmentid);     
        if(rs==null||(rs.size()==1&&(rs.get(0)==null||rs.get(0)==""))) {}
        else{
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		String[] aa=rs.get(i).split(";");
        		
        		for(int j=0;j<aa.length;j++)
        		{
        			if(leaderid.equals(aa[j])){
        				temp=false;
        				templeaderid=rs.get(i);        				
        				break;        				
        			}        			
        		}
        		if(temp==true)
        		{ 
        			if(rs.get(i).equals(""))
        				templeaderid= leaderid+","+segmentstartids[k]+","+String.valueOf(k+segmentorder);
        			else
        				templeaderid= rs.get(i)+";"+leaderid+","+segmentstartids[k]+","+String.valueOf(k+segmentorder);
        		}
        	}	
        		
        	}       	
        
                                    
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put(segmentid, templeaderid);            
        jedis.hmset("leadersegment", leadersegment); 
       // templeaderid="";
  
        }
        Map<String,String> map = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    public  void addreportbysegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true;
        
        String segtype =  ServletRequestUtils.getStringParameter(request,"segtype");
        String segmentid=ServletRequestUtils.getStringParameter(request,"segmentid");
    
        Jedis jedis = new Jedis(Serviceip,Serviceport);

        List<String> rs = jedis.hmget("affectedsegment", segmentid);     
        if(rs==null||(rs.size()==1&&(rs.get(0)==null||rs.get(0)==""))) {}
        else
        {
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		if(segtype.equals(rs.get(i)))
        		{
        				temp=false;       				
        				break;        				
        		}        			
        		
        	}	
        		
        }       	
        
		if(temp==true)
		{  			
		                          
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put(segmentid, segtype);            
        jedis.hmset("affectedsegment", leadersegment); 
		}  
  
      
        Map<String,String> map = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    
    public  void adddefaultleaderbysegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true;
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String[] segmentids=ServletRequestUtils.getStringParameter(request,"segmentid").split(",");
        String templeaderid=leaderid;
        Jedis jedis = new Jedis(Serviceip,Serviceport);

        for(int k=0;k<segmentids.length;k++){
        
        String segmentid =  segmentids[k];
                       
        templeaderid=leaderid;
        List<String> rs = jedis.hmget("defaultleadersegment", segmentid);     
        if(rs==null||(rs.size()==1&&(rs.get(0)==null||rs.get(0)==""))) {}
        else{
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		String[] aa=rs.get(i).split(";");
        		
        		for(int j=0;j<aa.length;j++)
        		{
        			if(leaderid.equals(aa[j])){
        				temp=false;
        				templeaderid=rs.get(i);        				
        				break;        				
        			}        			
        		}
        		if(temp==true)
        		{
        			templeaderid= rs.get(i)+";"+leaderid;        			
        		}
        	}	
        		
        	}       	
                                            
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put(segmentid, templeaderid);            
        jedis.hmset("defaultleadersegment", leadersegment); 
       // templeaderid="";
  
        }
        Map<String,String> map = jedis.hgetAll("defaultleadersegment");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    
    public  void addaffectedsegment(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String affectedcoor=ServletRequestUtils.getStringParameter(request,"affectedcoor");;

        Jedis jedis = new Jedis(Serviceip,Serviceport);
                                                 
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put(leaderid, affectedcoor);            
        jedis.hmset("affectedsegment", leadersegment); 
         
        Map<String,String> map = jedis.hgetAll("affectedsegment");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    public  void addpiandleaderipaddress(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String leaderip=ServletRequestUtils.getStringParameter(request,"leaderip");

        Jedis jedis = new Jedis(Serviceip,Serviceport);
                                                 
        Map<String,String> piandleader= new HashMap<String,String>();  
        piandleader.put(leaderid, leaderip);            
        jedis.hmset("piandleaderipaddress", piandleader); 
         
        Map<String,String> map = jedis.hgetAll("piandleaderipaddress");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    public  void addshelterbyleader(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true, temp1=true;
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String shelterid=ServletRequestUtils.getStringParameter(request,"shelterid");
        String templeaderid="";
        Jedis jedis = new Jedis(Serviceip,Serviceport);
        
        List<String> rs = jedis.hmget("leadershelter", shelterid);     
        if((rs.size()==1&&rs.get(0)=="")||(rs.size()==1&&rs.get(0)==null)||rs==null) {
	        Map<String,String> leadershelter = new HashMap<String,String>();  
	        leadershelter.put(shelterid, leaderid);            
	        jedis.hmset("leadershelter", leadershelter); 
        }
        else{
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		String[] aa=rs.get(i).split(";");
        		
        		for(int j=0;j<aa.length;j++)
        		{
        			if(leaderid.equals(aa[j])){
        				temp=false;
        				templeaderid=rs.get(i);        				
        				break;        				
        			}        			
        		}
        		if(temp==true)
        		{
        			templeaderid= rs.get(i)+";"+leaderid;  
         			Map<String,String> leadershelter = new HashMap<String,String>();  
         			leadershelter.put(shelterid, templeaderid.substring(1, templeaderid.length()));            
         			jedis.hmset("leadershelter", leadershelter); 
         			
         			templeaderid="";
        		}
        	  }	        		
        	} 
        
        Map<String,String> map0 = jedis.hgetAll("leadershelter");   
        for(Map.Entry entry: map0.entrySet()) {   
        
        templeaderid="";
        String tshelterid =  entry.getKey().toString();           
        String[] rs1 =entry.getValue().toString().split(";");
        if(tshelterid.equals(shelterid))
        {           	
           if(rs1==null||(rs1.length==1&&(rs1[0]==null||rs1[0]==""))) {}
           else{
        	 for(int i=0;i<rs1.length;i++){
        		if(rs1[i].equals("")||rs1[i].equals(null)) continue;
        
 
        			if(leaderid.equals(rs1[i])){
        				temp=false;
        				templeaderid=entry.getValue().toString();        				
        				break;        				
        			}        			        		
        	}
     		if(temp==true)
     		{   
     			if(entry.getValue().toString().equals(""))
     				templeaderid= leaderid;   
     			else 
     				templeaderid= entry.getValue().toString()+";"+leaderid;  
     		        		       	
     			Map<String,String> leadershelter = new HashMap<String,String>();  
     			leadershelter.put(tshelterid, templeaderid);            
     			jedis.hmset("leadershelter", leadershelter); 
     			
     			templeaderid="";  
     		}
           }                      
        }
        else{
            if((rs1.length==1&&rs1[0]=="")||(rs1.length==1&&rs1[0]==null)||rs1==null) {}
            else{
         	 for(int i=0;i<rs1.length;i++){
         		if(rs1[i].equals("")||rs1[i].equals(null)) continue;

         			if(leaderid.equals(rs1[i])){         			
         				  temp1=false;				         				       				
         			}
         			else{
         				templeaderid= templeaderid+";"+rs1[i];               				
         			}         		
         	  }         		        	
        	 if(temp1==false){
        		 
        	        Map<String,String> leadershelter = new HashMap<String,String>();  
        	        leadershelter.put(tshelterid, templeaderid);            
        	        jedis.hmset("leadershelter", leadershelter);         		        	       
        	 }       	        	 
            }                       
          }               
        }
    
        Map<String,String> map = jedis.hgetAll("leadershelter");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
    
    public  void adddefaultshelterbyleader(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        boolean temp=true, temp1=true;
        
        String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
        String shelterid=ServletRequestUtils.getStringParameter(request,"shelterid");
        String templeaderid="";
        Jedis jedis = new Jedis(Serviceip,Serviceport);
        
        List<String> rs = jedis.hmget("defaultleadershelter", shelterid);     
        if((rs.size()==1&&rs.get(0)=="")||(rs.size()==1&&rs.get(0)==null)||rs==null) {
	        Map<String,String> leadershelter = new HashMap<String,String>();  
	        leadershelter.put(shelterid, leaderid);            
	        jedis.hmset("defaultleadershelter", leadershelter); 
        }
        else{
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i).equals("")||rs.get(i).equals(null)) continue;
        
        		String[] aa=rs.get(i).split(";");
        		
        		for(int j=0;j<aa.length;j++)
        		{
        			if(leaderid.equals(aa[j])){
        				temp=false;
        				templeaderid=rs.get(i);        				
        				break;        				
        			}        			
        		}
        		if(temp==true)
        		{
        			templeaderid= rs.get(i)+";"+leaderid;  
         			Map<String,String> leadershelter = new HashMap<String,String>();  
         			leadershelter.put(shelterid, templeaderid);            
         			jedis.hmset("defaultleadershelter", leadershelter); 
         			
         			templeaderid="";
        		}
        	  }	        		
        	} 
        
        Map<String,String> map0 = jedis.hgetAll("defaultleadershelter");   
        for(Map.Entry entry: map0.entrySet()) {   
        
        templeaderid="";
        String tshelterid =  entry.getKey().toString();           
        String[] rs1 =entry.getValue().toString().split(";");
        if(tshelterid.equals(shelterid))
        {   
        	
           if(rs1==null||(rs1.length==1&&(rs1[0]==null||rs1[0]==""))) {}
           else{
        	 for(int i=0;i<rs1.length;i++){
        		if(rs1[i].equals("")||rs1[i].equals(null)) continue;
        
 
        			if(leaderid.equals(rs1[i])){
        				temp=false;
        				templeaderid=entry.getValue().toString();        				
        				break;        				
        			}        			        		
        	}
     		if(temp==true)
     		{
     			templeaderid= entry.getValue().toString()+";"+leaderid;        			
     		        		       	
     			Map<String,String> leadershelter = new HashMap<String,String>();  
     			leadershelter.put(tshelterid, templeaderid);            
     			jedis.hmset("defaultleadershelter", leadershelter); 
     			
     			templeaderid="";  
     		}
           }                     
        }
        else{
            if((rs1.length==1&&rs1[0]=="")||(rs1.length==1&&rs1[0]==null)||rs1==null) {}
            else{
         	 for(int i=0;i<rs1.length;i++){
         		if(rs1[i].equals("")||rs1[i].equals(null)) continue;

         			if(leaderid.equals(rs1[i])){         			
         				  temp1=false;				         				       				
         			}
         			else{
         				templeaderid= templeaderid+";"+rs1[i];               				
         			}         		
         	  }         		        	
        	 if(temp1==false){
        		 
        	        Map<String,String> leadershelter = new HashMap<String,String>();  
        	        leadershelter.put(tshelterid, templeaderid);            
        	        jedis.hmset("defaultleadershelter", leadershelter);         		        	       
        	 }       	        	 
            }                       
          }               
        }
        /*
          if(temp==true && temp1==true) {
  	        Map<String,String> leadershelter = new HashMap<String,String>();  
  	        leadershelter.put(shelterid, leaderid);            
  	        jedis.hmset("leadershelter", leadershelter); 
        	  
          }                         
         */
       // templeaderid="";     
        Map<String,String> map = jedis.hgetAll("defaultleadershelter");   
        for(Map.Entry entry: map.entrySet()) {   
             System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
        } 
    }
 
    public  void updateleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String oldleaderid =  ServletRequestUtils.getStringParameter(request,"oldleaderid");
         String newleaderid =  ServletRequestUtils.getStringParameter(request,"newleaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map.entrySet()) {   
              //System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         
         String segmentid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;

         				if(oldleaderid.equals(rs[j])){  
         					leaderids=leaderids+";"+newleaderid;
         				}
         				else{
         					leaderids=leaderids+";"+rs[j];
         				}	         				
         			}         		       		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadersegment = new HashMap<String,String>();  
         leadersegment.put(segmentid, leaderids);             
         jedis.hmset("leadersegment", leadersegment); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    
    public  void updatedefaultleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String oldleaderid =  ServletRequestUtils.getStringParameter(request,"oldleaderid");
         String newleaderid =  ServletRequestUtils.getStringParameter(request,"newleaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("defaultleadersegment");   
         for(Map.Entry entry: map.entrySet()) {   
              
         
         String segmentid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;

         				if(oldleaderid.equals(rs[j])){  
         					leaderids=leaderids+";"+newleaderid;
         				}
         				else{
         					leaderids=leaderids+";"+rs[j];
         				}	         				
         			}         		       		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadersegment = new HashMap<String,String>();  
         leadersegment.put(segmentid, leaderids);             
         jedis.hmset("defaultleadersegment", leadersegment); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("defaultleadersegment");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public  void updateshelterbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String oldleaderid =  ServletRequestUtils.getStringParameter(request,"oldleaderid");
         String newleaderid =  ServletRequestUtils.getStringParameter(request,"newleaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("leadershelter");   
         for(Map.Entry entry: map.entrySet()) {   
         
         String shelterid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;

         				if(oldleaderid.equals(rs[j])){  
         					leaderids=leaderids+";"+newleaderid;
         				}
         				else{
         					leaderids=leaderids+";"+rs[j];
         				}	         				
         			}        		       		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadershelter = new HashMap<String,String>();  
         leadershelter.put(shelterid, leaderids);             
         jedis.hmset("leadershelter", leadershelter); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadershelter");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public  void updatedefaultshelterbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String oldleaderid =  ServletRequestUtils.getStringParameter(request,"oldleaderid");
         String newleaderid =  ServletRequestUtils.getStringParameter(request,"newleaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("defaultleadershelter");   
         for(Map.Entry entry: map.entrySet()) {   
         
         String shelterid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;

         				if(oldleaderid.equals(rs[j])){  
         					leaderids=leaderids+";"+newleaderid;
         				}
         				else{
         					leaderids=leaderids+";"+rs[j];
         				}	         				
         			}         		       		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadershelter = new HashMap<String,String>();  
         leadershelter.put(shelterid, leaderids);             
         jedis.hmset("defaultleadershelter", leadershelter); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("defaultleadershelter");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public  void removeleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String tleaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
         String leaderid=tleaderid.split(",")[2]+","+tleaderid.split(",")[3];
            
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map.entrySet()) {   
                
         
         String segmentid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j].split(",").length<7) continue;                			
         			if(leaderid.equals(rs[j].split(",")[2]+","+rs[j].split(",")[3])){      				
         				}
         			else{
         				leaderids=leaderids+";"+rs[j];
         				}	         				         			
         		}         		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadersegment = new HashMap<String,String>();  
         leadersegment.put(segmentid, leaderids);             
         jedis.hmset("leadersegment", leadersegment); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    
    public  void removeleaderbyleaderidandsegmentorder(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String tleaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
         String leaderid=tleaderid.split(",")[2]+","+tleaderid.split(",")[3];
         String segmentorder= ServletRequestUtils.getStringParameter(request,"affectedsegmentorder");  
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map.entrySet()) {   
                
         
         String segmentid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j].split(",").length<7) continue;                			
         			if(leaderid.equals(rs[j].split(",")[2]+","+rs[j].split(",")[3]) && (Integer.valueOf(rs[j].split(",")[6])>=Integer.valueOf(segmentorder))){      				
         				}
         			else{
         				leaderids=leaderids+";"+rs[j];
         				}	         				         			
         		}         		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadersegment = new HashMap<String,String>();  
         leadersegment.put(segmentid, leaderids);             
         jedis.hmset("leadersegment", leadersegment); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    
    public  void removedefaultleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("defaultleadersegment");   
         for(Map.Entry entry: map.entrySet()) {   
              //System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         
         String segmentid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;                			
         			if(leaderid.equals(rs[j])){      				
         				}
         			else{
         				leaderids=leaderids+";"+rs[j];
         				}	         				         			
         		}         		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadersegment = new HashMap<String,String>();  
         leadersegment.put(segmentid, leaderids);             
         jedis.hmset("defaultleadersegment", leadersegment); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadersegment");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public  void removeshelterleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("leadershelter");   
         for(Map.Entry entry: map.entrySet()) {   
               
         
         String shelterid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;                			
         			if(leaderid.equals(rs[j])){      				
         				}
         			else{
         				leaderids=leaderids+";"+rs[j];
         				}	         				         			
         		}         		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadershelter= new HashMap<String,String>();  
         leadershelter.put(shelterid, leaderids);             
         jedis.hmset("leadershelter", leadershelter); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("leadershelter");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public  void removedefaultshelterleaderbyleaderid(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
         String leaderids="";
         String leaderid =  ServletRequestUtils.getStringParameter(request,"leaderid");
         
         Jedis jedis = new Jedis(Serviceip,Serviceport);  
         
         Map<String,String> map = jedis.hgetAll("defaultleadershelter");   
         for(Map.Entry entry: map.entrySet()) {   
     
         
         String shelterid =  entry.getKey().toString();           
         String[] rs =entry.getValue().toString().split(";");
         
         if(rs != null){         		
         		for(int j=0;j<rs.length;j++)
         		{
         			if(rs[j]=="") continue;                			
         			if(leaderid.equals(rs[j])){      				
         				}
         			else{
         				leaderids=leaderids+";"+rs[j];
         				}	         				         			
         		}         		         	       	
         }
         if(leaderids!="")
        	 leaderids=leaderids.substring(1, leaderids.length());
                       
         Map<String,String> leadershelter= new HashMap<String,String>();  
         leadershelter.put(shelterid, leaderids);             
         jedis.hmset("defaultleadershelter", leadershelter); 
         leaderids="";
         }
         
         Map<String,String> map1 = jedis.hgetAll("defaultleadershelter");   
         for(Map.Entry entry: map1.entrySet()) {   
              System.out.print(entry.getKey() + ":" + entry.getValue() + "\n");   
         } 
    }
    public void getleadersbysegmentid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                   
          
        List<String> rs = jedis.hmget("leadersegment", segmentid); 
        
        if(rs != null){       	
        	for(int i=0;i<rs.size();i++){
        	if(rs.get(i)=="" ||rs.get(i)==null) continue;
            	respstr=respstr+rs.get(i)+";";       			        		        			
        		
        	}                   	
        }
        if(respstr!="")
        	respstr=respstr.substring(0, respstr.length()-1);
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void getdefaultleadersbysegmentid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                   
             
        List<String> rs = jedis.hmget("defaultleadersegment", segmentid); 
        
        if(rs != null){       	
        	for(int i=0;i<rs.size();i++){
        	if(rs.get(i)=="" ||rs.get(i)==null) continue;
            	respstr=respstr+rs.get(i)+"'";       			        		        			
        		
        	}                   	
        }
        if(respstr!="")
        	respstr=respstr.substring(0, respstr.length()-1);
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void getleadersbyshelterid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String shelterid = ServletRequestUtils.getStringParameter(request,
                "shelterid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                              
        List<String> rs = jedis.hmget("leadershelter", shelterid); 
        
        if(rs != null){       	
        	for(int i=0;i<rs.size();i++){
        	if(rs.get(i)=="" ||rs.get(i)==null) continue;
            	respstr=respstr+rs.get(i)+"'";       			        		        			
        		
        	}                   	
        }
        respstr=respstr.substring(0, respstr.length()-1);
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void getpiandleaderipaddressbymacid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String leadermicid = ServletRequestUtils.getStringParameter(request,
                "leadermicid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                              
        List<String> rs = jedis.hmget("piandleaderipaddress", leadermicid); 
        
        if(rs != null){       	
        	for(int i=0;i<rs.size();i++){
        	if(rs.get(i)=="" ||rs.get(i)==null||rs.get(i)=="0") continue;
            	respstr=respstr+rs.get(i)+";";       			        		        			
        		
        	}                   	
        }
        respstr=respstr.substring(0, respstr.length()-1);
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void getdefaultleadersbyshelterid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String shelterid = ServletRequestUtils.getStringParameter(request,
                "shelterid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                              
        List<String> rs = jedis.hmget("defaultleadershelter", shelterid); 
        
        if(rs != null){       	
        	for(int i=0;i<rs.size();i++){
        	if(rs.get(i)=="" ||rs.get(i)==null) continue;
            	respstr=respstr+rs.get(i)+"'";       			        		        			
        		
        	}                   	
        }
        respstr=respstr.substring(0, respstr.length()-1);
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void findshelterbyleaderid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String leaderid = ServletRequestUtils.getStringParameter(request,
                "leaderid"); 
        
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	 String leaderids="";    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                   
        
        Map<String,String> map1 = jedis.hgetAll("leadershelter");   
        for(Map.Entry entry: map1.entrySet()) {
        	
            String shelterid =  entry.getKey().toString();           
            String[] rs =entry.getValue().toString().split(";");
            
            if(rs != null){         		
            		for(int j=0;j<rs.length;j++)
            		{
            			if(rs[j]=="") continue;                			
            			else if(rs[j].split(",").length==4){
            				if(leaderid.equals(rs[j].split(",")[0]+","+rs[j].split(",")[1]+","+rs[j].split(",")[2]+","+rs[j].split(",")[3])){  
            				respstr=shelterid;
            				}
            			else{
            				
            				}	         				            			
            		}         		         	       	
            }                                  	 
        } 
        }
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void findsegmentsbyleaderid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	        
        String leaderid = ServletRequestUtils.getStringParameter(request,
                "leaderid"); 
    	ArrayList<String> segmentsorts=new ArrayList<String>();
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	 String leaderids="";    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);                   
     
        Map<String,String> map1 = jedis.hgetAll("leadersegment");   
        for(Map.Entry entry: map1.entrySet()) {
        	
            String segmentid =  entry.getKey().toString();           
            String[] rs =entry.getValue().toString().split(";");
            
            if(rs != null){         		
            		for(int j=0;j<rs.length;j++)
            		{
            			if(rs[j]=="") continue; 
            			else if(rs[j].split(",").length==7){
            				if(leaderid.equals(rs[j].split(",")[1]+","+rs[j].split(",")[2]+","+rs[j].split(",")[3]+","+rs[j].split(",")[4])){ 
            					segmentsorts.add(rs[j].split(",")[6]+","+segmentid);
            					respstr=respstr+","+segmentid;
            				}	
            			}
            		}         		         	       	
            }                                  	 
        } 
        if(respstr!="")
        	respstr=respstr.substring(1, respstr.length());
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void findsafetydefaultroute(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="0"; 	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  
    	        
        String[] defaultroute = ServletRequestUtils.getStringParameter(request,
                "defaultroute").split(";")[0].split(","); 
        
        for(int k=0;k<defaultroute.length;k++){
        	                                               
            List<String> rs = jedis.hmget("affectedsegment", defaultroute[k]); 
            
            if(rs != null){       	
            	for(int i=0;i<rs.size();i++){
            	if(rs.get(i)=="" ||rs.get(i)==null) continue;
                	if(rs.get(i).equals("1"))
                		respstr="1";
            		
            	}                   	
            }
        }
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);            	
    }
    public void notificationleadersbyshelterid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
        String shelterid = ServletRequestUtils.getStringParameter(request,
                "shelterid");
        String leaderid = ServletRequestUtils.getStringParameter(request,
                "leaderid");
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="failure";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs = jedis.hmget("leadershelter", shelterid); 
        
        if(rs != null){    //sample: 41:1,000000000000,localhost:22223,136.142.186.102:22227    	
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
            				 
            				    String tempstr="16;"+ip+":"+port+";#;###";
            				    int strlen=tempstr.length();
            				    out.println("16;"+ip+":"+port+";#;###");   
            				    
            				    respstr="success";
            				                				    
        					}catch (Exception e) {
        						
        					}         				
        				       			       			        		        			
        			}       		                 	
        	}
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);        
        }    	
    }  
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
        /*
        Map<String,String> leadersegment = new HashMap<String,String>();  
        leadersegment.put("10000", "150.212.2.138:22223");          
        jedis.hmset("leadersegment", leadersegment);   
        */
        List<String> rs = jedis.hmget("leadersegment", segmentid); 
        
        if(rs != null){        	
        	for(int i=0;i<rs.size();i++){
        		if(rs.get(i)=="" ||rs.get(i)==null) continue;//[Pi ip, Pi port; L ip, L port]---192.168.1.113,22226,192.168.18.38,22223
        			String[] leaderids= rs.get(i).split(";");
        			for(int j=0;j<leaderids.length;j++){
        				String[] templeader=leaderids[j].split(":");
        				if(templeader.length==2){
        					String ip = templeader[0];
        					int port = Integer.parseInt(templeader[1]);
        		        
        					try
        					{             		    		        		    		
        						//Thread s=new SendThread(ip,port,str);   
        						//s.start();  
        						
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
        			if(leaderips.length==7){
        				String[] temppi=leaderips[4].split(":");
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
        							String tempstr="12;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";"+leaderips[5]+";"+leaderips[6]+";###";
        							int strlen=tempstr.length();          							
        							out.println(strlen+";12;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";"+leaderips[5]+";"+leaderips[6]+";###");            				    
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
    public void notificationdetourbysegmentid(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
    	
    	List<String> leaders= new ArrayList<String>();
    	List<String> shelters= new ArrayList<String>();
    	List<String> macs= new ArrayList<String>();
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
        String segmentid = ServletRequestUtils.getStringParameter(request,
                "segmentid");
        String startpt="";
		   
		String segmentcoor=getCCinfo("http://"+Serviceip+":8080/ga/roadsegment?where=&objectIds="+segmentid+"&returnGeometry=true&f=pjson",InetAddress.getByName(Serviceip),22225);
    	
		
	    JSONObject jsonObject = new JSONObject(segmentcoor);	     
	    JSONArray jsonArray = jsonObject.getJSONArray("features");
	    for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject object = jsonArray.getJSONObject(i);
	        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");

	        JSONArray  path=jsonObjectgeometry.getJSONArray("paths");
	        
		      for (int j = 0; j < path.length(); j++) {
		    		 
					JSONArray jsonArray1 = path.getJSONArray(j);
					for (int k = 0; k < jsonArray1.length(); k++) {
						JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
						
						Object jsonArray3 = jsonArray2.get(0);
						Object jsonArray4 = jsonArray2.get(1);
						
						startpt=jsonArray3.toString()+","+jsonArray4.toString();
						//double sx=Double.parseDouble(jsonArray3.toString());
						//double sy=Double.parseDouble(jsonArray4.toString());										
												
					}												
		    }
	      }
	      
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="";
    	    	
        Jedis jedis = new Jedis(Serviceip,Serviceport);  

        List<String> rs0 = jedis.hmget("leadersegment", segmentid); 
        
        if(rs0 != null){        	
        	for(int i=0;i<rs0.size();i++){
        		if(rs0.get(i)=="" ||rs0.get(i)==null) continue;
        		String[] leaderids= rs0.get(i).split(";");
        		for(int k=0;k<leaderids.length;k++){
            		
        			String[] leaderips= leaderids[k].split(",");
        			if(leaderips.length==7){
        				if(shelters.contains(leaderips[0])) continue;  //41,3,88708ca4537d,192.168.19.12:22223,192.168.1.111:22227
        					
        				else
        					shelters.add(leaderips[0]);
        				if(macs.contains(leaderips[2])) continue;
        				else
        				{
        					macs.add(leaderips[2]);
        					leaders.add(leaderips[3]+","+leaderips[4]);
        				}
        				
        				/*
        				String[] temppi=leaderips[4].split(":");
        				String[] templeader=leaderips[3].split(":");
        				if(temppi.length==2){
        					String piip = temppi[0];
        					int piport = Integer.parseInt(temppi[1]);
        					String leaderip=templeader[0];
        					int leaderport=Integer.parseInt(templeader[1]);
        				    try
        						{             		    		        		    		        						
        							outputSocket = new Socket(piip, piport);
        							System.out.println("Connected on " +piip+":"+ piport);

        							out = new PrintWriter(new BufferedWriter(
            							new OutputStreamWriter(outputSocket.getOutputStream())),
            							true);		    
        							
        							out.println("12;"+leaderips[0]+";"+leaderips[1]+";"+leaderips[2]+";###");            				    
        							respstr="success";
            				                				    
        						}catch (Exception e) {        						
        					}         				
        				}  
        				*/ 
        			}          			
        		
        		}
      		                 	
        	}
       
        }
        
        String sheltcoor="";
        String stops="";
        for(int l=0;l<shelters.size();l++){
        	
    		String sheltercoor=getCCinfo("http://"+Serviceip+":8080/ga/shelters?where=&objectIds="+shelters.get(l)+"&returnGeometry=true&f=pjson",InetAddress.getByName(Serviceip),22225);
        	
    		
    	    JSONObject jsonObject0 = new JSONObject(sheltercoor);	     
    	    JSONArray jsonArray0 = jsonObject0.getJSONArray("features");
    	    for (int m = 0; m < jsonArray0.length(); m++) {
    	    	
    	        JSONObject object0 = jsonArray0.getJSONObject(m);
    	        JSONObject jsonObjectgeometry = object0.getJSONObject("geometry");    	        
		        double x=jsonObjectgeometry.getDouble("x");
		        double y= jsonObjectgeometry.getDouble("y");

		        sheltcoor=x+","+y;
    	      }
    	    stops=startpt+";"+sheltcoor;
    	    
    	    String polylinebarriers=getCCinfo("http://"+Serviceip+":8080/ga/keyvaluestore?action=showaffectedbarriers",InetAddress.getByName(Serviceip),22225); 
				 
			 
			String result2=getCCinfo("http://"+Serviceip+":8080/ga/naroute?stops="+stops+
						"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
				   		"&defaultTargetFacilityCount=1&outSR=102100&impedanceAttributeName=Length" +
				   		"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=true&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
				   		"&returnPolylineBarriers=false&returnPolygonBarriers=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
				   		"&outputLines=esriNAOutputLineTrueShapeWithMeasure&outputGeometryPrecisionUnits=esriDecimalDegrees" +
				   		"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson",InetAddress.getByName(Serviceip),22225);												   															
			   												   													   																				
			if(result2!=""){
				respstr=result2;
			}
    	    
        	
        }
        
        byte[] resp = respstr.getBytes("UTF-8");
        outputStream.write(resp);  
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
            					String[] temppi=leaderips[4].split(":");
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
            							
            							String tempstr="15;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";###";
            							int strlen=tempstr.length();
            							
            							System.out.println(strlen+";"+tempstr);
            							out.println(strlen+";15;"+leaderips[1]+";"+leaderips[2]+";"+leaderips[3]+";###"); 
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
    public void udpmulticastwarning(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
        String str = ServletRequestUtils.getStringParameter(request,
                "str");         
    	
    	OutputStream outputStream = response.getOutputStream();
    	String respstr="OK";
    	 
		String FormatedData = "";	
		// TODO Auto-generated method stub
		MulticastSocket multicastSocket;
		try {
			multicastSocket = new MulticastSocket();
			InetAddress address = InetAddress.getByName("239.1.1.171"); 
	        multicastSocket.joinGroup(address); 
	        System.out.println("UDP Multicast Server Start Success");
	        
	        	if(str!=null && str!="")
	        		 FormatedData = str;//"23;1;0;199;01;1,2,3,4;5";
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
    public void getenews(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
        String page = ServletRequestUtils.getStringParameter(request,"page");    
        String date = ServletRequestUtils.getStringParameter(request,"date");   
  	
    	OutputStream outputStream = response.getOutputStream();
    	
		 String url="http://www.dawn.com/newspaper/"+page+"/"+date; 
		 	String result="";
	        try{
	        	InetAddress hosaddresss= InetAddress.getByName("192.168.1.110");
	        	int port =22225;
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

byte[] resp = result.getBytes("UTF-8");
outputStream.write(resp);   
		 
}
    public void getenewsdetails(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
    	   	
    	Socket outputSocket = null;
    	PrintWriter out = null; 
        String cid = ServletRequestUtils.getStringParameter(request,"cid");    
        String  uid = ServletRequestUtils.getStringParameter(request,"uid");   
  	
    	OutputStream outputStream = response.getOutputStream();
    	
		 String url="http://www.dawn.com/news/"+cid+"/"+uid; 
		 	String result="";
	        try{
	        	InetAddress hosaddresss= InetAddress.getByName("192.168.1.110");
	        	int port =22225;
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

byte[] resp = result.getBytes("UTF-8");
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
	 
public void gettime(HttpServletRequest request, HttpServletResponse response) throws Exception,  
     java.io.IOException {  
	
		for (int i = 0; i < 5; i++) {  
     try {  
         Thread.sleep(1000 * Integer.valueOf(1));  
     } catch (NumberFormatException e) {  
         e.printStackTrace();  
     } catch (InterruptedException e) {  
         e.printStackTrace();  
     }  
       
     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss E");  
     String date_str = df.format(new Date());  
       
     //writerResponse(response, date_str, "showServerTime");
     
     StringBuffer sb = new StringBuffer();  
     sb.append("<script type=\"text/javascript\">//<![CDATA[\n");  
     sb.append("     parent.").append("showServerTime").append("(\"").append(date_str).append("\");\n");  
     sb.append("//]]></script>");  
     System.out.println(sb.toString());  
     
     response.getWriter().write(date_str.toString());  
     response.flushBuffer();  

 }  
 

 return;  
}  

protected void writerResponse(HttpServletResponse response, String body, String client_method) throws IOException {  
 StringBuffer sb = new StringBuffer();  
 sb.append("<script type=\"text/javascript\">//<![CDATA[\n");  
 sb.append("     parent.").append(client_method).append("(\"").append(body).append("\");\n");  
 sb.append("//]]></script>");  
 System.out.println(sb.toString());  

 response.setContentType("text/html;charset=UTF-8");  
 response.addHeader("Pragma", "no-cache");  
 response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");  
 response.setHeader("Cache-Control", "pre-check=0,post-check=0");  
 response.setDateHeader("Expires", 0);  
 response.getWriter().write(sb.toString());  
 response.flushBuffer();  
 
}

//from CC to HPi--warning/detour with dynamic region id
public void UDPMulticast(HttpServletRequest request,
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

//from CC to affected HPi
public void UDPUnicast(HttpServletRequest request,
	        HttpServletResponse response)  throws Exception {
	
		DatagramSocket datagramSocket = null; 
		Random random = new Random();
		String uuid = UUID.randomUUID().toString();

		try {				
			datagramSocket = new DatagramSocket(random.nextInt(9999));
			datagramSocket.setSoTimeout(10 * 1000);
			System.out.println("UDP Unicast Sender start success");
			
			byte[] buffer = new byte[1024 * 64]; 
			
			String formantdata="23;1;0;199;01;1,2,3,4,5:6";
			byte[] btSend =formantdata.getBytes("UTF-8");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("130.49.219.87"), 7385);
			packet.setData(btSend);
			System.out.println(uuid + ":send:" + Arrays.toString(btSend));
			try {
				datagramSocket.send(packet);
			} catch(Exception e){
				e.printStackTrace();
			}
			
			datagramSocket.receive(packet);
			byte[] bt = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
			if(null != bt && bt.length > 0){
				System.out.println(uuid + ":receive:" + Arrays.toString(bt));
			}
			Thread.sleep(1 * 1000);
			datagramSocket.close();
		} catch (Exception e) {
			datagramSocket = null;
			System.out.println("UDP Unicast Sender start fail");
			e.printStackTrace();
		}
}

}
