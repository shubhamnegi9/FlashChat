package com.shubhamnegi.flashchatnewfirebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class RegisterActivity extends AppCompatActivity {

    // Constants
    static final String CHAT_PREFS = "ChatPrefs";
    static final String DISPLAY_NAME_KEY = "username";

    // TODO: Add member variables here:
    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;

    // Firebase instance variables
    private FirebaseAuth mAuth;     // The user registration & authentication are handled by this


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.register_username);

        // Keyboard sign in action
        // Adding a listener on mConfirmPasswordView such that when user hits enter button from keyboard after entering confirmed password,
        // attemptRegistration() function will be called
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        // TODO: Get hold of an instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();     // Creating instance of FirebaseAuth


    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        Log.d("FlashChat", "TextUtils.isEmpty(password): " + TextUtils.isEmpty(password));
        Log.d("FlashChat", "TextUtils.isEmpty(password) && !isPasswordValid(password): " + (TextUtils.isEmpty(password) && !isPasswordValid(password)));


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            Log.d("FlashChat", "Password Invalid");
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // TODO: Call create FirebaseUser() here
            createFirebaseUser();

        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Add own logic to check for a valid password
        String confirmPassword = mConfirmPasswordView.getText().toString();
        // Checking if confirmPassword is equal to password and length of password > 6
        return confirmPassword.equals(password) && password.length() > 6;
    }

    // TODO: Create a Firebase user
    private void createFirebaseUser() {

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        // createUserWithEmailAndPassword() method will return an object of type "Task". We will use this "Task" to listen for the event.
        // If creating the user on firebase server is successful, an event is triggered and we will use the "Task" to listen for the event using addOnCompleteListener
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("FlashChat", "createUser onComplete: " + task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.d("FlashChat", "user creation failed", task.getException());
                    showErrorDialog("Registration attempt failed");
                } else {
                    saveDisplayName();  // Display name to be saved only when user has been successfully registered on firebase server
                    showSuccessDialog("User Registration Successful!");
                }
            }
        });
    }


    // TODO: Save the display name to Shared Preferences
    private void saveDisplayName() {
        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = mUsernameView.getText().toString();

        /**
         *  Using Shared Preferences to store the username
         */
//        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, 0);
//        prefs.edit().putString(DISPLAY_NAME_KEY, displayName).apply();


        /**
         *  Using Shared Preferences, the username is saved only once during the registration process.
         *  A fresh app install will wipe this data, and our user after login is stuck being called 'Anonymous.
         *  This can be solved by persisting username in Firebase instead of local storage (Shared Preferences) as below:
         */
        if (user !=null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FlashChat", "User name updated.");
                            }
                        }
                    });
        }

        /**
         *  Managing users in firebase --> https://firebase.google.com/docs/auth/android/manage-users#update_a_users_profile
         */

    }


    // TODO: Create an alert dialog to show in case registration failed
    private void showErrorDialog(String message){

//        Using normal alert dialog :
//        new AlertDialog.Builder(this)
//                .setTitle("Oops")
//                .setMessage(message)
//                .setPositiveButton(android.R.string.ok, null)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();

//        Using SweetAlert dialog:
        new SweetAlertDialog(
                this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(message)
                .show();
    }

    private void showSuccessDialog(String message){
        new SweetAlertDialog(
                this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Great!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // Going back to login activity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                })
                .setContentText(message)
                .show();

    }

}
