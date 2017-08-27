package andy.firebasedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import andy.firebasedemo.Log.L;
import andy.firebasedemo.auth.AuthContract;
import andy.firebasedemo.auth.AuthPresenterImp;
import andy.firebasedemo.chatroom.ChatRoomFragment;
import andy.firebasedemo.helper.ToolbarUIHelper;
import andy.firebasedemo.login.LoginDialogFragment;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.util.AndroidUtils;

/**
 * Created by andyli on 2016/11/7.
 */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

	private LoginDialogFragment mLoginDialogFragment;



	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		L.i(TAG, "onCreate");
		FacebookSdk.sdkInitialize(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ChatRoomFragment mChatRoomFragment = new ChatRoomFragment();
		AndroidUtils.startFragment(getSupportFragmentManager(), mChatRoomFragment, R.id.fragment_container, null , false);
	}

	@Override
	protected void onStart() {
		L.i(TAG, "onStart");
		super.onStart();


	}

	@Override
	protected void onResume() {
		L.i(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		L.i(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		L.i(TAG, "onStop");
		super.onStop();


	}

	@Override
	protected void onDestroy() {
		L.i(TAG, "onDestroy");
		super.onDestroy();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(mLoginDialogFragment != null){
			mLoginDialogFragment.onActivityResult(requestCode, resultCode, data);
		}

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
							   Toast.makeText(MainActivity.this, R.string.loginOutSuccess, Toast.LENGTH_SHORT).show();
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
											Toast.makeText(MainActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
										}else{
											Toast.makeText(MainActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
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

	public void showLoginDialog(){
		if(mLoginDialogFragment == null){
			mLoginDialogFragment = new LoginDialogFragment();
			mLoginDialogFragment.setOnLoginSuccessListener(new LoginDialogFragment.OnLoginSuccessListener() {
				@Override
				public void onLogin() {
				}
			});
		}
		if(!mLoginDialogFragment.isAdded()) {
			mLoginDialogFragment.show(getSupportFragmentManager(),
					LoginDialogFragment.class.getName());
		}
	}
}


