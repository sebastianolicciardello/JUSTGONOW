package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity permette la reimpostazione della password di un utente.

 Riceve in input l'email, verifica che questa mail sia valida e vi sia un account
 associato, e, se così fosse, invia un link all'indirizzo inserito che consente la reimpostazione
 **************************************************************************************************/

public class ResetPasswordActivity extends AppCompatActivity {
    // elementi del file xml
    private EditText email;
    private Button resetButton;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User user;
    // ci servirà per cercare l'email fra gli utenti iscritti
    private Boolean isRegistered;

    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseDatabase db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(ResetPasswordActivity.this)){
            Utility.showToast(ResetPasswordActivity.this, getString(R.string.no_network));
        }

        // salvataggio dei riferimenti dei relativi componenti xml
        email = findViewById(R.id.email_edit_text);
        resetButton = findViewById(R.id.reset_btn);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // settiamo il listener
        resetButton.setOnClickListener(this::onClickResetPassword);
    }

    private void onClickResetPassword(View view) {
        // recuperiamo la stringa inserita dall'utente
        String email = this.email.getText().toString().trim();

        // controlliamo che abbia inserito qualcosa
        if (email.isEmpty()) {
            this.email.setError(getString(R.string.email_empty));
            this.email.requestFocus();
            return;
        }

        // controlliamo che abbia inserito qualcosa di valido
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError(getString(R.string.email_error_validate));
            this.email.requestFocus();
            return;
        }

        // a questo punto vediamo se a quest'email è associato un account vero
        db.getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            for (DataSnapshot userSnaphot: snapshot.getChildren()) {
                                user = userSnaphot.getValue(User.class);
                                isRegistered = false;
                                assert user != null;
                                if (user.getEmail().equals(email)) {
                                    isRegistered = true;
                                    break;
                                }
                            }
                            // se non è stato trovato nessuno con questa email, lanciamo un avviso
                            if (!isRegistered)
                                Utility.showToast(ResetPasswordActivity.this,
                                        getString(R.string.email_no_exist));
                            else {
                                // altrimenti mandiamo la mail di reset
                                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                                    Utility.showToast(ResetPasswordActivity.this,
                                            getString(R.string.reset_password_sent));
                                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(ResetPasswordActivity.this, error.getMessage());
                    }
                });
    }
}