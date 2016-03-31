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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.os.Environment;
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
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;

import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;

import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.pitt.cdm.arcgis.android.maps.R;

public class CopyOfRoutingdefaultActivity extends Activity {
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
    Graphic[] tgraphic;
    String barrierUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACBarriers/FeatureServer/1/query?where=1%3D1&f=pjson";

    Polygon pg;
    Point shelterlocaiton;
    Object shelterid;
   
    Object templeaderid;
    Object sheltercapacity;
    Graphic routegrac;
    FeatureSet queryrt; 
    ArcGISLocalTiledLayer localTiledLayer;
    private static File demoDataFile;
    private static String offlineDataSDCardDirName;
    private static String filename;
    public class shelter {  
        int id;
        double x;
		double y;        
        }
    public class roadsegment {  
        int id;  
        JSONArray paths;
        }
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_main);
		
		
		//####################
		//@ create the path to local tpk
	    demoDataFile = Environment.getExternalStorageDirectory();
	    offlineDataSDCardDirName = this.getResources().getString(R.string.offline_dir);
	    filename = this.getResources().getString(R.string.local_tpk);
	    //@ create the url
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
		//@ Add WEB tiled layer to MapView
	    	tileLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
	    	map.addLayer(tileLayer);
	    }
	    
	    //@ enable panning over date line
	    map.enableWrapAround(true);	 
	  //#########################
	    
	    
		//road segment layer
		/*mTemproadsegmentLayer= new ArcGISFeatureLayer("http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/ACFacilities/FeatureServer/5",
				ArcGISFeatureLayer.MODE.ONDEMAND);
		map.addLayer(mTemproadsegmentLayer);
		*/		 

		routeLayer = new GraphicsLayer();
		map.addLayer(routeLayer);

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
       // startpt=intent.getStringExtra("startpt");  
       // endpt=intent.getStringExtra("endpt");
       // followers=intent.getStringExtra("followers");
        leaderid=intent.getStringExtra("username");
        routedetail=intent.getStringExtra("routedetail");
		//directionsLabel.setText(getString(R.string.route_label));
       // directionsLabel.setText(startpt+"::"+endpt);
        

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

				//retrieve the user clicked location
				//final Point loc = map.toMapPoint(x, y);

				Thread t = new Thread() {
					@Override
					public void run() {
					
					    //^^^^^^^^^^^^^^^^^^^^^^^^ 
						if(routedetail!=""&& routedetail!=null)
						{
							updateroute();
																							
						}
						else{
							try {											
								JsonFactory factory = new JsonFactory();
							    JsonParser parser = factory.createJsonParser(getResources().getAssets().open("defaultroute.json"));
							    parser.nextToken();
							    mResults= RouteResult.fromJson(parser);  								    								
							    mHandler.post(mUpdateResults);
								

								
							} catch (JSONException e1) {
								
								e1.printStackTrace();
							} catch (JsonParseException e) {
								
								e.printStackTrace();
							} catch (IOException e) {
							
								e.printStackTrace();
							} catch (Exception e) {
							
								e.printStackTrace();
								mException = e;
								mHandler.post(mUpdateResults);
							}
							
					        //^^^^^^^^^^^^^^^^^^^^^^^^										
		
					}
					}
				};
				
				t.start();
				return true;
			}
		});
	}
	
void updateroute(){
	String rd[]=routedetail.split(";");
	String segid[]=rd[0].split(",");
	int startpt=0;				         				            
    multipath = new Polygon();
	//!!!!!!!!!!!!!!!!!!
	//prepare segment and shelter info
	ArrayList<shelter> shelters=readsheltercoor(); 
	ArrayList<roadsegment> roadsegments=readsegmentcoor(); 
	for(String sid:segid){
		for (roadsegment rs : roadsegments) {
			if(rs.id==Integer.parseInt(sid)){			      
			      for (int i = 0; i < rs.paths.length(); i++) {
			    	  try {			    		 
						JSONArray jsonArray1 = rs.paths.getJSONArray(i);
						for (int j = 0; j < jsonArray1.length(); j++) {
							JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
							for (int k = 0; k < jsonArray2.length(); k++) {
								JSONArray jsonArray3 = jsonArray2.getJSONArray(k);
								
								double sx=jsonArray3.getDouble(0);
								double sy=jsonArray3.getDouble(1);
								
					            Point pt = (Point) GeometryEngine.project(sx, sy, wm);							            
					           
								if(startpt==0){				
									multipath.startPath(pt);
									startpt=1;
								}
								else
									multipath.lineTo(pt);
								
							}
							
						}
						}
					 catch (JSONException e) {
						
						e.printStackTrace();
					}	
			}


			}									
				
										
		}
	}
	
	for (shelter st : shelters) {
		if(st.id==Integer.parseInt(rd[1]))
			directionsLabel.setText(Double.toString(st.x));
										
	}
	
}
	/**
	 * Updates the UI after a successful rest response has been received.
	 */
	void updateUI() {
		//dialog.dismiss();
		//curDirections = new ArrayList<String>();
		if (mResults == null) {
			Toast.makeText(CopyOfRoutingdefaultActivity.this, mException.toString(),
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
	
	public  ArrayList<shelter> readsheltercoor() {
	  try{
		  ArrayList<shelter> tshelter = new ArrayList<shelter>();
	      InputStreamReader inputStreamReader = new InputStreamReader(getResources().getAssets().open("AC_shelters_webmercator.json"), "UTF-8");
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
	public  ArrayList<roadsegment> readsegmentcoor() {
		  try{
			  ArrayList<roadsegment> tshelter = new ArrayList<roadsegment>();
		      InputStreamReader inputStreamReader = new InputStreamReader(getResources().getAssets().open("AC_roadsegments_webmercator.json"), "UTF-8");
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
		        /*
		          "features": [
  {
   "attributes": {
    "OBJECTID": 48
   },
   "geometry": {
    "paths": [
     [
      [
       -8890296.7289794218,
       4929135.752521175
      ],
      [
       -8890287.3103486262,
       4929140.7218013387
      ],
      [
       -8890285.9456829857,
       4929141.5269074449
      ],
      [
       -8890260.6194969527,
       4929151.9211915191
      ],
      [
       -8890240.2906655017,
       4929159.1890946385
      ],
      [
       -8890222.5120522715,
       4929166.1814674744
      ]
     ]
    ]
   }
  },
		         * */
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
}