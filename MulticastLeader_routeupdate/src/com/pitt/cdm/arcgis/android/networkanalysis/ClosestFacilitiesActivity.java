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

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class ClosestFacilitiesActivity extends Activity {

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
	  
	// basemap layer
	ArcGISTiledMapServiceLayer basemapStreet;
	// feature layer
	ArcGISFeatureLayer fLayer;
	// closest facility task
	ClosestFacilityTask closestFacilityTask;
	// route definition
	Route route;
	// graphics layer to show route
	GraphicsLayer routeLayer;
    Graphic startgraphic;
	// create UI components
	static ProgressDialog dialog;
	
	Boolean auth;
	TextView followernum;
	SpatialReference spatialRef =  SpatialReference.create(32612);//102100	4326
	
	// string feature query urls where 1=1
   
	//String queryUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/padangFacilitiesN/FeatureServer/41/query?where=1%3D1&f=pjson";
	String queryUrl = "";//"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/padangFacilitiesN/FeatureServer/0/query?where=Capacity>0&f=pjson";
	//String queryUrl = "http://localhost:6080/arcgis/rest/services/test/padang_featureservice/FeatureServer/0/query?where=1%3D1&f=pjson";
	   
	// default sample service
	//String closestFacilitySampleUrl = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/ClosestFacility";
	String closestFacilitySampleUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/Padang_NAN/NAServer/Closest Facility";
	//String closestFacilitySampleUrl = "http://localhost:6080/arcgis/rest/services/test/padang_evacuation/NAServer/Closest Facility";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.closestfacilities_activity_main);

		// add map view with web map
		mMapView = (MapView) findViewById(R.id.map);
		
	    //mMapView.setEsriLogoVisible(true);
	    mMapView.enableWrapAround(true);
	    	    
        
		//add shelter layer
		/*Options o = new Options();
		o.mode = MODE.ONDEMAND;
		o.outFields = new String[] {"OBJECTID ", "ID", "NAME"};
		fLayer = new ArcGISFeatureLayer(
				"http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/padangFacilitiesN/MapServer/0",
				o);	
		mMapView.addLayer(fLayer);
		*/
	    
		UserCredentials creds = new UserCredentials();
		// get user and password from string resource
		// create the user account to access service
		creds.setUserAccount("admin", "admin");
		fLayer = new ArcGISFeatureLayer("http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/padangFacilitiesN/MapServer/36",
				MODE.ONDEMAND, creds);
		// add secure layer to the map
		mMapView.addLayer(fLayer);
		
	    
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
				//final SimpleMarkerSymbol sms = new SimpleMarkerSymbol(
				//		Color.BLACK, 13, SimpleMarkerSymbol.STYLE.DIAMOND);
				
				PictureMarkerSymbol startSymbol = new PictureMarkerSymbol(mMapView.getContext(), 
						getResources()
								.getDrawable(R.drawable.route_strat_24));
				// set start location
				Point point = mMapView.toMapPoint(x, y);
				// create graphic
				final Graphic graphic = new Graphic(point, startSymbol);
				// set parameters graphic and query url
				startgraphic=graphic;
				//add follower number to SQL to get available shelter
				//followernum = (TextView) findViewById(R.id.followernum);
				//String folnum= (String) followernum.getText();
				
			    EditText editText = (EditText)findViewById(R.id.followernum);
			    String folnum= editText.getText().toString();
	              
				queryUrl = "http://concepcion.gspia.pitt.edu:6080/arcgis/rest/services/GISSERVER/padangFacilitiesN/FeatureServer/0/query?where=Capacity>"+folnum +"&f=pjson";
				setParameters(graphic, queryUrl);
				
				//************

			}
		});

	}

	 public OnClickListener returnOnClickApplyChangesListener() {

		    return new OnClickListener() {

		      public void onClick(View v) {


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
		cfp.setDefaultTargetFacilityCount(1);
		FindClosestFacilities find = new FindClosestFacilities();
		
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
				Toast toast = Toast.makeText(ClosestFacilitiesActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();
				return;
			} else{
				route = result.getRoutes().get(0);
				// Symbols for the route and the destination
				SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE,
						25);
				PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(mMapView.getContext(), 
						getResources()
								.getDrawable(R.drawable.route_stop_24));

				Graphic routeGraphic = new Graphic(route.getRouteGraphic()
						.getGeometry(), routeSymbol);
				Graphic endGraphic = new Graphic(
						((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
								.getGeometry()).getPointCount() - 1),
						destinationSymbol);
				
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
						.addGraphics(new Graphic[] {routeGraphic, endGraphic,startgraphic});
						//.addGraphics(new Graphic[] {endGraphic,startgraphic});
				// Zoom to the extent of the entire route with a padding
				mMapView.setExtent(route.getEnvelope(), 100);
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

}
