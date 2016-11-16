package andy.firebasedemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import andy.firebasedemo.login.LoginContract;
import andy.firebasedemo.login.LoginDialogFragment;
import andy.firebasedemo.login.LoginType;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.adapter.MessageAdapter;
import andy.firebasedemo.map.MapContract;
import andy.firebasedemo.map.MapPresenterImp;
import andy.firebasedemo.message.MessageContract;
import andy.firebasedemo.message.MessagePresenterImp;
import andy.firebasedemo.object.Message;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,MapContract.View, MessageContract.View,LoginContract.View,
        View.OnClickListener {
    private final static String TAG = "MapsActivity";
    private GoogleMap mGoogleMap;
    private MapView mapView;
    private EditText messageEditText;
    private Button send;
    private RecyclerView mRecycleView;
    private DrawerLayout mParentDrawlayout;
    private DrawerLayout mChildDrawlayout;
    private MessageAdapter mMessageAdapter;
    private FloatingActionButton chat;
    private RelativeLayout menu2;
    private Toolbar mToolbar;
    private MapPresenterImp mMapPresenterImp;
    private MessagePresenterImp mMessagePresenterImp;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LoginDialogFragment mLoginDialogFragment;
    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        messageEditText = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.send);
        chat = (FloatingActionButton) findViewById(R.id.chat);
        send.setOnClickListener(this);
        chat.setOnClickListener(this);

        mRecycleView = (RecyclerView) findViewById(R.id.recyclerView);

        mParentDrawlayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mChildDrawlayout = (DrawerLayout) findViewById(R.id.drawerLayout2);
        menu2 = (RelativeLayout) findViewById(R.id.menu2);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mParentDrawlayout,mToolbar,R.string.open_drawer,R.string.close_drawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                syncState();
            }
        };
        mParentDrawlayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();


        mMessageAdapter = new MessageAdapter(this);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(mMessageAdapter);
        mMapPresenterImp = new MapPresenterImp(this, this);
        mMessagePresenterImp = new MessagePresenterImp(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        mMapPresenterImp.start();
        mMessagePresenterImp.start();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
        mMessageAdapter.startLisetener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        mMapPresenterImp.stop();
        mMessagePresenterImp.stop();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        mMessageAdapter.stopLisetener();
        mGoogleMap.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mMapPresenterImp.setGoogleMap(googleMap);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                return false;
            }
        });

    }

    private FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() == null) {
                openLoginDialog();
            }else{
                closeLoginDialog();
            }
        }
    };


    @Override
    public void OnMoveCamera(LatLng latLng, float zoom) {
        if(mGoogleMap != null){
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    @Override
    public Marker addMarker(MarkerOptions options) {
        return mGoogleMap != null ? mGoogleMap.addMarker(options):null;
    }

    @Override
    public void sendMessageReady() {
       send.setEnabled(false);
    }

    @Override
    public void sendMessageSuccess() {
        send.setEnabled(true);
        messageEditText.setText("");
    }

    @Override
    public void sendMessageFailed(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onNotify(List<Message> data) {

    }

    @Override
    public void setRefresh(boolean isRefresh) {

    }

    @Override
    public void onClick(View view) {
       if(view.getId() == send.getId()){
           mMessagePresenterImp.sendMessage(messageEditText.getText().toString());
       } else if(view.getId() == chat.getId()){
           if (!mChildDrawlayout.isDrawerVisible(menu2)) {
               mChildDrawlayout.openDrawer(menu2, true);
           }
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginOut:
                if(FirebaseAuth.getInstance().getCurrentUser() !=null ) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.login_out)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mGoogleMap.clear();
                                    FireBaseManager.getInstance().signOut();
                                    recreate();
                                }
                            }).setNegativeButton("cancel", null)
                            .create().show();
                }else{
                    openLoginDialog();
                }
                return true;
        }
        return false;
    }

    @Override
    public void setProgessBarShow(boolean isShow) {
        if(mProgressBar == null){
            mProgressBar = new ProgressDialog(this);
            mProgressBar.setMessage(getString(R.string.logining));
        }
        if(isShow){
            if(!mProgressBar.isShowing()){
                mProgressBar.show();
            }
        }else{
            if(mProgressBar.isShowing()){
                mProgressBar.dismiss();
            }
        }
    }

    @Override
    public void LoginSuccess(LoginType type) {
       if(mLoginDialogFragment != null){
           mLoginDialogFragment.dismiss();
       }
        recreate();
    }

    @Override
    public void LoginFailed(String msg) {
        if(!TextUtils.isEmpty(msg)) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void openLoginDialog(){
        if(mLoginDialogFragment == null) {
            mLoginDialogFragment = new LoginDialogFragment(this);
        }
        if(!mLoginDialogFragment.isAdded()) {
            mLoginDialogFragment.show(getSupportFragmentManager(), LoginDialogFragment.class.getName());
        }
    }
    public void closeLoginDialog(){
        if(mLoginDialogFragment != null) {
            mLoginDialogFragment.dismiss();
        }
    }

}
