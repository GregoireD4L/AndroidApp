package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dataforlife.R;
import com.example.dataforlife.loggedservices.WelcomeLoggedActivity;
import com.example.dataforlife.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Author Yousria
 */
public class RegistrationActivity extends AppCompatActivity {

    private EditText registrationCode;
    private EditText email;
    private EditText password;
    private EditText firstName;
    private EditText lastName;

    private Button register;

    private static String TAG = "REGISTRATION ACTIVITY";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance();

        registrationCode = findViewById(R.id.registration_code);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register_registration);
        firstName =  findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(email.getText().toString(), password.getText().toString());
            }
        });


    }
    public void returnHome(View view){
        super.onBackPressed();
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(RegistrationActivity.this, "Registration performed, you can login", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            writeNewUser(user.getUid(),user.getEmail(),firstName.getText().toString(),lastName.getText().toString());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String emailInput = email.getText().toString();
        if (TextUtils.isEmpty(emailInput)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        String passwordInput = password.getText().toString();
        if (TextUtils.isEmpty(passwordInput)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegistrationActivity.this, WelcomeActivity.class);
            intent.putExtra("registrationCode", registrationCode.getText().toString());
            startActivity(intent);
        }else{
            Log.e(TAG, "Registration failed");
        }
    }

    private void writeNewUser(String userId, String email, String firstName, String lastName) {

        User user = new User(email, firstName, lastName);

        DatabaseReference ref =  mDataBase.getReference();
        ref.child("users").child(userId).setValue(user);
    }

}
