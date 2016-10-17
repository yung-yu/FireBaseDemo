package andy.firebasedemo;

import android.*;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.location.FusedLocationProviderApi;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HashMap<String, LocalMessage> msgCache = new HashMap<>();
    private Location myLocation;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String focusUId;
    private Button loginOutButton;
    private Button changeNameButton;
    private Button historyButton;
    private ListView historyListView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        messageEditText = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.button2);
        loginOutButton = (Button) findViewById(R.id.loginOut);
        historyButton = (Button) findViewById(R.id.historyButton);
        historyListView = (ListView) findViewById(R.id.historyListView);
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
        loginOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if (mAuth != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("localMessages");
            mDatabase.addChildEventListener(localMessagesListener);

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
        if (mDatabase != null) {
            mDatabase.removeEventListener(localMessagesListener);
        }
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
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
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

    private ChildEventListener localMessagesListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded");
            String message = (String) dataSnapshot.child("msg").getValue();
            Double lat = (Double) dataSnapshot.child("lat").getValue();
            Double lon = (Double) dataSnapshot.child("lon").getValue();
            Long time = (Long) dataSnapshot.child("time").getValue();
            Log.d(TAG, "key : " + dataSnapshot.getKey());
            Log.d(TAG, "message : " + message);
            Log.d(TAG, "lat : " + lat);
            Log.d(TAG, "lon : " + lon);
            Log.d(TAG, "time : " + time);
            if (lat == null || lon == null) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                lat = mCurrentLocation.getAltitude();
                lon = mCurrentLocation.getLongitude();
                mDatabase.child(mAuth.getCurrentUser().getUid()).child("lat").setValue(lat);
                mDatabase.child(mAuth.getCurrentUser().getUid()).child("lon").setValue(lon);
                mDatabase.child(mAuth.getCurrentUser().getUid()).child("time").setValue(System.currentTimeMillis());
            }else {
                addMarker(dataSnapshot.getKey(), new LatLng(lat, lon), message);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged");
            Log.d(TAG, dataSnapshot.toString());
            String message = (String) dataSnapshot.child("msg").getValue();
            Double lat = (Double) dataSnapshot.child("lat").getValue();
            Double lon = (Double) dataSnapshot.child("lon").getValue();
            Long time = (Long) dataSnapshot.child("time").getValue();
            Log.d(TAG, "key : "+dataSnapshot.getKey());
            Log.d(TAG, "message : "+message);
            Log.d(TAG, "lat : "+lat);
            Log.d(TAG, "lon : "+lon);
            Log.d(TAG, "time : "+time);
            if (lat != null && lon != null) {
                addMarker(dataSnapshot.getKey(), new LatLng(lat, lon), message);
            }
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
        mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("lon").getValue() == null
                        || !dataSnapshot.child("lon").getValue().equals(location.getLongitude())||
                        dataSnapshot.child("lat").getValue() == null ||!dataSnapshot.child("lat").getValue().equals(location.getLatitude())){
                    mDatabase.child(mAuth.getCurrentUser().getUid()).child("lat").setValue(location.getLatitude());
                    mDatabase.child(mAuth.getCurrentUser().getUid()).child("lon").setValue(location.getLongitude());
                    mDatabase.child(mAuth.getCurrentUser().getUid()).child("time").setValue(System.currentTimeMillis());
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

        mDatabase.child(mAuth.getCurrentUser().getUid()).child("msg").setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void addMarker(String key, LatLng latLng, String msg){
        Marker marker = null;
        if(mGoogleMap != null) {
            if(mapMarkerCache.containsKey(key)) {
                marker = mapMarkerCache.get(key);
                marker.remove();
            }
            MarkerOptions options = CustomMarker.createMarker(this);
            options.position(latLng);
            options.title(msg);
            marker = mGoogleMap.addMarker(options);
            mapMarkerCache.put(key, marker);
            marker.showInfoWindow();
        }
    }
    private void removeMarker(String key){
        if(mapMarkerCache.containsKey(key)) {
            Marker marker = mapMarkerCache.get(key);
            marker.remove();
        }
    }
}
