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
	private static final int UPDATE_INTERVAL = 10000;
	private static final int FASTEST_INTERVAL = 10000;
	private MapContract.View mapView;
	private GoogleApiClient mGoogleApiClient;
	private DatabaseReference mMemberDataBase;
	private DatabaseReference mMessagesDatabase;
	private Context context;

	public MapPresenterImp(Context context, MapContract.View mapView){
		this.context = context;
		this.mapView = mapView;
		this.mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

	}


	@Override
	public void start() {
		mGoogleApiClient.connect();
	}

	@Override
	public void stop() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();
		if (mMessagesDatabase != null) {
			mMessagesDatabase.removeEventListener(messagesListener);
		}
		if (mMemberDataBase != null) {
			mMemberDataBase.removeEventListener(memberListener);
		}
		MemberManager.getInstance().clear();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		 Login();
		 startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if(FirebaseAuth.getInstance().getCurrentUser()!= null) {
			Member member = MemberManager.getInstance().getMemberById(FirebaseAuth.getInstance().getCurrentUser().getUid());
			if (member != null) {
				member.lat = location.getLatitude();
				member.lot = location.getLongitude();
				mMemberDataBase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(member.toMap());
			}
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

	private void startDataBase() {
		mMemberDataBase = FirebaseDatabase.getInstance().getReference("users");
		mMemberDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if (dataSnapshot.getChildrenCount() > 0) {
					for (DataSnapshot data : dataSnapshot.getChildren()) {
						Member member = data.getValue(Member.class);
						MemberManager.getInstance().updateMember(data.getKey(), member);
						if (member.lat != 0 && member.lot != 0) {
							member.setMarker(addMarker(data.getKey(), new LatLng(member.lat, member.lot)));
							if(!TextUtils.isEmpty(member.icon)){
								new BigHeaderTask(member.getMarker()).execute(member.icon);
							}
						}

					}
				}
				mMessagesDatabase = FirebaseDatabase.getInstance().getReference("messages");
				mMessagesDatabase.orderByKey().limitToLast(1).addChildEventListener(messagesListener);
				mMemberDataBase.addChildEventListener(memberListener);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

	private ChildEventListener memberListener = new ChildEventListener() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "member", "onChildAdded");
			Member member = dataSnapshot.getValue(Member.class);
			Member oldMember = MemberManager.getInstance().getMemberById(dataSnapshot.getKey());
			if(oldMember != null ){
				member.setMarker(oldMember.getMarker());
				if (oldMember.getMarker() != null && member.lat != 0 && member.lot != 0) {
					member.getMarker().setPosition(new LatLng(member.lat, member.lot));
				}else if (member.lat != 0 && member.lot != 0) {
					member.setMarker(addMarker(dataSnapshot.getKey(), new LatLng(member.lat, member.lot)));
				}
				if(!oldMember.icon.equals(member.icon)){
					new BigHeaderTask(member.getMarker()).execute(member.icon);
				}
			}else if (member.lat != 0 && member.lot != 0) {
				member.setMarker(addMarker(dataSnapshot.getKey(), new LatLng(member.lat, member.lot)));
				if(!oldMember.icon.equals(member.icon)){
					new BigHeaderTask(member.getMarker()).execute(member.icon);
				}
			}
			MemberManager.getInstance().updateMember(dataSnapshot.getKey(), member);
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "member", "onChildChanged");
			Member member = dataSnapshot.getValue(Member.class);
			Member oldMember = MemberManager.getInstance().getMemberById(dataSnapshot.getKey());
			if(oldMember != null ){
				member.setMarker(oldMember.getMarker());
				if (oldMember.getMarker() != null && member.lat != 0 && member.lot != 0) {
					member.getMarker().setPosition(new LatLng(member.lat, member.lot));
				}else if (member.lat != 0 && member.lot != 0) {
					member.setMarker(addMarker(dataSnapshot.getKey(), new LatLng(member.lat, member.lot)));
				}
				if(!oldMember.icon.equals(member.icon)){
					new BigHeaderTask(member.getMarker()).execute(member.icon);
				}
			}else if (member.lat != 0 && member.lot != 0) {
				member.setMarker(addMarker(dataSnapshot.getKey(), new LatLng(member.lat, member.lot)));
				if(!oldMember.icon.equals(member.icon)){
					new BigHeaderTask(member.getMarker()).execute(member.icon);
				}
			}
			MemberManager.getInstance().updateMember(dataSnapshot.getKey(), member);
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			L.d(TAG + "member", "onChildRemoved");
			L.d(TAG, dataSnapshot.toString());
			Member member = MemberManager.getInstance().getMemberById(dataSnapshot.getKey());
			if(member != null) {
				if (member.getMarker() != null) member.getMarker().remove();
				MemberManager.getInstance().remove(dataSnapshot.getKey());
			}
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
	private ChildEventListener messagesListener = new ChildEventListener() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "messages", "onChildAdded");
			Message message = dataSnapshot.getValue(Message.class);
			Member member = MemberManager.getInstance().getMemberById(message.uid);
			if(member != null && member.getMarker() != null) {
				member.getMarker().setTitle(message.msg);
				member.getMarker().showInfoWindow();
			}


		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "messages", "onChildChanged");
			L.d(TAG, dataSnapshot.toString());
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			L.d(TAG + "messages", "onChildRemoved");
			L.d(TAG, dataSnapshot.toString());

		}

		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String s) {
			L.d(TAG + "messages", "onChildMoved");
			L.d(TAG, dataSnapshot.toString());
		}

		@Override
		public void onCancelled(DatabaseError databaseError) {
			L.d(TAG + "messages", databaseError.toString());
		}
	};



	private Marker addMarker(String key, LatLng latLng) {
		Marker marker = null;
		if ( latLng != null) {
			MarkerOptions options = new MarkerOptions();
			options.position(latLng);
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.visitor));
			marker = mapView.addMarker(options);
			marker.setTag(key);
		}
		return marker;
	}


	@Override
	public void Login() {

		Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (mCurrentLocation != null) {
			L.d(TAG, "current location: " + mCurrentLocation.toString());
			FireBaseManager.getInstance().login(covertLocation(mCurrentLocation), new OnCompleteListener() {
				@Override
				public void onComplete(@NonNull Task task) {
					if (task.isSuccessful()) {
						startDataBase();
					}
				}
			});
			mapView.OnMoveCamera(covertLocation(mCurrentLocation), 15);
		}else{
			FireBaseManager.getInstance().login(new LatLng(0,0), new OnCompleteListener() {
				@Override
				public void onComplete(@NonNull Task task) {
					if (task.isSuccessful()) {
						startDataBase();
					}
				}
			});
		}
	}
}
