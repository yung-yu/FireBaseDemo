package andy.firebasedemo.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.FacebookSdk;

import andy.firebasedemo.R;
import andy.firebasedemo.login.LoginContract;
import andy.firebasedemo.login.LoginPresenterImp;
import andy.firebasedemo.login.LoginType;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);
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
    public void setProgessBarVisiably(int visiably) {

    }

    @Override
    public void LoginSuccess(LoginType type) {
        Toast.makeText(this, type.toString()+" login success",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void LoginFailed(String msg) {
        Toast.makeText(this, " login fail:"+msg,Toast.LENGTH_SHORT).show();
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
}
