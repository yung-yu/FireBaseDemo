package andy.firebasedemo.manager;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.HashMap;

import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/10/19.
 */

public class FireBaseManager {
    private final static String TAG = "FireBaseManager";
    static FireBaseManager instance;
    private final static String FCM_SEND_API = "https://fcm.googleapis.com/fcm/send";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static FireBaseManager getInstance() {
        if (instance == null) {
            instance = new FireBaseManager();
        }
        return instance;
    }

    private FirebaseAuth mAuth;
    private DatabaseReference mOnLineDataBase;
    private DatabaseReference mMessagesDataBase;
    private Member myMember;


    public FireBaseManager() {
        mAuth = FirebaseAuth.getInstance();
        mOnLineDataBase = FirebaseDatabase.getInstance().getReference("users");
        mMessagesDataBase = FirebaseDatabase.getInstance().getReference("messages");
    }

    public void login(LatLng latLng, OnCompleteListener onCompleteListener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            myMember = new Member(user.getDisplayName(), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "", latLng.latitude, latLng.longitude, System.currentTimeMillis(), FirebaseInstanceId.getInstance().getToken());
            mOnLineDataBase.child(user.getUid()).setValue(myMember).addOnCompleteListener(onCompleteListener);
        }
    }

    public void loginOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mOnLineDataBase.child(user.getUid()).removeValue();

        }
    }



    public void sendMessage(Message item, OnCompleteListener<Void> listener) {
        mMessagesDataBase.push().setValue(item).addOnCompleteListener(listener);
    }


    public Member getMyMember() {
        return myMember;
    }

    public void sendNotification(final String token, final String sender, final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> item = new HashMap<>();
                try {
                    String jsonMessage = "{\"notification\":{\n" +
                            "\"title\":\""+sender+"\"," +
                            "\"body\": \""+msg+"\"\n" +
                            "  },\n" +
                            "  \"to\" : \""+token+"\""+
                            "}";
                    Log.d(TAG,jsonMessage);
                    RequestBody body = RequestBody.create(JSON, jsonMessage.trim());
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FCM_SEND_API)
                            .addHeader("Authorization","key=AIzaSyCd8z_J9fEPXiCfOSTNkWfQCIIB0p15fqU")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    Log.d(TAG,response.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }).start();

    }

    public void sendToken(String token){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mOnLineDataBase.child(user.getUid()).child("token").setValue(token);
        }
    }
}
