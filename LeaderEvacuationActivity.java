package com.pitt.cdm.arcgis.android.evacuation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.codehaus.jackson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.na.ClosestFacilityTask;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.pitt.cdm.arcgis.android.networkanalysis.RoutingShowDirections;
import com.pitt.cdm.offlinemapupdate.PropertiesUtil;
import com.pitt.cdm.arcgis.android.maps.R;


public class LeaderEvacuationActivity extends FragmentActivity {
	
	  // The basemap switching menu items.
	  MenuItem mTwitterMenuItem = null;
	  MenuItem mShelterMenuItem = null;
	  MenuItem mLeaderMenuItem = null;
	  MenuItem mBarrierMenuItem = null;
	  MenuItem mSenderMenuItem = null;

	  // Create MapOptions for each type of basemap.
	  final MapOptions mTopoBasemap = new MapOptions(MapType.TOPO);
	  final MapOptions mStreetsBasemap = new MapOptions(MapType.STREETS);
	  final MapOptions mGrayBasemap = new MapOptions(MapType.GRAY);
	  final MapOptions mOceansBasemap = new MapOptions(MapType.OCEANS);

	
  //string
  protected static final String TAG = "Evacuation";
  
  String mMapState;
  
  String leaderid,leaderip,regionid,segmentids="";
  
  String startpt,endpt,startptLatLon,endptLatLon,followers,segmentstartpts="", pushinfofromleader2followers="",endstoplatlon, pushedinfo="";

  String routedetail,pushedroutedetail,mrouteresult="",routeSummary = null;
  
  String barrierUrl,queryUrl = "",closestFacilitySampleUrl;
  
  String updateinfo; 
                            
  String tUsername;
  
  String tip,tport,gatewayip;
  
  String username,tempmsg="";
  
  String broadcastinfo="";
  
//map and UI
  FragmentActivity fragactivity=new FragmentActivity();
  
  Menu mOptionsMenu;

  MapView map;
  
  EditMode mEditMode;
  
  private enum EditMode {
	    NONE, POINT, POLYLINE, POLYGON, SAVING
	  }
  
  FeatureTemplate mTemplate;
  
  MotionEvent mLongPressEvent;
  
  //Array
  ArrayList<Point> mPoints = new ArrayList<Point>();

  ArrayList<Point> mMidPoints = new ArrayList<Point>();
  
  ArrayList<ArcGISFeatureLayer> mFeatureLayerList;
  
  Map<String, Object> tempattributes = new HashMap<String, Object>();
  
  ArrayList<shelter> shelters;
  
  ArrayList<roadsegment> roadsegments; 
  
  ArrayList<shelter> routesegments; 
  
  ArrayList<String> segmentsorts;
  
  // List of the directions for the current route (used for the ListActivity)
  ArrayList<String> curDirections = null;
  
  
  public class shelter {  
      int id;
      double x;
		double y;        
      }
  public class roadsegment {  
      int id;  
      JSONArray paths;
      }

  //boolean
  boolean mMidPointSelected = false;

  boolean mVertexSelected = false;
  
  boolean mClosingTheApp = false;
  
  boolean auth;
  
  boolean start;
  
  boolean rereport= true;

  //int
  int mInsertingIndex;
  
  int tempoldrouteupdate=0;
  
  // Index of the currently selected route segment (-1 = no selection)
  int selectedSegmentID = -1;
/*
 * map long press action control:  
 * 0=default or received route; when the leader get his evacuation path for the first time
 * 1=shelter allocater,default=1; when in the register process
 * 2=path finder, detour--when the  evacuation path is unavailable or the allocated shelter is in risk
 */
  int mapoperationtype=1;
  
  int defaulroute=1;  //default=0, 0=new route, 1=default route
  
  int socketreceiveport = 22223,tsocketport;
		
  String reporttype;//s=shelter,b=bridge,r=road
		
  int affectedsegmentorder=0;
		
  //layers
  // basemap layer
  ArcGISTiledMapServiceLayer basemap,tileLayer;
  //feature layer
  ArcGISFeatureLayer fLayer,mTemplatebarrierLayer,mTemplateLayer, mRouteLayer,mTemproadsegmentLayer,mTempSheltersLayer,
  mTempLeaderrouteLayer,mTemppolylinebarrierLayer;
  //graphic layer
  GraphicsLayer routeLayer, hiddenSegmentsLayer,mGraphicsLayerEditing;
  //tile layer
  ArcGISLocalTiledLayer localTiledLayer;

  //symbols
  SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
  //for leader's risk report, include shelter, bridge, road
  PictureMarkerSymbol reporticon;
  
  //for routing to shelter activity
  // Symbol used to make route segments "invisible"
  SimpleLineSymbol segmentHider = new SimpleLineSymbol(Color.WHITE, 5);
  
  // Symbol used to highlight route segments
  SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);

 //features  and graphics
  FeatureSet queryrt; 
  
  Graphic startGraphic,endGraphic,newGraphic,routegrac;
  
  Graphic[] tgraphic;  
  
  MultiPath multipath;
  
  Point mLocation = null,shelterlocaiton,tsegment=null, endpoint, tsartpt=null;;
  
  // The current map extent, use to set the extent of the map after switching basemaps.
  Polygon mCurrentMapExtent = null, pg;
  
  Object shelterid,templeaderid,sheltercapacity;
   
  //for offline routing
  private static File demoDataFile;
  
  private static String offlineDataSDCardDirName;
  
  private static String filename;
  
  final String extern = Environment.getExternalStorageDirectory().getPath();  
  
  // closest facility task
  ClosestFacilityTask closestFacilityTask;
  
  //network analysis and route
  NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();
   
  Locator mLocator = null;
  
  View mCallout = null; 
 
  // Current route, route summary, and gps location
  Route curRoute = null;

  // route definition
  Route route;
  
// Global results variable for calculating route on separate thread
  RouteTask mRouteTask = null;
  
  RouteResult mResults = null;
  
	// Variable to hold server exception to show to user
  Exception mException = null;
  
	// Handler for processing the results
  final Handler mHandler = new Handler();
  
  final Runnable mUpdateResults = new Runnable() {
		public void run() {
			try {
				updateUI();
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			}
		}
	};
	
	final Runnable mUpdatedefaultResults = new Runnable() {
		public void run() {
			updaterouteUI();
		}
	};

	// Progress dialog to show when route is being calculated
	ProgressDialog dialog;
	
	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	
	final SpatialReference egs = SpatialReference.create(4326);
	
	SpatialReference spatialRef =  SpatialReference.create(32612);

    Button btngotobroadcastform;
    
    Button btnEditApply;
    
    Button startmulticast, stopmulticast;
    
    Button StartButton,StopButton;
    
    ImageButton dialogButtonBuilding, dialogButtonBridge, dialogButtonRoad;

  // Label showing the current direction, time, and length
  TextView directionsLabel,followernumlabel,multicastlabel,Label,pushtxt;
  
  EditText followernum;

  private TextView text;
		 
  Handler updateConversationHandler;
  
  Thread serverThread = null, udpmulticastserverThread=null,udpunicastservertThread=null; 

  //Multicast & socket
  MulticastSocket Sndmcast =null,Rcvcast =null;
  
  DataStruct InputData=null;

  BufferedReader in = null;
  
  PrintWriter out = null;
  
  private ServerSocket serverSocket;
  
  Socket sk = null;
  
  InetAddress tsocketaddress;
  //use common functions
  Communication cc=new Communication();
  
  Properties prop1 = PropertiesUtil.getProperties();
  
  ArcGISFeatureLayer mTempSheltersLayer1;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    

    // Initialize progress bar before setting content
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(false);
    setContentView(R.layout.ge_main);
    
    //receive 
	updateConversationHandler = new Handler();		 
    this.serverThread = new Thread(new ServerThread());
    this.serverThread.start();
    
    //receive UDP Multicast info
    this.udpmulticastserverThread = new Thread(new UDPMulticastServerThread());
    this.udpmulticastserverThread.start();
    
    //receive UDP Unicast info
    this.udpunicastservertThread = new Thread(new UDPUnicastServerThread());
    this.udpunicastservertThread.start();
    
    //get ip and port of CC
    tip=getResources().getString(R.string.coordinator_ip);
    tport=getResources().getString(R.string.coordinator_port);
    tsocketport=Integer.parseInt(getResources().getString(R.string.coordinator_socketport));
    try {
		tsocketaddress=InetAddress.getByName(tip);
	} catch (Exception e3) {
		
		e3.printStackTrace();
	}
    
    //step 1: add local map
	//@ create the path to local map tpk
    demoDataFile = Environment.getExternalStorageDirectory();
    offlineDataSDCardDirName = this.getResources().getString(R.string.offline_dir);
    filename = this.getResources().getString(R.string.local_tpk);
    //@ create the map file url
    String basemap = demoDataFile + File.separator + offlineDataSDCardDirName + File.separator + filename;
    String basemapurl = "file://" + basemap;
    //@ create the mapview
    map = (MapView) findViewById(R.id.map);
    //@ create the local tpk
    localTiledLayer = new ArcGISLocalTiledLayer(basemapurl);
    //@ add the local map layer
	    
	    if(localTiledLayer.getLayers().isEmpty()==false)
	    	map.addLayer(localTiledLayer);	    
   
	    else{	
	    	
	    //}
	//@ Add WEB tiled layer to MapView
    	tileLayer = new ArcGISTiledMapServiceLayer(
			"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
    	map.addLayer(tileLayer);
    }
    
    //@ enable panning over date line
    map.enableWrapAround(true);	 
    
    
    //Step 2: add feature layers   
    //@Create status listener for feature layers
    OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
      }
    };
    
	//0. Add the route graphic layer (shows the full route)
	routeLayer = new GraphicsLayer();
	routeLayer.setOnStatusChangedListener(statusChangedListener);
	map.addLayer(routeLayer);
	
	
	//prepare and cache segment and shelter info from local json file
	shelters=readsheltercoor(); 
	roadsegments=readsegmentcoor(); 
	

	/*
	 //1. polyline barriers
	 mTemppolylinebarrierLayer = new ArcGISFeatureLayer(
			 getResources().getString(R.string.arcgis_server_url)+"ACBarriers/FeatureServer/1",
	    		ArcGISFeatureLayer.MODE.ONDEMAND);			 
	 map.addLayer(mTemppolylinebarrierLayer);
	 mTemppolylinebarrierLayer.setVisible(false);
*/
    
	//2. shelter
	
	mTempSheltersLayer = new ArcGISFeatureLayer(			
			getResources().getString(R.string.arcgis_server_url)+"KelurahanFacilities/FeatureServer/34",
			ArcGISFeatureLayer.MODE.ONDEMAND);
	mTempSheltersLayer.setVisible(false);
	map.addLayer(mTempSheltersLayer);
			
	/*
	//3. road network segment 
	mTemproadsegmentLayer= new ArcGISFeatureLayer(
			//"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACFacilities/FeatureServer/5",
			getResources().getString(R.string.arcgis_server_url)+"KelurahanFacilities/FeatureServer/44",
			ArcGISFeatureLayer.MODE.ONDEMAND);
	map.addLayer(mTemproadsegmentLayer);
	//mTemproadsegmentLayer.setVisible(false);
	
	//4. leader evacuation route
	 mTempLeaderrouteLayer = new ArcGISFeatureLayer(
	    		//"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/3",
	    		getResources().getString(R.string.arcgis_server_url)+"ACBarriers/FeatureServer/3",
	        ArcGISFeatureLayer.MODE.ONDEMAND);			 
	 map.addLayer(mTempLeaderrouteLayer);
	 mTempLeaderrouteLayer.setVisible(false);
	 
	 //5. evacuation route layer
	 mRouteLayer = new ArcGISFeatureLayer(
		    	//"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/3",
		    	getResources().getString(R.string.arcgis_server_url)+"ACBarriers/FeatureServer/3",
		        ArcGISFeatureLayer.MODE.ONDEMAND);
		 
     map.addLayer(mRouteLayer);
	 mRouteLayer.setVisible(false);
	 
	 */
     
	 //6. Add the hidden segments layer (for highlighting route segments)
	 hiddenSegmentsLayer = new GraphicsLayer();
	 map.addLayer(hiddenSegmentsLayer);

	 // Make the segmentHider symbol "invisible"
	 segmentHider.setAlpha(1);
	 
	
    
	// Set the directionsLabel with initial instructions.
	directionsLabel = (TextView) findViewById(R.id.directionsLabel);
	
	 //@show road barriers in key value store
    //showbarriers(0);
			
		/**
	 * On single clicking the directions label, start a ListActivity to show
	 * the list of all directions for this route. Selecting one of those
	 * items will return to the map and highlight that segment.
	 * 
	 */
	directionsLabel.setOnClickListener(new OnClickListener() {

		public void onClick(View v) {
			if (curDirections == null)
				return;
			Intent i = new Intent(getApplicationContext(),
					RoutingShowDirections.class);
			i.putStringArrayListExtra("directions", curDirections);
			startActivityForResult(i, 1);
		}

	});

	/**
	 * On long clicking the directions label, removes the current route and
	 * resets all affiliated variables.
	 * 
	 */
	directionsLabel.setOnLongClickListener(new OnLongClickListener() {

		public boolean onLongClick(View v) {
			routeLayer.removeAll();
			hiddenSegmentsLayer.removeAll();
			curRoute = null;
			curDirections = null;
			directionsLabel.setText(getString(R.string.route_label));
			return true;
		}

	});

	/**
	 * On single tapping the map, query for a route segment and highlight
	 * the segment and show direction summary in the label if a segment is
	 * found.
	 */
	map.setOnSingleTapListener(new OnSingleTapListener() {
		private static final long serialVersionUID = 1L;

		public void onSingleTap(float x, float y) {
			
			int[] indexesshelter = mTempSheltersLayer.getGraphicIDs(x, y, 20);
			if(indexesshelter==null||indexesshelter.length==0){
				
				 map.getCallout().hide();
			}
			else{
				
				int selectedshelter=indexesshelter[0];
				Graphic graphicshelter=mTempSheltersLayer.getGraphic(selectedshelter);
				Map<String, Object> tmap = new HashMap<String, Object>();
				tmap=graphicshelter.getAttributes();
			
				String name= "Address:"+(String) tmap.get("ADDRESS")+"\n Name:"+(String) tmap.get("NAME")+"\n Function:"+(String) tmap.get("FUNCTION");
			
				Point mapPoint = map.toMapPoint(x, y);
			
				showCallout(name, mapPoint);
			}
		
			// Get all the graphics within 20 pixels the click
			int[] indexes = hiddenSegmentsLayer.getGraphicIDs(x, y, 20);
			// Hide the currently selected segment
			hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
					segmentHider);

			if (indexes.length < 1) {
				// If no segments were found but there is currently a route,
				// zoom to the extent of the full route
				if (curRoute != null) {
					map.setExtent(curRoute.getEnvelope(), 250);
					directionsLabel.setText(routeSummary);
				}
				return;
			}
			// Otherwise update our currently selected segment
			selectedSegmentID = indexes[0];
			Graphic selected = hiddenSegmentsLayer
					.getGraphic(selectedSegmentID);
			// Highlight it on the map
			hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
					segmentShower);
			String direction = ((String) selected.getAttributeValue("text"));
			double time = ((Double) selected.getAttributeValue("time"))
					.doubleValue();
			double length = ((Double) selected.getAttributeValue("length"))
					.doubleValue();
			// Update the label with this direction's information
			String label = String.format(
					"%s%nTime: %.1f minutes, Length: %.1f miles",
					direction, time, length);
			directionsLabel.setText(label);
			// Zoom to the extent of that segment
			map.setExtent(selected.getGeometry(), 50);
		}

	});
	
	map.setOnLongPressListener(new OnLongPressListener() {

		private static final long serialVersionUID = 1L;

		public boolean onLongPress(final float x, final float y) {
			
			final Dialog dialog=new Dialog(map.getContext(),R.style.cust_dialog);
			dialog.setContentView(R.layout.custom_operationtype);
			dialog.setTitle("Operation choose");
	
			// set the custom dialog components - text, image and button
			Button dialogButtonRoute = (Button) dialog.findViewById(R.id.dialogButtonRoute);
			// if button is clicked, close the custom dialog
			dialogButtonRoute.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				
					dialog.dismiss();
																					
		      		Dialog prcoseedialog = ProgressDialog.show(LeaderEvacuationActivity.this, "",
							"Path Finding...", true);
		      		
		      		//search if there is default path for the leader
		      		
				    pushedroutedetail = (String) prop1.get("defaultroute");
				   
			        if(pushedroutedetail==null ||pushedroutedetail==""){
		        		mapoperationtype=1;
		        	}
			        else if( mapoperationtype!=2 ){
		        		mapoperationtype=0;			        		
		        	}
	
					//Option 0. default route
					if(mapoperationtype==0){
						
			      		Toast.makeText(LeaderEvacuationActivity.this,
			      				"Default Route", Toast.LENGTH_SHORT).show();	
						
						// Clear the graphics and empty the directions list
						routeLayer.removeAll();
						hiddenSegmentsLayer.removeAll();													
						mResults = null;
						startGraphic=null;
						endGraphic=null;
						
					    // Initialize the RouteTask and Locator with the local data
					    initializeRoutingAndGeocoding();
						try{
							if(pushedroutedetail!=""&& pushedroutedetail!=null)
								{								
									showdefaultroute(0,pushedroutedetail);
								}							
							} catch (Exception e) {							
									e.printStackTrace();
									mException = e;
							}						
					}
					
					//option 1. Closest shelter allocate
					if(mapoperationtype==1){
																	
			      		Toast.makeText(LeaderEvacuationActivity.this,
			      				"New Route", Toast.LENGTH_SHORT).show();							
				
						try {
							
							affectedsegmentorder=0;
							
							Point point = map.toMapPoint(x, y);	

							Point leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));								
														
							String shelterallocator=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/shelterallocator?action=findclosestshelter" +
													"&llx="+leaderlocaiton.getX()+
													"&lly="+leaderlocaiton.getY()+
													"&leaderid="+regionid+","+leaderid);
							/*Information Format:
							 * len; command; sender id;receiver id; option; data
							 *{135;6;0;3LL;01;1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322:40:
							 * Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728}																						 						
							*/
							if(shelterallocator!=""){

								String routesegment=shelterallocator.split(";")[5];
								String segmentandshelter=routesegment.split(":")[0]+";"+routesegment.split(":")[1];
								showdefaultroute(0,segmentandshelter);
								
								//1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322;40
								PropertiesUtil.setProperties("defaultroute", segmentandshelter);
								//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728
								PropertiesUtil.setProperties("shelterinfo",routesegment.split(":")[2]);
								
								
							   }
						} catch (Exception e) {
							mException = e;

						}
												
						mapoperationtype=0;	
					}
					//option 2. detour, when the allocated path is unavailable
					else if(mapoperationtype==2){
																	
			      		Toast.makeText(LeaderEvacuationActivity.this,
			      				"Update Route", Toast.LENGTH_SHORT).show();	
				
								try {
									Point point = map.toMapPoint(x, y);														
									Point leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));											
									/*
									 * shelter info
									 */
									//"Name<"+jsonObjectattribute.getString("NAME")+";Address<"+jsonObjectattribute.getString("ADDRESS")
				        			//+"<location:"+shelterlocaiton.getX()+","+shelterlocaiton.getY();
									
									//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728
									String shelterin = (String) prop1.get("shelterinfo");
									String[] shelterll=shelterin.split("%")[2].split(">")[1].split(",");
																				
									String stops="";

									if(mLocation!=null)								
									{															
										 stops=mLocation.getX()+","+mLocation.getY()+";"+shelterll[0]+","+shelterll[1];																											
									}
									else{															
										 stops=leaderlocaiton.getX()+","+leaderlocaiton.getY()+";"+shelterll[0]+","+shelterll[1];	
									}
									
									String shelterallocator=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/pathfinder?action=pathfinder" +
											"&stops="+stops+
											"&leaderid="+regionid+","+leaderid);																																   
									
									//135;6;0;3LL;01;1379,1348,1349,1350,1351,1352,1353,1354,1327,1322:40:
									//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33595962500004,-0.8653165459999741
									
									if(shelterallocator!=""){
										String routesegment=shelterallocator.split(";")[5];
										String segmentandshelter=routesegment.split(":")[0]+";"+routesegment.split(":")[1];
										showdefaultroute(0,segmentandshelter);
																				
										PropertiesUtil.setProperties("defaultroute", segmentandshelter);
										//PropertiesUtil.setProperties("shelterinfo",routesegment.split(":")[2]);
										
										
									   }
								} 
								catch (Exception e) {
									mException = e;
									
								}
												
						mapoperationtype=0;	
						affectedsegmentorder=0;
						
					}
					if (prcoseedialog.isShowing()) {
						prcoseedialog.dismiss();
					}
				}
			});
			
	
			Button dialogButtonReport = (Button) dialog.findViewById(R.id.dialogButtonReport);
			// if button is clicked, close the custom dialog
			dialogButtonReport.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
												
		      		Toast.makeText(LeaderEvacuationActivity.this,
		      				"Report", Toast.LENGTH_SHORT).show();
		      		
		      		dialog.dismiss();
		      		
		      		rereport=false;
				    mFeatureLayerList = new ArrayList<ArcGISFeatureLayer>();

				    //add polyline barrier template
				    //mTemplate = mTemppolylinebarrierLayer.getTypes()[0].getTemplates()[0];
				    //mTemplateLayer = mTemppolylinebarrierLayer;
				    mEditMode = EditMode.POLYLINE;
				    //add two side points
				    mPoints.clear();        
				    Point point1 = map.toMapPoint(new Point(x, y+50));
				    Point point2 = map.toMapPoint(new Point(x, y-50));
				    mPoints.add(point1);
				    mPoints.add(point2);
				    //add mid-point
				    mMidPoints.clear();
				    mMidPoints.add(map.toMapPoint(new Point(x, y)));
				    // Draw the mid-point
				    int index = 0;
				    Graphic graphic;
				    for (Point pt : mMidPoints) {
				      if (mMidPointSelected && mInsertingIndex == index) {
				        graphic = new Graphic(pt, mRedMarkerSymbol);
				      } else {
				        graphic = new Graphic(pt, mGreenMarkerSymbol);
				      }
				      //mGraphicsLayerEditing.addGraphic(graphic);
				      index++;
				    }
				    //draw polyline
				    //drawPolylineOrPolygon();
				    
		      	    //^^^^^^^^^^^^^^
		      		//report icon show on map
		      		final Dialog dialog2=new Dialog(map.getContext(),R.style.cust_dialog);
					dialog2.setContentView(R.layout.custom_reporttype);
					dialog2.setTitle("Report type");
					
					// set the custom dialog components - text, image and button
					 dialogButtonBuilding = (ImageButton) dialog2.findViewById(R.id.building);
									
					// if button is clicked, close the custom dialog
					dialogButtonBuilding.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog2.dismiss();
							reporttype="s";
							reporticon = new PictureMarkerSymbol(
									map.getContext(), getResources().getDrawable(
											R.drawable.report_building01));
							
							Point reportpt = map.toMapPoint(x, y);
							Graphic reportgraphic = new Graphic(reportpt, reporticon);									
							//routeLayer.addGraphics(new Graphic[] { reportgraphic});
							
							//change shelter attributes
							//*********************							        								       
					        pg= GeometryEngine.buffer(reportpt, map.getSpatialReference(), 50.0, null); 
							
					        // build a query to select the clicked feature
					        Query query = new Query();
					        query.setOutFields(new String[] { "*" });
					        query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
					        query.setGeometry(pg);
					        query.setInSpatialReference(map.getSpatialReference());

					        // call the select features method and implement the callbacklistener
					        mTempSheltersLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, new CallbackListener<FeatureSet>() {

					          // handle any errors
					          public void onError(Throwable e) {

					            Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());

					          }

					          public void onCallback(FeatureSet queryResults) {

					              if (queryResults.getGraphics().length > 0) {								             
					              	queryrt=queryResults;
					               
					              	Map<String, Object> attributes = new HashMap<String, Object>();
					              	attributes.put("updatetime",  new Date(System.currentTimeMillis()));        
					              	attributes.put("available" , 0);
					              	attributes.put(mTempSheltersLayer.getObjectIdField(), queryResults.getGraphics()[0].getAttributeValue(mTempSheltersLayer.getObjectIdField()));
					              	
					              	//Point pt1=(Point)queryResults.getGraphics()[0].getGeometry(); 							              	
					              	//Point pt2=(Point)GeometryEngine.project(pt1, wm, egs);								              
					              	//Graphic newGraphic = new Graphic(pt2, null, attributes); 
					            	Graphic newGraphic = new Graphic(null, null, attributes);
					       										          	 
					            
					              	mTempSheltersLayer.applyEdits(null, null,new Graphic[] {newGraphic}, new CallbackListener<FeatureEditResult[][]>() {

					              		@Override
					              		public void onError(Throwable e){ 
					              			 Log.d(LeaderEvacuationActivity.TAG, "error updating feature: " + e.getLocalizedMessage());
					              			 //Toast.makeText(GeometryEditorActivity.this,"Fail", Toast.LENGTH_SHORT).show();  
					              		}

					              		@Override
					              		public void onCallback(FeatureEditResult[][] result) { 
					              	        // check the response for success or failure
					              	        if (result[2] != null && result[2][0] != null && result[2][0].isSuccess()) {

					              	          Log.d(LeaderEvacuationActivity.TAG, "Success updating feature with id=" + result[2][0].getObjectId());
					              	          // Toast.makeText(GeometryEditorActivity.this,"Success: "+result[2][0].getObjectId(), Toast.LENGTH_SHORT).show();  
					              	        	shelterid=result[2][0].getObjectId();
					              	          // see if we want to update the dynamic layer to get new symbols for
					              	          // updated features
					              	        		
					              	        }
					       	       
					              		}

					              	});
					              }
					          }
					        });
							//*********************	
					        if (shelterid!=null)
					        	report(Long.parseLong(shelterid.toString()),reporttype,reportgraphic);																			
						}
					});
					
					dialogButtonBridge = (ImageButton) dialog2.findViewById(R.id.bridge);
					// if button is clicked, close the custom dialog
					dialogButtonBridge.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog2.dismiss();
							reporttype="b";
							reporticon = new PictureMarkerSymbol(
									map.getContext(), getResources().getDrawable(
											R.drawable.report_bridge01));
							Point reportpt = map.toMapPoint(x, y);
							Graphic reportgraphic = new Graphic(reportpt, reporticon);									
							//routeLayer.addGraphics(new Graphic[] { reportgraphic});
							report(-1,reporttype,reportgraphic);							
																											
						}
					});
					
					dialogButtonRoad = (ImageButton) dialog2.findViewById(R.id.road);
					// if button is clicked, close the custom dialog
					dialogButtonRoad.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog2.dismiss();
							reporttype="r";
							reporticon = new PictureMarkerSymbol(
									map.getContext(), getResources().getDrawable(
											R.drawable.report_road01));
							
							Point reportpt = map.toMapPoint(x, y);
							Graphic reportgraphic = new Graphic(reportpt, reporticon);									
							//routeLayer.addGraphics(new Graphic[] { reportgraphic});
							report(-1,reporttype,reportgraphic);									
																											
						}
					});
		      		
					dialog2.show();
		      		//^^^^^^^^^^^^^^^^
				}
			});
			
			//if(rereport==true)
			dialog.show();
									
			return true;		
		}
	});         

	socketreceiveport=Integer.parseInt(getResources().getString(R.string.leader_receive_port));
	
	// Get the location service and start reading location. Don't auto-pan
	// to center our position
	LocationDisplayManager ls = map.getLocationDisplayManager();
	ls.setLocationListener(new MyLocationListener());
	ls.start();
	ls.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);

	Intent intent=getIntent();          			
    leaderid=cc.getMacAddress(getBaseContext())+","+intent.getStringExtra("username");
    //leaderid=getMacAddress(getBaseContext())+","+getIp(getBaseContext())+":22223";        
    pushedroutedetail=intent.getStringExtra("routedetail");
    //regionid=intent.getStringExtra("regionid");
    
    //Properties prop1 = PropertiesUtil.getProperties();
    regionid = (String) prop1.get("regionid");
    
    //String piip=cc.getGateWay(getBaseContext());
	    
	//step 5: multicast by operate
	//StartButton = (Button) this.findViewById(R.id.buttonsend);
	//StopButton = (Button) this.findViewById(R.id.buttonstop);
	//Label = (TextView) this.findViewById(R.id.sendinfo);		
	//StartButton.setOnClickListener(listener);
	//StopButton.setOnClickListener(listener);
	
	//for WIFIDirect communication among smart phones
	//WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
   	//WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
    //multicastLock.acquire(); 
   	
   	
	ListMcast MCastListen = new ListMcast();
	Thread ListCastThread = new Thread(MCastListen);
	InputData=new DataStruct();	 	
   	ListCastThread.start();
   //	Label.setText("");
   	
   	
   	//get pi  ip info   
   	//String localip=cc.getLocalIPAddress();
       
    // Set listeners on MapView
    map.setOnStatusChangedListener(new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(final Object source, final STATUS status) {
        if (STATUS.INITIALIZED == status) {
          if (source instanceof MapView) {
            mGraphicsLayerEditing = new GraphicsLayer();
            map.addLayer(mGraphicsLayerEditing);
          }
        }
        if (STATUS.LAYER_LOADED == status) {
            map.setExtent(mCurrentMapExtent);         
          }
      }
    });
    map.setOnTouchListener(new MyTouchListener(LeaderEvacuationActivity.this, map));

    // If map state (center and resolution) has been stored, update the MapView with this state
    if (!TextUtils.isEmpty(mMapState)) {
      map.restoreState(mMapState);
    }
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items from the Menu XML to the action bar, if present.
    getMenuInflater().inflate(R.menu.option_menu, menu);
    
    // Get the basemap switching menu items.
 
    mShelterMenuItem = menu.getItem(0);
    mTwitterMenuItem = menu.getItem(1);
    mLeaderMenuItem = menu.getItem(2);
    mBarrierMenuItem = menu.getItem(3);
    mSenderMenuItem = menu.getItem(4);
    
    // Also set the topo basemap menu item to be checked, as this is the default.
    mBarrierMenuItem.setChecked(true); 
    
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Save the current extent of the map before changing the map.
    mCurrentMapExtent = map.getExtent();
    
    // Handle menu item selection.
    switch (item.getItemId()) {
      case R.id.Shelter_onMap:
       // map.setMapOptions(mStreetsBasemap);
    		mTempSheltersLayer.setVisible(true);
    		map.refreshDrawableState();
    		 mGraphicsLayerEditing.removeAll();
    		//map.addLayer(mTempSheltersLayer1);    		
    		mShelterMenuItem.setChecked(true);
        return true;
      case R.id.Twitter_onMap:
        //map.setMapOptions(mTopoBasemap);
    	  mTempSheltersLayer.setVisible(false);
    	  mGraphicsLayerEditing.removeAll();
    	  showshelters();
    	  map.refreshDrawableState();
        mTwitterMenuItem.setChecked(true);
        return true;
      case R.id.Leader_onMap:
        //map.setMapOptions(mGrayBasemap);
        mLeaderMenuItem.setChecked(true);
        return true;
      case R.id.Barrier_onMap:
       // map.setMapOptions(mOceansBasemap);
    	  mGraphicsLayerEditing.removeAll();
    	  showbarriers(0);
        mBarrierMenuItem.setChecked(true);
        return true;
      case R.id.Path_send:
          // map.setMapOptions(mOceansBasemap);
           mSenderMenuItem.setChecked(true);
           return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  
  public void report(final long shelterid,  final String type, final Graphic gra){
	  
	  final Dialog dialog1=new Dialog(map.getContext(),R.style.cust_dialog);
		dialog1.setContentView(R.layout.custom_saveorcancel);
		dialog1.setTitle("Save or cancel");

		Button dialogButtonSave = (Button) dialog1.findViewById(R.id.dialogButtonSave);
		//@if button is clicked, close the custom dialog
		dialogButtonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
											
	      		Toast.makeText(LeaderEvacuationActivity.this,
	      				"Save", Toast.LENGTH_SHORT).show();      		
				dialog1.dismiss();
				//clear all the report icons.
				routeLayer.removeAll();
				String msg;
				/*
				 * shelter is in risk
				 */
				if(type.equals("s"))
				{						    			
	    			msg="6;3"+leaderid+";200;#;"+type+":"+String.valueOf(shelterid)+":#";
	    			udpunicastsender(msg);
				
					mTempSheltersLayer.refresh();
				//*******************
				}// other type risks, bridge & road segment
				else{
				// Graphic g;
			      // For polylines and polygons, create a MultiPath from the points...
			      MultiPath multipath;
			     
			      multipath = new Polyline();
			
			      multipath.startPath(mPoints.get(0));
			      for (int i = 1; i < mPoints.size(); i++) {
			        multipath.lineTo(mPoints.get(i));
			      }			      
			    //  
			    String affectedstr="";
			    String tempaffectedstr="";
				for (int j = 0; j < mPoints.size(); j++) {
					Point barriercoor = (Point) GeometryEngine.project(mPoints.get(j),SpatialReference.create(102100),SpatialReference.create(4326));
					affectedstr+=barriercoor.getX()+","+barriercoor.getY()+",";
					tempaffectedstr+="["+barriercoor.getX()+","+barriercoor.getY()+"],";
				}
				affectedstr=affectedstr.substring(0, affectedstr.length()-1);
				tempaffectedstr=tempaffectedstr.substring(0, tempaffectedstr.length()-1);
												
				try {
	
					String roadsegmentidstr=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/pathfinder?action=riskreport" +
							"&paths="+tempaffectedstr+
							"&leaderid="+regionid+","+leaderid);
  
				   if(roadsegmentidstr!=""){
					   	
				   		msg = "6;3"+leaderid+";200;#;"+type+":"+roadsegmentidstr+":"+affectedstr;	
				   		
				   		udpunicastsender(msg);
				   		
					    routeLayer.addGraphics(new Graphic[] {gra});									    			
						} 
				   else
				   {
			      		Toast.makeText(LeaderEvacuationActivity.this,
			      				"report fail, try again.", Toast.LENGTH_SHORT).show();
				   }				  
				 }catch (Exception e) {							
							e.printStackTrace();
					}	     												    						    													  			  
				}  								   
				   //********************************
				 rereport=true;
			}
		});
		Button dialogButtonCancel = (Button) dialog1.findViewById(R.id.dialogButtonCancel);
		// if button is clicked, close the custom dialog
		dialogButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
											
	      		Toast.makeText(LeaderEvacuationActivity.this,
	      				"Cancel", Toast.LENGTH_SHORT)
	      		.show();      		
				dialog1.dismiss();
				
				rereport=true;
				
				mClosingTheApp = true;				
			}
		});
		
		dialog1.show();	
  }
  /*
   * @UDP Unicast report, from leader to MPi
   * msg=risktype+shelterid+#  or risktype+segmentid+coordination of report barrier line
   */
  public String udpunicastsender(String msg){

	   //******************	
	  String response="",result="";
	   try {
			DatagramSocket datagramSocket = null; 
			Random random = new Random();
			datagramSocket = new DatagramSocket(random.nextInt(9999));
			datagramSocket.setSoTimeout(10 * 1000);		
			byte[] buffer = new byte[1024 * 64]; 
			
			String tempstr=msg;
			int strlen=tempstr.length();						
			String formantdata=strlen+";"+tempstr;
			byte[] btSend =formantdata.getBytes("UTF-8");
			
			//send format to Current Pi, send to group ip and port, MPi will listen this msg and process it
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("150.212.70.84"), 7385);
			packet.setData(btSend);
			//System.out.println("Leader send report to MPi:" + Arrays.toString(btSend));	        						
			datagramSocket.send(packet);
			
			datagramSocket.receive(packet);
			byte[] bt = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
			if(null != bt && bt.length > 0){
				System.out.println("Leader receive:" + Arrays.toString(bt));
			}
			Thread.sleep(1 * 1000);
			
			datagramSocket.close();
			
			result=Arrays.toString(bt);
			if(result.split(";")[1].equals("11"))
				response=result;
		
		} catch(Exception e){
			e.printStackTrace();
		}
	return response;
	  
  }
  @Override  
  public boolean onKeyDown(int keyCode, KeyEvent event)  
  {  
      if (keyCode == KeyEvent.KEYCODE_BACK )  
      {  
         
			new AlertDialog.Builder(LeaderEvacuationActivity.this)
			.setTitle("Exit?")
	        //.setMessage("update map")
	        //.setIcon(R.drawable.ic_launcher)
	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					 
					 Intent intent = new Intent(Intent.ACTION_MAIN);  
                     intent.addCategory(Intent.CATEGORY_HOME);  
                     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
                     startActivity(intent);  
                     android.os.Process.killProcess(android.os.Process.myPid());
				}

			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			})
			.show();
      }          
      return false;          
  }  
 
  DialogInterface.OnClickListener clicklistener = new DialogInterface.OnClickListener()  
  {  
      public void onClick(DialogInterface dialog, int which)  
      {  
          switch (which)  
          {  
          case AlertDialog.BUTTON_POSITIVE:
              System.exit(0); 
              break;  
          case AlertDialog.BUTTON_NEGATIVE:
              break;  
          default:  
              break;  
          }  
      }  
  }; 

		private View.OnClickListener listener = new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				
				MCast MCastRun = new MCast();
				Thread MCastThread = new Thread(MCastRun);
				
				//String IPaddr=null;
				//String[] Segment;
				try{
					if (v == StartButton){						
		                //Multicast notification to followers						
						//pushedroutedetail = (String) prop1.get("defaultroute");				
						//multicasttofollower(pushedroutedetail);
					    //*******************
					    //judge internet available
					    if (isNetworkAvailable(LeaderEvacuationActivity.this))
					    {
					        Toast.makeText(getApplicationContext(), "on", Toast.LENGTH_LONG).show();
					    }
					    else
					    {
					        Toast.makeText(getApplicationContext(), "off", Toast.LENGTH_LONG).show();
					    }
					    //*******************
						/*
						 * 
						 * 
						start = true;
						
						StopButton.setEnabled(true);
						
									
						IPaddr = cc.getLocalIPAddress();
					
						InputData.addr=new String(IPaddr);
						
		            	if(pushedinfo==""){
		            		
		        			Toast.makeText(LeaderEvacuationActivity.this, "There is no route update",
		        					Toast.LENGTH_LONG).show();
		        			return;
		            	}	
						InputData.SegID=new String[(pushedinfo.split(";")[0].split(",")).length];
						InputData.SegID=pushedinfo.split(";")[0].split(",");
						InputData.Port=22220;
						InputData.com=1;
						InputData.ShellID= Integer.parseInt(pushedinfo.split(";")[1]);
						MCastRun.GetInputData(InputData);
					
						MCastThread.start();
						*/
					} else if (v == StopButton) {
						start = false;
						StartButton.setEnabled(true);
						StopButton.setEnabled(false);
						//Label.setText(" ");
						MCastThread.stop();
						//MCastThread.interrupt();
						
					}
					else if(v==dialogButtonBuilding){
						
						reporttype="s";
						reporticon = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_building01));
					}
					else if (v==dialogButtonBridge){
						
						reporttype="b";
						reporticon = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_bridge01));
					}
					else if (v==dialogButtonRoad){
						reporttype="r";
						reporticon = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_road01));
					}

				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
	 private void initializeRoutingAndGeocoding() {

		    // We will spin off the initialization in a new thread
		    new Thread(new Runnable() {

		      @Override
		      public void run() {
		    	  
		        // Get the external directory		        
		        String locatorPath = "/ArcGIS/samples/OfflineRouting/Geocoding/road_CreateAddress.loc";
		        String networkPath = "/ArcGIS/samples/OfflineRouting/Routing/arcsde_data.geodatabase";        
		        String networkName = "Kelurahan_ND";

		        // Attempt to load the local geocoding and routing data
		        try {
		          mLocator = Locator.createLocalLocator(extern + locatorPath);
		          mRouteTask = RouteTask.createLocalRouteTask(extern + networkPath, networkName);
		        } catch (Exception e) {
		          //popToast("Error while initializing :" + e.getMessage(), true);
		          e.printStackTrace();
		        }
		      }
		    }).start();
		  }
	 
		void updaterouteUI() {
			//dialog.dismiss();
			
			routeLayer.removeAll();
			hiddenSegmentsLayer.removeAll();
			curDirections = new ArrayList<String>();
			
			if (mResults == null ||mResults.getRoutes().size()==0) {
				Toast.makeText(LeaderEvacuationActivity.this, mException.toString(),
						Toast.LENGTH_LONG).show();
				return;
			}
			curRoute = mResults.getRoutes().get(0);
			
			// Symbols for the route and the destination (blue line, checker flag)
			PictureMarkerSymbol startSymbol = new PictureMarkerSymbol(
					map.getContext(), getResources().getDrawable(
							R.drawable.route_strat_24));
			
			PictureMarkerSymbol endSymbol = new PictureMarkerSymbol(
					map.getContext(), getResources().getDrawable(
							R.drawable.route_stop_24));

			SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);

			// Add all the route segments with their relevant information to the
			// hiddenSegmentsLayer, and add the direction information to the list
			// of directions
			for (RouteDirection rd : curRoute.getRoutingDirections()) {
				HashMap<String, Object> attribs = new HashMap<String, Object>();
				attribs.put("text", rd.getText());
				attribs.put("time", Double.valueOf(rd.getMinutes()));
				attribs.put("length", Double.valueOf(rd.getLength()));
				curDirections.add(String.format(
						"%s%nTime: %.1f minutes, Length: %.1f miles", rd.getText(),
						rd.getMinutes(), rd.getLength()));
				Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider, attribs);
				hiddenSegmentsLayer.addGraphic(routeGraphic);
				
				routedetail=routedetail+rd.getText();
			}
			// Reset the selected segment
			selectedSegmentID = -1;

			// Add the full route graphic and destination graphic to the routeLayer
			Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic()
					.getGeometry(), routeSymbol);
			routegrac=routeGraphic;
			startGraphic = new Graphic(
					((Polyline) routeGraphic.getGeometry()).getPoint(0), startSymbol);
			endGraphic = new Graphic(
					((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
							.getGeometry()).getPointCount() - 1), endSymbol);
			
			routeLayer.addGraphics(new Graphic[] { routeGraphic, startGraphic, endGraphic });					
				        
			// Get the full route summary and set it as our current label
			routeSummary = String.format(
					"%s%nTotal time: %.1f minutes, length: %.1f miles",
					curRoute.getRouteName(), curRoute.getTotalMinutes(),
					curRoute.getTotalMiles());
			directionsLabel.setText(routeSummary);
			// Zoom to the extent of the entire route with a padding
			map.setExtent(curRoute.getEnvelope(), 250);						
		}
		
		  private void showCallout(String text, Point location) {

			    // If the callout has never been created, inflate it
			    if (mCallout == null) {
			      LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			      mCallout = inflater.inflate(R.layout.callout, null);
			    }

			    // Show the callout with the given text at the given location
			    ((TextView) mCallout.findViewById(R.id.calloutText)).setText(text);
			    map.getCallout().show(location, mCallout);
			    map.getCallout().setMaxWidth(700);
			  }
		/**
		 * Updates the UI after a successful rest response has been received.
		 */
		void updateUI() throws UnknownHostException {
			//dialog.dismiss();
			
			if(affectedsegmentorder==0)
			routeLayer.removeAll();
			hiddenSegmentsLayer.removeAll();
			curDirections = new ArrayList<String>();
			segmentids="";
			segmentstartpts="";
			/*
			if (mResults == null) {
				Toast.makeText(GeometryEditorActivity.this, mException.toString(),
						Toast.LENGTH_LONG).show();
				return;
			}
			*/
			// Symbols for the route and the destination (blue line, checker flag)
			PictureMarkerSymbol startSymbol = new PictureMarkerSymbol(
					map.getContext(), getResources().getDrawable(
							R.drawable.route_strat_24));
			
			PictureMarkerSymbol endSymbol = new PictureMarkerSymbol(
					map.getContext(), getResources().getDrawable(
							R.drawable.route_stop_24));
			// Symbols for the route and the destination (blue line, checker flag)
			SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
			
			
		if (mResults == null ||mResults.getRoutes().size()==0) {
			Toast.makeText(LeaderEvacuationActivity.this, "No road available, be careful.",
					Toast.LENGTH_LONG).show();	
			showdefaultroute(0,"");
			showbarriers(1);
			return;
		}
			
		if(mResults.getRoutes().size()>1){
			for(int l=0;l<mResults.getRoutes().size();l++){
			curRoute = mResults.getRoutes().get(l);
			
			Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic()
					.getGeometry(), routeSymbol);
			routegrac=routeGraphic;
			startGraphic = new Graphic(
					((Polyline) routeGraphic.getGeometry()).getPoint(0), startSymbol);				
			 
			endGraphic = new Graphic(
    					((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
    							.getGeometry()).getPointCount() - 1), endSymbol);
     	    
            routeLayer.addGraphics(new Graphic[] { routeGraphic, startGraphic, endGraphic});   
            
			//map.setExtent(curRoute.getEnvelope(), 250);			
			}
		}
		else{
			curRoute = mResults.getRoutes().get(0);
			
			// Add all the route segments with their relevant information to the
			// hiddenSegmentsLayer, and add the direction information to the list
			// of directions
			for (RouteDirection rd : curRoute.getRoutingDirections()) {
				HashMap<String, Object> attribs = new HashMap<String, Object>();
				attribs.put("text", rd.getText());
				attribs.put("time", Double.valueOf(rd.getMinutes()));
				attribs.put("length", Double.valueOf(rd.getLength()));
				curDirections.add(String.format(
						"%s%nTime: %.1f minutes, Length: %.1f miles", rd.getText(),
						rd.getMinutes(), rd.getLength()));
				Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider, attribs);
				hiddenSegmentsLayer.addGraphic(routeGraphic);
				
				routedetail=routedetail+rd.getText();
			}
			// Reset the selected segment
			selectedSegmentID = -1;

			// Add the full route graphic and destination graphic to the routeLayer
			Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic()
					.getGeometry(), routeSymbol);
			routegrac=routeGraphic;
			startGraphic = new Graphic(
					((Polyline) routeGraphic.getGeometry()).getPoint(0), startSymbol);
     
     	    endGraphic = new Graphic(
    					((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
    							.getGeometry()).getPointCount() - 1), endSymbol);
     	    
            routeLayer.addGraphics(new Graphic[] { routeGraphic, startGraphic, endGraphic});   
            
			// Get the full route summary and set it as our current label
			routeSummary = String.format(
					"%s%nTotal time: %.1f minutes, length: %.1f miles",
					curRoute.getRouteName(), curRoute.getTotalMinutes(),
					curRoute.getTotalMiles());
			directionsLabel.setText(routeSummary);
			// Zoom to the extent of the entire route with a padding
			map.setExtent(curRoute.getEnvelope(), 250);
		}
			
}
		/**
		 * On returning from the list of directions, highlight and zoom to the
		 * segment that was selected from the list. (Activity simply resumes if the
		 * back button was hit instead).
		 */
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			// Response from directions list view
			if (requestCode == 1) {
				if (resultCode == RESULT_OK) {
					String direction = data.getStringExtra("returnedDirection");
					if (direction == null)
						return;
					// Look for the graphic that corresponds to this direction
					for (int index : hiddenSegmentsLayer.getGraphicIDs()) {
						Graphic g = hiddenSegmentsLayer.getGraphic(index);
						if (direction
								.contains((String) g.getAttributeValue("text"))) {
							// When found, hide the currently selected, show the new
							// selection
							hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
									segmentHider);
							hiddenSegmentsLayer.updateGraphic(index, segmentShower);
							selectedSegmentID = index;
							// Update label with information for that direction
							directionsLabel.setText(direction);
							// Zoom to the extent of that segment
							map.setExtent(
									hiddenSegmentsLayer.getGraphic(
											selectedSegmentID).getGeometry(), 50);
							break;
						}
					}
				}
			}
		}
		
		public  ArrayList<shelter> readsheltercoor() {
			  try{
				  ArrayList<shelter> tshelter = new ArrayList<shelter>();
			      InputStreamReader inputStreamReader = new InputStreamReader(getResources().getAssets().open("AC_shelters.json"), "UTF-8");
			      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			      String line;
			      StringBuilder stringBuilder = new StringBuilder();
			      while((line = bufferedReader.readLine()) != null) {
			        stringBuilder.append(line);
			      }
			      bufferedReader.close();
			      inputStreamReader.close();
			      JSONObject jsonObject = new JSONObject(stringBuilder.toString());	     
			      JSONArray jsonArray = jsonObject.getJSONArray("features");
			      for (int i = 0; i < jsonArray.length(); i++) {
			        JSONObject object = jsonArray.getJSONObject(i);
			        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
			        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
			        
			        int id= jsonObjectattribute.getInt("OBJECTID");
			        double x=jsonObjectgeometry.getDouble("x");
			        double y= jsonObjectgeometry.getDouble("y");
			        
			        shelter st=new shelter();
			        st.id=id;
			        st.y=y;
			        st.x=x;
			        tshelter.add(st);
			      }
			      return tshelter;//-79.941703,40.447945,-79.923992,40.457085  

			    }catch (JSONException e2) {
					
					e2.printStackTrace();
				} catch (JsonParseException e3) {
					
					e3.printStackTrace();
				} catch (IOException e4) {
				
					e4.printStackTrace();
				}
				return null; 
			}
			public  ArrayList<roadsegment> readsegmentcoor() {
				  try{
					  ArrayList<roadsegment> tshelter = new ArrayList<roadsegment>();
				      InputStreamReader inputStreamReader = new InputStreamReader(getResources().getAssets().open("AC_roadsegments.json"), "UTF-8");
				      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				      String line;
				      StringBuilder stringBuilder = new StringBuilder();
				      while((line = bufferedReader.readLine()) != null) {
				        stringBuilder.append(line);
				      }
				      bufferedReader.close();
				      inputStreamReader.close();
				      JSONObject jsonObject = new JSONObject(stringBuilder.toString());	     
				      JSONArray jsonArray = jsonObject.getJSONArray("features");
				      for (int i = 0; i < jsonArray.length(); i++) {
				        JSONObject object = jsonArray.getJSONObject(i);
				        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
				        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
				        
				        int id= jsonObjectattribute.getInt("OBJECTID");
				        JSONArray  path=jsonObjectgeometry.getJSONArray("paths");
				        
				        roadsegment st=new roadsegment();
				        st.id=id;
				        st.paths=path;
				       
				        tshelter.add(st);
				      }
				      return tshelter;

				    }catch (JSONException e2) {
						
						e2.printStackTrace();
					} catch (JsonParseException e3) {
						
						e3.printStackTrace();
					} catch (IOException e4) {
					
						e4.printStackTrace();
					}
					return null; 
				}
			
		private class MyLocationListener implements LocationListener {

			public MyLocationListener() {
				super();
			}

			/**
			 * If location changes, update our current location. If being found for
			 * the first time, zoom to our current position with a resolution of 20
			 */
			public void onLocationChanged(Location loc) {
				if (loc == null)
					return;
				boolean zoomToMe = (mLocation == null) ? true : false;
				mLocation = new Point(loc.getLongitude(), loc.getLatitude());
				if (zoomToMe) {
					Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
					map.zoomToResolution(p, 20.0);
				}
			}

			public void onProviderDisabled(String provider) {
				Toast.makeText(getApplicationContext(), "GPS Disabled",
						Toast.LENGTH_SHORT).show();
			}

			public void onProviderEnabled(String provider) {
				Toast.makeText(getApplicationContext(), "GPS Enabled",
						Toast.LENGTH_SHORT).show();
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

		}

		@Override
		protected void onPause() {
			super.onPause();
			map.pause();
		}

		@Override
		protected void onResume() {
			super.onResume();
			map.unpause();
		}
		
  /**
   * Redraws everything on the mGraphicsLayerEditing layer following an edit and updates the items shown on the action
   * bar.
   */
  void refresh() {
    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }
    drawPolylineOrPolygon();
    drawMidPoints();

    //updateActionBar();
  }

  /**
   * Draws polyline or polygon (dependent on current mEditMode) between the vertices in mPoints.
   */
  private void drawPolylineOrPolygon() {
    Graphic graphic;
    MultiPath multipath;

    // Create and add graphics layer if it doesn't already exist
    if (mGraphicsLayerEditing == null) {
      mGraphicsLayerEditing = new GraphicsLayer();
      map.addLayer(mGraphicsLayerEditing);
    }

    if (mPoints.size() > 1) {

      // Build a MultiPath containing the vertices
      if (mEditMode == EditMode.POLYLINE) {
        multipath = new Polyline();
      } else {
        multipath = new Polygon();
      }
      multipath.startPath(mPoints.get(0));
      for (int i = 1; i < mPoints.size(); i++) {
        multipath.lineTo(mPoints.get(i));
      }

      // Draw it using a line or fill symbol
      if (mEditMode == EditMode.POLYLINE) {
        graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK, 4));
      } else {
        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
        simpleFillSymbol.setAlpha(100);
        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
        graphic = new Graphic(multipath, (simpleFillSymbol));
      }
      mGraphicsLayerEditing.addGraphic(graphic);
    }
  }

  /**
   * Draws mid-point half way between each pair of vertices in mPoints.
   */
  private void drawMidPoints() {
    int index;
    Graphic graphic;

    mMidPoints.clear();
    if (mPoints.size() > 1) {

      // Build new list of mid-points
      for (int i = 1; i < mPoints.size(); i++) {
        Point p1 = mPoints.get(i - 1);
        Point p2 = mPoints.get(i);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }
      if (mEditMode == EditMode.POLYGON && mPoints.size() > 2) {
        // Complete the circle
        Point p1 = mPoints.get(0);
        Point p2 = mPoints.get(mPoints.size() - 1);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }

      // Draw the mid-points
      index = 0;
      for (Point pt : mMidPoints) {
        if (mMidPointSelected && mInsertingIndex == index) {
          graphic = new Graphic(pt, mRedMarkerSymbol);
        } else {
          graphic = new Graphic(pt, mGreenMarkerSymbol);
        }
        mGraphicsLayerEditing.addGraphic(graphic);
        index++;
      }
    }
  }
  /**
   * Clears feature editing data and updates action bar.
   */
  void clear() {
    // Clear feature editing data
    mPoints.clear();
    mMidPoints.clear();


    mMidPointSelected = false;
    mVertexSelected = false;
    mInsertingIndex = 0;

    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }

  }
  /**
   * The MapView's touch listener.
   */
  private class MyTouchListener extends MapOnTouchListener {
    MapView mapView;

    public MyTouchListener(Context context, MapView view) {
      super(context, view);
      mapView = view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        // Start of a new gesture. Make sure mLongPressEvent is cleared.
        mLongPressEvent = null;
      }
      return super.onTouch(v, event);
    }

    @Override
    public void onLongPress(MotionEvent point) {
      // Set mLongPressEvent to indicate we are processing a long-press
      mLongPressEvent = point;
      super.onLongPress(point);
    }

  }
  
  /*
   * Leader send udp multicast warning info to followers when he get warning from CC
   * it include the default path and allocated shelter
   */
  private void multicasttofollower(String pushedroutedetail){
	  
		MCast MCastRun = new MCast();
		Thread MCastThread = new Thread(MCastRun);						
		String IPaddr=null;
		//String[] Segment;
		
		InputData.com=51;
		InputData.SenderID=3;
		InputData.ReceiverID=4;
		IPaddr = cc.getLocalIPAddress();		
		InputData.addr=new String(IPaddr);
		InputData.Port=7404;
		//InputData.SegID=new String[(pushedroutedetail.split(";")[0].split(",")).length];
		InputData.RegionID=Integer.parseInt(regionid);
		InputData.SegID=pushedroutedetail.split(";")[0].split(",");
		InputData.ShellID= Integer.parseInt(pushedroutedetail.split(";")[1]);
		InputData.Context=getBaseContext();
		MCastRun.GetInputData(InputData);
		
		try{
	
		MCastThread.start();
		
		while(running) {
			createTimerThreadAndStart();
			}	
		
		MCastThread.stop();
		}
		catch(Exception e){
			
		}
	  
  }

  private void showbarriers(int tp){
	  
      mGraphicsLayerEditing = new GraphicsLayer();
      map.addLayer(mGraphicsLayerEditing);
      mGraphicsLayerEditing.removeAll(); 
	
      String result;
      Graphic affectedgraphic=null;
		try {                  
			result = cc.socketservice("http://"+tip+":"+tport+"/ga/keyvaluestore?action=showallaffectedsegment&leaderid="+regionid+","+leaderid,tsocketaddress,tsocketport);
			//result = cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/keyvaluestore?action=showallaffectedsegment&leaderid="+regionid+","+leaderid);
			//1327:100.33693217061852,-0.8653466361882022,100.33693217061852,-0.8651093009464204;1155:100.34162787347653,-0.872974507181835,100.34162787347653,-0.8727954521969702  
			if(result!=""){//
				   
				   String[] affectedsegments=result.split(";");
				   	for(int j=0;j<affectedsegments.length;j++){
				   		String tempcoor0=affectedsegments[j].split(":")[1].replace("[", "");
				   		String tempcoor1=tempcoor0.replace("]", "");
				   		String[] coor=tempcoor1.split(",");
				   		
				   		Point affected0=(Point) GeometryEngine.project(new Point(Double.valueOf(coor[0]), Double.valueOf(coor[1])), egs, wm);
				   		Point affected1=(Point) GeometryEngine.project(new Point(Double.valueOf(coor[2]), Double.valueOf(coor[3])), egs, wm);
				   		
						PictureMarkerSymbol affectedSymbol = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_road03));
						
					    MultiPath multipath;						     
					    multipath = new Polyline();					
					    multipath.startPath(affected0);					   
					    multipath.lineTo(affected1);
					   if(tp==0)
					     affectedgraphic  = new Graphic(multipath, new SimpleLineSymbol(Color.RED, 4));
					   else if(tp==1)
						 affectedgraphic = new Graphic(affected0, affectedSymbol);
															 	
						mGraphicsLayerEditing.addGraphics(new Graphic[] { affectedgraphic });

				      }

		}	
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	  
  }
  
private void showshelters(){
	  
      mGraphicsLayerEditing = new GraphicsLayer();
      map.addLayer(mGraphicsLayerEditing);
      mGraphicsLayerEditing.removeAll(); 
	 
      Graphic affectedgraphic=null;
		try {
			
			for (shelter st : shelters) {													
					double sx=st.x;
					double sy=st.y;								
					tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
					endpoint=(Point)GeometryEngine.project(tsegment, egs, wm);
					
					PictureMarkerSymbol affectedSymbol = new PictureMarkerSymbol(
							map.getContext(), getResources().getDrawable(
									R.drawable.shelters));					
				    
				    affectedgraphic = new Graphic(endpoint, affectedSymbol);
				    
				    mGraphicsLayerEditing.addGraphics(new Graphic[] { affectedgraphic });
																										
			}
			
		
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	  
  }
  // tcp socket receive service
	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(socketreceiveport);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					socket = serverSocket.accept();

					CommunicationThread commThread = new CommunicationThread(socket);
					new Thread(commThread).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	class UDPMulticastServerThread implements Runnable {

		public void run() {			
	
			MulticastSocket multicastSocket;
			while (!Thread.currentThread().isInterrupted()) {
			try {
				    multicastSocket = new MulticastSocket(7404);
				    InetAddress address = InetAddress.getByName("239.1.1.171");
			        multicastSocket.joinGroup(address);
			        byte[] buf = new byte[1024];
			        System.out.println("UDP Multicast receiver");
			        while (true) {
			            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
			            multicastSocket.receive(datagramPacket);
			            
			            byte[] message = new byte[datagramPacket.getLength()]; 
			            System.arraycopy(buf, 0, message, 0, datagramPacket.getLength());
			            //System.out.println(datagramPacket.getAddress());
			            
			            updateConversationHandler.post(new updateUIThread(new String(message)));
			            
			            //System.out.println(new String(message));
			        }
			} catch (IOException e) {
				
				e.printStackTrace();
			} 

			}
		}
	}
	class UDPUnicastServerThread implements Runnable {

		public void run() {			
		
			Random random = new Random();
			DatagramSocket datagramSocket = null;
			try {
				datagramSocket = new DatagramSocket(random.nextInt(9999));
				datagramSocket.setSoTimeout(10 * 1000);				
			
				byte[] buffer = new byte[1024 * 64]; 
				String HPi=cc.getGateWay(getBaseContext());
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(HPi), 7404);		
				datagramSocket.receive(packet);
				byte[] bt = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), 0, bt, 0, packet.getLength());
				if(null != bt && bt.length > 0){
					
					 updateConversationHandler.post(new updateUIThread(new String(Arrays.toString(bt))));
					 
				}
				Thread.sleep(1 * 1000);
				} catch (Exception e) {
					datagramSocket = null;
					//System.out.println("Client start fail");
					e.printStackTrace();
				}
		}
	}
	class CommunicationThread implements Runnable {

		private Socket clientSocket;

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {

				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			

			//while (!Thread.currentThread().isInterrupted()) {

				try {

					String read = input.readLine();

					updateConversationHandler.post(new updateUIThread(read));
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			//}
		}

	}

	class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			if(msg!=null){
				tempmsg=msg;
				text = (TextView) findViewById(R.id.directionsLabel);
                //Properties prop1 = PropertiesUtil.getProperties();
                String strleaderlocation = (String) prop1.get("leaderlocation");
                //String strleaderlocation = msg.split(";")[4];
                String coor[]=strleaderlocation.split(",");
            	Point leaderlocaiton=null;
	               
				tsegment = (Point)GeometryEngine.project(Double.valueOf(coor[0]),Double.valueOf(coor[1]),egs);								
				Point point=(Point)GeometryEngine.project(tsegment, egs, wm);																																		
				leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));	
            	
            	
            	/**
            	 * 1.warning from cc to HPi to MPi to leader and follower
            	 * Format	Value
					Len		
					Cmd	1
					Sender ID	0
					Receiver ID	199
					Option	RR:regionid
					Data	{Data}
   	
            	 */
				if(msg.split(";")[1].equals("1")){//fromat data = "13;1;0;199;1;###"		
					mapoperationtype=0;
					text.setText("Warning Notification.");
					final DialogInterface dialog=new AlertDialog.Builder(LeaderEvacuationActivity.this)
					.setIcon(R.drawable.warning)
					.setTitle(" ")
					.show();
					
					TimerTask task = new TimerTask(){    
					public void run(){    
						dialog.dismiss();
						
						//add default route when get notification						
						showdefaultroute(0,"");

						pushedroutedetail = (String) prop1.get("defaultroute");
						multicasttofollower(pushedroutedetail);	
						  }    
						};    
					Timer timer = new Timer();  
					timer.schedule(task, 5000); 
				}
				/**
				 * udp unicast--detour-from  MPi to leader
				 */ 
				if(msg.split(";")[1].equals("7")){//"len;7;2PP;3LL;RR;39,b079949f8ac8,10.130.10.55:22223,100.3412778:-0.8766734,6	
					mapoperationtype=2;
					mPoints.clear(); 
					mGraphicsLayerEditing.removeAll(); 
					//routeLayer.removeAll();
					//hiddenSegmentsLayer.removeAll();													
					//mResults = null;
					text.setText("Detour Notification.");
						
					//***************************
					try {
       		                
		                affectedsegmentorder=Integer.valueOf(msg.split(";")[5].split(",")[4]);//Integer.valueOf(msg.split(";")[5]);
		                
		                if(affectedsegmentorder>=0){
								                
			                showdefaultroute(affectedsegmentorder,"");			
							
						   //shelter location from local config
			                double sx=0,sy=0;
						    String tshelterinfo = (String) prop1.get("shelterinfo");
						    String[] shelterinfos=tshelterinfo.split(";");
						    sx=Double.valueOf(shelterinfos[2].split(":")[1].split(",")[0]);
						    sy=Double.valueOf(shelterinfos[2].split(":")[1].split(",")[1]);
						    							   
						    if(sx!=0&&sy!=0)
						    	tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
						    String stops;							
							if(mLocation!=null)								
							{															
								stops=mLocation.getX()+","+mLocation.getY()+";"+tsegment.getX()+","+tsegment.getY();																											
							}
							else{		
								
								stops=leaderlocaiton.getX()+","+leaderlocaiton.getY()+";"+tsegment.getX()+","+tsegment.getY();
							}
							
							
							String shelterallocator=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/pathfinder?action=pathfinder" +
									"&stops="+stops+
									"&leaderid="+regionid+","+leaderid);																																   
							
							//135;6;0;3LL;01;1379,1348,1349,1350,1351,1352,1353,1354,1327,1322:40:
							//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33595962500004,-0.8653165459999741
							
							if(shelterallocator!=""){
								String routesegment=shelterallocator.split(";")[5];
								String segmentandshelter=routesegment.split(":")[0]+";"+routesegment.split(":")[1];
								showdefaultroute(0,segmentandshelter);
																		
								PropertiesUtil.setProperties("defaultroute", segmentandshelter);
								//PropertiesUtil.setProperties("shelterinfo",routesegment.split(":")[2]);								
								
							   }

					}
						//when the default path unavailable for some reason, 
						//the CC will find a new shelter for the leader
						//with safety path
						//***************************************
		            else if(affectedsegmentorder==-1){
		                	
		                	Toast.makeText(LeaderEvacuationActivity.this,
									"New shelter/path", Toast.LENGTH_SHORT).show();	
																								
							try {
							
								affectedsegmentorder=0;
																							
								String shelterallocator=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/shelterallocator?action=findclosestshelter" +
														"&llx="+leaderlocaiton.getX()+
														"&lly="+leaderlocaiton.getY()+
														"&leaderid="+regionid+","+leaderid);
								/*Information Format:
								 * len; command; sender id;receiver id; option; data
								 *{135;6;0;3LL;01;1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322:40:
								 * Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728}																						 						
								*/
								if(shelterallocator!=""){

									String routesegment=shelterallocator.split(";")[5];
									String segmentandshelter=routesegment.split(":")[0]+";"+routesegment.split(":")[1];
									showdefaultroute(0,segmentandshelter);
									
									//1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322;40
									PropertiesUtil.setProperties("defaultroute", segmentandshelter);
									//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728
									PropertiesUtil.setProperties("shelterinfo",routesegment.split(":")[2]);																		
								   }																						
							} 
						catch (Exception e) {
								mException = e;
								//mHandler.post(mUpdateResults);
							}
											
					mapoperationtype=0;	
		                }
				}
					catch (Exception e) {
						mException = e;
						//mHandler.post(mUpdateResults);
					}
				}
				
				//when the default path unavailable for some reason, 
				//the CC will find a new shelter for the leader
				//with safety path
				//***************************************
				if(msg.split(";")[1].equals("6")){	
																																		
					Toast.makeText(LeaderEvacuationActivity.this,
							"New shelter/path", Toast.LENGTH_SHORT).show();	
																						
					try {
															
						affectedsegmentorder=0;						
						
						String shelterallocator=cc.udpmulticastcommunicater("http://"+tip+":"+tport+"/ga/shelterallocator?action=findclosestshelter" +
								"&llx="+leaderlocaiton.getX()+
								"&lly="+leaderlocaiton.getY()+
								"&leaderid="+regionid+","+leaderid);
						/*Information Format:
						 * len; command; sender id;receiver id; option; data
						 *{135;6;0;3LL;01;1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322:40:
						 * Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728}																						 						
						*/
						if(shelterallocator!=""){
				
							String routesegment=shelterallocator.split(";")[5];
							String segmentandshelter=routesegment.split(":")[0]+";"+routesegment.split(":")[1];
							showdefaultroute(0,segmentandshelter);
							
							//1333,1334,1335,1336,1337,1332,1572,1351,1352,1353,1354,1327,1322;40
							PropertiesUtil.setProperties("defaultroute", segmentandshelter);
							//Name>MASJID MUHAJIRIN%Address>Pasir Putih Kelurahan Bungo Pasang%location>100.33596019200007,-0.8653164869999728
							PropertiesUtil.setProperties("shelterinfo",routesegment.split(":")[2]);
										
						   }
					} 
			catch (Exception e) {
						mException = e;
						//mHandler.post(mUpdateResults);
					}
									
			mapoperationtype=0;	
			//*****************************
		}									

		else if(msg.split(";")[1].equals("3")){	
					mapoperationtype=0;
				}
		}
			}

		
	}  
	
	private void showdefaultroute( int tsegmentorder, String pathdetail){
		
		//Properties prop1 = PropertiesUtil.getProperties();
		if(pathdetail!="")
			pushedroutedetail = pathdetail;
		else
			pushedroutedetail = (String) prop1.get("defaultroute");
        
        
		// Clear the graphics and empty the directions list
		routeLayer.removeAll();
		hiddenSegmentsLayer.removeAll();													
		mResults = null;
		
	    initializeRoutingAndGeocoding();
		try{
			if(pushedroutedetail!=""&& pushedroutedetail!=null)
			{
				String rd[]=pushedroutedetail.split(";");
				String segid[]=rd[0].split(",");
				int startpt=0;
				Graphic routeGraphic=null;
				//add segment to map
				SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
				
				// Symbols for the route and the destination (blue line, checker flag)
				PictureMarkerSymbol startSymbol = new PictureMarkerSymbol(
						map.getContext(), getResources().getDrawable(
								R.drawable.route_strat_24));
				
				PictureMarkerSymbol endSymbol = new PictureMarkerSymbol(
						map.getContext(), getResources().getDrawable(
								R.drawable.route_stop_24));
			    multipath = new Polyline();
			    double mindistance=999999;
			
				//prepare segment and shelter info
			    int order=0;
				//ArrayList<shelter> shelters=readsheltercoor(); 
				//ArrayList<roadsegment> roadsegments=readsegmentcoor(); 
				for(String sid:segid){
					for (roadsegment rs : roadsegments) {
						startpt=0;
						if(rs.id==Integer.parseInt(sid) && ((tsegmentorder==0) ||(order < tsegmentorder))){			      
						      for (int i = 0; i < rs.paths.length(); i++) {
						    				    		 
									JSONArray jsonArray1 = rs.paths.getJSONArray(i);
									for (int j = 0; j < jsonArray1.length(); j++) {
										JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
										
										Object jsonArray3 = jsonArray2.get(0);
										Object jsonArray4 = jsonArray2.get(1);
											
										double sx=Double.parseDouble(jsonArray3.toString());
										double sy=Double.parseDouble(jsonArray4.toString());								
										tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
										Point pt2=(Point)GeometryEngine.project(tsegment, egs, wm);
											
										
										//search the closest point of the route to the user location
										if(mLocation!=null && (Math.sqrt(Math.pow((sx-mLocation.getX()),2)+Math.pow((sy-mLocation.getY()),2))<mindistance))
										//if((Math.sqrt(sx+79.953910)+Math.sqrt(sy-40.442547))<mindistance){
										{
											tsartpt=tsegment;
											mindistance=Math.sqrt(Math.pow((sx-mLocation.getX()),2)+Math.pow((sy-mLocation.getY()),2));
										}
											
										if(startpt==0){				
											multipath.startPath(pt2);
											startpt=1;
										}
										else
											multipath.lineTo(pt2);								
																
									}												
						    }
						      if(multipath!=null){
						    	  routeGraphic = new Graphic(multipath, routeSymbol);
						    	  routeLayer.addGraphics(new Graphic[] {routeGraphic});
						      }
						      order++;
						}																							
					}
				}
				
				for (shelter st : shelters) {
					if(st.id==Integer.parseInt(rd[1])){														
						double sx=st.x;
						double sy=st.y;								
						tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
						endpoint=(Point)GeometryEngine.project(tsegment, egs, wm);
						
						if(mLocation!=null && (Math.sqrt(Math.pow((sx-mLocation.getX()),2)+Math.pow((sy-mLocation.getY()),2))<mindistance))								
						{
							tsartpt=tsegment;
							mindistance=Math.sqrt(Math.pow((sx-mLocation.getX()),2)+Math.pow((sy-mLocation.getY()),2));
						}
					}																							
				}
				if(mLocation!=null)
				{
					Point pl = (Point) GeometryEngine.project(mLocation, egs, wm);
					startGraphic = new Graphic(pl, startSymbol);
				}
				endGraphic = new Graphic(endpoint, endSymbol);
				
				if(tsartpt!=null)
				{
					
					  Point s = (Point) GeometryEngine.project(tsartpt, egs, wm);
				      StopGraphic stop = new StopGraphic(s.copy());
				      stop.setName("");
				      mStops.addFeature(stop);
					
				      if(mLocation!=null)
				      {
				    	
				    	  Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
				    	
				    	  StopGraphic mlocationstop = new StopGraphic(p.copy());
				    	  mlocationstop.setName("");
				    	  mStops.addFeature(mlocationstop);
				      }    
				      try{
				    	// Set the correct input spatial reference on the stops and the
				          // desired output spatial reference on the RouteParameters object.
				          SpatialReference mapRef = map.getSpatialReference();
				          RouteParameters params = mRouteTask.retrieveDefaultRouteTaskParameters();
				          params.setOutSpatialReference(mapRef);
				          mStops.setSpatialReference(mapRef);						          
				         
				          // Set the stops and since we want driving directions,
				          // returnDirections==true
				          params.setStops(mStops);
				          params.setReturnDirections(true);
				          
				          //set barrier polyline
						 /* NAFeaturesAsFeature barriernafaf = new NAFeaturesAsFeature();
						  barriernafaf.setSpatialReference(map.getSpatialReference());
						  barriernafaf.addFeature(feature);
				          params.setPolylineBarriers(barriernafaf);
							*/
				          // Perform the solve
				          RouteResult results = mRouteTask.solve(params);

				          // Grab the results; for offline routing, there will only be one
				          // result returned on the output.
				          Route result = results.getRoutes().get(0);

				          // Remove any previous route Graphics


				          // Add the route shape to the graphics layer
				          Geometry geom = result.getRouteGraphic().getGeometry();
				          routeLayer.addGraphic(new Graphic(geom, new SimpleLineSymbol(0x99990055, 5)));							         
				          map.getCallout().hide();
				    	  
				      }catch (Exception e) {
				         
				          e.printStackTrace();
				        }
				      
					
				}
				if(tsegmentorder==0)		
					routeLayer.addGraphics(new Graphic[] {startGraphic, endGraphic});				
				else
					routeLayer.addGraphics(new Graphic[] {startGraphic});
				
				showbarriers(0);
				// Zoom to the extent of the entire route with a padding
				map.setExtent(routeGraphic.getGeometry(), 250);
																				
			}
			}
			catch (JSONException e1) {
					
					e1.printStackTrace();
				}  catch (Exception e) {
				
					e.printStackTrace();
					mException = e;
					mHandler.post(mUpdatedefaultResults);
			}
		
	}
	private boolean running = true;
    private Thread timerThread;
	 private void createTimerThreadAndStart() {
		 if(timerThread==null) {
		 timerThread = new Thread(new Runnable() {
		 @Override
		 public void run() {
		 for(int i=0;i<1;i++) {
		 try {
			 Random rm=new Random();
			 int timer=rm.nextInt(2)*1000+1000;
		
			 Thread.sleep(timer);
		 } catch (InterruptedException e) {
		 running = false;
		 }
		 if(i==1) running = false;
		 }
		 }
		 });
		 timerThread.start();
		 }
		     }

	     
	     /**
	      * check current network available
	      * 
	      * @param context
	      * @return
	      */
	     
	     public boolean isNetworkAvailable(Activity activity)
	     {
	         Context context = activity.getApplicationContext();
	         // get phone connectivity manager object, include wi-fi,net
	         ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	         
	         if (connectivityManager == null)
	         {
	             return false;
	         }
	         else
	         {
	             // get NetworkInfo object
	             NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
	             
	             if (networkInfo != null && networkInfo.length > 0)
	             {
	                 for (int i = 0; i < networkInfo.length; i++)
	                 {
	                     System.out.println(i + "===Status===" + networkInfo[i].getState());
	                     System.out.println(i + "===type===" + networkInfo[i].getTypeName());
	                     // judge current network status on/off
	                     if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
	                     {
	                         return true;
	                     }
	                 }
	             }
	         }
	         return false;
	     }
	 
}