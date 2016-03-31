/* Copyright 2013 ESRI
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

//import java.util.HashMap;
//import java.util.Map;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.ags.ArcGISFeatureLayer;
//import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
//import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
//import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
//import com.esri.arcgis.android.samples.attributeeditor.AttributeItem;
//import com.esri.arcgis.android.samples.attributeeditor.FeatureLayerUtils;
//import com.esri.arcgis.android.samples.attributeeditor.R;
//import com.esri.arcgis.android.samples.attributeeditor.FeatureLayerUtils.FieldType;
//import com.esri.arcgis.android.samples.routing.R;
//import com.esri.arcgis.android.samples.routing.R;
//import com.esri.core.geometry.GeometryEngine;
//import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
//import com.esri.core.geometry.Unit;
//import com.esri.core.geometry.Unit.UnitType;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.UserCredentials;
//import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
//import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
//import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.na.ClosestFacilityParameters;
import com.esri.core.tasks.na.ClosestFacilityResult;
import com.esri.core.tasks.na.ClosestFacilityTask;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.NATravelDirection;
import com.esri.core.tasks.na.Route;
import com.pitt.cdm.arcgis.android.maps.R;

public class ClosestShelterActivity extends Activity {

	// mapview definition
	  MapView mMapView = null;
	
	  // The basemap switching menu items.
	  MenuItem mStreetsMenuItem = null;
	  MenuItem mTopoMenuItem = null;
	  MenuItem mGrayMenuItem = null;
	  MenuItem mOceansMenuItem = null;

	  // Create MapOptions for each type of basemap.
	  final MapOptions mTopoBasemap = new MapOptions(MapType.TOPO);
	  final MapOptions mStreetsBasemap = new MapOptions(MapType.STREETS);
	  final MapOptions mGrayBasemap = new MapOptions(MapType.GRAY);
	  final MapOptions mOceansBasemap = new MapOptions(MapType.OCEANS);

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
	GraphicsLayer routeLayer;
    Graphic startgraphic,endGraphic;
    
    String startptLatLon,endptLatLon;
	// create UI components
	static ProgressDialog dialog;
	
	Boolean auth;
	TextView followernum;
	String followers;
	SpatialReference spatialRef =  SpatialReference.create(32612);//102100	4326
	
	String queryUrl = "";

	
	String barrierUrl = "http://192.168.18.33:6080/arcgis/rest/services/AC/ACBarriers/FeatureServer/1/query?where=1%3D1&f=pjson";
	// default sample service	
	String closestFacilitySampleUrl = "http://192.168.18.33:6080/arcgis/rest/services/AC/ACNetworkAnalysis/NAServer/Closest Facility";

	  private String resultData;
	  String tUsername;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.closestshelter_activity_main);

		// add map view with web map
		mMapView = (MapView) findViewById(R.id.map);
	
		basemap = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
		mMapView.addLayer(basemap);
		
	    //mMapView.setEsriLogoVisible(true);
	    mMapView.enableWrapAround(true);
	    
	    
		// Set the followers number Label.
	    Intent intent=getIntent();  
	    tUsername=intent.getStringExtra("username");  
        
	    followernum = (TextView) findViewById(R.id.followernumlabel);		
	    followernum.setText("Number:");
	    //followernum.setText(username);
		
	    
        btnEditApply = (Button) findViewById(R.id.btn_shelter_sure);
        btnEditApply.setOnClickListener(returnOnClickApplyChangesListener());
        
	    
		UserCredentials creds = new UserCredentials();
		// get user and password from string resource
		// create the user account to access service
		creds.setUserAccount("admin", "admin");
		fLayer = new ArcGISFeatureLayer("http://192.168.18.33:6080/arcgis/rest/services/AC/ACFacilities/FeatureServer/5",
				MODE.ONDEMAND, creds);
		// add road network layer to the map
		mMapView.addLayer(fLayer);
		
		//add polyline barriers
		 mTemplatebarrierLayer = new ArcGISFeatureLayer(
		    		"http://192.168.18.33:6080/arcgis/rest/services/AC/ACFacilities/FeatureServer/1",
		    		ArcGISFeatureLayer.MODE.ONDEMAND);
		 
		 mMapView.addLayer(mTemplatebarrierLayer);
		 mTemplatebarrierLayer.setVisible(false);
	    
		routeLayer = new GraphicsLayer();
		mMapView.addLayer(routeLayer);
		
		// wait for web map to load prior to adding graphic layer
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				if (source == mMapView && status == STATUS.INITIALIZED) {
					// Add the route graphic layer (shows the full route)
					//routeLayer = new GraphicsLayer();
					//mMapView.addLayer(routeLayer);
				}
		        if (STATUS.LAYER_LOADED == status) {
		            mMapView.setExtent(mCurrentMapExtent);         
		          }
			}

		});

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {
				// create a graphic for facility				
				PictureMarkerSymbol startSymbol = new PictureMarkerSymbol(mMapView.getContext(), 
						getResources()
								.getDrawable(R.drawable.route_strat_24));
				// set start location
				Point point = mMapView.toMapPoint(x, y);
				// create graphic
				final Graphic graphic = new Graphic(point, startSymbol);
				// set parameters graphic and query url
				
				//get leader LatLon for Routing analysis
				startgraphic=graphic;				
					
				Point leaderlocaiton = (Point) GeometryEngine.project(point,SpatialReference.create(102100),SpatialReference.create(4326));								
				startptLatLon=leaderlocaiton.getX()+","+leaderlocaiton.getY();
								
			    EditText editText = (EditText)findViewById(R.id.followernum);
			    followers= editText.getText().toString();
			    
		    	  if(followers==""){
		    		  
		    			Toast toast = Toast.makeText(ClosestShelterActivity.this,
								"Leader followers number is unknown.", Toast.LENGTH_LONG);
						toast.show();
						return;		    		  
		    		 
		    	  }
	             //query shelter with enough capacity 
				queryUrl = "http://192.168.18.33:6080/arcgis/rest/services/AC/ACFacilities/FeatureServer/0/query?where=Capacity>"+followers +"&f=pjson";
				setParameters(graphic, queryUrl);
				
				//************

			}
		});

	}
	 public OnClickListener returnOnClickApplyChangesListener() {

		    return new OnClickListener() {

		      public void onClick(View v) {
		    	  
		    	  //String aa=sendDataByGet("http://130.49.219.87:8081/segmentleader.do","findleadersbysegmentid","2");
		    	  /*
			     	String purl="http://130.49.219.87:8081/segmentleader.do?action=findleadersbysegmentid&segmentid=2";
		        	RegisterAsyncTask Register = new RegisterAsyncTask(purl);
			        Register.execute("");			        
			        String cc= Register.doInBackground();
			       // Register.onPostExecute(cc);
			        TextView tvQueryResult = (TextView) findViewById(R.id.followernumlabel); 
			        tvQueryResult.setText(cc);
					*/
		    	  
		    	  if(startptLatLon==""||endptLatLon==""){
		    		  
		    		  Toast toast = Toast.makeText(ClosestShelterActivity.this,
								"Leader or shelter location is unknown.", Toast.LENGTH_LONG);
						toast.show();
						return;
		    	  }
		    	  
		    	  Intent intent=new Intent();  
		            if(v==btnEditApply){  
		            	
		             intent.setClass(ClosestShelterActivity.this, RoutingtoshelterActivity.class); 
		              Bundle bundle=new Bundle();  
		              bundle.putString("startpt", startptLatLon);  
		              bundle.putString("endpt",endptLatLon); 
		              bundle.putString("followers",followers); 
		              bundle.putString("leaderid", tUsername);
		              intent.putExtras(bundle);
		            } 
		              
		            startActivity(intent);    

		    	  

		      }
		    };

		  }
	private void setParameters(Graphic graphic, String url) {
		// create graphic for location to start
		NAFeaturesAsFeature myLocationFeature = new NAFeaturesAsFeature();
		myLocationFeature.addFeature(graphic);
		myLocationFeature.setSpatialReference(mMapView.getSpatialReference());
		// create feature based on query url limiting stations by city
		NAFeaturesAsFeature nafaf = new NAFeaturesAsFeature();
		nafaf.setSpatialReference(mMapView.getSpatialReference());
		nafaf.setURL(url);
		
		// create feature based on query url limiting stations by city
		NAFeaturesAsFeature barriernafaf = new NAFeaturesAsFeature();
		barriernafaf.setSpatialReference(mMapView.getSpatialReference());
		barriernafaf.setURL(barrierUrl);
		
		// set up parameters
		ClosestFacilityParameters cfp = new ClosestFacilityParameters();
		cfp.setReturnFacilities(true);
		cfp.setOutSpatialReference(mMapView.getSpatialReference());
		
		// route direction to facility
		cfp.setTravelDirection(NATravelDirection.TO_FACILITY);
		
		// set incident to single tap location
		cfp.setIncidents(myLocationFeature);
		
		// set facilities to query url
		cfp.setFacilities(nafaf);
		cfp.setPolylineBarriers(barriernafaf);
		cfp.setDefaultTargetFacilityCount(1);
		FindClosestFacilities find = new FindClosestFacilities();
		cfp.setReturnFacilities(true);
		
		// execute task
		find.execute(cfp);
	}

	private class FindClosestFacilities extends
			AsyncTask<ClosestFacilityParameters, Void, ClosestFacilityResult> {

		protected void onPreExecute() {
			// remove any previous routes
			routeLayer.removeAll();
			// show progress dialog while geocoding address
			dialog = ProgressDialog.show(mMapView.getContext(),
					"ClosestFacilities",
					"Searching for route to closest facility ...");
		}

		@Override
		protected ClosestFacilityResult doInBackground(
				ClosestFacilityParameters... params) {

			ClosestFacilityResult result = null;
					
				auth = true;
				closestFacilityTask = new ClosestFacilityTask(
						closestFacilitySampleUrl, null);
		

			try {
				// solve the route
				result = closestFacilityTask.solve(params[0]);
			} catch (EsriSecurityException ese) {
				// username and password incorrect
				auth = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(ClosestFacilityResult result) {
			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			// The result of FindClosestFacilities task is passed as a parameter to map the
			// results
			if (result == null) {
				// update UI with notice that no results were found
				Toast toast = Toast.makeText(ClosestShelterActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();
				return;
			} else{
				route = result.getRoutes().get(0);
			
				// Symbols for the route and the destination
				SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE,
						3);
				PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(mMapView.getContext(), 
						getResources()
								.getDrawable(R.drawable.route_stop_24));

				Graphic routeGraphic = new Graphic(route.getRouteGraphic()
						.getGeometry(), routeSymbol);
				endGraphic = new Graphic(
						((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
								.getGeometry()).getPointCount() - 1),
						destinationSymbol);
				
				//get shelter LatLon for Routing analysis
				 Point pt=((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
							.getGeometry()).getPointCount() - 1);
				 
				 
				 Point shelterlocaiton = (Point) GeometryEngine.project(pt,SpatialReference.create(102100),SpatialReference.create(4326));				 
				 endptLatLon=shelterlocaiton.getX()+","+shelterlocaiton.getY();
				//**************
				/*
				 spatialRef=route.getRouteGraphic().getSpatialReference();
				 
				
				//Unit unit = spatialRef.getUnit();
				Unit unit =route.getRouteGraphic().getSpatialReference().getUnit();
				
				double bufferdistance = 0.002;	
				//unit = Unit.create(LinearUnit.Code.METER);			
				// get the result polygon from the buffer operation
			
				Polygon p = GeometryEngine.buffer(route.getRouteGraphic()
						.getGeometry(), spatialRef,
						bufferdistance, unit);

				// Render the polygon on the result graphic layer
				SimpleFillSymbol sfs = new SimpleFillSymbol(Color.BLUE);
				sfs.setOutline(new SimpleLineSymbol(Color.RED, 4,
						com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));
				sfs.setAlpha(75);
				Graphic g = new Graphic(p, sfs);
				*/
				//resultGeomLayer.addGraphic(g);
				//****************
				// Get the full route summary
				//show start location and target shelter 
				routeLayer
						//.addGraphics(new Graphic[] {routeGraphic, endGraphic,startgraphic});
						.addGraphics(new Graphic[] {endGraphic,startgraphic});
				// Zoom to the extent of the entire route with a padding
				mMapView.setExtent(route.getEnvelope(), 250);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.closestfacilities_main, menu);
		
	    
	    // Get the basemap switching menu items.
	    mStreetsMenuItem = menu.getItem(0);
	    mTopoMenuItem = menu.getItem(1);
	    mGrayMenuItem = menu.getItem(2);
	    mOceansMenuItem = menu.getItem(3);
	    
	    // Also set the topo basemap menu item to be checked, as this is the default.
	    mTopoMenuItem.setChecked(true); 
	    
	    return true;
	}
	
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    // Save the current extent of the map before changing the map.
	    mCurrentMapExtent = mMapView.getExtent();

	    // Handle menu item selection.
	    switch (item.getItemId()) {
	      case R.id.World_Street_Map:
	        mMapView.setMapOptions(mStreetsBasemap);
	        mStreetsMenuItem.setChecked(true);
	        return true;
	      case R.id.World_Topo:
	        mMapView.setMapOptions(mTopoBasemap);
	        mTopoMenuItem.setChecked(true);
	        return true;
	      case R.id.Gray:
	        mMapView.setMapOptions(mGrayBasemap);
	        mGrayMenuItem.setChecked(true);
	        return true;
	      case R.id.Ocean_Basemap:
	        mMapView.setMapOptions(mOceansBasemap);
	        mOceansMenuItem.setChecked(true);
	        return true;
	      default:
	        return super.onOptionsItemSelected(item);
	    }
	  }

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	// create user/pass to use commercial service
	//private static String user = "";
	//private static String pass = "";
	
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
