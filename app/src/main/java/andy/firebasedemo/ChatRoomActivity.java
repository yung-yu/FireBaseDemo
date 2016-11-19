package andy.firebasedemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import andy.firebasedemo.adapter.ChatRoomMessageAdapter;
import andy.firebasedemo.login.LoginDialogFragment;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.message.MessageContract;
import andy.firebasedemo.message.MessagePresenterImp;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/7.
 */
public class ChatRoomActivity extends AppCompatActivity implements MessageContract.View {
	private EditText editText;
	private Button button;
	private ListView listView;
	private MessagePresenterImp mMessagePresenterImp;
	private ChatRoomMessageAdapter mMsgAdapter;
	private Toolbar mToolbar;
	private LoginDialogFragment mLoginDialogFragment;
	private ProgressBar toolbarProgressBar;
	private TextView toolbar_text;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		FacebookSdk.sdkInitialize(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatroom);
		editText = (EditText) findViewById(R.id.editText);
		button = (Button) findViewById(R.id.button);
		listView = (ListView) findViewById(R.id.listview);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbarProgressBar = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
		toolbar_text = (TextView) findViewById(R.id.toolbar_text);
		setSupportActionBar(mToolbar);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mMessagePresenterImp.sendMessage(editText.getText().toString());
			}
		});
		mMsgAdapter = new ChatRoomMessageAdapter(this);
		listView.setAdapter(mMsgAdapter);
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Message msg = mMsgAdapter.getItem(i);
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if(user != null && msg.fromId.equals(user.getUid())) {
					new AlertDialog.Builder(ChatRoomActivity.this)
							.setMessage(R.string.app_name)
							.setMessage(R.string.delete_tip)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {

									FireBaseManager.getInstance().deleteMessage(msg.id);
									dialogInterface.cancel();
								}
							})
							.setNegativeButton(R.string.cancel, null)
							.create().show();
				}
				return true;
			}
		});
		mMessagePresenterImp = new MessagePresenterImp(this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMessagePresenterImp.start();
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
		mMessagePresenterImp.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void sendMessageReady() {
		button.setEnabled(false);
	}

	@Override
	public void sendMessageSuccess() {
		button.setEnabled(true);
		editText.setText("");
	}

	@Override
	public void sendMessageFailed(String msg) {
		button.setEnabled(true);
		editText.setText("");
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNotify(List<Message> data) {
		mMsgAdapter.setData(data);
		mMsgAdapter.notifyDataSetChanged();
		listView.setSelection(data.size());
	}

	@Override
	public void onNotify() {
		mMsgAdapter.notifyDataSetChanged();
	}

	@Override
	public void setRefresh(boolean isRefresh, String progressMsg) {
		toolbarProgressBar.setVisibility(isRefresh?View.VISIBLE:View.GONE);
		toolbar_text.setVisibility(isRefresh?View.VISIBLE:View.GONE);
		toolbar_text.setText(progressMsg);
	}

	@Override
	public void onLoginFailed() {
		showLoginDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginOut = menu.findItem(R.id.loginOut);
		loginOut.setVisible(FirebaseAuth.getInstance().getCurrentUser() != null);
		MenuItem loginIn = menu.findItem(R.id.loginIn);
		loginIn.setVisible(FirebaseAuth.getInstance().getCurrentUser() == null);
		MenuItem changeName = menu.findItem(R.id.changeName);
		changeName.setVisible(FirebaseAuth.getInstance().getCurrentUser() != null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()){
		   case R.id.loginOut:
			   new AlertDialog.Builder(this)
				   .setTitle(R.string.app_name)
				   .setMessage(R.string.login_out)
				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialogInterface, int i) {
						   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
						   if(user != null) {
							   FirebaseAuth.getInstance().signOut();
							   mMsgAdapter.notifyDataSetChanged();
							   Toast.makeText(ChatRoomActivity.this, R.string.loginOutSuccess, Toast.LENGTH_SHORT).show();
						   }
					   }
				   }).setNegativeButton(R.string.cancel, null)
				   .create().show();
			   break;
		   case R.id.loginIn:
			   showLoginDialog();
			   break;
		   case R.id.changeName:
			   showChangeNameDialog();
			   break;
	   }
		return true;
	}



	private void showLoginDialog(){
		if(mLoginDialogFragment == null){
			mLoginDialogFragment = new LoginDialogFragment();
			mLoginDialogFragment.setOnLoginSuccessListener(new LoginDialogFragment.OnLoginSuccessListener() {
				@Override
				public void onLogin() {
					mMessagePresenterImp.start();
				}
			});
		}
		if(!mLoginDialogFragment.isAdded()) {
			mLoginDialogFragment.show(getSupportFragmentManager(),
					LoginDialogFragment.class.getName());
		}
	}

	private void showChangeNameDialog(){
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if(user != null){
			final Member member = MemberManager.getInstance().getMemberById(user.getUid());
			final EditText et = new EditText(this);
			et.setText(member.name);
			et.setGravity(Gravity.CENTER);
			et.setMaxLines(1);
			new AlertDialog.Builder(this)
					.setTitle(R.string.app_name)
					.setMessage(R.string.change_name)
					.setView(et)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							String newName = et.getText().toString();
                            if(!TextUtils.isEmpty(newName) &&
									!newName.equals(member.name)){
                                FireBaseManager.getInstance().updateMemberName(newName, new OnCompleteListener() {
									@Override
									public void onComplete(@NonNull Task task) {
										if(task.isSuccessful()){
											Toast.makeText(ChatRoomActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
											mMsgAdapter.notifyDataSetChanged();
										}else{
											Toast.makeText(ChatRoomActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
										}
									}
								});

							}
						}
					})
					.setNegativeButton(R.string.cancel, null)
					.create().show();
		}

	}
}


