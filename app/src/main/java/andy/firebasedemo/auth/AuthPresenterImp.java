package andy.firebasedemo.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import andy.firebasedemo.R;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.ＴoolbarUIHelper;

/**
 * Created by andyli on 2016/11/25.
 */

public class AuthPresenterImp implements AuthContract.Presenter, FirebaseAuth.AuthStateListener {

	private AuthContract.View authView;


	@Override
	public void start() {
		ＴoolbarUIHelper.getInstance().setProgressBarVisibility(View.VISIBLE);
		ＴoolbarUIHelper.getInstance().setTextViewVisibility(View.VISIBLE);
		ＴoolbarUIHelper.getInstance().setText(R.string.logining);
		FirebaseAuth.getInstance().addAuthStateListener(this);
		if (FirebaseAuth.getInstance().getCurrentUser() == null) {
			if (authView != null) {
				authView.onAuthSignOut();
			}
			ＴoolbarUIHelper.getInstance().setProgressBarVisibility(View.GONE);
			ＴoolbarUIHelper.getInstance().setTextViewVisibility(View.GONE);
			ＴoolbarUIHelper.getInstance().setText("");
		}
		FireBaseManager.getInstance().login(new OnCompleteListener() {
			@Override
			public void onComplete(@NonNull Task task) {
				if (task.isSuccessful()) {
					authView.onLogin();
				}
				ＴoolbarUIHelper.getInstance().setProgressBarVisibility(View.GONE);
				ＴoolbarUIHelper.getInstance().setTextViewVisibility(View.GONE);
				ＴoolbarUIHelper.getInstance().setText("");
			}
		});

	}

	@Override
	public void stop() {
		FirebaseAuth.getInstance().removeAuthStateListener(this);
		FireBaseManager.getInstance().loginOut();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
		if (firebaseAuth.getCurrentUser() == null) {
			if (authView != null) {
				authView.onAuthSignOut();
			}
			ＴoolbarUIHelper.getInstance().setProgressBarVisibility(View.GONE);
			ＴoolbarUIHelper.getInstance().setTextViewVisibility(View.GONE);
			ＴoolbarUIHelper.getInstance().setText("");
		}
	}

	@Override
	public void setAuthView(AuthContract.View authView) {
		this.authView = authView;
	}
}
