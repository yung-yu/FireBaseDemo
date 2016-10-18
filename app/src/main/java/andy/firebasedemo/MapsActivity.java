package andy.firebasedemo;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import andy.firebasedemo.adapter.MessageAdapter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final static String TAG = "MapsActivity";
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;


    private GoogleMap mGoogleMap;
    private EditText messageEditText;
    private Button send;
    private List<String> memberList = new ArrayList<>();
    private HashMap<String, ValueEventListener> mValueEventListenerCache = new HashMap<>();
    private HashMap<String, Marker> mapMarkerCache = new HashMap<>();
    private HashMap<String, Message> msgCache = new HashMap<>();
    private Location myLocation;
    private DatabaseReference mLocationDatabase;
    private DatabaseReference mMessagesDatabase;
    private FirebaseAuth mAuth;
    private String focusUId;
    private Button loginOutButton;
    private Button changeNameButton;
    private Button historyButton;
    private RecyclerView mRecycleView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private MessageAdapter mMessageAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        messageEditText = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.send);
        mRecycleView = (RecyclerView) findViewById(R.id.recyclerView);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingUpPanelLayout);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendMessage(messageEditText.getText().toString());
            }
        });

        mMessageAdapter = new MessageAdapter(this);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(mMessageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if (mAuth != null) {
            mLocationDatabase = FirebaseDatabase.getInstance().getReference("locations");
            mLocationDatabase.addChildEventListener(locationListener);
            mMessagesDatabase = FirebaseDatabase.getInstance().getReference("messages");
            mMessagesDatabase.orderByKey().limitToLast(1).addChildEventListener(messagesListener);
            mMessageAdapter.startLisetener();
        } else {
            Toast.makeText(this, "login out ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        if (mLocationDatabase != null) {
            mLocationDatabase.removeEventListener(locationListener);
        }
        if (mMessagesDatabase != null) {
            mMessagesDatabase.removeEventListener(messagesListener);
        }
        mMessageAdapter.stopLisetener();
    }

    @Override
    protected void onDestroy() {


        super.onDestroy();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        if (mCurrentLocation != null) {
            Log.d(TAG, "current location: " + mCurrentLocation.toString());
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if(latLng != null) {
                LocationItem item = new LocationItem(latLng.latitude, latLng.longitude, System.currentTimeMillis());
                mLocationDatabase.child(mAuth.getCurrentUser().getUid()).setValue(item);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            }
        }
        // Begin polling for new location updates.
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

    }

    private ChildEventListener locationListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded");
            final LocationItem item = dataSnapshot.getValue(LocationItem.class);
            if (item != null) {
               mMessagesDatabase.orderByChild("uid").equalTo(dataSnapshot.getKey()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       Message message = dataSnapshot.getValue(Message.class);
                       if(message != null) {
                           addMarker(message.uid, new LatLng(item.lat, item.lon), message.msg);
                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged");
            Log.d(TAG, dataSnapshot.toString());
            final LocationItem item = dataSnapshot.getValue(LocationItem.class);
            if (item != null) {
                mMessagesDatabase.orderByChild("uid").equalTo(dataSnapshot.getKey()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Message message = dataSnapshot.getValue(Message.class);
                        if(message != null) {
                            addMarker(message.uid, new LatLng(item.lat, item.lon), message.msg);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, databaseError.toString());
        }
    };
    private ChildEventListener messagesListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded");
             Message message = dataSnapshot.getValue(Message.class);
             updateMarkerTitle(message.uid, message.msg);
             Toast toast = Toast.makeText(MapsActivity.this, message.name+":"+message.msg, Toast.LENGTH_SHORT);
             toast.setGravity(Gravity.TOP|Gravity.CENTER ,0,0);
             toast.show();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged");
            Log.d(TAG, dataSnapshot.toString());
            Message message = dataSnapshot.getValue(Message.class);
            updateMarkerTitle(message.uid, message.msg);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved");
            Log.d(TAG, dataSnapshot.toString());
            removeMarker(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, databaseError.toString());
        }
    };

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        mLocationDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("lon").getValue() == null
                        || !dataSnapshot.child("lon").getValue().equals(location.getLongitude())||
                        dataSnapshot.child("lat").getValue() == null ||!dataSnapshot.child("lat").getValue().equals(location.getLatitude())){
                    LocationItem item = new LocationItem(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
                    mLocationDatabase.child(mAuth.getCurrentUser().getUid()).setValue(item);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String message){
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"please input message",Toast.LENGTH_SHORT).show();
            return;
        }
        send.setEnabled(false);
        Message msg = new Message(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(),message);
        mMessagesDatabase.child(String.valueOf(System.currentTimeMillis())).setValue(msg.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                send.setEnabled(true);
                messageEditText.setText("");
            }
        });
    }
    private LatLng covertLocation(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    private Marker addMarker(String key, LatLng latLng, String msg){
        Marker marker = null;
        boolean isShow = false;
        if(mGoogleMap != null) {
            if(mapMarkerCache.containsKey(key)) {
                marker = mapMarkerCache.get(key);
                marker.remove();
                mapMarkerCache.remove(key);
            }
            MarkerOptions options = CustomMarker.createMarker(this);
            options.position(latLng);
            options.title(msg);
            marker = mGoogleMap.addMarker(options);
            mapMarkerCache.put(key, marker);
        }
        return  marker;
    }
    private void removeMarker(String key){
        if(mapMarkerCache.containsKey(key)) {
            Marker marker = mapMarkerCache.get(key);
            marker.remove();
        }
    }

    private void updateMarkerTitle(final String key, final String msg){
        Marker marker = null;
        if(mGoogleMap != null) {
            if(mapMarkerCache.containsKey(key)) {
                marker = mapMarkerCache.get(key);
                LatLng latLng = marker.getPosition();
                marker.remove();
                mapMarkerCache.remove(key);
                MarkerOptions options = CustomMarker.createMarker(this);
                options.position(latLng);
                options.title(msg);
                marker = mGoogleMap.addMarker(options);
                mapMarkerCache.put(key, marker);
                marker.showInfoWindow();
            }else{
                mLocationDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final LocationItem item = dataSnapshot.getValue(LocationItem.class);
                        if(item != null ) {
                            MarkerOptions options = CustomMarker.createMarker(MapsActivity.this);
                            options.position(new LatLng(item.lat, item.lon));
                            options.title(msg);
                            Marker marker = mGoogleMap.addMarker(options);
                            mapMarkerCache.put(key, marker);
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }
}
