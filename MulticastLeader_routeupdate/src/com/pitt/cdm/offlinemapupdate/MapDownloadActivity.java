package com.pitt.cdm.offlinemapupdate;

import java.io.File;
import com.pitt.cdm.arcgis.android.maps.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.Menu;

public class MapDownloadActivity extends Activity {

	public static final String ROOT_DIR = "/mnt/sdcard/Download";
	private final String TAG="MainActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d(TAG, "Environment.getExternalStorageDirectory()="+Environment.getExternalStorageDirectory());
		Log.d(TAG, "getCacheDir().getAbsolutePath()="+getCacheDir().getAbsolutePath());
		
		showmapDownLoadDialog();
		showappDownLoadDialog();
		mopo();
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
			new AlertDialog.Builder(MapDownloadActivity.this).setTitle("download & upzip:")
	        .setMessage("update map").setIcon(R.drawable.ic_launcher)
	        .setPositiveButton("close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					 finish();
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
	
	private void showmapDownLoadDialog(){
		new AlertDialog.Builder(this).setTitle("download map")
		.setMessage("Download offline map")
		
		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("download", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				domapDownLoadWork();
			}
		})

		.show();
	}
	
	public void showmapUnzipDialog(){
		new AlertDialog.Builder(this).setTitle("Unzip map")
		.setMessage("unzip map to local path")

		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("Unzip", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				domapZipExtractorWork();
			}
		})
		.show();
	}
	
	private void showappDownLoadDialog(){
		new AlertDialog.Builder(this).setTitle("download app")
		.setMessage("Download offline app")
		
		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("download", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				doappDownLoadWork();
			}
		})

		.show();
	}
	
	public void showappUnzipDialog(){
		new AlertDialog.Builder(this).setTitle("Unzip app")
		.setMessage("unzip app to local path")

		.setNegativeButton("cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 2 = "+which);
			}
		})
		.setPositiveButton("Unzip", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick 1 = "+which);
				doappZipExtractorWork();
			}
		})
		.show();
	}
	public void domapZipExtractorWork(){
		ZipExtractorTask task = new ZipExtractorTask("/mnt/sdcard/Download/map.zip", "/mnt/sdcard/", this, true);
		task.execute();
	}
	
	private void domapDownLoadWork(){
		DownLoaderTask task = new DownLoaderTask("http://136.142.186.102:8080/Downloads/map.zip", "/mnt/sdcard/Download/", this,1);
		task.execute();
	}
	public void doappZipExtractorWork(){
		ZipExtractorTask task = new ZipExtractorTask("/mnt/sdcard/Download/app_leader.zip", "/mnt/sdcard/Download/", this, true);
		task.execute();
	}
	
	private void doappDownLoadWork(){
		DownLoaderTask task = new DownLoaderTask("http://136.142.186.102:8080/Downloads/app_leader.zip", "/mnt/sdcard/Download/", this,2);
		task.execute();
	}

}