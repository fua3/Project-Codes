package com.pitt.cdm.arcgis.android.geometryeditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.util.InetAddressUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.esri.android.map.MapView;
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
import com.pitt.cdm.arcgis.android.networkanalysis.NetworkService;
import com.pitt.cdm.arcgis.android.networkanalysis.RoutingShowDirections;
import com.pitt.cdm.arcgis.android.attributeeditor.AttributeEditorActivity;
import com.pitt.cdm.arcgis.android.maps.MainActivity;
import com.pitt.cdm.offlinemapupdate.PropertiesUtil;
import com.pitt.cdm.arcgis.android.maps.R;



public class GeometryEditorActivity extends FragmentActivity {

  protected static final String TAG = "EditGraphicElements";

  private static final String TAG_DIALOG_FRAGMENTS = "dialog";

  private static final String KEY_MAP_STATE = "com.esri.MapState";

  private enum EditMode {
    NONE, POINT, POLYLINE, POLYGON, SAVING
  }

  
  Menu mOptionsMenu;

  MapView map;

  String mMapState;

  android.support.v4.app.DialogFragment mDialogFragment;

  GraphicsLayer mGraphicsLayerEditing;

  ArrayList<Point> mPoints = new ArrayList<Point>();

  ArrayList<Point> mMidPoints = new ArrayList<Point>();

  boolean mMidPointSelected = false;

  boolean mVertexSelected = false;

  int mInsertingIndex;

  EditMode mEditMode;

  boolean mClosingTheApp = false;



  ArrayList<ArcGISFeatureLayer> mFeatureLayerList;
  
  ArcGISTiledMapServiceLayer tileLayer;

  FeatureTemplate mTemplate;

  ArcGISFeatureLayer mTemplateLayer;

  SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);

  MotionEvent mLongPressEvent;
  
  ArcGISFeatureLayer mRouteLayer,mTemproadsegmentLayer,mTempSheltersLayer,
  mTempLeaderrouteLayer,mTemppolylinebarrierLayer;
  
  GraphicsLayer routeLayer, hiddenSegmentsLayer;
  TextView pushtxt;
  private String resultData;
  String leaderid,leaderip,regionid;
  FeatureSet queryrt; 
  FragmentActivity fragactivity=new FragmentActivity();
  
  
  //for offline routing
  private static File demoDataFile;
  private static String offlineDataSDCardDirName;
  private static String filename;
  ArcGISLocalTiledLayer localTiledLayer;
  final String extern = Environment.getExternalStorageDirectory().getPath();   
  NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();
  Point tsartpt=null;
  Locator mLocator = null;
  View mCallout = null;
  
  //for routing to shelter activity
	// Symbol used to make route segments "invisible"
  SimpleLineSymbol segmentHider = new SimpleLineSymbol(Color.WHITE, 5);
	// Symbol used to highlight route segments
  SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);
	// Label showing the current direction, time, and length
  TextView directionsLabel;
	// List of the directions for the current route (used for the ListActivity)
  ArrayList<String> curDirections = null;
	// Current route, route summary, and gps location
  Route curRoute = null;
  String routeSummary = null;
  Point mLocation = null;
	// Global results variable for calculating route on separate thread
  RouteTask mRouteTask = null;
  RouteResult mResults = null;
  String mrouteresult="";
	// Variable to hold server exception to show to user
  Exception mException = null;
	// Handler for processing the results
  final Handler mHandler = new Handler();
  final Runnable mUpdateResults = new Runnable() {
		public void run() {
			try {
				updateUI();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
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
	// Index of the currently selected route segment (-1 = no selection)
	int selectedSegmentID = -1;
	String startpt,endpt,followers;
    String routedetail,pushedroutedetail;
    Graphic startGraphic,endGraphic,newGraphic;
    Map<String, Object> tempattributes = new HashMap<String, Object>();
    MultiPath multipath;
    int tempoldrouteupdate=0;
    Graphic[] tgraphic;
  

    String barrierUrl;

    Polygon pg;
    Point shelterlocaiton;
    Object shelterid;
 
    Object templeaderid;
    Object sheltercapacity;
    Graphic routegrac;
    String segmentids="", segmentstartpts="", pushinfofromleader2followers="",endstoplatlon, pushedinfo="";
  

  //use for leader broadcast info to followers
    private static String LOG_TAG = "WifiBroadcastActivity";
    Button btngotobroadcastform;

	
    String updateinfo;
  
  //************************
  //closest shelter
  // The current map extent, use to set the extent of the map after switching basemaps.
  Polygon mCurrentMapExtent = null;
  
  
  Button btnEditApply;
// basemap layer
  ArcGISTiledMapServiceLayer basemap;
// feature layer
  ArcGISFeatureLayer fLayer;
  ArcGISFeatureLayer mTemplatebarrierLayer;
// closest facility task
  ClosestFacilityTask closestFacilityTask;
// route definition
  Route route;
// graphics layer to show route
  //Graphic startgraphic;

  String startptLatLon,endptLatLon;
// create UI components

  Boolean auth;
  TextView followernumlabel,multicastlabel;
  EditText followernum;
  Button startmulticast, stopmulticast;
  SpatialReference spatialRef =  SpatialReference.create(32612);//102100	4326

  String queryUrl = "";
  // default sample service	
  //String closestFacilitySampleUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACNetworkAnalysis/NAServer/Closest Facility";
  String closestFacilitySampleUrl;// = getResources().getString(R.string.arcgis_server_url)+"ACNetworkAnalysis/NAServer/Closest Facility";                                 

  String tUsername;
  
  int mapoperationtype=1;//default=1;map long press control:  0=default or received route, 1=shelter allocater, 2=path finder
  int defaulroute=1;  //default=0, 0=new route, 1=default route
  public class shelter {  
      int id;
      double x;
		double y;        
      }
  public class roadsegment {  
      int id;  
      JSONArray paths;
      }
  Point tsegment=null, endpoint;
  
  private ServerSocket serverSocket;
  Handler updateConversationHandler;
  Thread serverThread = null;
  int SERVERPORT = 22223;
  String username,serverport,tempmsg="";
  private TextView text;
	
	//for multicast
	
  TextView Label;
  Button StartButton;
  Button StopButton;
  
  boolean start;
  MulticastSocket Sndmcast =null;
  MulticastSocket Rcvcast =null;
  DatagramSocket Snducast=null;
  DataStruct InputData=null;
  String broadcastinfo="";
  
  BufferedReader in = null;
  PrintWriter out = null;
  Socket sk = null;
  String tip="136.142.186.102";
  
  String gatewayip;
  
	ArrayList<shelter> shelters;
	ArrayList<roadsegment> roadsegments; 
	ArrayList<shelter> routesegments; 
	ArrayList<String> segmentsorts;
	
	int reporttype;
	PictureMarkerSymbol reporticon;
	ImageButton dialogButtonBuilding, dialogButtonBridge, dialogButtonRoad;
	
	boolean rereport= true;
	int affectedsegmentorder=0;
	

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize progress bar before setting content
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(false);
    setContentView(R.layout.ge_main);
    

	updateConversationHandler = new Handler();		 
    this.serverThread = new Thread(new ServerThread());
    this.serverThread.start();
    
    tip=getResources().getString(R.string.coordinator_ip);
//step 1: add local map
			//@ create the path to local tpk
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
		    
		    //followernum.setText("F Number:");
//Step 2: add feature layers
		    
		    // Create status listener for feature layers
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
			
			
			//prepare segment and shelter info
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
					//"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACFacilities/FeatureServer/0",
					getResources().getString(R.string.arcgis_server_url)+"KelurahanFacilities/FeatureServer/34",
					ArcGISFeatureLayer.MODE.ONDEMAND);
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
			 

				 		
				// Get the location service and start reading location. Don't auto-pan
				// to center our position
				LocationDisplayManager ls = map.getLocationDisplayManager();
				ls.setLocationListener(new MyLocationListener());
				ls.start();
				ls.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
				
			 
				// Set the directionsLabel with initial instructions.
				directionsLabel = (TextView) findViewById(R.id.directionsLabel);
    	
				Intent intent=getIntent();  
		        
		        //leaderid=getMacAddress(getBaseContext())+","+getIp(getBaseContext())+":22223";
		        leaderid=getMacAddress(getBaseContext())+","+intent.getStringExtra("username");
		       
		        pushedroutedetail=intent.getStringExtra("routedetail");
		        //regionid=intent.getStringExtra("regionid");
		        
                Properties prop1 = PropertiesUtil.getProperties();
                regionid = (String) prop1.get("regionid");
		        
		        String piip=getGateWay(getBaseContext());
		        
		        
				 //show road barriers in key value store
		        showbarriers(0);
					
		        //update leader position to pi, never go to CC
		        /*
		        String turl="10;"+leaderid+";"+leaderip+"###";
		        try {
		        			        	
		        	String updateip=getCCinfo(turl,InetAddress.getByName(piip),22226);
					
				} catch (UnknownHostException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				*/	
		 
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
																								
					      		Dialog prcoseedialog = ProgressDialog.show(GeometryEditorActivity.this, "",
										"Path Finding...", true);	
							    try{
							        	
										String result=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findsegmentsbyleaderid&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
										
								        
								        if(result.equals("")){
								        		mapoperationtype=1;
								        		pushedroutedetail="";
								        	}
								        else if( mapoperationtype!=2 ){
								        		mapoperationtype=0;	
												String result1=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findshelterbyleaderid&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);  
							    		        if(result1.equals(""))
							    		        	pushedroutedetail="";					    		        
							    		        else
							    		        	pushedroutedetail=result+";"+result1;
								        	}									
									   
								}  catch (UnknownHostException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}catch (Exception e) {
									mException = e;
								}
												        	
						        	/*   }
							
						        else if(defaultsafety.equals("0")){
						        	mapoperationtype=0;						        	
						        }
							*/	
								//0. default route
								if(mapoperationtype==0){
									
						      		Toast.makeText(GeometryEditorActivity.this,
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
											
											routeLayer.removeAll();
											hiddenSegmentsLayer.removeAll();
											curDirections = new ArrayList<String>();
											
											
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
											//!!!!!!!!!!!!!!!!!!

											for(String sid:segid){
												for (roadsegment rs : roadsegments) {
													startpt=0;
													if(rs.id==Integer.parseInt(sid)){			      
													      for (int i = 0; i < rs.paths.length(); i++) {
													    				    		 
																JSONArray jsonArray1 = rs.paths.getJSONArray(i);
																for (int j = 0; j < jsonArray1.length(); j++) {
																	JSONArray jsonArray2 = jsonArray1.getJSONArray(j);//=[12.33,34.34]&Point [m_attributes=[NaN, NaN], m_description=com.esri.core.geometry.VertexDescription@7c5d0f85]
																	
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
											
						
											
											Properties prop1 = PropertiesUtil.getProperties();
											String shinfo = (String) prop1.get("shelterinfo");											
							                Map<String, Object> tmap = new HashMap<String, Object>();											
							                tmap.put("attributes", shinfo);		
							                endGraphic = new Graphic(endpoint, endSymbol,tmap);
							                
											routeLayer.addGraphics(new Graphic[] {startGraphic,endGraphic});
											
											
											


											// Zoom to the extent of the entire route with a padding
											map.setExtent(routeGraphic.getGeometry(), 250);
																											
										}
										else{
																	
												JsonFactory factory = new JsonFactory();
											    JsonParser parser = factory.createJsonParser(getResources().getAssets().open("defaultroute.json"));											
											    parser.nextToken();
											    mResults= RouteResult.fromJson(parser); 
											    mHandler.post(mUpdatedefaultResults);
											    																								
											} 
										}
										catch (JSONException e1) {
												
												e1.printStackTrace();
											} catch (JsonParseException e) {
												
												e.printStackTrace();
											} catch (IOException e) {
											
												e.printStackTrace();
											} catch (Exception e) {
											
												e.printStackTrace();
												mException = e;
												mHandler.post(mUpdatedefaultResults);
											}
									
								}
								
								//1. Closest shelter allocate
								if(mapoperationtype==1){
																				
						      		Toast.makeText(GeometryEditorActivity.this,
						      				"New Route", Toast.LENGTH_SHORT).show();	
									
							
											try {
												
												affectedsegmentorder=0;
												Point point;
												 point = map.toMapPoint(x, y);	
												
											    /*if(mLocation!=null)
											    {
											    	
											    	  point = (Point) GeometryEngine.project(mLocation, egs, wm);
											    }  
											      */
												Point leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));								
												//String incidentpt=leaderlocaiton.getX()+","+leaderlocaiton.getY();
												
												//get the closest road junction
												
												String envelopcoor=(leaderlocaiton.getX()-0.0005)+","+(leaderlocaiton.getY()-0.0005)+","+(leaderlocaiton.getX()+0.0005)+","+(leaderlocaiton.getY()+0.0005);
												
												String closestjunctionresult=getCCinfo("http://"+tip+":8080/ga/closestjunction?&geometry="+envelopcoor+"&geometryType=esriGeometryEnvelope"
														+ "&inSR=4326&spatialRel=esriSpatialRelContains&outFields=OBJECTID&returnGeometry=true&returnTrueCurves=false&returnIdsOnly=false&returnCountOnly=false&returnZ=false"
														+ "&returnM=false&returnDistinctValues=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
											   double dis=999999;
											   double startx=0,starty=0;
												if(closestjunctionresult!=""){
													
												   	  JSONObject jsonObject = new JSONObject(closestjunctionresult);	     
												      JSONArray jsonArray = jsonObject.getJSONArray("features");
												      for (int i = 0; i < jsonArray.length(); i++) {
												        JSONObject object = jsonArray.getJSONObject(i);
												        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
												        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
												        
												        int id= jsonObjectattribute.getInt("OBJECTID");
												        double x=jsonObjectgeometry.getDouble("x");
												        double y= jsonObjectgeometry.getDouble("y");
												        
												       if(Math.sqrt(Math.pow((x-leaderlocaiton.getX()),2)+Math.pow((y-leaderlocaiton.getY()),2))<dis)
												        {
												    	   dis=Math.sqrt(Math.pow((x-leaderlocaiton.getX()),2)+Math.pow((y-leaderlocaiton.getY()),2));
												    	   startx=x;
												    	   starty=y;
												    	   
												        }

												      }
													
												}
												
												
												String incidentpt=startx+","+starty;
												
												PropertiesUtil.setProperties("leaderlocation", incidentpt); 
												
												//query envelope
												//String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fshelters%3Fwhere%3D1%253D1%26outFields%3DNAME%26returnGeometry%3Dtrue%26f%3Dpjson%22%0D%0A%7D";
												  String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fshelters%3Fwhere%3Davailable%253D1%26returnGeometry%3Dtrue%26outFields%3DName%26f%3Dpjson%22%0D%0A%7D";
												//polyline barriers or key value store
												//String polylinebarriers="%7B+%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fpolylinebarriers%3Fwhere%3D1%253D1%26outFields%3DNAME%26returnGeometry%3Dtrue%26f%3Dpjson%22%0D%0A%7D";
												String polylinebarriers=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=showaffectedbarriers&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225); 
												   												 
												
												String result=getCCinfo("http://"+tip+":8080/ga/nacloseshelter?incidents="+incidentpt+"&facilities="+shelters+
															"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
													   		"&defaultTargetFacilityCount=1&outSR=102100&impedanceAttributeName=Length" +
													   		"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=true&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
													   		"&returnPolylineBarriers=false&returnPolygonBarriers=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
													   		"&outputLines=esriNAOutputLineTrueShapeWithMeasure&outputGeometryPrecisionUnits=esriDecimalDegrees" +
													   		"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
												   
												   													   							
													
												if(result!=""){
													   
													mrouteresult=result;
													JsonFactory factory = new JsonFactory();												   
													JsonParser parser =factory.createJsonParser(result);
												    parser.nextToken();
												    mResults= RouteResult.fromJson(parser); 
												    mHandler.post(mUpdateResults);											
												   }
												else{
										      		Toast.makeText(GeometryEditorActivity.this,
										      				"No result, try again.", Toast.LENGTH_SHORT).show();
													
												}
											} 
										catch (JSONException e1) {
												
												e1.printStackTrace();
											} catch (JsonParseException e) {
												
												e.printStackTrace();
											} catch (IOException e) {
											
												e.printStackTrace();
											} catch (Exception e) {
												mException = e;
												mHandler.post(mUpdateResults);
											}
															
									mapoperationtype=0;	
									

								}
								//2. route
								else if(mapoperationtype==2){
																				
						      		Toast.makeText(GeometryEditorActivity.this,
						      				"Update Route", Toast.LENGTH_SHORT).show();	
									
							
											try {
												Point point = map.toMapPoint(x, y);														
												Point leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));		
												
												String result0=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findshelterbyleaderid&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);  
												
												String incidentpt="";
												for (shelter st : shelters) {
													if(st.id==Integer.parseInt(result0)){														
														double sx=st.x;
														double sy=st.y;								
														tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
														//endpoint=(Point)GeometryEngine.project(tsegment, egs, wm);
														
														if(mLocation!=null)								
														{															
															 incidentpt=mLocation.getX()+","+mLocation.getY()+";"+tsegment.getX()+","+tsegment.getY();																											
														}
														else{															
															 incidentpt=leaderlocaiton.getX()+","+leaderlocaiton.getY()+";"+tsegment.getX()+","+tsegment.getY();
														}
													}																							
												}																																				   
												//polyline barriers or key value store
											
												String polylinebarriers=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=showaffectedbarriers&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225); 
												   												 
												 
												String result=getCCinfo("http://"+tip+":8080/ga/naroute?stops="+incidentpt+
															"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
													   		"&defaultTargetFacilityCount=1&outSR=102100&impedanceAttributeName=Length" +
													   		"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=true&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
													   		"&returnPolylineBarriers=false&returnPolygonBarriers=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
													   		"&outputLines=esriNAOutputLineTrueShapeWithMeasure&outputGeometryPrecisionUnits=esriDecimalDegrees" +
													   		"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
												   
												if(result!=""){
													
													   
													mrouteresult=result;
													JsonFactory factory = new JsonFactory();												   
													JsonParser parser =factory.createJsonParser(result);
												    parser.nextToken();
												    mResults= RouteResult.fromJson(parser);  	
													mHandler.post(mUpdateResults);											
												   }
												
												else{
										      		Toast.makeText(GeometryEditorActivity.this,
										      				"No road available, try again.", Toast.LENGTH_SHORT).show();
										      		 
													
												}
											} 
										catch (JSONException e1) {
												
												e1.printStackTrace();
											} catch (JsonParseException e) {
												
												e.printStackTrace();
											} catch (IOException e) {
											
												e.printStackTrace();
											} catch (Exception e) {
												mException = e;
												//mHandler.post(mUpdateResults);
											}
															
									mapoperationtype=0;	
									

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
															
					      		Toast.makeText(GeometryEditorActivity.this,
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
							   // drawPolylineOrPolygon();
							    
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
										reporttype=1;
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
								              			 Log.d(GeometryEditorActivity.TAG, "error updating feature: " + e.getLocalizedMessage());
								              			 //Toast.makeText(GeometryEditorActivity.this,"Fail", Toast.LENGTH_SHORT).show();  
								              		}

								              		@Override
								              		public void onCallback(FeatureEditResult[][] result) { 
								              	        // check the response for success or failure
								              	        if (result[2] != null && result[2][0] != null && result[2][0].isSuccess()) {

								              	          Log.d(GeometryEditorActivity.TAG, "Success updating feature with id=" + result[2][0].getObjectId());
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
								        	report(Long.parseLong(shelterid.toString()),reportgraphic);																			
									}
								});
								
								 dialogButtonBridge = (ImageButton) dialog2.findViewById(R.id.bridge);
								// if button is clicked, close the custom dialog
								dialogButtonBridge.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog2.dismiss();
										reporttype=2;
										reporticon = new PictureMarkerSymbol(
												map.getContext(), getResources().getDrawable(
														R.drawable.report_bridge01));
										Point reportpt = map.toMapPoint(x, y);
										Graphic reportgraphic = new Graphic(reportpt, reporticon);									
										//routeLayer.addGraphics(new Graphic[] { reportgraphic});
										report(-1,reportgraphic);							
																														
									}
								});
								
								 dialogButtonRoad = (ImageButton) dialog2.findViewById(R.id.road);
								// if button is clicked, close the custom dialog
								dialogButtonRoad.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog2.dismiss();
										reporttype=3;
										reporticon = new PictureMarkerSymbol(
												map.getContext(), getResources().getDrawable(
														R.drawable.report_road01));
										
										Point reportpt = map.toMapPoint(x, y);
										Graphic reportgraphic = new Graphic(reportpt, reporticon);									
										//routeLayer.addGraphics(new Graphic[] { reportgraphic});
										report(-1,reportgraphic);									
																														
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
    
//step 5: multicast
    
	//StartButton = (Button) this.findViewById(R.id.buttonsend);
	//StopButton = (Button) this.findViewById(R.id.buttonstop);
	Label = (TextView) this.findViewById(R.id.sendinfo);		
	//StartButton.setOnClickListener(listener);
	//StopButton.setOnClickListener(listener);
	
	
	WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
   	WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
   	multicastLock.acquire(); 
   	
   	
	ListMcast MCastListen = new ListMcast();
	Thread ListCastThread = new Thread(MCastListen);
	InputData=new DataStruct();
	 	
   	ListCastThread.start();
   	Label.setText("");
   	
   	
   	//get pi gateway ip info
   	
    //gatewayip=getGateWay(getBaseContext());
    //gatewayip=getMacAddress(getBaseContext());
    
    //if(gatewayip!="")
    //		tip=gatewayip;
    //Label.setText(gatewayip);
    
    
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
    map.setOnTouchListener(new MyTouchListener(GeometryEditorActivity.this, map));

    // If map state (center and resolution) has been stored, update the MapView with this state
    if (!TextUtils.isEmpty(mMapState)) {
      map.restoreState(mMapState);
    }
    
  }

  public void report(final long tid,  final Graphic gra){
	  
	  final Dialog dialog1=new Dialog(map.getContext(),R.style.cust_dialog);
		dialog1.setContentView(R.layout.custom_saveorcancel);
		dialog1.setTitle("Save or cancel");

		Button dialogButtonSave = (Button) dialog1.findViewById(R.id.dialogButtonSave);
		// if button is clicked, close the custom dialog
		dialogButtonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
											
	      		Toast.makeText(GeometryEditorActivity.this,
	      				"Save", Toast.LENGTH_SHORT).show();      		
				dialog1.dismiss();
				//clear all the report icons.
				routeLayer.removeAll();
				
				if(tid !=-1)
				{
					//routeLayer.addGraphics(new Graphic[] { gra});
				//******************
				    String result2;
					try {
						result2 = getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=getleadersbyshelterid&shelterid="+tid+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
						//,000000000000,localhost:22223,150.212.123.52:22227;1,0800278dbc61,localhost:22223,136.142.186.102:22227
					   
				    if(result2.equals("")){
				       }
				    else{
				    	
		  		        	String [] temp = null;  
		  		        	temp = result2.split(";");
		  		        	
		  		        	for(int j=0;j<temp.length;j++){
		  		        		if(temp[j]!="")
		  		        		{
		  		        			String tleaderid=temp[j];
		  		        			String[] tleaderid1 = tleaderid.split(",");
		  		        			String tleaderid2=tleaderid1[0]+","+tleaderid1[1]+","+tleaderid1[2]+","+tleaderid1[3];
									//String result3= getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findshelterbyleaderid&leaderid="+tleaderid2,InetAddress.getByName(tip),22225);
								        
								   String result4=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=notificationleadersbyshelterid&shelterid="+tid+"&str=&leaderid="+tleaderid2,InetAddress.getByName(tip),22225);						   										   											
		  		        		}
		  		        }														        																        		
				    }
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					result2="";
					mTempSheltersLayer.refresh();
				//*******************
				}
				else{
				 Graphic g;
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
				
				
				int roadsegmentid=0; 
				try {
				   String result=getCCinfo("http://"+tip+":8080/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+tempaffectedstr+"]],\"spatialReference\":{\"wkid\":4326}}" +
					   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelIntersects&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
					   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
				   if(result!=""){
					   
					   		JSONObject jsonObject;
						
							jsonObject = new JSONObject(result);
							  JSONArray jsonArray = jsonObject.getJSONArray("features");
						      for (int i = 0; i < jsonArray.length(); i++) {
						        JSONObject object = jsonArray.getJSONObject(i);
						        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
						        roadsegmentid= jsonObjectattribute.getInt("OBJECTID");	
						        
								   if (roadsegmentid>0){
									   String result0=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findsegmentsbysegmentid&segmentid="+roadsegmentid+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
									   
									   if(result0.equals("true")){
									   
										   String result3=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=removeaffectsegment&segmentid="+roadsegmentid+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);	
									   }
									   else{
										routeLayer.addGraphics(new Graphic[] { gra});
										
									   	String result1=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=addaffectedsegment&segmentid="+roadsegmentid+"&affectedcoor="+affectedstr+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);	
									   	
									    String result2=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=getleadersbysegmentid&segmentid="+roadsegmentid+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);	
										   
									    if(result2.equals("")){
									       }
									    else{
									        	
							  		        	String [] temp = null;  
							  		        	temp = result2.split(";");
							  		        	
							  		        	for(int j=0;j<temp.length;j++){
							  		        		if(temp[j]!="")
							  		        		{
							  		        			String tleaderid=temp[j];
							  		        			String[] tleaderid1 = tleaderid.split(",");
							  		        			String tleaderid2=tleaderid1[0]+","+tleaderid1[1]+","+tleaderid1[2]+","+tleaderid1[3];
														String result3;
														
													   result3 = getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findshelterbyleaderid&leaderid="+tleaderid2,InetAddress.getByName(tip),22225);
														
														//notification from CC to pi to leader
													        
														String result4=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=notificationpiandleadersbysegmentid&segmentid="+roadsegmentid+"&str="+result3+"&leaderid="+tleaderid2,InetAddress.getByName(tip),22225);
											   
															   											
							  		        		}
							  		        }														        																        		
									    }
								   }
								   }
						      }	
						} 
				   else
				   {
			      		Toast.makeText(GeometryEditorActivity.this,
			      				"No road choosen, try again.", Toast.LENGTH_SHORT).show();
				   }
				  
				 }catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
					}	     												    						    													  
			  
				}  
				 rereport=true;
			}
		});
		Button dialogButtonCancel = (Button) dialog1.findViewById(R.id.dialogButtonCancel);
		// if button is clicked, close the custom dialog
		dialogButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
											
	      		Toast.makeText(GeometryEditorActivity.this,
	      				"Cancel", Toast.LENGTH_SHORT)
	      		.show();      		
				dialog1.dismiss();
				
				rereport=true;
				mClosingTheApp = true;
				
			}
		});
	
	
		dialog1.show();	
  }
  @Override  
  public boolean onKeyDown(int keyCode, KeyEvent event)  
  {  
      if (keyCode == KeyEvent.KEYCODE_BACK )  
      {  
         

			new AlertDialog.Builder(GeometryEditorActivity.this)
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
	 private String getCCinfo(String url, InetAddress hosaddresss, int port ){
		 
		 	String result="";
		 
	        try{
	        	//InetAddress.getByName(tip)
			   sk = new Socket (hosaddresss, port);
			   sk.setSoTimeout(20000);
			   InputStreamReader isr;
			   isr = new InputStreamReader (sk.getInputStream ());
			   in = new BufferedReader (isr);
			   
			   //create output
			  			   
		       out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sk.getOutputStream())), true);		   											   
		       out.println (url);   
		       String line;
				
/*			   while(true){
 * */
		            try{
		 			   while ((line = in.readLine()) != null) {
					       result += line;
					   }
		            }
		            catch(SocketTimeoutException e){
		                System.out.println("Timeout");
		                // user connected, no data received
		            }
		            catch(EOFException e){
		                System.out.println("Disconnected");
		                // user disconnected
		            }
		            /*
		        }
			   
*/
		   
			   
			  

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
	  private static WifiManager wifiManager;
	  private static DhcpInfo dhcpInfo;
	  private static WifiInfo wifiInfo;
	 public static String getIp(Context context){
	      wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	      dhcpInfo = wifiManager.getDhcpInfo();
	      wifiInfo = wifiManager.getConnectionInfo();
	      int ip = wifiInfo.getIpAddress();
	      return FormatIP(ip);
	     }
	    
	     //get gateway ip
	 public static String getGateWay(Context context){
	      wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	         dhcpInfo = wifiManager.getDhcpInfo();
	        
	      //return "dh_ip:"+FormatIP(dhcpInfo.ipAddress)+"\n"+"dh_gateway"+FormatIP(dhcpInfo.gateway); 
	      return FormatIP(dhcpInfo.gateway); 
	     }
	 //get mac address
	 public static String getMacAddress(Context context) {
	        
	        String macAddress = "000000000000";
	        try {
	            WifiManager wifiMgr = (WifiManager) context
	                    .getSystemService(Context.WIFI_SERVICE);
	            WifiInfo info = (null == wifiMgr ? null : wifiMgr
	                    .getConnectionInfo());
	            if (null != info) {
	                if (!TextUtils.isEmpty(info.getMacAddress()))
	                    macAddress = info.getMacAddress().replace(":", "");
	                else
	                    return macAddress;
	            }
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            return macAddress;
	        }
	        return macAddress;
	    }
	     // IP transfer
	  public static String FormatIP(int IpAddress) {
	    	return Formatter.formatIpAddress(IpAddress);
	     }
	 
	 
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
						
		            	if(pushedinfo==""){
		            		
		        			Toast.makeText(GeometryEditorActivity.this, "There is no route update",
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
						
					} else if (v == StopButton) {
						start = false;
						StartButton.setEnabled(true);
						StopButton.setEnabled(false);
						Label.setText(" ");
						MCastThread.stop();
						//MCastThread.interrupt();
						
					}
					else if(v==dialogButtonBuilding){
						
						reporttype=1;
						reporticon = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_building01));
					}
					else if (v==dialogButtonBridge){
						
						reporttype=2;
						reporticon = new PictureMarkerSymbol(
								map.getContext(), getResources().getDrawable(
										R.drawable.report_bridge01));
					}
					else if (v==dialogButtonRoad){
						reporttype=3;
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
				Toast.makeText(GeometryEditorActivity.this, mException.toString(),
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
			if (mResults == null ||mResults.getRoutes().size()==0) {
				Toast.makeText(GeometryEditorActivity.this, "No road available, be careful.",
						Toast.LENGTH_LONG).show();	
				showdefaultroute(0);
				showbarriers(1);
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
			// Symbols for the route and the destination (blue line, checker flag)
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
			
			//get shelter LatLon for Routing analysis
			 Point pt=((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
						.getGeometry()).getPointCount() - 1);
			 
			 
			shelterlocaiton = (Point) GeometryEngine.project(pt,SpatialReference.create(102100),SpatialReference.create(4326));	
			  			  
			//endstoplatlon=Double.toString(((Point)endGraphic.getGeometry()).getX())+","+Double.toString(((Point)endGraphic.getGeometry()).getY());	
			
			
			
			String tpath="";
			String tstartpath="", tendpath="";
			routesegments= new ArrayList<shelter>();
			segmentsorts = new ArrayList<String>();
			NumberFormat nFormat=NumberFormat.getNumberInstance(); 
			nFormat.setMaximumFractionDigits(9);
			
			
			for(int i=0;i<((Polyline) routeGraphic.getGeometry()).getPointCount();i++ )
			{
				 Point pt1=((Polyline) routeGraphic.getGeometry()).getPoint(i);
				 Point pt2 = (Point) GeometryEngine.project(pt1,SpatialReference.create(102100),SpatialReference.create(4326));	
				 
				 tpath+="["+Double.toString(pt2.getX())+","+Double.toString(pt2.getY())+"],";
				 //manage the route conjection info
			     shelter st=new shelter();
			     st.id=i;
			     st.y=Double.parseDouble(nFormat.format(pt2.getY()));
			     st.x=Double.parseDouble(nFormat.format(pt2.getX()));
			     routesegments.add(st);
				
			}
			
			tpath=tpath.substring(0, tpath.length()-1);
			
			//get start segment id
			 int tempsegmentid;
			 Point t_start_pt_1=((Polyline) routeGraphic.getGeometry()).getPoint(0);
			 Point t_start_pt_11 = (Point) GeometryEngine.project(t_start_pt_1,SpatialReference.create(102100),SpatialReference.create(4326));	
			 
			 Point t_start_pt_2=((Polyline) routeGraphic.getGeometry()).getPoint(1);
			 Point t_start_pt_21 = (Point) GeometryEngine.project(t_start_pt_2,SpatialReference.create(102100),SpatialReference.create(4326));
			 
			 tstartpath="["+Double.toString(t_start_pt_11.getX())+","+Double.toString(t_start_pt_11.getY())+"],["+Double.toString(t_start_pt_21.getX())+","+Double.toString(t_start_pt_21.getY())+"]";
			 
			   try {
							
				   String result=getCCinfo("http://"+tip+":8080/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+tstartpath+"]],\"spatialReference\":{\"wkid\":4326}}" +
					   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelWithin&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
					   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
				   if(result!=""){	
					   	  JSONObject jsonObject = new JSONObject(result);	     
					      JSONArray jsonArray = jsonObject.getJSONArray("features");
					      for (int i = 0; i < jsonArray.length(); i++) {
					        JSONObject object = jsonArray.getJSONObject(i);
					        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
					        int id= jsonObjectattribute.getInt("OBJECTID");
					        
					        //get start point coor in each segment
					        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
					        JSONArray  path=jsonObjectgeometry.getJSONArray("paths");
					        
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			   
			 //get middle segment ids
			 

				
			
			   try {
					   													   							
				   String result=getCCinfo("http://"+tip+":8080/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+tpath+"]],\"spatialReference\":{\"wkid\":4326}}" +
					   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelContains&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
					   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
				   if(result!=""){	
					   	  JSONObject jsonObject = new JSONObject(result);	     
					      JSONArray jsonArray = jsonObject.getJSONArray("features");
					      for (int i = 0; i < jsonArray.length(); i++) {
					        JSONObject object = jsonArray.getJSONObject(i);
					        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
					        int id= jsonObjectattribute.getInt("OBJECTID");
					        
					        //get start point coor in each segment
					        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
					        JSONArray  path=jsonObjectgeometry.getJSONArray("paths");
					        
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			   
			 //get end segment id
				 Point t_start_pt_3=((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic.getGeometry()).getPointCount()-1);
				 Point t_start_pt_31 = (Point) GeometryEngine.project(t_start_pt_3,SpatialReference.create(102100),SpatialReference.create(4326));	
				 
				 Point t_start_pt_4=((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic.getGeometry()).getPointCount()-2);
				 Point t_start_pt_41 = (Point) GeometryEngine.project(t_start_pt_4,SpatialReference.create(102100),SpatialReference.create(4326));
				 
				 tendpath="["+Double.toString(t_start_pt_31.getX())+","+Double.toString(t_start_pt_31.getY())+"],["+Double.toString(t_start_pt_41.getX())+","+Double.toString(t_start_pt_41.getY())+"]";
				 
				   try {
								
					   String result=getCCinfo("http://"+tip+":8080/ga/roadsegment?where=1%3D1&geometry={\"paths\":[["+tendpath+"]],\"spatialReference\":{\"wkid\":4326}}" +
						   		"&geometryType=esriGeometryPolyline&inSR=&spatialRel=esriSpatialRelWithin&outFields=OBJECTID&returnGeometry=true&returnDistinctValues=false"
						   		+ "&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
					   if(result!=""){	
						   	  JSONObject jsonObject = new JSONObject(result);	     
						      JSONArray jsonArray = jsonObject.getJSONArray("features");
						      for (int i = 0; i < jsonArray.length(); i++) {
						        JSONObject object = jsonArray.getJSONObject(i);
						        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
						        int id= jsonObjectattribute.getInt("OBJECTID");
						        
						        //get start point coor in each segment
						        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
						        JSONArray  path=jsonObjectgeometry.getJSONArray("paths");
						        
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
					   
					   //Collections.sort(segmentsorts);
					   
				        for (int i = 0; i < segmentsorts.size() - 1; i++) {  
				            for (int j = 1; j < segmentsorts.size() - i; j++) {  
				                String temp;  
				               // if ((segmentsorts.get(j - 1).split(",")[0]).compareTo(segmentsorts.get(j).split(",")[0]) > 0) { 
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
					   result="";
					   	   
					   //String result1=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=removeleaderbyleaderid&leaderid="+shelterid+","+regionid+","+leaderid,InetAddress.getByName(tip),22225);
					   
					   String result1=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=removeleaderbyleaderidandsegmentorder&affectedsegmentorder="+affectedsegmentorder+"&leaderid="+shelterid+","+regionid+","+leaderid,InetAddress.getByName(tip),22225);
													   										
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				   
				   
			    
			//query shelter id
				   String shelterinfo="";
				   try {				   													   							
						
					   String result=getCCinfo("http://"+tip+":8080/ga/shelters?where=&geometry="+(shelterlocaiton.getX())+"%2C"+(shelterlocaiton.getY())+
					    		"&geometryType=esriGeometryPoint&inSR=&spatialRel=esriSpatialRelRelation&distance=1000&units=esriSRUnit_Foot&outFields=OBJECTID_1%2CNAME%2CADDRESS&returnGeometry=true"
					    		+ "&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnZ=false&returnM=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
					   if(result!=""){	
						   	  JSONObject jsonObject = new JSONObject(result);	     
						      JSONArray jsonArray = jsonObject.getJSONArray("features");
						      for (int i = 0; i < jsonArray.length(); i++) {
						        JSONObject object = jsonArray.getJSONObject(i);
						        JSONObject jsonObjectattribute = object.getJSONObject("attributes");						        						        
						        shelterid= jsonObjectattribute.getInt("OBJECTID_1");
						        shelterinfo="Name:"+jsonObjectattribute.getString("NAME")+"\n Address:"+jsonObjectattribute.getString("ADDRESS");

						      }							    													  
					   }
			  
									
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
				   
				 
				 if(affectedsegmentorder==0){
					 
					 pushedinfo=segmentids+";"+shelterid;
					 PropertiesUtil.setProperties("defaultroute", pushedinfo);
				 }
				 else
				 {
					Properties prop1 = PropertiesUtil.getProperties();
					pushedroutedetail = (String) prop1.get("defaultroute");
					String tempstr="";
					String rd[]=pushedroutedetail.split(";");
					String segid[]=rd[0].split(",");
					for(int i=0;i<affectedsegmentorder;i++){
						
						tempstr=tempstr+segid[i]+",";						
					}
					
					pushedinfo=tempstr+segmentids+";"+shelterid;
					//segmentids=tempstr+segmentids;
					PropertiesUtil.setProperties("defaultroute", pushedinfo);
					 
				 }
				 
				//leader register to cc for the default route
				 
				 String result1=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=addshelterbyleader&shelterid="+shelterid+"&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);   
				 //String result2=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=addleaderbysegment&segmentid="+segmentids+"&segmentstartpts="+segmentstartpts+"&leaderid="+shelterid+","+regionid+","+leaderid,InetAddress.getByName(tip),22225);
						 
				 String result2=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=addleaderbysegmentorder&segmentid="+segmentids+"&affectedsegmentorder="+affectedsegmentorder+"&segmentstartpts="+segmentstartpts+"&leaderid="+shelterid+","+regionid+","+leaderid,InetAddress.getByName(tip),22225);
				
					 
                                    
                 if( mapoperationtype==2)							
                 {
                	 mapoperationtype=0; 
                	 
                	 affectedsegmentorder=0;
                	 //multicasttofollower(pushedinfo);
	                

                 }
                 showbarriers(0);
                 
	                Map<String, Object> infomap = new HashMap<String, Object>();
	                infomap.put("attributes", shelterinfo);
	                if(shelterinfo!="")
	                	PropertiesUtil.setProperties("shelterinfo", shelterinfo);
	              
     	    endGraphic = new Graphic(
    					((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
    							.getGeometry()).getPointCount() - 1), endSymbol,infomap);
     	    
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

		  class RegisterAsyncTask extends AsyncTask<String, Integer, String> {
				 
			    String myContext;
			    TextView tvQueryResult = (TextView) findViewById(R.id.followernumlabel);    
			    
			
			    public RegisterAsyncTask(String url) {
			        myContext = url;
			        
			    }

			    @Override
			    protected String doInBackground(String... params) {
			       
			        try {
			            resultData = InitData();
			            Thread.sleep(5000);
			        } catch (Exception e) {
			        }
			        return resultData;
			    }
			    @Override
			    protected void onPreExecute() {
			    }
			   protected String InitData() {

			        
			        String str ="";
			    
			        str = NetworkService.getPostResult(myContext);//, paramList);
			        Log.i("msg", str);
			        return str;
			    }
			 
			    protected void onPostExecute(String result) {
			         
			        try {
			        JSONTokener jsonParser = new JSONTokener(result);
			        JSONObject responseobj = (JSONObject) jsonParser.nextValue();
			        if("failed".equals(responseobj.getString("errorMsg")))
			        {
			        tvQueryResult.setText(responseobj.getString("resul"));
			            }
			        else {
			            
			        }
			       } catch (Exception e) {
			            
			    }
			}  
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
  private void multicasttofollower(String pushedroutedetail){
	  
		MCast MCastRun = new MCast();
		Thread MCastThread = new Thread(MCastRun);						
		String IPaddr=null;
		String[] Segment;
		
		IPaddr=getLocalIPAddress(); 
		InputData.addr=new String(IPaddr);
		
		InputData.SegID=new String[(pushedroutedetail.split(";")[0].split(",")).length];
		InputData.SegID=pushedroutedetail.split(";")[0].split(",");
		InputData.Port=22220;
		InputData.com=1;
		InputData.RegionID=Integer.parseInt(regionid);
		InputData.ShellID= Integer.parseInt(pushedroutedetail.split(";")[1]);
		MCastRun.GetInputData(InputData);
		
		try{
	
		MCastThread.start();
		
		while(running) {
			createTimerThreadAndStart();
			}	
		
		MCastThread.stop();
		}
		catch(Exception e){}
	  
  }
  private void showbarriers(int tp){
	  
      mGraphicsLayerEditing = new GraphicsLayer();
      map.addLayer(mGraphicsLayerEditing);
      mGraphicsLayerEditing.removeAll(); 
	
      String result;
      Graphic affectedgraphic=null;
		try {                  
			result = getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=showallaffectedsegment&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);
			//1327:100.33693217061852,-0.8653466361882022,100.33693217061852,-0.8651093009464204;1155:100.34162787347653,-0.872974507181835,100.34162787347653,-0.8727954521969702  
			if(result!=""){
				   
				   String[] affectedsegments=result.split(";");
				   	for(int j=0;j<affectedsegments.length;j++){
				   		String[] coor=affectedsegments[j].split(":")[1].split(",");
				   		
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
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
  }

	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(SERVERPORT);
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
                Properties prop1 = PropertiesUtil.getProperties();
                String strleaderlocation = (String) prop1.get("leaderlocation");
                //String strleaderlocation = msg.split(";")[4];
                String coor[]=strleaderlocation.split(",");
            	Point leaderlocaiton=null;
	               
				tsegment = (Point)GeometryEngine.project(Double.valueOf(coor[0]),Double.valueOf(coor[1]),egs);								
				Point point=(Point)GeometryEngine.project(tsegment, egs, wm);																																		
				leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));	
            	
				String incidentpt="";
            	String polylinebarriers="";
				if(msg.split(";")[0].equals("12")){	//				
					mapoperationtype=2;
					mPoints.clear(); 
					mGraphicsLayerEditing.removeAll(); 
					//routeLayer.removeAll();
					//hiddenSegmentsLayer.removeAll();													
					//mResults = null;
					text.setText("Detour Notification.");
	
					
					//***************************
					try {
		                
		                affectedsegmentorder=Integer.valueOf(msg.split(";")[5]);
						
		                
		                showdefaultroute(affectedsegmentorder);
	
						
						String result0=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=findshelterbyleaderid&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);  
						
						
						for (shelter st : shelters) {
							if(st.id==Integer.parseInt(result0)){														
								double sx=st.x;
								double sy=st.y;								
								tsegment = (Point)GeometryEngine.project(sx,sy,egs);								
								//endpoint=(Point)GeometryEngine.project(tsegment, egs, wm);
								
								if(mLocation!=null)								
								{															
									 incidentpt=mLocation.getX()+","+mLocation.getY()+";"+tsegment.getX()+","+tsegment.getY();																											
								}
								else{															
									 incidentpt=leaderlocaiton.getX()+","+leaderlocaiton.getY()+";"+tsegment.getX()+","+tsegment.getY();
								}
							}																							
						}																																				   
						//polyline barriers or key value store
					
						 polylinebarriers=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=showaffectedbarriers&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225); 
						   												 
						 
						String result2=getCCinfo("http://"+tip+":8080/ga/naroute?stops="+incidentpt+
									"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
							   		"&defaultTargetFacilityCount=1&outSR=102100&impedance=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
							   		"&outputLines=esriNAOutputLineTrueShapeWithMeasure&AttributeName=Length" +
							   		"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=true&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
							   		"&returnPolylineBarriers=false&returnPolygonBarriersoutputGeometryPrecisionUnits=esriDecimalDegrees" +
							   		"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
						   												   													   																				
						if(result2!=""){												

							mrouteresult=result2;
							JsonFactory factory = new JsonFactory();												   
							JsonParser parser =factory.createJsonParser(result2);
						    parser.nextToken();
						    mResults= RouteResult.fromJson(parser);  
							mHandler.post(mUpdateResults);														
			
							}
					}
					catch (JSONException e1) {
						
						e1.printStackTrace();
					} catch (JsonParseException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
					
						e.printStackTrace();
					} catch (Exception e) {
						mException = e;
						//mHandler.post(mUpdateResults);
					}
				}
				if(msg.split(";")[0].equals("16")){	//	
										
				//when the default path unavailable for some reason, 
				//	the CC will find a new shelter for the leader
				//***************************************
																								
				Toast.makeText(GeometryEditorActivity.this,
					"Find new route", Toast.LENGTH_SHORT).show();	
																						
					try {
															
						affectedsegmentorder=0;
						
						
						//get the closest road junction
						
						String envelopcoor=(leaderlocaiton.getX()-0.0005)+","+(leaderlocaiton.getY()-0.0005)+","+(leaderlocaiton.getX()+0.0005)+","+(leaderlocaiton.getY()+0.0005);
						
						String closestjunctionresult=getCCinfo("http://"+tip+":8080/ga/closestjunction?&geometry="+envelopcoor+"&geometryType=esriGeometryEnvelope"
								+ "&inSR=4326&spatialRel=esriSpatialRelContains&outFields=OBJECTID&returnGeometry=true&returnTrueCurves=false&returnIdsOnly=false&returnCountOnly=false&returnZ=false"
								+ "&returnM=false&returnDistinctValues=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
					   double dis=999999;
					   double startx=0,starty=0;
						if(closestjunctionresult!=""){
							
						   	  JSONObject jsonObject = new JSONObject(closestjunctionresult);	     
						      JSONArray jsonArray = jsonObject.getJSONArray("features");
						      for (int i = 0; i < jsonArray.length(); i++) {
						        JSONObject object = jsonArray.getJSONObject(i);
						        JSONObject jsonObjectattribute = object.getJSONObject("attributes");
						        JSONObject jsonObjectgeometry = object.getJSONObject("geometry");
						        
						        int id= jsonObjectattribute.getInt("OBJECTID");
						        double x=jsonObjectgeometry.getDouble("x");
						        double y= jsonObjectgeometry.getDouble("y");
						        
						       if(Math.sqrt(Math.pow((x-leaderlocaiton.getX()),2)+Math.pow((y-leaderlocaiton.getY()),2))<dis)
						        {
						    	   dis=Math.sqrt(Math.pow((x-leaderlocaiton.getX()),2)+Math.pow((y-leaderlocaiton.getY()),2));
						    	   startx=x;
						    	   starty=y;
						    	   
						        }

						      }
							
						}
						
						
						incidentpt=startx+","+starty;
						
						PropertiesUtil.setProperties("leaderlocation", incidentpt); 
						
						//query envelope   																														1%3D1+and+available%3D1&
						//String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fshelters%3Fwhere%3D1%253D1%26outFields%3DNAME%26returnGeometry%3Dtrue%26f%3Dpjson%22%0D%0A%7D";
						//String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fshelters%3Fwhere=available%3D1&outFields%3DNAME%26returnGeometry%3Dtrue%26f%3Dpjson%22%0D%0A%7D";
						String shelters="%7B%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fshelters%3Fwhere%3Davailable%253D1%26returnGeometry%3Dtrue%26outFields%3DName%26f%3Dpjson%22%0D%0A%7D";
						//polyline barriers or key value store
						//String polylinebarriers="%7B+%0D%0A++%22type%22+%3A+%22features%22%2C%0D%0A++%22url%22+%3A+%22http%3A%2F%2F"+tip+"%3A8080%2Fga%2Fpolylinebarriers%3Fwhere%3D1%253D1%26outFields%3DNAME%26returnGeometry%3Dtrue%26f%3Dpjson%22%0D%0A%7D";
						polylinebarriers=getCCinfo("http://"+tip+":8080/ga/keyvaluestore?action=showaffectedbarriers&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225); 
						   												 
						
						String result=getCCinfo("http://"+tip+":8080/ga/nacloseshelter?incidents="+incidentpt+"&facilities="+shelters+
									"&polylineBarriers="+polylinebarriers+"&travelDirection=esriNATravelDirectionToFacility" +
							   		"&defaultTargetFacilityCount=1&outSR=102100&impedanceAttributeName=Length" +
							   		"&restrictUTurns=esriNFSBAllowBacktrack&useHierarchy=false&returnDirections=true&returnCFRoutes=true&returnFacilities=false&returnIncidents=false&returnBarriers=false" +
							   		"&returnPolylineBarriers=false&returnPolygonBarriers=false&directionsLanguage=en&directionsOutputType=esriDOTComplete" +
							   		"&outputLines=esriNAOutputLineTrueShapeWithMeasure&outputGeometryPrecisionUnits=esriDecimalDegrees" +
							   		"&directionsLengthUnits=esriNAUMiles&timeOfDayUsage=esriNATimeOfDayUseAsStartTime&timeOfDayIsUTC=false&returnZ=false&f=pjson&leaderid="+regionid+","+leaderid,InetAddress.getByName(tip),22225);												   															
						   
						   													   																							
						if(result!=""){
							   
							mrouteresult=result;
							JsonFactory factory = new JsonFactory();												   
							JsonParser parser =factory.createJsonParser(result);
						    parser.nextToken();
						    mResults= RouteResult.fromJson(parser); 
						    mHandler.post(mUpdateResults);											
						   }
						else{
				      		Toast.makeText(GeometryEditorActivity.this,
				      				"No result, try again.", Toast.LENGTH_SHORT).show();
							
						}
					} 
				catch (JSONException e1) {
						
						e1.printStackTrace();
					} catch (JsonParseException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
					
						e.printStackTrace();
					} catch (Exception e) {
						mException = e;
						mHandler.post(mUpdateResults);
					}
									
			mapoperationtype=0;	
//*****************************
		}									

				if(msg.split(";")[0].equals("15")){					
					mapoperationtype=0;
					text.setText("Warning Notification.");
					final DialogInterface dialog=new AlertDialog.Builder(GeometryEditorActivity.this)
					.setIcon(R.drawable.warning)
					.setTitle(" ")
			        /*.setMessage("update map")
			        		       
			        .setPositiveButton("close", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
	
						}

					})
					*/
					.show();
					
					TimerTask task = new TimerTask(){    
					public void run(){    
						//execute the task  
						//DialogInterface dialog;
						dialog.dismiss();
						
						//add default route when get notification						
						showdefaultroute(0);

		                //Multicast notification to followers
						Properties prop1 = PropertiesUtil.getProperties();
						pushedroutedetail = (String) prop1.get("defaultroute");
						//multicasttofollower(pushedroutedetail);						
						  }    
						};    
					Timer timer = new Timer();  
					timer.schedule(task, 5000); 
				}
				else if(msg.split(";")[0].equals("3")){	
					mapoperationtype=0;
				}
		}
			}
			//text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
			//text = (TextView) findViewById(R.id.directionsLabel);
			//text.setText("info: "+ msg + "\n");
			//text.setText("Get Notification.");
		
	}  
	
	private void showdefaultroute( int tsegmentorder){
		
		Properties prop1 = PropertiesUtil.getProperties();
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
								R.drawable.route_strat_24));
			    multipath = new Polyline();
			    double mindistance=999999;
				//!!!!!!!!!!!!!!!!!!
				//prepare segment and shelter info
			    int order=0;
				ArrayList<shelter> shelters=readsheltercoor(); 
				ArrayList<roadsegment> roadsegments=readsegmentcoor(); 
				for(String sid:segid){
					for (roadsegment rs : roadsegments) {
						startpt=0;
						if(rs.id==Integer.parseInt(sid) && order < tsegmentorder){			      
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
		 for(int i=0;i<3;i++) {
		 try {
			 Random rm=new Random();
			 int timer=rm.nextInt(2)*1000+1000;
		
			 Thread.sleep(timer);
		 } catch (InterruptedException e) {
		 running = false;
		 }
		 if(i==3) running = false;
		 }
		 }
		 });
		 timerThread.start();
		 }
		     }
	//*************************
    /**
     * UDP  Multicast send
     * @author fua3
     *
     */
class MCast implements Runnable{
		
		private String addr;
		private int Port;
		private int com;
		private String[] SegID;
		private int ShellID;
		private int RegionID;
		public void GetInputData(DataStruct input){
			this.addr=input.addr;
			this.Port=input.Port;
			this.com=input.com;
			this.SegID=input.SegID;
			this.ShellID=input.ShellID;		
			this.RegionID=input.RegionID;
		}
		
		private String ConstructData(){
			String Frame = null;
			String Segments=null;
			int i;
			
			if(com==2){
				addr="#";
				Frame=new String (Integer.toString(com)+";"+Integer.toString(RegionID)+";#;#;#;###");
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
				Frame=new String (Integer.toString(com)+";"+Integer.toString(RegionID)+";"+addr+":"+Integer.toString(Port)+
						";"+Segments+";"+Integer.toString(ShellID)+";###");
				return Frame;
			}
			if(com==0){
				Frame=new String (Integer.toString(com)+";"+Integer.toString(RegionID)+";"+addr+":"+Integer.toString(Port)+
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
			//Multicast
			SocketAddress dest= new InetSocketAddress("224.0.0.3",22220);
			//Unicast with MPi address and port
			//SocketAddress dest = new InetSocketAddress("130.49.219.87", 7385);
			try {
				//Multicast
				Sndmcast=new MulticastSocket(22220);				
				OutIFaddr=InetAddress.getByName(address);
				Sndmcast.setInterface(OutIFaddr);
				//Sndmcast.joinGroup(OutIFaddr);
				//Unicast
				//Snducast= new DatagramSocket(7385);
				
				
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
				//Multicast
				Sndmcast.send(dataPacket);
				//Unicast
				//Snducast.send(dataPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Label.post(new Runnable() {
			
			final String Disp=ConstructData();
			@Override
			public void run() {
				
				//Label.append("Sent : " + Disp + "\n" );
				Label.setText("Notification Multicast.");
			
			}
		});
		//while(true);
		//Multicast
		Sndmcast.close();
		//Unicast
		//Snducast.close();
		
		}
	}
	/**
	 * UDP Multicast receive
	 * @author fua3
	 *
	 */
	class ListMcast implements Runnable{
		String address=null;
		DatagramPacket IncdataPacket =null;
		private DataStruct ReceivedData=new DataStruct();
		
		private void ParseData(String Data)
		{
			
			String[] Field;
			//String[] IPPort = new String[2];
			String[] SegID;
			Field=new String[6];
			Field=Data.split(";");
			ReceivedData.RegionID=Integer.parseInt(Field[1]);
			ReceivedData.com=Integer.parseInt(Field[0]);
			ReceivedData.addr=(Field[2]).split(":")[0];
			ReceivedData.Port=Integer.parseInt((Field[2]).split(":")[1]);
			SegID=new String[Field[3].length()];
			SegID=Field[3].split(",");
			ReceivedData.SegID=SegID;
			ReceivedData.ShellID=Integer.parseInt(Field[4]);
			
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
				Rcvcast.joinGroup(InetAddress.getByName("224.0.0.3"));
				IncdataPacket = new DatagramPacket(incdata,incdata.length);
				while(true){
					Rcvcast.receive(IncdataPacket);
					
					if (null != IncdataPacket.getAddress()&& 
							!(IncdataPacket.getAddress().toString().equals("/"+ address))){
						
						final String codeString = new String(incdata, 0, IncdataPacket.getLength());
						final String ip=IncdataPacket.getAddress().toString();
						
						ParseData(codeString);
						//with regionid		
						if(ReceivedData.RegionID==Integer.parseInt(regionid))
						{
							if(codeString.split(";")[0].equals("1")){
								mapoperationtype=0;
								pushedroutedetail=codeString.split(";")[3]+";"+codeString.split(";")[4];
							}
							if(codeString.split(";")[1].equals(regionid)){}
							else
							{
							Label.post(new Runnable() {
														
							@Override
							public void run() {
								
								Label.setText("Received from : "+ ip + "\n" );
								//Label.append("Content : "+ codeString + "\n" );
							
							}
						});	
					   }
					}
					}
					
					Rcvcast.close();
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
		public int RegionID;
		
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



