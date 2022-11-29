package com.example.vchat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText mgetphonenumber;
    android.widget.Button msendotp;
    CountryCodePicker mcountrycodepicker;
    String countrycode;
    String phonenumber;

    FirebaseAuth firebaseAuth;
    ProgressBar mprogressbarofmain;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String codesent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mcountrycodepicker = findViewById(R.id.countrucodepicker);
        msendotp = findViewById(R.id.sendOtp);
        mgetphonenumber = findViewById(R.id.getphoneno);
        mprogressbarofmain = findViewById(R.id.progressbarofmain);

        firebaseAuth = FirebaseAuth.getInstance();

        countrycode = mcountrycodepicker.getSelectedCountryCodeWithPlus();

        mcountrycodepicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countrycode = mcountrycodepicker.getSelectedCountryCodeWithPlus();
            }
        });




        msendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                number = mgetphonenumber.getText().toString();
                if(number.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Please Enter Your Number",Toast.LENGTH_SHORT).show();
                }
                   else if(number.length()<10)
                {
                   // String s1 = String.valueOf(number);
                    //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();
                    Log.d("ADebugTag", "Value: " + number);
                    }
                else
                {
                    mprogressbarofmain.setVisibility(View.VISIBLE);
                    phonenumber = countrycode + number;

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber(phonenumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(MainActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks )          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }

        });

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential s) {
                Log.d(TAG, "onVerificationCompleted:" + s);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                } else if (e instanceof FirebaseTooManyRequestsException) {
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s,token);
                Toast.makeText(getApplicationContext(),"OTP sent successfully",Toast.LENGTH_SHORT).show();
                mprogressbarofmain.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onCodeSent:" + s);

                codesent = s;
                Intent intent = new Intent(MainActivity.this,otpAuthentication.class);
                intent.putExtra("otp",codesent);
                startActivity(intent);

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!= null)
        {
            Intent intent = new Intent(MainActivity.this,chatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

}