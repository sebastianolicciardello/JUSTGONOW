package com.sebastiano.licciardello.justgonowtoadventure;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

import java.util.Objects;

/***************************************************************************************************
 Quest'activity è dedicata alla registrazione di un nuovo utente.
 Riceve in input: email e password (e conferma password) di un nuovo utente, verifica che i campi
 non siano vuoti, che abbiano contenuto valido e solo alla fine avvia il processo di registrazione
 con email e password grazie alla funzione apposita di FireBase, dopodichè andiamo a recuperare l'id
 univoco dell'utente corrente (appena creato) e si va a inserire un nuovo utente nella "collection"
 users, se tutto va bene, si rimanda l'utente all'Activity di login. Oltre ai controlli sull’input
 vengono fatti i controlli sul database, nel caso l’email sia già registrata o ci sono problemi di
 rete.

 **************************************************************************************************/

public class RegisterActivity extends AppCompatActivity {
    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    // elementi del file xml
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button createAccountBtn;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(RegisterActivity.this)){
            Utility.showToast(RegisterActivity.this, getString(R.string.no_network));
        }

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // salvataggio dei riferimenti dei relativi componenti xml
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        createAccountBtn = findViewById(R.id.create_account_btn);
        progressBar = findViewById(R.id.progress_bar);
        TextView loginBtnTextView = findViewById(R.id.login_text_view_btn);

        //pulsante di login
        loginBtnTextView.setOnClickListener(v-> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //imposto il listener del pulsante crea account
        createAccountBtn.setOnClickListener(this::registerUser);
    }

    private void registerUser(View v){

        //conservo le stringhe
        String email = this.emailEditText.getText().toString().trim();
        String password = this.passwordEditText.getText().toString().trim();
        String confirmPassword = this.confirmPasswordEditText.getText().toString().trim();

        // client validations

        // controlliamo che il campo "email" non sia vuoto
        if (email.isEmpty()) {
            this.emailEditText.setError(getString(R.string.email_empty));
            this.emailEditText.requestFocus();
            return;
        }

        // controlliamo che l'email sia valida
        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.emailEditText.setError(getString(R.string.email_error_validate));
            this.emailEditText.requestFocus();
            return;
        }

        // controlliamo che il campo "password" non sia vuoto
        if (password.isEmpty()) {
            this.passwordEditText.setError(getString(R.string.password_empty));
            this.passwordEditText.requestFocus();
            return;
        }

        // controlliamo che la password sia lunga almeno 8 caratteri
        if (password.length() < 8 ) {
            this.passwordEditText.setError(getString(R.string.password_error_validate));
            this.passwordEditText.requestFocus();
            return;
        }

        // controlliamo che il campo "conferma password" non sia vuoto
        if (confirmPassword.isEmpty()) {
            this.confirmPasswordEditText.setError(getString(R.string.password_empty));
            this.confirmPasswordEditText.requestFocus();
            return;
        }


        //verifica che le due password corrispondono
        if (!password.equals(confirmPassword)){
            this.confirmPasswordEditText.setError(getString(R.string.confirm_password_error_validate));
            return;
        }

        //mostro la barra di caricamento
        changeInProgress(true);

        //procedo all'inserimento dell'account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // se la registrazione è andata a buon fine, creiamo una nuova istanza della
                        // classe User e la inseriamo nella collection "users" creando un "child" avente
                        // Uid generato alla registrazione da Firebase.
                        User newUser = new User(email, false);

                        if (mAuth.getCurrentUser() != null)
                            db.getReference("users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(newUser).addOnCompleteListener(task1 -> {
                                        changeInProgress(false);
                                        if (task1.isSuccessful()) {
                                            // se l'utente è stato anche salvato, comunichiamo che è tutto ok
                                            Utility.showToast(RegisterActivity.this,
                                                    getString(R.string.success_register));
                                            // mandiamo la mail di verifica
                                            mAuth.getCurrentUser().sendEmailVerification();
                                            // avviamo login activity
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                        else
                                            Utility.showToast(RegisterActivity.this, getString(R.string.error_account_database));
                                    });
                    }
                    else {
                        //fallimento, fa il check degli account, se già è presente l'email utilizzata, oppure vede se non è presente una connessione ad internet, o errore generico
                        //Ho dovuto tradurre le stringhe di errore in quanto firebase le mostra solo in inglese, nonostante l'utilizzo di getLocalizedMessage
                        if (String.valueOf(Objects.requireNonNull(task.getException()).getLocalizedMessage()).equals("The email address is already in use by another account.")){
                            Utility.showToast(RegisterActivity.this, getString(R.string.email_already_in_use));
                        }else if (String.valueOf(task.getException().getLocalizedMessage()).equals("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")){
                            Utility.showToast(RegisterActivity.this, getString(R.string.no_network));
                        }else
                            Utility.showToast(RegisterActivity.this, task.getException().getLocalizedMessage());
                        changeInProgress(false);
                    }
                });



    }

    //torna a welcome
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
    }

    //mostra barra di caricamento durante il caricamento dei dati di registrazione, nascondendo il tasto di creazione account
    void changeInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            createAccountBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            createAccountBtn.setVisibility(View.VISIBLE);
        }
    }
}