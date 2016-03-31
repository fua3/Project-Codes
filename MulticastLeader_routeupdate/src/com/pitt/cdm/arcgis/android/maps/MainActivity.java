package com.pitt.cdm.arcgis.android.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.http.conn.util.InetAddressUtils;

import com.esri.core.internal.tasks.ags.s;
import com.pitt.cdm.arcgis.android.attributeeditor.AttributeEditorActivity;
import com.pitt.cdm.arcgis.android.featurelayer.ShowfacilitiesLayerActivity;
import com.pitt.cdm.arcgis.android.featurelayer.ShowriskLayerActivity;
import com.pitt.cdm.arcgis.android.geometryeditor.GeometryEditorActivity;
import com.pitt.cdm.arcgis.android.networkanalysis.ClosestFacilitiesActivity;
import com.pitt.cdm.arcgis.android.networkanalysis.ClosestShelterActivity;
import com.pitt.cdm.arcgis.android.networkanalysis.RoutingdefaultActivity;
import com.pitt.cdm.arcgis.android.networkanalysis.RoutingtoshelterActivity;
import com.pitt.cdm.arcgis.android.networkanalysis.ServiceAreaActivity;
import com.pitt.cdm.offlinemapupdate.DownLoaderTask;
import com.pitt.cdm.offlinemapupdate.PropertiesUtil;
import com.pitt.cdm.offlinemapupdate.ZipExtractorTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;  
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;  
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.view.View;





public class MainActivity extends Activity {
	
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
	Thread serverThread = null;
	int SERVERPORT = 22227;
	String username,serverport,tempmsg="";
	private TextView text;
	
	//private static final int REQUEST_CODE=1;
	
	TextView textView;  
	ImageButton imageButton1,imageButton2,imageButton3,imageButton4,
	imageButton5,imageButton6,imageButton7,imageButton8,imageButton9; 
	
	//for offline map download
	public static final String ROOT_MAP = "/mnt/sdcard/ArcGIS/kelurahan_102100.tpk";

	public static final String ROOT_DIR = "/mnt/sdcard/Download";
	private final String TAG="MainActivity";
	TextView downloadinfo;
	ImageButton downloadmap;
	Button button3,button4;
	Spinner userrole, userregion;
	int usertype,regionid;
	
	

	 final String[] m_Role = { "Leader", "Follower" }; 
	 final String[] m_Region = {"Airtawar_Timur", "Bungo_Pasang","Koto_Pulai", "Parupuk_Tabing","Pasir_Nan_Tigo"};   
	 private RadioOnClick radioOnClick = new RadioOnClick(1); 	
	 String selected_name;
	 int selected_id;
	 String regionname;
	    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   
       
       //username=getLocalIPAddress()+":22223";
       username="localhost:22223";
      
        //textView = (TextView)findViewById(R.id.TextView01);  
        imageButton2 = (ImageButton)findViewById(R.id.imagebutton2);   
        imageButton4 = (ImageButton)findViewById(R.id.imagebutton4);   
        imageButton5 = (ImageButton)findViewById(R.id.imagebutton6);  
        imageButton8 = (ImageButton)findViewById(R.id.imagebutton8);  
        imageButton9 = (ImageButton)findViewById(R.id.imagebutton9);  
        

       // button3=(Button)findViewById(R.id.button3);  
       // button4=(Button)findViewById(R.id.button4); 
        //for map download
        
     
        
        downloadinfo= (TextView)findViewById(R.id.textView7);
        downloadmap= (ImageButton)findViewById(R.id.imagebutton3);
        /*
        userrole= (Spinner)findViewById(R.id.spinnerrole);
        userregion= (Spinner)findViewById(R.id.spinneregion);
        
       
        
        ArrayAdapter adapterrole,adapterregion; 
   
        adapterrole = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_Role);  
       
        adapterrole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
            
        userrole.setAdapter(adapterrole);
        
        
             
        adapterregion = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_Region);  
        
        adapterregion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
            
        userregion.setAdapter(adapterregion);
       */         
        configchange();

        imageButton2.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
                    Intent intent = new Intent();
        			intent.setClass(MainActivity.this, RoutingtoshelterActivity.class);
        			intent.putExtra("Routing", "Route analysis");
        			startActivity(intent);
       		
            }  
        });
        
        imageButton9.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	showRoleDialog();
       		
            }  
        });
        
        imageButton8.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	showRegionDialog();
       		
            }  
        });
        /*
        button3.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	showRoleDialog();
            }
            

        });
        
        button4.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	showRegionDialog();
    	
            } 

        });*/
       /* 
        userrole.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String userrole = m_Role[arg2];
         
            
            if(userrole.equals("Leader"))
            	usertype=0;
            else
            	usertype=1;

            arg0.setVisibility(View.VISIBLE);
            }
           @Override
           public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            }
            });
        
        userregion.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String userregion = m_Region[arg2];
            
            if(userregion.equals("Airtawar Timur"))
            	regionid=0;
            else if(userregion.equals("Bungo Pasang"))
            	regionid=1;
            else if(userregion.equals("Koto Pulai"))
            	regionid=2;
            else if(userregion.equals("Parupuk Tabing"))
            	regionid=3;
            else
            	regionid=4;

            arg0.setVisibility(View.VISIBLE);
            }
           @Override
           public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            }
            });
        
        */
        downloadmap.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	
            	//get user role and region type
                 
                 PropertiesUtil.setProperties("regionid", regionid+"");
                 PropertiesUtil.setProperties("usertype", usertype+"");
                 
                 //download                
        		showmapDownLoadDialog(regionid);
        		mopo();
      		
            }  
        });
        
        imageButton4.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
            	try{
            		       			
                    Intent intent = new Intent();
        			intent.setClass(MainActivity.this, GeometryEditorActivity.class);      			
        			intent.putExtra("Goemetry edit", "Geometry Edit");
        			intent.putExtra("username", username); 

        			startActivity(intent);
            	}catch(Exception e){
            		
            	}
            	
            }  
        });
        imageButton5.setOnClickListener(new Button.OnClickListener(){  
            public void onClick(View v) {  
                    Intent intent = new Intent();
        			intent.setClass(MainActivity.this, AttributeEditorActivity.class);
        			intent.putExtra("Shelter Manage", "Shelter Manage");
                	        			    			    	 	           		 		 
    		        String username=intent.getStringExtra("username"); 
        			intent.putExtra("username", username);
        			startActivity(intent);       		
            }  
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
    //get leader login ip
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
			Log.e("GetIP", ex.toString());
		}
		return null;
	}
    
    public static boolean GetMapFile(String path){
    	if(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
    	   File file = new File(path);
    	   if(!file.exists()){
    	      return false;
    	    }
    	}
    	   return true;
    	}
    

	/**
     * finish operation
     */
    private boolean mopo() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			file();
		return true;
		} else {
			new AlertDialog.Builder(MainActivity.this).setTitle("download & upzip:")
	        .setMessage("update map").setIcon(R.drawable.ic_launcher)
	        .setPositiveButton("close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					 finish();					 
		            	try{		           			
		                    Intent intent = new Intent();
		        			intent.setClass(MainActivity.this, GeometryEditorActivity.class);      			
		        			intent.putExtra("Goemetry edit", "Geometry Edit");
		        			intent.putExtra("regionid", regionid);
		        			intent.putExtra("username", username); 
		        			startActivity(intent);
		            	}catch(Exception e){
		            		
		            	}
				}

			}).show();
		}
	
		return false;
	}
    
    /**
     * create file path
     */
	private void file() {
		File destDir = new File(ROOT_DIR);
		if (!destDir.exists()) {
		destDir.mkdirs();
		}
	}
	private void showRoleDialog(){
		   AlertDialog ad =new AlertDialog.Builder(MainActivity.this).setTitle("Role")  
				   .setSingleChoiceItems(m_Role,radioOnClick.getIndex(0),radioOnClick) 
				   //areaRadioListView=ad.getListView();  
		.show();
	}
	
	private void showRegionDialog(){
		   AlertDialog ad =new AlertDialog.Builder(MainActivity.this).setTitle("Region")  
				   .setSingleChoiceItems(m_Region,radioOnClick.getIndex(1),radioOnClick) 
				   //areaRadioListView=ad.getListView();  

		.show();
	}
	private void showmapDownLoadDialog(final int regionid){
		new AlertDialog.Builder(this).setTitle("Push OK to confirm")
		//.setMessage("Prepare offline region map")
		
		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("ok", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				
				
	            if(regionid==0)
	            	regionname="AirtawarTimur";
	            else if(regionid==1)
	            	regionname= "BungoPasang";
	            else if(regionid==2)
	            	regionname="KotoPulai";
	            else if(regionid==3)
	            	regionname="ParupukTabing";
	            else if(regionid==4)
	            	regionname="PasirNanTigo";
				
				domapDownLoadWork(regionname);
			}
		})

		.show();
	}
	
	public void showmapUnzipDialog(){
		new AlertDialog.Builder(this).setTitle("Push OK to Initialize")
		//.setMessage("Initial local map")

		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("ok", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				domapZipExtractorWork(regionname);
			}
		})
		.show();
	}
	
	public void domapZipExtractorWork(String regionname){
		ZipExtractorTask task = new ZipExtractorTask("/mnt/sdcard/Download/map_"+regionname+".zip", "/mnt/sdcard/", this, true);
		task.execute();
	}
	
	private void domapDownLoadWork(String regionname){
		DownLoaderTask task = new DownLoaderTask("http://"+getResources().getString(R.string.coordinator_ip)+":8080/Downloads/map_"+regionname+".zip", "/mnt/sdcard/Download/", this,1);
		task.execute();
	}
	
	public void configchange(){
        boolean fileexist=GetMapFile(ROOT_MAP);
        if(fileexist==true){
        	try{
       			
                Intent intent = new Intent();
    			intent.setClass(MainActivity.this, GeometryEditorActivity.class);      			
    			intent.putExtra("Goemetry edit", "Geometry Edit");
    			intent.putExtra("username", username); 
    			startActivity(intent);
        	}catch(Exception e){
        		
        	}
        }
        else{
        	imageButton4.setVisibility(View.INVISIBLE);   
        	downloadinfo.setVisibility(View.VISIBLE);
        	downloadmap.setVisibility(View.VISIBLE);
        	imageButton8.setVisibility(View.VISIBLE);  
        	imageButton9.setVisibility(View.VISIBLE);  

        }
		
	}

		    /** 
		     * click radio event 		  
		     * 
		     */  
	class RadioOnClick implements DialogInterface.OnClickListener{  
		  private int index; 
		  private int tp;
		  public RadioOnClick(int index){  
		   this.index = index;  
		  }  
		  public void setIndex(int index){  
		   this.index=index; 
		  }  
		  public int getIndex(int tp){ 
			this.tp=tp;  
		   return index;  
		  }  
		  
		  public void onClick(DialogInterface dialog, int whichButton){  
		    setIndex(whichButton); 
		    if(tp==0)
		    {
		    	usertype=index;
		    	//button3.setText(m_Role[index]);	
		    	if(index==0)
		    		imageButton9.setImageDrawable(getResources().getDrawable(R.drawable.leader)); 
		    	else if (index==1)
		    		imageButton9.setImageDrawable(getResources().getDrawable(R.drawable.follower)); 
    
		    }
		    else if(tp==1)
		    {		    			    	    	
		    	regionid=index; 
		    	if(index==0)
		    		imageButton8.setImageDrawable(getResources().getDrawable(R.drawable.at)); 
		    	else if (index==1)
		    		imageButton8.setImageDrawable(getResources().getDrawable(R.drawable.bp)); 
		    	else if(index==2)
		    		imageButton8.setImageDrawable(getResources().getDrawable(R.drawable.kp)); 
		    	else if (index==3)
		    		imageButton8.setImageDrawable(getResources().getDrawable(R.drawable.pt)); 
		    	else if(index==4)
		    		imageButton8.setImageDrawable(getResources().getDrawable(R.drawable.pnt)); 
		    	//button4.setText(m_Region[index]);		
		    }
		       
		    dialog.dismiss();  
		  }  
		 }  

}
