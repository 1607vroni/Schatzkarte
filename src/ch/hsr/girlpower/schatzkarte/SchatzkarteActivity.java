package ch.hsr.girlpower.schatzkarte;

import java.io.File;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import ch.hsr.girlpower.schatzkarte.MyItemizedOverlay;



import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SchatzkarteActivity extends Activity implements LocationListener{
	
	private org.osmdroid.views.MapView map;

	private LocationManager locationManager;
	private GeoPoint GeoPoint;	
	private IMapController controller;
	private MyItemizedOverlay myItemizedOverlay = null;
	private MyItemizedOverlay myLocation;
	
	private final static int ACTIVITY_CHOOSE_FILE = 1;
	private final static int ACTIVITY_ACTIVATE_GPS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_karte);
		
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
	       final MapView mapView = (MapView) findViewById(R.id.mapview);
	        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
	        mapView.setMultiTouchControls(true);
	        mapView.setBuiltInZoomControls(true);
	        
	        controller = mapView.getController();
	        controller.setZoom(17);
 
	        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        	warnungUndBeenden();
	        }
	        else
	        {
	        
	        File offlineMap = new File(Environment.getExternalStorageDirectory(), "hsr.mbtiles");
			map = (MapView) findViewById(R.id.mapview);
			map.setTileSource(TileSourceFactory.MAPQUESTOSM);
			map.setMultiTouchControls(true);
			map.setBuiltInZoomControls(true);
			IMapController controller = map.getController();
			controller.setZoom(15);
			XYTileSource mapTileSource = new XYTileSource("mbtiles",ResourceProxy.string.offline_mode,
					1, 20, 256, ".png", "http://example.org/");
			
			/*
			MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(
					new SimpleRegisterReceiver(this), mapTileSource,
					new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(offlineMap) });

			MapTileProviderBase treasureMapProvider = new MapTileProviderArray(
					mapTileSource, null,
					new MapTileModuleProviderBase[] { treasureMapModuleProvider });

			TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
			treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
			
			
			map.getOverlays().add(treasureMapTilesOverlay);
			*/
			
			Drawable marker=getResources().getDrawable(android.R.drawable.star_on);
	        int markerWidth = marker.getIntrinsicWidth();
	        int markerHeight = marker.getIntrinsicHeight();
	        marker.setBounds(0, markerHeight, markerWidth, 0);
	        
	        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
	        
	        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
	        marker = getResources().getDrawable(android.R.drawable.ic_menu_myplaces);
	        mapView.getOverlays().add(myItemizedOverlay);
	        
	        myLocation = new MyItemizedOverlay(marker, resourceProxy);
	        map.getOverlays().add(myItemizedOverlay);
	        mapView.getOverlays().add(myLocation);

	        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	        }
	}
			
	/** Benutzer auffordern GPS zu aktivieren und Activity beenden 
	 * 
	*/
	private void warnungUndBeenden() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
        String text   = res.getString(R.string.keinGPS);
	    builder.setMessage(text); 
	 	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int id) {
	            dialog.dismiss();
	            finish(); // Activity beenden
	       }
	    });
	  	AlertDialog dialog = builder.create();
	  	dialog.show();
	}		
		        
	@Override
	public void onLocationChanged(Location location){
		GeoPoint overlocGeoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
		GeoPoint = overlocGeoPoint;
		myLocation.setHomeLocation(overlocGeoPoint, "CurrentLocation", "CurrentLocation");
	}
		    	    
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		locationManager = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager = (LocationManager) this .getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
			
	public void Standort(View v){
		controller.setZoom(18);
		controller.animateTo(GeoPoint);
	}
	
	public void Markersetzen(View v){
		myItemizedOverlay.addItem(GeoPoint, "onLocationChanged", "onLocationChanged");
	}
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(R.string.LogText);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				log(myLocation);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("Marks", myLocation);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		myLocation = savedInstanceState.getParcelable("Marks");
		map.getOverlays().add(myLocation);
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	private AlertDialog createMessageDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				finish();
			}
		});
		return builder.create();
	}
	
	private void longToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	public String stackTraceToString(StackTraceElement[] stacktrace) {
		String res = new String();
		for (int i = 0; i < stacktrace.length; i++) {
			res = res + stacktrace[i].toString();
		}
		return res;
	}
	
	private void log(MyItemizedOverlay itemizedOverlayToLog) {
		Intent intent = new Intent("ch.appquest.intent.LOG");

		if (getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}
		
		intent.putExtra("ch.appquest.taskname", "Schatzkarte");
		intent.putExtra("ch.appquest.logmessage", markersToString(itemizedOverlayToLog));

		startActivity(intent);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_CHOOSE_FILE: {
			if (resultCode == RESULT_OK) {
				try {
					Uri uri = data.getData();
					SharedPreferences settings = getSharedPreferences("strings", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("TileFilePath", uri.getPath());
					editor.commit();
					onCreate(null);
				} catch (Exception e) {
					AlertDialog dialog = createMessageDialog(stackTraceToString(e.getStackTrace()));
					dialog.show();
				}
			}
			else if (resultCode==RESULT_CANCELED) {
				longToast("Select Offline Data!!");
			}
			break;
		}
		case ACTIVITY_ACTIVATE_GPS: {
			if (resultCode == RESULT_OK) {
				onCreate(null);
				break;
			}
		}
		}

	}
	
	private String markersToString(MyItemizedOverlay io) {
		String res = new String();
		for (int i = 0; i < io.size(); i++) {
			GeoPoint actual = io.createItem(i).getPoint();
			if (i == 0)
				res += "(" + actual.getLatitudeE6() + "/"+ actual.getLongitudeE6() + ")";
			else
				res += ",(" + actual.getLatitudeE6() + "/"+ actual.getLongitudeE6() + ")";
		}
		return res;
	}

	public void removed(int index) {
		try {
			MyItemizedOverlay oldOverlay = (MyItemizedOverlay) map.getOverlays().get(1);
			MyItemizedOverlay newOverlay = new MyItemizedOverlay(oldOverlay.getMarker(),
					oldOverlay.getResourceProxy(), (RemoveItemCallback) this);
			oldOverlay.remove(index);
			for (int i = 0; i < oldOverlay.size(); i++) {
				OverlayItem acualItem = oldOverlay.createItem(i);
				newOverlay.addItem(acualItem.getPoint(), acualItem.getTitle(),acualItem.getSnippet());
			}
			myLocation = newOverlay;
			map.getOverlays().remove(oldOverlay);
			map.getOverlays().add(myLocation);
			map.invalidate();
		} catch (Exception e) {
			AlertDialog dialog=createMessageDialog(stackTraceToString(e.getStackTrace()));
			dialog.show();
		}
	}
}
