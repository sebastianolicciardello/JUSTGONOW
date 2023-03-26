package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity è quella d'avvio (settata nel file AndroidManifest.xml).

 Controlla se l'utente è loggato o meno:
 * se è loggato, recuperiamo le sue informazioni e vediamo se è un supeuser:
 * se lo è andiamo nella home relativa agli admins
 * se non lo è andiamo nella home "normale"
 * se NON è loggato andiamo nella welcome.
 * verifichiamo inoltre se l'utente si è registrato ma non ha ancora verificato l'email
 **************************************************************************************************/

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    // dichiariamo un oggetto della classe astratta 'FirebaseAuth',
    // rappresenta il punto di accesso all'SDK di autenticazione Firebase
    private FirebaseAuth mAuth;

    // dichiariamo un oggetto della classe astratta 'FirebaseDatabase',
    // rappresenta il punto di accesso al database connesso all'app
    private FirebaseDatabase db;

    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(() -> {

            //controllo se è presente una connessione di rete
            if(CheckNetwork.isInternetAvailable(SplashActivity.this)){
                Utility.showToast(SplashActivity.this, getString(R.string.no_network));
            }

            // otteniamo le istanze sia del database che dell'SDK relativo all'autenticazione
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance();

            // se l'utente è loggato ed ha verificato l'email
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                db.getReference("users") //accediamo alla "collection" degli utenti
                        .child(mAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()  && snapshot.getValue() != null) {
                                    loggedUser = snapshot.getValue(User.class);

                                    //se l'utente è loggato
                                    if (loggedUser != null && loggedUser.isSuperuser()) //se è superuser apro la SuperHome
                                        startActivity(new Intent(SplashActivity.this, SuperHomeActivity.class));
                                    else //altrimenti apre la Home
                                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Utility.showToast(SplashActivity.this, error.getMessage());
                            }
                        });
            }else if (mAuth.getCurrentUser() != null) {
                // se l'utente è loggato ma non ha verificato l'email, mando al welcome e mostro messaggio
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                Utility.showToast(SplashActivity.this, getString(R.string.no_verified_email));
                finish();
            }else {
                // l'utente deve loggarsi o registrarsi, quindi mostro il welcome
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                finish();
            }
        }, 100); //ritardo di 100 millisecondi
    }
}