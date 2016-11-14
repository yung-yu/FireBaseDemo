package andy.firebasedemo.login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import andy.firebasedemo.ChatRoomActivity;
import andy.firebasedemo.R;

/**
 * Created by andyli on 2016/10/8.
 */

public class LoginDialogFragment extends DialogFragment implements View.OnClickListener, LoginContract.View{
    private final static String TAG = "LoginDialogFragment";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button googleAuthButton;
    private Button facebookButton;
    private LoginPresenterImp mLoginPresenterImp;
    private LoginContract.View loginView;
    private ProgressDialog mProgressBar;
    private OnLoginSuccessListener loginSuccessListener;

    public interface OnLoginSuccessListener{
        void onLogin();
    }

    public void setOnLoginSuccessListener(OnLoginSuccessListener loginSuccessListener) {
        this.loginSuccessListener = loginSuccessListener;
    }

    public LoginDialogFragment() {
        this.loginView = this;
    }

    public LoginDialogFragment(LoginContract.View loginView) {
        this.loginView = loginView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity());
        super.onCreate(savedInstanceState);



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailEditText = (EditText) view.findViewById(R.id.email);
        passwordEditText = (EditText) view.findViewById(R.id.password);
        loginButton = (Button) view.findViewById(R.id.loginButton);
        googleAuthButton = (Button) view.findViewById(R.id.googleAuthButton);
        facebookButton = (Button) view.findViewById(R.id.facebookButton);
        mLoginPresenterImp = new LoginPresenterImp(this, loginView);
        loginButton.setOnClickListener(this);
        googleAuthButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLoginPresenterImp.start();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
    public void setProgessBarShow(boolean isShow) {
        if(mProgressBar == null){
            mProgressBar = new ProgressDialog(getActivity());
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
        Toast.makeText(getActivity(), "登入成功", Toast.LENGTH_SHORT).show();
        if(loginSuccessListener != null){
            loginSuccessListener.onLogin();
        }
        dismiss();
    }

    @Override
    public void LoginFailed(String msg) {
        if(!TextUtils.isEmpty(msg)) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
