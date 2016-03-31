/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the use restrictions
 * http://help.arcgis.com/en/sdk/10.0/usageRestrictions.htm.
 */

package com.pitt.cdm.arcgis.android.networkanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.pitt.cdm.arcgis.android.attributeeditor.AttributeEditorActivity;
import com.pitt.cdm.arcgis.android.maps.R;

public class RoutingtoshelterupdateActivity extends Activity {
	MapView map = null;
	ArcGISTiledMapServiceLayer tileLayer;
	ArcGISFeatureLayer mTempSheltersLayer, mTemproadsegmentLayer;
	GraphicsLayer routeLayer, hiddenSegmentsLayer;
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
	// Variable to hold server exception to show to user
	Exception mException = null;
	// Handler for processing the results
	final Handler mHandler = new Handler();
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	// Progress dialog to show when route is being calculated
	ProgressDialog dialog;
	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);
	// Index of the currently selected route segment (-1 = no selection)
	int selectedSegmentID = -1;
	String startpt,endpt,followers,leaderid;
	ArcGISFeatureLayer mTempLeaderrouteLayer,mTemppolylinebarrierLayer;
    String routedetail;
    Graphic startGraphic,endGraphic,newGraphic;
    Map<String, Object> tempattributes = new HashMap<String, Object>();
    MultiPath multipath;
    int tempoldrouteupdate=0;
    protected static final String TAG = "EvcuationRoute";
    private static final SimpleLineSymbol sDefaultPolylineSymbol = new SimpleLineSymbol(0x99ED951A, 3);
    private static final SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 5, STYLE.CIRCLE);
    private final List<Graphic> mPolylineBarriers = new ArrayList<Graphic>();
    Graphic[] tgraphic;
    String barrierUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/1/query?where=1%3D1&f=pjson";

    Polygon pg;
    Point shelterlocaiton;
    Object shelterid;
   
    Object templeaderid;
    Object sheltercapacity;
    Graphic routegrac;
    FeatureSet queryrt; 
    private String resultData;
    String segmentids="", pushinfofromleader2followers,endstoplatlon;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_main);

		// Retrieve the map and initial extent from XML layout
		map = (MapView) findViewById(R.id.map);
		// Add tiled layer to MapView
		tileLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
		map.addLayer(tileLayer);
		
		//UserCredentials creds = new UserCredentials();
		// get user and password from string resource
		// create the user account to access service
		//creds.setUserAccount("admin", "admin");
		
		//shelter
		mTempSheltersLayer = new ArcGISFeatureLayer("http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACFacilities/FeatureServer/0",
				ArcGISFeatureLayer.MODE.ONDEMAND);
		// add secure layer to the map
		map.addLayer(mTempSheltersLayer);
		
		//road segment layer
		mTemproadsegmentLayer= new ArcGISFeatureLayer("http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACFacilities/FeatureServer/5",
				ArcGISFeatureLayer.MODE.ONDEMAND);
		// add secure layer to the map
		map.addLayer(mTemproadsegmentLayer);
		
		//leader evacuation route
		 mTempLeaderrouteLayer = new ArcGISFeatureLayer(
		    		"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/3",
		        //"http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/2",
		        ArcGISFeatureLayer.MODE.ONDEMAND);
		 
		 map.addLayer(mTempLeaderrouteLayer);
		 mTempLeaderrouteLayer.setVisible(false);
		 
		 //polyline barriers
		 mTemppolylinebarrierLayer = new ArcGISFeatureLayer(
		    		"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/1",
		        //"http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/2",
		    		ArcGISFeatureLayer.MODE.ONDEMAND);
		 
		 map.addLayer(mTemppolylinebarrierLayer);
		 mTemppolylinebarrierLayer.setVisible(false);
		// Add the route graphic layer (shows the full route)
		routeLayer = new GraphicsLayer();
		map.addLayer(routeLayer);

		// Initialize the RouteTask
		try {
			mRouteTask = RouteTask
					.createOnlineRouteTask(
							"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACNetworkAnalysis/NAServer/Route",
							null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Add the hidden segments layer (for highlighting route segments)
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
        //startpt=intent.getStringExtra("startpt");  
        endpt=intent.getStringExtra("endpt");
        followers=intent.getStringExtra("followers");
        leaderid=intent.getStringExtra("leaderid");
		//directionsLabel.setText(getString(R.string.route_label));
        //directionsLabel.setText(startpt+"::"+endpt);

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

		/**
		 * On long pressing the map view, route from our current location to the
		 * pressed location.
		 * 
		 */
		map.setOnLongPressListener(new OnLongPressListener() {

			private static final long serialVersionUID = 1L;

			public boolean onLongPress(final float x, final float y) {

				// Clear the graphics and empty the directions list
				routeLayer.removeAll();
				hiddenSegmentsLayer.removeAll();
				curDirections = new ArrayList<String>();
				mResults = null;

				// retrieve the user clicked location
				//final Point loc = map.toMapPoint(x, y);

				// Show that the route is calculating
				dialog = ProgressDialog.show(RoutingtoshelterupdateActivity.this, "",
						"Calculating route...", true);
				// Spawn the request off in a new thread to keep UI responsive
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							// Start building up routing parameters
							RouteParameters rp = mRouteTask
									.retrieveDefaultRouteTaskParameters();
							NAFeaturesAsFeature rfaf = new NAFeaturesAsFeature();
							
							// create feature based on query url limiting stations by city
							NAFeaturesAsFeature barriernafaf = new NAFeaturesAsFeature();
							barriernafaf.setSpatialReference(map.getSpatialReference());
							barriernafaf.setURL(barrierUrl);
							
							
							Intent intent=getIntent();  
					        startpt=intent.getStringExtra("startpt");  
					       // StopGraphic point1 = new StopGraphic(mLocation);
					        endpt=intent.getStringExtra("endpt");
							String st[]=startpt.split(",");
							String et[]=endpt.split(",");
							Point ss = (Point) GeometryEngine.project(Double.parseDouble(st[0]), Double.parseDouble(st[1]), egs);
							Point ee = (Point) GeometryEngine.project(Double.parseDouble(et[0]), Double.parseDouble(et[1]), egs);
							
							shelterlocaiton = (Point) GeometryEngine.project(ee,egs,wm);
							
							StopGraphic point1 = new StopGraphic(ss);
							
							StopGraphic point2 = new StopGraphic(ee);
							rfaf.setFeatures(new Graphic[] { point1, point2 });
							rfaf.setCompressedRequest(true);
							rp.setPolylineBarriers(barriernafaf);
							rp.setStops(rfaf);
							// Set the routing service output SR to our map
							// service's SR
							rp.setOutSpatialReference(wm);

							// Solve the route and use the results to update UI
							// when received
							mResults = mRouteTask.solve(rp);
							mHandler.post(mUpdateResults);
							
					   		//##########################
					        //1.if there have leader route in the key value store in leaderroutelayer, <update> the Graphic
					        //2. if the request is new, <insert> the Graphic to leaderroutelayer
					        //Queryfeatures
					            Query routequery = new Query();
					            routequery.setOutFields(new String[] { "*" });
					            routequery.setSpatialRelationship(SpatialRelationship.INTERSECTS);
					            
					            //search from the whole search region
					            //xmin=-80.365886464696
					            //ymin=40.1902126171786
					            //xmax=-79.6830892774438
					            //ymax=40.6852549310297
					            Point pt1 = (Point) GeometryEngine.project(-80.365886464696, 40.1902126171786, egs);
					            Point pt2 = (Point) GeometryEngine.project(-79.6830892774438, 40.1902126171786, egs);
					            Point pt3 = (Point) GeometryEngine.project(-79.6830892774438, 40.6852549310297, egs);
					            Point pt4 = (Point) GeometryEngine.project(-80.365886464696, 40.6852549310297, egs);
					            
					            Point pt11 = (Point) GeometryEngine.project(pt1,egs,wm);
					            Point pt21 = (Point) GeometryEngine.project(pt2,egs,wm);
					            Point pt31 = (Point) GeometryEngine.project(pt3,egs,wm);
					            Point pt41 = (Point) GeometryEngine.project(pt4,egs,wm);
					         				            
					            multipath = new Polygon();
					            multipath.startPath(pt11);					          
					            multipath.lineTo(pt21);
					            multipath.lineTo(pt31);
					            multipath.lineTo(pt41);
					            //multipath.lineTo(pt11);
					            routequery.setGeometry(multipath);
					            
					           // routequery.setGeometry(map.getExtent());
					            routequery.setInSpatialReference(map.getSpatialReference());
					            
					            mTempLeaderrouteLayer.selectFeatures(routequery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, new CallbackListener<FeatureSet>() {

					                // handle any errors
					                public void onError(Throwable e) {

					                  Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());

					                }

					                public void onCallback(FeatureSet queryResults) {
					                	//have old route of the leader, update
					                  if (queryResults.getGraphics().length > 0) {
					                	  
					                	  for(int i=0; i<queryResults.getGraphics().length;i++){		                	 		               	    
					                	   String templeaderid= queryResults.getGraphics()[i].getAttributeValue(mTempLeaderrouteLayer.getFields()[2].getName()).toString();
					                	   //1. update
					        		     
					        		        	if((templeaderid.equals(leaderid))){
					        		        	tempoldrouteupdate=1;
					        		        	tempattributes.put(mTempLeaderrouteLayer.getObjectIdField(), queryResults.getGraphics()[i].getAttributeValue(mTempLeaderrouteLayer.getObjectIdField()));
					        		        	
					        		        }	        		        
					                	  }

					                      }					                 
					                  }
					                //}
					              });
					        	
					        //###########################
							
						} catch (Exception e) {
							mException = e;
							mHandler.post(mUpdateResults);
						}
					}
				};
				// Start the operation
				t.start();
				return true;
			}
		});
	}

	/**
	 * Updates the UI after a successful rest response has been received.
	 */
	void updateUI() {
		dialog.dismiss();
		if (mResults == null) {
			Toast.makeText(RoutingtoshelterupdateActivity.this, mException.toString(),
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
		// Symbols for the route and the destination (blue line, checker flag)
		SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
		/*PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(
				map.getContext(), getResources().getDrawable(
						R.drawable.flag_finish));
		*/
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
		endstoplatlon=Double.toString(((Point)endGraphic.getGeometry()).getX())+","+Double.toString(((Point)endGraphic.getGeometry()).getY());	
		routeLayer.addGraphics(new Graphic[] { routeGraphic, startGraphic, endGraphic });
		
		


		 //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		 //input route_segment relationship into key value store
		    //**************************************
		    // build a query to select the clicked feature
        
        	MultiPath routepath;
        	routepath = new Polyline();
        	routepath=(Polyline) routegrac.getGeometry();
        	
        	//Polygon rpg= GeometryEngine.buffer(routepath, map.getSpatialReference(), 500, null);
		 	Log.d(TAG, "begin Select segments");
		 	
		    Query segmentquery = new Query();
		    segmentquery.setOutFields(new String[] { "*" });
		    segmentquery.setSpatialRelationship(SpatialRelationship.CONTAINS);
		    segmentquery.setGeometry(routepath);
		    segmentquery.setInSpatialReference(map.getSpatialReference());
		    // call the select features method and implement the callbacklistener
		    mTemproadsegmentLayer.selectFeatures(segmentquery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, new CallbackListener<FeatureSet>() {

		      // handle any errors
		      public void onError(Throwable e) {

		        Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());

		      }

		      public void onCallback(FeatureSet queryResults) {
		    	  
		    	  Log.d(TAG, "affected road segment:" + queryResults.getGraphics().length);

		        if (queryResults.getGraphics().length > 0) {	
		        	Log.d(TAG, "affected road segment:" + queryResults.getGraphics().length);
		        	//remove segment with leaderid
		        	String removeurl="http://10.0.2.2:8081/segmentleader.do?action=removeleadersbyleaderid&leaderid="+leaderid;
			     	RegisterAsyncTask Registerremove = new RegisterAsyncTask(removeurl);
			     	Registerremove.execute("");	
			     	 
		    	 for(int i=0;i<queryResults.getGraphics().length;i++){
		    		 
		    		String segmentid=queryResults.getGraphics()[i].getAttributeValue(mTemproadsegmentLayer.getObjectIdField()).toString();
		    		segmentids=segmentids+","+segmentid;
		    		String purl="http://10.0.2.2:8081/segmentleader.do?action=saveleadersegments&segmentid="+segmentid+"&leaderid="+leaderid;
			     	//String purl="http://10.0.2.2:8081/leadershelterroute.do?action=saveleadershelterroute&leaderid=2&leaderx=-79.93068557932682&leadery=40.45178437435361&shelterid=91&shelterx=-79.93429488933373&sheltery=40.453873690885544&follownum=50&routedetail=nnnnn";
			     	RegisterAsyncTask Register = new RegisterAsyncTask(purl);
			        Register.execute("");			        
			        
		    	     }
			       					        	            	
		            }
		        
		        //???????????????????????
		        //update leader-shelter-route <key, value> store
		        int shid=Integer.parseInt(shelterid.toString());
		        Date updatetime=new Date(System.currentTimeMillis());
		        int followernum=Integer.parseInt(followers);
		   		tempattributes.put("LeaderID", leaderid);
		   		tempattributes.put("Shelterid" , shid);
		   		tempattributes.put("updatetime",  updatetime);        
		   		tempattributes.put("available" , 1);
		   		if(segmentids!="")
		   			segmentids=segmentids.substring(1, segmentids.length()-1);
		   		pushinfofromleader2followers=segmentids+":"+Integer.toString(shid);
		   		//tempattributes.put("routdetail" , routedetail);
		   		tempattributes.put("routdetail" , pushinfofromleader2followers);
		   		tempattributes.put("followers" ,  followernum);
		   	
		   		
		   		String leaderx=startpt.split(",")[0];
		   		String leadery=startpt.split(",")[1];
		   		String shelterx=endpt.split(",")[0];
		   		String sheltery=endpt.split(",")[1];
		   		
		   		//remove then add new route to leader-shelter-route key value store
		   		String removerouteurl="http://10.0.2.2:8081/leadershelterroute.do?action=removeroutebyleaderid"+
		   				"&leaderid="+leaderid;
	        	RegisterAsyncTask Registerremoveroute = new RegisterAsyncTask(removerouteurl);
	        	Registerremoveroute.execute("");
	        	
		   		String saverouteurl="http://10.0.2.2:8081/leadershelterroute.do?action=saveleadershelterroute"+
		   				"&leaderid="+leaderid+"&leaderx="+leaderx+"&leadery="+leadery+"&shelterid="+shid+"&shelterx="
		   				+shelterx+"&sheltery="+sheltery+"&follownum="+followernum+"&routedetail="+pushinfofromleader2followers;
		   		
		   		//String purl="http://10.0.2.2:8081/leadershelterroute.do?action=saveleadershelterroute&leaderid=2&leaderx=-79.93068557932682&leadery=40.45178437435361&shelterid=91&shelterx=-79.93429488933373&sheltery=40.453873690885544&follownum=50&routedetail=nnnnn";
	        	RegisterAsyncTask Registersaveroute = new RegisterAsyncTask(saverouteurl);
	        	Registersaveroute.execute("");
		        //???????????????????????
		       
		        }
		      //}
		    });
		    //********************************
		 //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		    
		//query shelter id
	    pg= GeometryEngine.buffer(shelterlocaiton, map.getSpatialReference(), 50, null); 
							
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
            	
              Log.d(
                  TAG,
                  "Feature found shelterid="
                      + queryResults.getGraphics()[0].getAttributeValue(mTempSheltersLayer.getObjectIdField()));

              shelterid=queryResults.getGraphics()[0].getAttributeValue(mTempSheltersLayer.getObjectIdField());

              sheltercapacity=queryResults.getGraphics()[0].getAttributeValue(mTempSheltersLayer.getFields()[9].getName());
                              
              //RoutingtoshelterActivity.this.runOnUiThread(new Runnable() {

               // public void run() {
                	double newcap=Double.parseDouble(sheltercapacity.toString())- Double.parseDouble(followers);
                	Map<String, Object> tattributes = new HashMap<String, Object>();
                	tattributes.put(mTempSheltersLayer.getObjectIdField(), queryResults.getGraphics()[0].getAttributeValue(mTempSheltersLayer.getObjectIdField()));
    		   		tattributes.put("CAPACITY", newcap );
    		   		
    		   		//attributes.put("updatetime",  new Date(System.currentTimeMillis()));        
    		   		Log.d(
    		                  TAG,
    		                  "capacity:"
    		                      + Double.parseDouble(sheltercapacity.toString())+"--followers:"+Double.parseDouble(followers.toString()));

    		   	Graphic graphic = new Graphic(shelterlocaiton,null,tattributes); 
    		   	
    		   	//update shelter capacity
    		   	mTempSheltersLayer.applyEdits( null, null, new Graphic[] { graphic },new CallbackListener<FeatureEditResult[][]>() {

  			       @Override
  			       public void onError(Throwable e){ 
  			        // Log.d(TAG, e.getMessage());
  			         //completeSaveAction(null);
  			       }

  			       @Override
  			       public void onCallback(FeatureEditResult[][] results) {
  			        // completeSaveAction(results);
  			       }

  			     });
                	
    		  //*************
    		   	//input evacuation route in the key value store- one path feature layer
				 //MultiPath multipath;
				 multipath = new Polyline();
				 multipath=(Polyline) routegrac.getGeometry();	
				

			       // Map<String, Object> attributes = new HashMap<String, Object>();
			        
			      
			        int shid=Integer.parseInt(shelterid.toString());
			        Date updatetime=new Date(System.currentTimeMillis());
			        int followernum=Integer.parseInt(followers);
			   		tempattributes.put("LeaderID", leaderid);
			   		tempattributes.put("Shelterid" , shid);
			   		tempattributes.put("updatetime",  updatetime);        
			   		tempattributes.put("available" , 1);
			   		pushinfofromleader2followers=segmentids+";"+Integer.toString(shid);
			   		//tempattributes.put("routdetail" , routedetail);
			   		tempattributes.put("routdetail" , pushinfofromleader2followers);
			   		tempattributes.put("followers" ,  followernum);
			   		
			   		newGraphic = new Graphic(multipath, null, tempattributes); 
			   		/*
			   		String leaderx=startpt.split(",")[0];
			   		String leadery=startpt.split(",")[1];
			   		String shelterx=endpt.split(",")[0];
			   		String sheltery=endpt.split(",")[1];
			   		
			   		//remove then add new route to leader-shelter-route key value store
			   		String removerouteurl="http://10.0.2.2:8081/leadershelterroute.do?action=removeroutebyleaderid"+
			   				"&leaderid="+leaderid;
		        	RegisterAsyncTask Registerremoveroute = new RegisterAsyncTask(removerouteurl);
		        	Registerremoveroute.execute("");
		        	
			   		String saverouteurl="http://10.0.2.2:8081/leadershelterroute.do?action=saveleadershelterroute"+
			   				"&leaderid="+leaderid+"&leaderx="+leaderx+"&leadery="+leadery+"&shelterid="+shid+"&shelterx="
			   				+shelterx+"&sheltery="+sheltery+"&follownum="+followernum+"&routedetail="+pushinfofromleader2followers;
			   		
			   		//String purl="http://10.0.2.2:8081/leadershelterroute.do?action=saveleadershelterroute&leaderid=2&leaderx=-79.93068557932682&leadery=40.45178437435361&shelterid=91&shelterx=-79.93429488933373&sheltery=40.453873690885544&follownum=50&routedetail=nnnnn";
		        	RegisterAsyncTask Registersaveroute = new RegisterAsyncTask(saverouteurl);
		        	Registersaveroute.execute("");
		   		*/
		   		//##########################
		        //1.if there have leader route in the key value store in leaderroutelayer, <update> the Graphic
		        //2. if the request is new, <insert> the Graphic to leaderroutelayer
		        //Queryfeatures

			   		
			   					//1. new
			   					if(tempoldrouteupdate==0){
			   						newGraphic = new Graphic(multipath, null, tempattributes);  
			   						mTempLeaderrouteLayer.applyEdits(new Graphic[] {newGraphic},null, null, new CallbackListener<FeatureEditResult[][]>() {
			   							@Override
			   							public void onError(Throwable e){ 
			   							}
			   							@Override
			   							public void onCallback(FeatureEditResult[][] results) {
      					    	   
			   							}
			   						});
              		  
              	  				}
		                	  
		                	   //2. update
		        		        if(tempoldrouteupdate==1){	
		        		        	
		        		        	//tempattributes.put(mTempLeaderrouteLayer.getObjectIdField(), queryResults.getGraphics()[i].getAttributeValue(mTempLeaderrouteLayer.getObjectIdField()));
		        		        	newGraphic = new Graphic(multipath, null, tempattributes);  
		        		        	mTempLeaderrouteLayer.applyEdits(null, null,new Graphic[] {newGraphic}, new CallbackListener<FeatureEditResult[][]>() {
		        					       @Override
		        					       public void onError(Throwable e){ 
		        					       }
		        					       @Override
		        					       public void onCallback(FeatureEditResult[][] results) {
		        					    	  // tempoldrouteupdate=0;
		        					       }
		        					     });
		        		        	
		        		        	        		        
		                	  }

		        	
		        //###########################
		   		
		   		 
				
			   /*  mTempLeaderrouteLayer.applyEdits(new Graphic[] { newGraphic }, null, null, new CallbackListener<FeatureEditResult[][]>() {
			       @Override
			       public void onError(Throwable e){ 
			       }
			       @Override
			       public void onCallback(FeatureEditResult[][] results) {
			       }
			     });
			     */
            }
            else{
                Log.d(
                        TAG,
                        "NO Feature found");
            	
            }
          }
        });  
			        
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
}