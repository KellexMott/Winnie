package com.techart.winnie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ServerValue;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.utilities.EditorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registration process
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etLogin;
    private EditText etPassword;
    private EditText etRepeatedPassword;
    private String firstPassword;
    private String repeatedPassword;
    private String name;
    private String email;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private Button btRegister;
    private String signingInAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.et_username);
        etLogin =  findViewById(R.id.et_login);
        etPassword =  findViewById(R.id.et_password);
        etRepeatedPassword = findViewById(R.id.et_repeatPassword);
        btRegister =  findViewById(R.id.bt_register);
        btRegister.setClickable(true);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (haveNetworkConnection() && validateCredentials())
            {
                startRegister();
            } else {
                Toast.makeText(RegisterActivity.this,"Ensure that your internet is working", Toast.LENGTH_LONG ).show();
            }
            }
        });
    }


    /**
     * implementation of the registration
     */
    private void startRegister() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Signing Up  ...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.createUserWithEmailAndPassword(email,firstPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String,Object> values = new HashMap<>();
                        values.put("name",name);
                        values.put("imageUrl","default");
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);

                        FireBaseUtils.mDatabaseUsers.child(userId).setValue(values);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    if (user != null) {
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "User profile updated.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                        mProgress.dismiss();
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error encountered, Please try again later", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    mProgress.dismiss();
                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Toast.makeText(RegisterActivity.this,"User already exits, use another email address", Toast.LENGTH_LONG ).show();
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Ensure that your internet is working", Toast.LENGTH_LONG ).show();
                    }
                }
            }
        });
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null)
        {
            NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
            if (netWorkInfo != null && netWorkInfo.getState() == NetworkInfo.State.CONNECTED)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the entries
     * @return true if they all true
     */
    private boolean validateCredentials()
    {
        firstPassword =  etPassword.getText().toString().trim();
        repeatedPassword =  etRepeatedPassword.getText().toString().trim();
        name =  etUsername.getText().toString().trim();
        email = etLogin.getText().toString().trim();
        return  EditorUtils.isEmpty(getApplicationContext(),name,"username") &&
                EditorUtils.isEmpty(getApplicationContext(),email,"email") &&
                EditorUtils.isEmailValid(getApplicationContext(), email) &&
                EditorUtils.isEmpty(getApplicationContext(),firstPassword,"password") &&
                EditorUtils.doPassWordsMatch(getApplicationContext(),firstPassword,repeatedPassword);
    }
}

