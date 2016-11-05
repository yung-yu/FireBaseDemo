package andy.firebasedemo.message;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import andy.firebasedemo.R;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public class MessagePresenterImp implements MessageContract.Presenter {

	private MessageContract.View messageView;
	private Context context;
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
								messageView.sendNotifcationMessageFinish();
							}
						}
					})
					.setNegativeButton("cancel", null)
					.create().show();
		}
	}

	@Override
	public  void sendMessage(String message) {
		if (TextUtils.isEmpty(message)) {
			messageView.sendMessageFailed("please input message");
			return;
		}
		messageView.sendMessageReady();
		Message msg = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), message, System.currentTimeMillis());
		FireBaseManager.getInstance().sendMessage(msg, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				messageView.sendMessageSuccess();
			}
		});

	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
}
