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

import java.text.NumberFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pitt.server.console.controller.ShelterAllocatorController.shelter;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map; 
import java.util.Random;

public class PathFinderController extends MultiActionController {

    private static final String Serviceip="10.130.10.58";
    private static final int Serviceport=8080;
    private static final String  TcpSocketAddress="10.130.10.58";
    private static final int TcpSocketPort=22225;
    BufferedReader in = null;
    PrintWriter out = null;
    Socket sk = null;
    Exception mException = null;
    
    public class shelter {  
        int id;
        double x;
  		double y;        
        }
    public class roadsegment {  
        int id;  
        JSONArray paths;
        }
    
    ArrayList<shelter> routesegments; 
    
    ArrayList<String> segmentsorts;

	public PathFinderController() {
		
    }
	
	/*
	 * shelter allocator
	 * Function: find closest shelter for the leader
	 * parameter leader location
	 * cmd=1
	 */
	public void pathfinder(HttpServletRequest request,
	            HttpServletResponse response)  throws Exception {
		
		    String segmentids="", segmentstartpts="", tpath="";
			int affectedsegmentorder=0;
	    	
	    	OutputStream outputStream = response.getOutputStream();
	    	String respstr = "OK";
	    	
	    	String roadsegment = "";
	    	String[] stops  = ServletRequestUtils.getStringParameter(request,"stops").split(";");  	        
	        double llx = Double.valueOf(stops[0].split(",")[0]);
	        double lly = Double.valueOf(stops[0].split(",")[1]);        
	        double slx = Double.valueOf(stops[1].split(",")[0]);
	        double sly = Double.valueOf(stops[1].split(",")[1]);
	        
	        String leaderid = ServletRequestUtils.getStringParameter(request,
	                "leaderid");
	        
	        String envelopcoor=(llx-0.0005)+","+(lly-0.0005)+","+(llx+0.0005)+","+(lly+0.0005);
	        
	        String closestjunctionresult=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/closestjunction?&geometry="+envelopcoor+"&geometryType=esriGeometryEnvelope"
					+ "&inSR=4326&spatialRel=esriSpatialRelContains&outFields=OBJECTID&returnGeometry=true&returnTrueCurves=false&returnIdsOnly=false&returnCountOnly=false&returnZ=false"
					+ "&returnM=false&returnDistinctValues=false&f=pjson&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort);
	        			
			double dis=999999;
		    double startx=0,starty=0;
			if(closestjunctionresult!=""){
				
			   	  JSONObject jsonObject = new JSONObject(closestjunctionresult);	     
			      JSONArray jsonArray = jsonObject.getJSONArray("features");
			      for (int i = 0; i < jsonArray.length(); i++) {
			        JSONObject object = jsonArray.getJSONObject(i);

			        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
			        

			        double x=jsonObjectgeometry.getDouble("x");
			        double y= jsonObjectgeometry.getDouble("y");
			        
			       if(Math.sqrt(Math.pow((x-llx),2)+Math.pow((y-lly),2))<dis)
			        {
			    	   dis=Math.sqrt(Math.pow((x-llx),2)+Math.pow((y-lly),2));
			    	   startx=x;
			    	   starty=y;			    	   
			        }
			      }				
			}
			
			String incidentpt=startx+","+starty+";"+slx+","+sly;
			//incidentpt=leaderlocaiton.getX()+","+leaderlocaiton.getY()+";"+tsegment.getX()+","+tsegment.getY();
			
			//query envelope								
			  String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+Serviceip+"%3A"+Serviceport+"%2Fga%2Fshelters%3Fwhere%3Davailable%253D1%26returnGeometry%3Dtrue%26outFields%3DName%26f%3Dpjson%22%0D%0A%7D";
			//polyline barriers or key value store 	
			String polylinebarriers=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/keyvaluestore?action=showaffectedbarriers&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort); 	
			
									   																	
			
			String safetypath=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/naroute?stops="+incidentpt+
					"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
					"&defaultTargetFacilityCount=1&outSR=4326&impedanceAttributeName=Length" +
					"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=false&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
					"&returnPolylineBarriers=false&returnPolygonBarriers=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
					"&outputLines=esriNAOutputLineTrueShape&outputGeometryPrecisionUnits=esriDecimalDegrees" +
					"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort); 
			
						
			if(safetypath!="")	
			{
				routesegments= new ArrayList<shelter>();
				  segmentsorts = new ArrayList<String>();
					NumberFormat nFormat=NumberFormat.getNumberInstance(); 
					nFormat.setMaximumFractionDigits(9);
			
			   	  JSONObject jsonObject = new JSONObject(safetypath);	
			   	  JSONObject jsonObject1 =  jsonObject.getJSONObject("routes");
			   	 //JSONObject jsonObject2 = jsonObject1.getJSONObject("features");
			      JSONArray jsonArray = jsonObject1.getJSONArray("features");
			      for (int i = 0; i < jsonArray.length(); i++) {
			        JSONObject object = jsonArray.getJSONObject(i);

			        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
			        JSONArray jsonArraypaths = jsonObjectgeometry.getJSONArray("paths");
			        JSONArray jsonArraypath = jsonArraypaths.getJSONArray(0);

			        //get road segments of path
			     
					
					for(int k=0;k<jsonArraypath.length();k++ )
					{
						JSONArray jsonArraypt1 = jsonArraypath.getJSONArray(k);
						
									
						Object jsonArraypt1x = jsonArraypt1.get(0);
						Object jsonArraypt1y = jsonArraypt1.get(1);
						
						double ex1=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt1x.toString())));
						double ey1=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt1y.toString())));
						 
						 tpath+="["+Double.toString(ex1)+","+Double.toString(ey1)+"],";
						 //manage the route conjection info
					     shelter st=new shelter();
					     st.id=i;
					     st.x=Double.parseDouble(nFormat.format(ex1));
					     st.y=Double.parseDouble(nFormat.format(ey1));				    
					     routesegments.add(st);
						
					}
					
					for(int l=0;l<jsonArraypath.length()-1;l++ )
					{
				
					JSONArray jsonArraypt1 = jsonArraypath.getJSONArray(l);
					JSONArray jsonArraypt2 = jsonArraypath.getJSONArray(l+1);
								
					Object jsonArraypt1x = jsonArraypt1.get(0);
					Object jsonArraypt1y = jsonArraypt1.get(1);
					
					double ex1=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt1x.toString())));
					double ey1=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt1y.toString())));
					
					Object jsonArraypt2x = jsonArraypt2.get(0);
					Object jsonArraypt2y = jsonArraypt2.get(1);
					
					double ex2=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt2x.toString())));
					double ey2=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArraypt2y.toString())));
					
					String sgeometry ="["+Double.toString(ex1)+","+Double.toString(ey1)+"],["+Double.toString(ex2)+","+Double.toString(ey2)+"]";
					
					try {

					   String result=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+sgeometry+"]],\"spatialReference\":{\"wkid\":4326}}" +
						   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelWithin&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
						   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort); 	
					   
					   if(result!=""){	
						   JSONObject jsonObjectsegment = new JSONObject(result);	     
						      JSONArray jsonArraysegment = jsonObjectsegment.getJSONArray("features");
						      for (int m = 0; m < jsonArraysegment.length(); m++) {
						        JSONObject objectsegment = jsonArraysegment.getJSONObject(m);
						        JSONObject jsonObjectattribute = objectsegment.getJSONObject("attributes");
						        int id= jsonObjectattribute.getInt("OBJECTID");
						        
						        //get start point coor in each segment
						        JSONObject jsonObjectgeometrysegment = objectsegment.getJSONObject("geometry");
						        JSONArray  path=jsonObjectgeometrysegment.getJSONArray("paths");
						        
							     for (int k = 0; k < path.length(); k++) {
		    				    		 
										JSONArray jsonArray1 = path.getJSONArray(k);
										
										
										JSONArray jsonArray2 = jsonArray1.getJSONArray(0);
										JSONArray jsonArray5 = jsonArray1.getJSONArray(jsonArray1.length()-1);
											
										Object jsonArray3 = jsonArray2.get(0);
										Object jsonArray4 = jsonArray2.get(1);									
										Object jsonArray6 = jsonArray5.get(0);
										Object jsonArray7 = jsonArray5.get(1);
												
										double sx=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArray3.toString())));
										double sy=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArray4.toString())));	
										
										double ex=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArray6.toString())));
										double ey=Double.parseDouble(nFormat.format(Double.parseDouble(jsonArray7.toString())));	
										int sid=99999,eid=99999;
										
										for (shelter st : routesegments) {
											if(st.x==sx && st.y==sy){														
												sid=st.id;
												
											}
											if(st.x==ex && st.y==ey){														
												eid=st.id;
												
											}
										}										
										
										if(sid<eid)
										{										
											
											if(segmentsorts.contains(String.valueOf(sid)+","+String.valueOf(id)+","+sx+":"+sy)){}
											else
												segmentsorts.add(String.valueOf(sid)+","+String.valueOf(id)+","+sx+":"+sy);
											
										}
										else
										{
											if(segmentsorts.contains(String.valueOf(eid)+","+String.valueOf(id)+","+ex+":"+ey)){}
											else	
												segmentsorts.add(String.valueOf(eid)+","+String.valueOf(id)+","+ex+":"+ey);
										}
																																							
							    }
						        
						      }								    													  
					   }
					   				  												   										
				} catch (Exception e1) {
					
					e1.printStackTrace();
				} 
					
					}
				  
				
			        for (int n = 0; n < segmentsorts.size() - 1; n++) {  
			            for (int j = 1; j < segmentsorts.size() - n; j++) {  
			                String temp;  
			               
			                if (Integer.valueOf(segmentsorts.get(j - 1).split(",")[0])-Integer.valueOf(segmentsorts.get(j).split(",")[0]) > 0) { 
			                    temp = segmentsorts.get(j - 1);  
			                    segmentsorts.set((j - 1), segmentsorts.get(j));  
			                    segmentsorts.set(j, temp);  
			                }  
			            }  
			        }

			        for (int k= 0; k < segmentsorts.size(); k++) {  
			        	 segmentids=segmentids+","+segmentsorts.get(k).split(",")[1];
			        	 segmentstartpts=segmentstartpts+","+segmentsorts.get(k).split(",")[2];
			        }
			        
					   segmentids=segmentids.substring(1, segmentids.length());
					   segmentstartpts=segmentstartpts.substring(1, segmentstartpts.length());
						   
						   
					//get shelter id
					int shelterid=0;
					String shelterinfo="";
					
					
					JSONArray jsonArray1 = jsonArraypaths.getJSONArray(jsonArraypaths.length()-1);
					JSONArray jsonArray5 = jsonArray1.getJSONArray(jsonArray1.length()-1);
								
					Object jsonArray6 = jsonArray5.get(0);
					Object jsonArray7 = jsonArray5.get(1);
					
					double ex=Double.parseDouble(jsonArray6.toString());
					double ey=Double.parseDouble(jsonArray7.toString());
					
					
					String resultshelter=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/shelters?where=&geometry="+ "%7B"+(ex-0.001)+"%2C"+(ey-0.001)+"%2C"+(ex+0.001)+"%2C"+(ey+0.001)+"%7D"+
					    		"&geometryType=esriGeometryEnvelope&inSR=4326&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=OBJECTID_1%2CNAME%2CADDRESS&returnGeometry=true"
					    		+ "&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort); 						   
					   
					if(resultshelter!=""){	
						   	  JSONObject jsonObjectshelter = new JSONObject(resultshelter);	     
						      JSONArray jsonArrayshelter = jsonObjectshelter.getJSONArray("features");
						      for (int k = 0; k < jsonArrayshelter.length(); k++) {
						        JSONObject objectshelter = jsonArrayshelter.getJSONObject(k);
						        JSONObject jsonObjectattribute = objectshelter.getJSONObject("attributes");						        						        
						        shelterid= jsonObjectattribute.getInt("OBJECTID_1");
						        shelterinfo="Name>"+jsonObjectattribute.getString("NAME")+"%Address>"+jsonObjectattribute.getString("ADDRESS")
						         			+"%location>"+ex+","+ey;
						      }							    													  
					   }
					
				//save segment and shelter in CC					 
				socketservice("http://"+Serviceip+":"+Serviceport+"/ga/keyvaluestore?action=addshelterbyleader&shelterid="+shelterid+"&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort); 		  
							 
				socketservice("http://"+Serviceip+":"+Serviceport+"/ga/keyvaluestore?action=addleaderbysegmentorder&segmentid="+segmentids+"&affectedsegmentorder="+affectedsegmentorder+"&segmentstartpts="+segmentstartpts+"&leaderid="+shelterid+","+leaderid,TcpSocketAddress,TcpSocketPort); 		
							
				respstr=segmentids+":"+shelterid+":"+shelterinfo;
				
			    }
			}	
			byte[] resp = respstr.getBytes("UTF-8");
	        outputStream.write(resp); 
	}
	
	public void riskreport(HttpServletRequest request,
            HttpServletResponse response)  throws Exception {
		
    	OutputStream outputStream = response.getOutputStream();
    	String respstr = "OK", msg="";
    	
		int roadsegmentid;
		String paths  = ServletRequestUtils.getStringParameter(request,"paths"); 
		String leaderid  = ServletRequestUtils.getStringParameter(request,"leaderid");
		String reporttype  = ServletRequestUtils.getStringParameter(request,"risktype");
		
		
		String resultsegment=socketservice("http://"+Serviceip+":"+Serviceport+"/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+paths+"]],\"spatialReference\":{\"wkid\":4326}}" +
		   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelIntersects&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
		   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort);
		
		   if(resultsegment!=""){
			   
		   		JSONObject jsonObject;						
				jsonObject = new JSONObject(resultsegment);
				JSONArray jsonArray = jsonObject.getJSONArray("features");
			    for (int i = 0; i < jsonArray.length(); i++) {
			        JSONObject object = jsonArray.getJSONObject(i);
			        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
			        roadsegmentid= jsonObjectattribute.getInt("OBJECTID");	
			        
					   if (roadsegmentid>0){
						   
							   
							respstr=Integer.toString(roadsegmentid);
					     
						   						   
						   //**********
						    if(reporttype.equals("s")){	
						    	
						    	//send udp multcast detour warning to the affected Leader  through HPI, MPI   
						    	socketservice("http://"+Serviceip+":"+Serviceport+"/ga/communication?action=udpunicastnewpath&shelterid="+roadsegmentid+"&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort);						   										   																				   	        					
						    }
						    if(reporttype.equals("r")||reporttype.equals("b")){			    				 				 	
						 	
					        	//add segment to CC
						    	socketservice("http://"+Serviceip+":"+Serviceport+"/ga/keyvaluestore?action=addaffectedsegment&segmentid="+roadsegmentid+"&affectedcoor="+paths+"&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort);	
					        	
					        	//notification from CC to pi to leader								        																	
						    	socketservice("http://"+Serviceip+":"+Serviceport+"/ga/communication?action=udpunicastdetour&segmentid="+roadsegmentid+"&leaderid="+leaderid,TcpSocketAddress,TcpSocketPort);							
					       		        
						    }
						  //**************
						    
						    

					   }
			      }	
			} 
		   
			byte[] resp = respstr.getBytes("UTF-8");
	        outputStream.write(resp); 	   	
	}
	  	
	 public String socketservice(String url, String hosaddresss, int port ){
		 
		 	String result="";
	        try{
	        	//InetAddress.getByName(tip)
	        	InetAddress addresss=InetAddress.getByName(hosaddresss); 
			   sk = new Socket (addresss, port);
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


}
