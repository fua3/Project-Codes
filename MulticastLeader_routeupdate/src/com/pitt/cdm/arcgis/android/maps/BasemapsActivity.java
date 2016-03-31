/* Copyright 2013 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.pitt.cdm.arcgis.android.maps;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Polygon;

/* The Basemaps sample app shows how you can switch basemaps, by showing choices on the Android options menu or action bar,
/ and using the MapOptions.MapType enum, the MapOptions class, and the setMapOptions method on the MapView. The setExtent 
/ and setOnStatusChangedListener methods are used to preserve the current extent of the map when the basemap is changed.
  */
public class BasemapsActivity extends Activity {

  // The MapView.
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

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.maps_main);

    // Retrieve the map and initial extent from XML layout
    mMapView = (MapView) findViewById(R.id.map);

    // Set the Esri logo to be visible, and enable map to wrap around date line.
    mMapView.setEsriLogoVisible(true);
    mMapView.enableWrapAround(true);

    // Set a listener for map status changes; this will be called when switching basemaps.
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
        // Set the map extent once the map has been initialized, and the basemap is added
        // or changed; this will be indicated by the layer initialization of the basemap layer. As there is only
        // a single layer, there is no need to check the source object.
        if (STATUS.LAYER_LOADED == status) {
          mMapView.setExtent(mCurrentMapExtent);         
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items from the Menu XML to the action bar, if present.
    getMenuInflater().inflate(R.menu.basemap_menu, menu);
    
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

}