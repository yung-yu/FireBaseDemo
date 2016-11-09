package andy.firebasedemo.message;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.R;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public class MessagePresenterImp implements MessageContract.Presenter{

	private MessageContract.View messageView;
	private Context context;
	private DatabaseReference messageDatabase;
	private List< Message> data ;

	public MessagePresenterImp(Context context, MessageContract.View messageView) {
		this.messageView = messageView;
		this.context = context;
	}


	@Override
	public void sendNotifcationMessage(String uid) {
		final EditText et = new EditText(context);
		final Member member = MemberManager.getInstance().getMemberById(uid);
		if(member != null){
			new AlertDialog.Builder(context)
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

	@Override
	public  void sendMessage(String text) {
		if(FirebaseAuth.getInstance().getCurrentUser() == null){
			messageView.sendMessageFailed(context.getString(R.string.please_login));
			return;
		}
		if (TextUtils.isEmpty(text)) {
			messageView.sendMessageFailed(context.getString(R.string.please_input_message));
			return;
		}
		messageView.sendMessageReady();
		Message msg = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), text, System.currentTimeMillis());
		FireBaseManager.getInstance().sendMessage(msg, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful()) {
					messageView.sendMessageSuccess();
				}else{
					messageView.sendMessageFailed(task.getException().getMessage());
				}
			}
		});

	}

	@Override
	public void start() {
		data = new ArrayList<>();
		messageDatabase = FirebaseDatabase.getInstance().getReference("messages");
		messageDatabase.addValueEventListener(messageListener);
	}

	@Override
	public void stop() {
		messageDatabase.removeEventListener(messageListener);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	private ValueEventListener messageListener = new ValueEventListener() {
		@Override
		public void onDataChange(DataSnapshot dataSnapshot) {
			messageView.setRefresh(true);
			data.clear();
			if(dataSnapshot.getChildrenCount() > 0) {
				for (DataSnapshot item: dataSnapshot.getChildren()) {
					Message msg = item.getValue(Message.class);
					msg.id = item.getKey();
					data.add(msg);
				}
			}
			messageView.onNotify(data);
			messageView.setRefresh(false);
		}

		@Override
		public void onCancelled(DatabaseError databaseError) {
			Toast.makeText(context, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();
		}
	};



}
