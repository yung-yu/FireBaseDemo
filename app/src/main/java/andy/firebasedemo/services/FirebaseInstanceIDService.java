package andy.firebasedemo.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import andy.firebasedemo.manager.FireBaseManager;

/**
 * Created by andyli on 2016/10/23.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    private final static String TAG = "FirebaseInstanceIDService";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG,"update gcm token :"+token);
        FireBaseManager.getInstance().sendToken(token);
    }
}
