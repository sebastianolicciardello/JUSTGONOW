package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity è quella relativa alle impostazioni

 Permette il Logout
 **************************************************************************************************/

public class SuperSettingsActivity extends AppCompatActivity {

    // otteniamo le istanze relative a Firebase
    private FirebaseAuth mAuth;

    // elementi del file xml
    private Button logoutBtn;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_settings);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(SuperSettingsActivity.this)){
            Utility.showToast(SuperSettingsActivity.this, getString(R.string.no_network));
        }

        //barra di navigazione
        bottomNavigationView = findViewById(R.id.bottom_navigation_super_settings);
        bottomNavigationView.getMenu().getItem(2).setChecked(true);

        // salvataggio dei riferimenti dei relativi componenti xml
        logoutBtn = findViewById(R.id.logout_btn);

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
           startActivity(new Intent(SuperSettingsActivity.this, WelcomeActivity.class));
           finish();
       });

        //gestisco la navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Trails:
                    startActivity(new Intent(SuperSettingsActivity.this, SuperHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.AddTrail:
                    startActivity(new Intent(SuperSettingsActivity.this, AddItemActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Settings:
                    break;
            }
            return false;
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0); //rimuove animazione
    }
}