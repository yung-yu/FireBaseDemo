package andy.firebasedemo.chatroom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.R;
import andy.firebasedemo.main.SystemＣonstants;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public class ChatRoomPresenterImp implements ChatRoomContract.Presenter, MemberManager.OnMemberChangeListener{

	private ChatRoomContract.View chatRoomView;
	private Context context;
	private DatabaseReference messageDatabase;
	private List< Message> data;

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
	public void start() {
		chatRoomView.setRefresh(true,context.getString(R.string.logining));
		FireBaseManager.getInstance().login(new OnCompleteListener() {
			@Override
			public void onComplete(@NonNull Task task) {
				if(task.isSuccessful()) {
					chatRoomView.setRefresh(true, context.getString(R.string.loadDataing));
					data = new ArrayList<>();
					messageDatabase = FirebaseDatabase.getInstance().getReference(SystemＣonstants.TABLE_MESSAGES);
					messageDatabase.limitToLast(100).addValueEventListener(messageListener);
					MemberManager.getInstance().registerUserListener(ChatRoomPresenterImp.this);
				} else {
					chatRoomView.setRefresh(false,"");
					chatRoomView.onLoginFailed();
				}
			}
		});

	}

	@Override
	public void stop() {
		if(messageDatabase!= null) {
			messageDatabase.removeEventListener(messageListener);
		}
		MemberManager.getInstance().unRegisterUserListener();
		FireBaseManager.getInstance().loginOut();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	private ValueEventListener messageListener = new ValueEventListener() {
		@Override
		public void onDataChange(DataSnapshot dataSnapshot) {
			chatRoomView.setRefresh(true, context.getString(R.string.messsge_updateing));
			data.clear();
			if(dataSnapshot.getChildrenCount() > 0) {
				for (DataSnapshot item: dataSnapshot.getChildren()) {
					Message msg = item.getValue(Message.class);
					msg.id = item.getKey();
					data.add(msg);
				}
			}
			chatRoomView.onNotify(data);
			chatRoomView.setRefresh(false, "");
		}

		@Override
		public void onCancelled(DatabaseError databaseError) {
			Toast.makeText(context, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
		}
	};


	@Override
	public void OnMemberChange() {
		chatRoomView.onNotify();
	}
}
