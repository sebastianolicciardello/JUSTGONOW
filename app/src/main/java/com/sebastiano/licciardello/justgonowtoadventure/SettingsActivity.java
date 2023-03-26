package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;
/***************************************************************************************************
 Quest'activity è quella relativa alle impostazioni

 Permette il logout e presenta i tasti di contactUs, con action per email e chiamata.
 **************************************************************************************************/

public class SettingsActivity extends AppCompatActivity {

    // otteniamo le istanze relative a Firebase
    private FirebaseAuth mAuth;

    // elementi del file xml
    private Button logoutBtn;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton call, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(SettingsActivity.this)){
            Utility.showToast(SettingsActivity.this, getString(R.string.no_network));
        }

        //barra di navigazione
        bottomNavigationView = findViewById(R.id.bottom_navigation_settings);
        bottomNavigationView.getMenu().getItem(2).setChecked(true);

        // salvataggio dei riferimenti dei relativi componenti xml
        logoutBtn = findViewById(R.id.logout_btn);
        call = findViewById(R.id.callButton);
        email = findViewById(R.id.emailButton);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onStart() {
        super.onStart();

        // settiamo il listener
        logoutBtn.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
            finish();
        });

        //gestisco la navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Trails:
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Favorites:
                    startActivity(new Intent(SettingsActivity.this, FavoritesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Settings:
                    break;
            }
            return false;
        });

        //LISTENERS
        call.setOnClickListener(this::onClick);
        email.setOnClickListener(this::onClick);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0); //rimuove animazione
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.emailButton:
                Intent iEmail = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:justgonowtoadventure@gmail.com"));
                startActivity(Intent.createChooser(iEmail, getString(R.string.open_url_with)));
                break;
            case R.id.callButton:
                checkPermission(Manifest.permission.CALL_PHONE, 0);
                Intent iCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:333333"));
                startActivity(iCall);
                break;
        }
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(SettingsActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[] { permission }, requestCode);
        }
    }
}