package com.valerio.kevin.syncmytodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    public static final String USER = "USER";
    public static final String PASS = "PASS";
    public static final String SERVER = "SERVER";
    public static final String NOTIF = "NOTIF";
    public static final String AUTODOWNLOAD = "AUTODOWNLOAD";
    public static final String TUDOURL = "TUDOURL";
    TextView txtServer = null;
    TextView txtUser = null;
    TextView txtTodoURL = null;
    TextView txtPass = null;
    CheckBox checkBox = null;
    CheckBox auto_download = null;
    Button btnSave = null;
    SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnSave = findViewById(R.id.submitModif);
        txtServer = findViewById(R.id.ftphostname);
        txtUser = findViewById(R.id.ftpusername);
        txtTodoURL = findViewById(R.id.todoLink);
        txtPass = findViewById(R.id.ftppassword);
        checkBox = findViewById(R.id.notifdownload);
        auto_download = findViewById(R.id.auto_download);
        preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    edit.putBoolean(NOTIF, false);
                } else {
                    edit.putBoolean(NOTIF, true);
                }
            }
        });

        auto_download.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    edit.putBoolean(AUTODOWNLOAD, true);
                } else {
                    edit.putBoolean(AUTODOWNLOAD, false);
                }
            }
        });
        btnSave.setOnClickListener(view -> {

            edit
                    .putString(USER, String.valueOf(txtUser.getText()))
                    .putString(TUDOURL, String.valueOf(txtTodoURL.getText()))
                    .putString(PASS, String.valueOf(txtPass.getText()))
                    .putString(SERVER, String.valueOf(txtServer.getText()))
                    .apply();

            //On revient à l'accueil
            Toast.makeText(SettingsActivity.this, "Paramètres enregistrés", Toast.LENGTH_SHORT).show();
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);

        });

        loadUIPreferences();
    }

    private void loadUIPreferences() {

        txtServer.setText(preferences.getString(SERVER, ""));
        txtPass.setText(preferences.getString(PASS, ""));
        txtTodoURL.setText(preferences.getString(TUDOURL, ""));
        txtUser.setText(preferences.getString(USER, ""));
        checkBox.setChecked(!preferences.getBoolean(NOTIF, false));
        auto_download.setChecked(preferences.getBoolean(AUTODOWNLOAD, true));

    }

}
