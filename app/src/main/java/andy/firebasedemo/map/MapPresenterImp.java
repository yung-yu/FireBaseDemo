package andy.firebasedemo.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import andy.firebasedemo.BigHeaderTask;
import andy.firebasedemo.Log.L;
import andy.firebasedemo.R;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public class MapPresenterImp implements MapContract.Presenter, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    private final static String TAG = "MapPresenterImp";
	private static final int UPDATE_INTERVAL = 5000;
	private static final int FASTEST_INTERVAL = 5000;
	private MapContract.View mapView;
	private GoogleApiClient mGoogleApiClient;
	private DatabaseReference mLocationDataBase;
	private Context context;
	private GoogleMap map;

	public MapPresenterImp(Context context, MapContract.View mapView){
		this.context = context;
		this.mapView = mapView;
		this.mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		this.mLocationDataBase = FirebaseDatabase.getInstance().getReference("locations");

	}


	@Override
	public void start() {
		mGoogleApiClient.connect();
		mLocationDataBase.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				L.d(TAG, "onDataChange"+dataSnapshot.getKey());
				L.d(TAG, "onDataChange"+dataSnapshot.getValue());
				map.clear();
				if(dataSnapshot.getChildrenCount() > 0){
					for(DataSnapshot item: dataSnapshot.getChildren()){
						double lat = (double) item.child("latitude").getValue();
						double lon = (double) item.child("longitude").getValue();
						String title = (String) item.child("title").getValue();
						addMarker(new LatLng(lat, lon),title);
					}
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
		FireBaseManager.getInstance().login(null);
	}

	@Override
	public void stop() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();
		if (mLocationDataBase != null) {
			mLocationDataBase.removeEventListener(locationListener);
		}
		MemberManager.getInstance().clear();
		FireBaseManager.getInstance().loginOut();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		 startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
    Location curlocation;
	@Override
	public void onLocationChanged(Location location) {
		if((curlocation == null || !curlocation.equals(location)) && FirebaseAuth.getInstance().getCurrentUser() != null){
			HashMap<String,Object> item = new HashMap<>();
			item.put("latitude", location.getLatitude());
			item.put("longitude", location.getLongitude());
			mLocationDataBase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(item);
			//addMarker(covertLocation(location));

			curlocation = location;
		}
	}
	private void startLocationUpdates() {
		// Create the location request
		LocationRequest mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(UPDATE_INTERVAL)
				.setFastestInterval(FASTEST_INTERVAL);
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
				mLocationRequest, this);
	}

	private LatLng covertLocation(Location location) {
		return new LatLng(location.getLatitude(), location.getLongitude());
	}



	private ChildEventListener locationListener = new ChildEventListener() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			L.d(TAG, "onChildAdded"+dataSnapshot.getKey());
			L.d(TAG, "onChildAdded"+dataSnapshot.getValue());
			L.d(TAG, "onChildAdded"+s);
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			L.d(TAG, "onChildAdded"+dataSnapshot.getKey());
			L.d(TAG, "onChildAdded"+dataSnapshot.getValue());
			L.d(TAG, "onChildAdded"+s);
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			L.d(TAG + "member", "onChildRemoved");
			L.d(TAG, "onChildAdded"+dataSnapshot.getKey());
			L.d(TAG, "onChildAdded"+dataSnapshot.getValue());
		}

		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "member", "onChildMoved");
			L.d(TAG, dataSnapshot.toString());
		}

		@Override
		public void onCancelled(DatabaseError databaseError) {
			L.d(TAG + "member", databaseError.toString());
		}
	};



	private Marker addMarker( LatLng latLng, String title) {
		Marker marker = null;
		if ( latLng != null) {
			MarkerOptions options = new MarkerOptions();
			options.position(latLng);
			options.title(title);
//			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.visitor));
			marker = map.addMarker(options);
			marker.showInfoWindow();
		}

		return marker;
	}


	@Override
	public void setGoogleMap(GoogleMap map) {
		this.map = map;
	}
}
