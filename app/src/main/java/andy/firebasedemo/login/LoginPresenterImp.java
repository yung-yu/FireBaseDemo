package andy.firebasedemo.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import andy.firebasedemo.Log.L;
import andy.firebasedemo.R;

/**
 * Created by andyli on 2016/11/5.
 */

public class LoginPresenterImp implements LoginContract.Presenter {
	private final static String TAG = "LoginPresenterImp";
	private final static String[] FACEBOOK_PERMISSIONS = {"email", "public_profile"};
	private final static int GOOGLE_AUTH_REQUEST_CODE = 9898;
	public LoginContract.View  loginView;
	public Context context;
	private CallbackManager mCallbackManager;
	private Fragment mFragment;
	private Activity mActivity;
	private GoogleApiClient mGoogleApiClient;


	public LoginPresenterImp(Fragment fragment, LoginContract.View loginView) {
		this.loginView = loginView;
		this.context = fragment.getContext();
		this.mActivity = fragment.getActivity();
		this.mFragment = fragment;


	}

	public LoginPresenterImp(Activity activity, LoginContract.View loginView) {
		this.loginView = loginView;
		this.context = activity;
		this.mActivity = activity;

	}

	@Override
	public void doEmailLoginLogin(String email, String password) {
		loginView.setProgessBarShow(true);
		normalLogin(email,password);
	}

	@Override
	public void doGoogleLogin() {
		loginView.setProgessBarShow(true);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(context.getString(R.string.default_web_client_id))
				.requestEmail()
				.requestProfile()
				.build();
		if(mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(context)
					.enableAutoManage((FragmentActivity) mActivity, connectionFailedListener)
					.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
					.build();
		}
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		mActivity.startActivityForResult(signInIntent, GOOGLE_AUTH_REQUEST_CODE);
	}

	@Override
	public void doFacebookLogin() {
		loginView.setProgessBarShow(true);
		if (mCallbackManager == null) mCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(mCallbackManager, mFacebookCallback);
		if (mFragment != null) {
			LoginManager.getInstance().logInWithReadPermissions(mFragment, Arrays.asList(FACEBOOK_PERMISSIONS));
		} else if (mActivity != null) {
			LoginManager.getInstance().logInWithReadPermissions(mActivity, Arrays.asList(FACEBOOK_PERMISSIONS));
		}
	}

	@Override
	public void doAnonymouslyLogin() {
		loginView.setProgessBarShow(true);
		FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					loginView.LoginSuccess(LoginType.Anonymously);
				} else {
					L.e(TAG, task.getException().toString());
					loginView.LoginFailed(task.getException().getMessage());
				}
				loginView.setProgessBarShow(false);
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
		if (mCallbackManager != null)
			mCallbackManager.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GOOGLE_AUTH_REQUEST_CODE) {
			//google login 處理 onActivityResult
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			}
		}
	}

	private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {

		@Override
		public void onSuccess(LoginResult loginResult) {
			L.d(TAG, "facebook:onSuccess:" + loginResult);
			handleFacebookAccessToken(loginResult.getAccessToken());
		}

		@Override
		public void onCancel() {
			L.d(TAG, "facebook:onCancel");
			loginView.LoginFailed(context.getString(R.string.loginFailed));
			loginView.setProgessBarShow(false);
		}

		@Override
		public void onError(FacebookException error) {
			L.d(TAG, "facebook:onError:" + error.getMessage());
			loginView.LoginFailed(error.getMessage());
			loginView.setProgessBarShow(false);
		}
	};

	private void handleFacebookAccessToken(AccessToken token) {
		L.d(TAG, "handleFacebookAccessToken:" + token);
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		FirebaseAuth.getInstance().signInWithCredential(credential)
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						L.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
						if (task.isSuccessful()) {
							loginView.LoginSuccess(LoginType.Facebook);
						} else {
							L.e(TAG, "signInWithCredential" + task.getException().getMessage());
							loginView.LoginFailed(task.getException().getMessage());
						}
						loginView.setProgessBarShow(false);

					}
				});
	}

	private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
			L.e(TAG, connectionResult.getErrorCode() + ":" + connectionResult.getErrorMessage());
			loginView.LoginFailed(context.getString(R.string.loginFailed));
			loginView.setProgessBarShow(false);
		}
	};

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		FirebaseAuth.getInstance().signInWithCredential(credential)
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							loginView.LoginSuccess(LoginType.Google);
						} else {
							L.e(TAG, task.getException().getMessage());
							loginView.LoginFailed(task.getException().getMessage());
						}
						loginView.setProgessBarShow(false);
					}
				});
	}

	private void normalLogin(final String email, final String password) {
		if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
			loginView.LoginFailed(context.getString(R.string.plase_input_email_and_password));
			loginView.setProgessBarShow(false);
			return;
		}
		FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					loginView.LoginSuccess(LoginType.normal);
					loginView.setProgessBarShow(false);
				} else {
					L.e(TAG, task.getException().toString());
					FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							L.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
							if (task.isSuccessful()) {
								loginView.LoginSuccess(LoginType.normal);
							} else{
								L.e(TAG, task.getException().getMessage());
								loginView.LoginFailed(task.getException().getMessage());
							}
							loginView.setProgessBarShow(false);
						}
					});
				}
			}
		});
	}
}
