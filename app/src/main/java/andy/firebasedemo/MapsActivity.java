package andy.firebasedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;

import andy.firebasedemo.component.HeadShotMarker;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.adapter.MessageAdapter;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final static String TAG = "MapsActivity";
    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 10000;


    private GoogleMap mGoogleMap;
    private EditText messageEditText;
    private Button send;
    private HashMap<String, Member> memberCache = new HashMap<>();
    private HashMap<String, Bitmap> bigHeadCache = new HashMap<>();
    private DatabaseReference mMessagesDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mMemberDataBase;
    private RecyclerView mRecycleView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DrawerLayout mParentDrawlayout;
    private DrawerLayout mChildDrawlayout;
    private MessageAdapter mMessageAdapter;
    private FloatingActionButton chat;
    private RelativeLayout menu2;
    private Toolbar mToolbar;


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
        mParentDrawlayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mChildDrawlayout = (DrawerLayout) findViewById(R.id.drawerLayout2);
        chat = (FloatingActionButton) findViewById(R.id.chat);
        menu2 = (RelativeLayout) findViewById(R.id.menu2);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.loginOut:
                        FireBaseManager.getInstance().loginOut();
                        mAuth.signOut();
                        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                        finish();
                        break;

                }
                return false;
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mapFragment.getMapAsync(this);


        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendMessage(messageEditText.getText().toString());
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!mChildDrawlayout.isDrawerVisible(menu2)) {
                    mChildDrawlayout.openDrawer(menu2, true);
                }
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParentDrawlayout.openDrawer(Gravity.LEFT, true);
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
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            mAuth.addAuthStateListener(mAuthStateListener);
        } else {
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
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
        mAuth.removeAuthStateListener(mAuthStateListener);
        FireBaseManager.getInstance().loginOut();
        if (mMessagesDatabase != null) {
            mMessagesDatabase.removeEventListener(messagesListener);
        }
        if (mMemberDataBase != null) {
            mMemberDataBase.removeEventListener(memberListener);
        }
        if (mGoogleMap != null) {
            mGoogleMap.clear();
        }
        memberCache.clear();
        mMessageAdapter.stopLisetener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        if (mCurrentLocation != null) {
            Log.d(TAG, "current location: " + mCurrentLocation.toString());
            FireBaseManager.getInstance().login(covertLocation(mCurrentLocation), new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MapsActivity.this, "login success", Toast.LENGTH_SHORT).show();
                        startDataBase();
                    } else {
                        Toast.makeText(MapsActivity.this, "login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(covertLocation(mCurrentLocation), 15));
        }else{
            FireBaseManager.getInstance().login(new LatLng(0,0), new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MapsActivity.this, "login success", Toast.LENGTH_SHORT).show();
                        startDataBase();
                    } else {
                        Toast.makeText(MapsActivity.this, "login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                showP2pMsg((String) marker.getTag());
                return false;
            }
        });

    }
    public void showP2pMsg(final String uid){
        final EditText et = new EditText(this);
        final Member member = memberCache.get(uid);
        if(member != null){
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       String msg = et.getText().toString();
                       if(!TextUtils.isEmpty(msg)){
                           FireBaseManager.getInstance().sendNotification(member.token, member.name, msg);
                       }
                    }
                })
                .setNegativeButton("cancel", null)
                .create().show();
        }
    }
    public void startDataBase() {
        mMemberDataBase = FirebaseDatabase.getInstance().getReference("users");
        mMemberDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Member member = data.getValue(Member.class);
                        memberCache.put(data.getKey(), member);
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
                mMessageAdapter.startLisetener();
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
            Log.d(TAG + "member", "onChildAdded");
            Member member = dataSnapshot.getValue(Member.class);
            Member oldMember = memberCache.get(dataSnapshot.getKey());
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
            memberCache.put(dataSnapshot.getKey(), member);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "member", "onChildChanged");
            Member member = dataSnapshot.getValue(Member.class);
            Member oldMember = memberCache.get(dataSnapshot.getKey());
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
            memberCache.put(dataSnapshot.getKey(), member);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG + "member", "onChildRemoved");
            Log.d(TAG, dataSnapshot.toString());
            Member member = memberCache.get(dataSnapshot.getKey());
            member.getMarker().remove();
            memberCache.remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "member", "onChildMoved");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG + "member", databaseError.toString());
        }
    };
    private ChildEventListener messagesListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "messages", "onChildAdded");
            Message message = dataSnapshot.getValue(Message.class);
            Member member = memberCache.get(message.uid);
            if(member != null && member.getMarker() != null) {
                member.getMarker().setTitle(message.msg);
                member.getMarker().showInfoWindow();
            }


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "messages", "onChildChanged");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG + "messages", "onChildRemoved");
            Log.d(TAG, dataSnapshot.toString());

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG + "messages", "onChildMoved");
            Log.d(TAG, dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG + "messages", databaseError.toString());
        }
    };

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Member member = memberCache.get(mAuth.getCurrentUser().getUid());
        if (member != null) {
            member.lat = location.getLatitude();
            member.lot = location.getLongitude();
            mMemberDataBase.child(mAuth.getCurrentUser().getUid()).setValue(member.toMap());
        }
    }

    private void sendMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "please input message", Toast.LENGTH_SHORT).show();
            return;
        }
        send.setEnabled(false);
        Message msg = new Message(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), message, System.currentTimeMillis());
        FireBaseManager.getInstance().sendMessage(msg, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                send.setEnabled(true);
                messageEditText.setText("");
            }
        });

    }

    private LatLng covertLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }



    private synchronized Marker addMarker(String key, LatLng latLng) {
        Marker marker = null;
        if (mGoogleMap != null && latLng != null) {
            MarkerOptions options = CustomMarker.createMarker(this);
            options.position(latLng);
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.visitor));
            marker = mGoogleMap.addMarker(options);
            marker.setTag(key);
        }
        return marker;
    }



    private FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                finish();
            }
        }
    };



}
