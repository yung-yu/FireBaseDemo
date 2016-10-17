package andy.firebasedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser()==null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }else{
                    mAuth.signOut();
                    Toast.makeText(MainActivity.this,"login out",Toast.LENGTH_SHORT).show();
                    loginButton.setText("Login");
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser()==null){
            loginButton.setText("Login");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            loginButton.setText("Login out");
            startActivity(new Intent(this, MapsActivity.class));
            finish();
        }
    }
}
