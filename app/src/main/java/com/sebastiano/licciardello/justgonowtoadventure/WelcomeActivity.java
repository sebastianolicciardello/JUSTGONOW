package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Welcome Activity:  pagina iniziale con i pulsanti di login e sign in, mostra anche un logo e una
 breve descrizione dell’app.

 **************************************************************************************************/

public class WelcomeActivity extends AppCompatActivity {
    Button loginBtn;
    Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(WelcomeActivity.this)){
            Utility.showToast(WelcomeActivity.this, getString(R.string.no_network));
        }

        loginBtn =findViewById(R.id.login_btn);
        signInBtn =findViewById(R.id.sign_in_btn);
        loginBtn.setOnClickListener((v)->startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));
        signInBtn.setOnClickListener((v)->startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class)));
    }

    //esce
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}