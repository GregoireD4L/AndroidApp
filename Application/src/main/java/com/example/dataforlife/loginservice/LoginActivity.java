package com.example.dataforlife.loginservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dataforlife.R;
import com.example.dataforlife.loggedservices.WelcomeLoggedActivity;
import com.example.dataforlife.pairservice.PairPagerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Author Yousria
 */
public class LoginActivity extends AppCompatActivity{

    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnResetPassword;
    private TextView mStatusTextView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //verify if user is authentified
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, PairPagerActivity.class));
            finish();
        }


        btnSignIn = findViewById(R.id.login);
        btnResetPassword = findViewById(R.id.forgot_password);
        inputEmail = findViewById(R.id.email_login);
        inputPassword = findViewById(R.id.password_login);
        mStatusTextView = findViewById(R.id.status_login);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputPassword.getWindowToken(), 0);
                mStatusTextView.setText("");
                signIn(inputEmail.getText().toString(), inputPassword.getText().toString());
            }
        });

    }

    private void signIn(String email, String password) {
        Log.d("LOGIN ACTIVITY", "signIn:" + email);

        if(!validateForm()){
            return;
        }
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LOGIN ACTIVITY", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LOGIN ACTIVITY", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText("AUTH FAILED!");
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                mStatusTextView.setText("Wrong email or password");
                            }
                            if(task.getException() instanceof FirebaseAuthEmailException){
                                mStatusTextView.setText("Invalid email");
                            }
                        }

                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null){
            Intent intent = new Intent(LoginActivity.this, PairPagerActivity.class);
            startActivity(intent);
        }else{
            Log.e("LOGIN ACTIVITY", "AUTH FAILED");
            mStatusTextView.setText("Authentification failed");
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = inputEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Required.");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        String password = inputPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Required.");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }
}
