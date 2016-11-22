package andy.firebasedemo.manager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.HashMap;

import andy.firebasedemo.main.SystemＣonstants;
import andy.firebasedemo.object.LoginFailedTask;
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
	private DatabaseReference mMemberReference;
	private DatabaseReference mMessagesReference;
	private Member myMember;


	public FireBaseManager() {
		mAuth = FirebaseAuth.getInstance();
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
		mMemberReference = FirebaseDatabase.getInstance().getReference(SystemＣonstants.TABLE_USERS);
		mMessagesReference = FirebaseDatabase.getInstance().getReference(SystemＣonstants.TABLE_MESSAGES);
	}

	public void login(OnCompleteListener listener) {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			Log.d(TAG, user.getProviderId());
			myMember = new Member(user.getDisplayName(),
					user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "",
					System.currentTimeMillis(), FirebaseInstanceId.getInstance().getToken(), Member.STATUS_ONLINE);
			mMemberReference.child(user.getUid()).updateChildren(myMember.toMap()).addOnCompleteListener(listener);
			HashMap<String,Object> item = new HashMap<>();
			item.put("status", Member.STATUS_OFFLINE);
			mMemberReference.child(user.getUid()).onDisconnect().updateChildren(item);
		}else{
			if(listener != null) {
				listener.onComplete(new LoginFailedTask());
			}
		}
	}

	public void loginOut() {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			HashMap<String, Object> item = new HashMap<>();
			item.put("status", Member.STATUS_OFFLINE);
			mMemberReference.child(user.getUid()).updateChildren(item);
		}
	}

	public void updateMemberName(final String newName, final OnCompleteListener listener ) {
		final FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
					.setDisplayName(newName)
					.build();
			user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if(task.isSuccessful()) {
						HashMap<String, Object> item = new HashMap<>();
						item.put("name", newName);
						mMemberReference.child(user.getUid()).updateChildren(item).addOnCompleteListener(listener);
					}
				}
			});

		}
	}


	public void requestFBUserId() {

//		FirebaseUser user = mAuth.getCurrentUser();
//		if (user != null) {
//			user.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//				@Override
//				public void onComplete(@NonNull final Task<GetTokenResult> task) {
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							OkHttpClient client = new OkHttpClient();
//							Request request = new Request.Builder()
//									.url("https://graph.facebook.com/me?fields=id&access_token=" + task.getResult().getToken().trim())
//									.build();
//
//							try {
//								Response response = client.newCall(request).execute();
//								JSONObject json = new JSONObject(response.body().string());
//								Log.d(TAG, json.toString());
//							} catch (IOException e) {
//								e.printStackTrace();
//							} catch (JSONException e) {
//								e.printStackTrace();
//							}
//						}
//					}).start();
//
//				}
//			});
//		}
	}

	public void sendMessage(Message item, OnCompleteListener<Void> listener) {
		mMessagesReference.push().setValue(item).addOnCompleteListener(listener);
	}


	public void deleteMessage(String msgID) {
		mMessagesReference.child(msgID).removeValue();
	}


	public void sendNotification(final String token , final String sender, final String msg) {
		new Thread("SendFCM"){
			@Override
			public void run() {
				try {
					String jsonMessage = "{\"notification\":{\n" +
							"\"title\":\"" + sender + "\"," +
							"\"sound\":\"" + "default" + "\"," +
							"\"tag\":\"" + 999 + "\"," +
							"\"body\": \"" + msg + "\"\n" +
							"  },\n" +
							"  \"to\" : \"" + token + "\"" +
							"}";
					Log.d(TAG, jsonMessage);
					RequestBody body = RequestBody.create(JSON, jsonMessage.trim());
					OkHttpClient client = new OkHttpClient();
					Request request = new Request.Builder()
							.url(FCM_SEND_API)
							.addHeader("Authorization", "key=AIzaSyCd8z_J9fEPXiCfOSTNkWfQCIIB0p15fqU")
							.post(body)
							.build();
					Response response = client.newCall(request).execute();
					Log.d(TAG, response.toString());
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		}.start();

	}

	public void signOut(){
		if(FirebaseAuth.getInstance().getCurrentUser() != null) {
			loginOut();
			FirebaseAuth.getInstance().getCurrentUser().delete();
			mAuth.signOut();
		}
	}

	public void sendToken(String token) {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			mMemberReference.child(user.getUid()).child("token").setValue(token);
		}
	}
}
