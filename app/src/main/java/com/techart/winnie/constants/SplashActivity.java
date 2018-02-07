package com.techart.winnie.constants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.techart.winnie.LoginActivity;
import com.techart.winnie.MainActivity;
import com.techart.winnie.R;

public class SplashActivity extends AppCompatActivity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */
            SharedPreferences isLogedin = getSharedPreferences("userData", MODE_PRIVATE);
            @Override
            public void run() {
                Class activityToLaunch = null;
                Boolean loginState = isLogedin.getBoolean("logInStatus",false);
                Log.i("LOG STATE", String.valueOf(loginState));
                if (loginState) {
                    activityToLaunch = MainActivity.class;
                }else{
                    activityToLaunch = LoginActivity.class;
                }

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, activityToLaunch);
                startActivity(i);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
