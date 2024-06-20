package com.cs407.madcal;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextWiscId;
    private Button buttonLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            navigateToMainActivity();
            return; // Important to prevent further execution of onCreate
        }

        setContentView(R.layout.activity_login);

        toolbarSetup();
        databaseHelper = new DatabaseHelper(this);
        setupUIViews();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyFromSQLite();
            }
        });
    }

    private void toolbarSetup() {
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupUIViews() {
        editTextWiscId = findViewById(R.id.editTextWiscId);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    private void verifyFromSQLite() {
        String wiscId = editTextWiscId.getText().toString().trim();

        if (wiscId.isEmpty() || wiscId.length() < 1) {
            new android.app.AlertDialog.Builder(this)
                    .setMessage("You must enter an id.")
                    .setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        if (databaseHelper.checkUser(wiscId)) {
            onSuccessfulLogin(wiscId);
        } else {
            showAlertDialog(wiscId);
        }
    }

    public void onSuccessfulLogin(String wiscId) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("WISC_ID", wiscId);
        editor.apply();

        navigateToMainActivity();
    }

    private void showAlertDialog(final String wiscId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("WISC ID not found. Would you like to create a new one?")
                .setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        createUser(wiscId);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createUser(String wiscId) {
        databaseHelper.addUser(wiscId);
        // Optionally, proceed to the main activity or stay on the login page
        // For example, you could call onSuccessfulLogin(wiscId) here to auto-login
        onSuccessfulLogin(wiscId);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void emptyInputEditText() {
        editTextWiscId.setText(null);
    }
}
