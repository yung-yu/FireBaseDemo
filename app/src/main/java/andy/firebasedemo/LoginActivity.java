package andy.firebasedemo;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by andyli on 2016/10/8.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button firstLoginButton;
    private Button loginButton;
    private Button googleAuthButton;
    private CallbackManager mCallbackManager;
    private final static int GOOGLE_AUTH_REQUEST_CODE = 8787;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);
        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        firstLoginButton = (Button) findViewById(R.id.firstLoginButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        googleAuthButton = (Button) findViewById(R.id.googleAuthButton);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            }
        };
        firstLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
        googleAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleAuthLogin();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onCancel"+error.toString());

            }


        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "please input email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            final EditText et = new EditText(LoginActivity.this);
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(R.string.app_name)
                                    .setMessage("please input nickName:")
                                    .setView(et)
                                    .setNegativeButton("ok", new DatePickerDialog.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            updateNickName(task.getResult().getUser(), et.getText().toString());
                                        }
                                    })
                                    .setPositiveButton("cancel", new DatePickerDialog.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mAuth.signOut();
                                        }
                                    })
                                    .show();

                        }

                        // ...
                    }
                });

    }

    private void login(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "please input email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            if(TextUtils.isEmpty(task.getResult().getUser().getDisplayName())){
                                final EditText et = new EditText(LoginActivity.this);
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle(R.string.app_name)
                                        .setMessage("please input nickName:")
                                        .setView(et)
                                        .setNegativeButton("ok", new DatePickerDialog.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                updateNickName(task.getResult().getUser(), et.getText().toString());

                                            }
                                        })
                                        .setPositiveButton("cancel", new DatePickerDialog.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                mAuth.signOut();
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                });
    }

    public void updateNickName(FirebaseUser user, String nickName) {
        if( TextUtils.isEmpty(nickName)){
            Toast.makeText(LoginActivity.this, "please input NickName",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(user == null){
            Toast.makeText(LoginActivity.this, "please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nickName)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            finish();
                        }
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_AUTH_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }
    public void googleAuthLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googlesSgnIn(mGoogleApiClient);
    }

    private void googlesSgnIn(GoogleApiClient mGoogleApiClient) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_AUTH_REQUEST_CODE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "login success",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            finish();
                        }
                        // ...
                    }
                });
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "login success",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            finish();
                        }

                    }
                });
    }
}
