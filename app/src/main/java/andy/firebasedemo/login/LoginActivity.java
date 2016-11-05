package andy.firebasedemo.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.FacebookSdk;

import andy.firebasedemo.R;

/**
 * Created by andyli on 2016/10/8.
 */

public class LoginActivity extends AppCompatActivity implements LoginContract.View,View.OnClickListener{
    private final static String TAG = "LoginActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button googleAuthButton;
    private Button facebookButton;
    private LoginPresenterImp mLoginPresenterImp;
    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.loginButton);
        googleAuthButton = (Button) findViewById(R.id.googleAuthButton);
        facebookButton = (Button) findViewById(R.id.facebookButton);
        mLoginPresenterImp = new LoginPresenterImp(this, this);
        loginButton.setOnClickListener(this);
        googleAuthButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        mLoginPresenterImp.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLoginPresenterImp.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginPresenterImp.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void setProgessBarShow(boolean isShow) {
        if(mProgressBar == null){
            mProgressBar = new ProgressDialog(this);
            mProgressBar.setMessage(getString(R.string.logining));
        }
        if(isShow){
            if(!mProgressBar.isShowing()){
                mProgressBar.show();
            }
        }else{
            if(mProgressBar.isShowing()){
                mProgressBar.dismiss();
            }
        }
    }

    @Override
    public void LoginSuccess(LoginType type) {
        finish();
    }

    @Override
    public void LoginFailed(String msg) {
        if(!TextUtils.isEmpty(msg)) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
       if (view.getId() == loginButton.getId()){
           mLoginPresenterImp.doEmailLoginLogin(emailEditText.getText().toString(), passwordEditText.getText().toString());
       } else if (view.getId() == facebookButton.getId()){
           mLoginPresenterImp.doFacebookLogin();
       } else if (view.getId() == googleAuthButton.getId()){
           mLoginPresenterImp.doGoogleLogin();
       }
    }

    @Override
    public void setPresenter(LoginContract.Presenter Presenter) {

    }
}
