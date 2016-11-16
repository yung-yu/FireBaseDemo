package andy.firebasedemo.manager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import andy.firebasedemo.main.SystemＣonstants;
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
	private DatabaseReference mMemberDataBase;
	private DatabaseReference mMessagesDataBase;
	private Member myMember;


	public FireBaseManager() {
		mAuth = FirebaseAuth.getInstance();
		mMemberDataBase = FirebaseDatabase.getInstance().getReference(SystemＣonstants.TABLE_USERS);
		mMessagesDataBase = FirebaseDatabase.getInstance().getReference(SystemＣonstants.TABLE_MESSAGES);
	}

	public void login(OnCompleteListener onCompleteListener) {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			Log.d(TAG, user.getProviderId());
			myMember = new Member(user.getDisplayName(),
					user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "",
					System.currentTimeMillis(), FirebaseInstanceId.getInstance().getToken(), Member.STATUS_ONLINE);
			mMemberDataBase.child(user.getUid()).updateChildren(myMember.toMap()).addOnCompleteListener(onCompleteListener);
			requestFBUserId();
		}
	}

	public void loginOut() {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			HashMap<String, Object> item = new HashMap<>();
			item.put("status", Member.STATUS_OFFLINE);
			mMemberDataBase.child(user.getUid()).updateChildren(item);
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
						mMemberDataBase.child(user.getUid()).updateChildren(item).addOnCompleteListener(listener);
					}
				}
			});

		}
	}

	public void requestFBUserId() {

		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			user.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
				@Override
				public void onComplete(@NonNull final Task<GetTokenResult> task) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							OkHttpClient client = new OkHttpClient();
							Request request = new Request.Builder()
									.url("https://graph.facebook.com/me?fields=id&access_token=" + task.getResult().getToken().trim())
									.build();

							try {
								Response response = client.newCall(request).execute();
								JSONObject json = new JSONObject(response.body().string());
								Log.d(TAG, json.toString());
							} catch (IOException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}).start();

				}
			});
		}
	}

	public void sendMessage(Message item, OnCompleteListener<Void> listener) {
		mMessagesDataBase.push().setValue(item).addOnCompleteListener(listener);
		List<Member> offlineMembers = MemberManager.getInstance().getOffLineMembers();
		Member sender = MemberManager.getInstance().getMemberById(item.fromId);
		if(offlineMembers.size() > 0){
			for(Member member : offlineMembers){
				sendNotification(member.token, sender.name, item.msg);
			}
		}

	}

	public void updateMessage(String msgID, String changeText) {
		HashMap<String, Object> item = new HashMap<>();
		item.put("msg", changeText);
		mMessagesDataBase.child(msgID).updateChildren(item);
	}
	public void deleteMessage(String msgID) {
		mMessagesDataBase.child(msgID).removeValue();
	}
	public Member getMyMember() {
		return myMember;
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
			mMemberDataBase.child(user.getUid()).child("token").setValue(token);
		}
	}
}
