package andy.firebasedemo.chatroom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.Log.L;
import andy.firebasedemo.R;
import andy.firebasedemo.main.SystemConstants;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Message;
import andy.firebasedemo.object.MessageType;

/**
 * Created by andyli on 2016/11/5.
 */

public class ChatRoomPresenterImp implements ChatRoomContract.Presenter, MemberManager.OnMemberChangeListener{
    private final static String TAG = "ChatRoomPresenterImp";
	private ChatRoomContract.View chatRoomView;
	private Context context;
	private DatabaseReference messageDatabase;
	private List<Message> data;

	public ChatRoomPresenterImp(Context context, ChatRoomContract.View chatRoomView) {
		this.chatRoomView = chatRoomView;
		this.context = context;
	}

	@Override
	public  void sendMessage(String text) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if(user == null){
			chatRoomView.sendMessageFailed(context.getString(R.string.please_login));
			return;
		}
		if (TextUtils.isEmpty(text)) {
			chatRoomView.sendMessageFailed(context.getString(R.string.please_input_message));
			return;
		}
		chatRoomView.sendMessageReady();
		Message msg = new Message(user.getUid(),
				text,
				System.currentTimeMillis());
		FireBaseManager.getInstance().sendMessage(msg, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful()) {
					chatRoomView.sendMessageSuccess();
				}else{
					chatRoomView.sendMessageFailed(task.getException().getMessage());
				}
			}
		});

	}

	@Override
	public void sendImage(Uri fileUri) {
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if(user == null){
			chatRoomView.sendMessageFailed(context.getString(R.string.please_login));
			return;
		}
		if(fileUri == null){
			chatRoomView.sendMessageFailed("找不到圖片");
			return;
		}
		StorageReference imagesRef = FirebaseStorage.getInstance().getReference("images");
		UploadTask uploadTask = imagesRef.child("photo").child("image"+user.getUid()+System.currentTimeMillis()).putFile(fileUri);
		uploadTask.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception exception) {
				L.e(exception);
				chatRoomView.sendImageFailed(exception.toString());
			}
		}).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
				long cur = taskSnapshot.getBytesTransferred();
				long tal = taskSnapshot.getTotalByteCount();
				L.d(TAG, "cur "+cur);
				L.d(TAG, "tal "+tal);
				chatRoomView.onImageUploadProgress((int)((cur/tal)*100L));

			}
		}).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				L.d(TAG, "url "+taskSnapshot.getDownloadUrl());
				L.d(TAG, "metaData "+taskSnapshot.getMetadata().getContentType());


				Message msg = new Message(user.getUid(),
						MessageType.Photo.name(),
						taskSnapshot.getDownloadUrl().toString(),
						System.currentTimeMillis());
				FireBaseManager.getInstance().sendMessage(msg, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if(task.isSuccessful()) {
							chatRoomView.sendImageSuccess();
						}else{
							chatRoomView.sendImageFailed(task.getException().getMessage());
						}
					}
				});

			}
		});


	}

	@Override
	public void start() {
		if(FirebaseAuth.getInstance().getCurrentUser() != null){
			data = new ArrayList<>();
			messageDatabase = FirebaseDatabase.getInstance().getReference(SystemConstants.TABLE_MESSAGES);
			messageDatabase.limitToLast(100).addValueEventListener(messageListener);
			MemberManager.getInstance().registerUserListener(ChatRoomPresenterImp.this);
		}
	}

	@Override
	public void stop() {
		if(messageDatabase!= null) {
			messageDatabase.removeEventListener(messageListener);
		}
		MemberManager.getInstance().unRegisterUserListener();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	private ValueEventListener messageListener = new ValueEventListener() {
		@Override
		public void onDataChange(DataSnapshot dataSnapshot) {
			data.clear();
			if(dataSnapshot.getChildrenCount() > 0) {
				for (DataSnapshot item: dataSnapshot.getChildren()) {
					Message msg = item.getValue(Message.class);
					msg.id = item.getKey();
					data.add(msg);
				}
			}
			chatRoomView.onNotify(data);
		}
		@Override
		public void onCancelled(DatabaseError databaseError) {
			L.e(TAG,  databaseError.toException().toString());
		}
	};


	@Override
	public void OnMemberChange() {
		chatRoomView.onNotify();
	}
}
