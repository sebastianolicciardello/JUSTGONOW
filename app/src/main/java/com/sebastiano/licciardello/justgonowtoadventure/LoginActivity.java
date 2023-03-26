package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

import java.util.Objects;

/***************************************************************************************************
 Riceve in input due stringhe, email e password, con cui (se valide) l'utente accede
 all'app. Inoltre presenta la possibilità di essere rimandati sia al form di registrazione che
 alla schermata di reset della password. Oltre ai controlli sull’input vengono fatti i controlli
 sul database, nel caso l’email non sia registrata o ci sono problemi di rete o la password è errata
 **************************************************************************************************/

public class LoginActivity extends AppCompatActivity {
    // oggetti che rappresentano i punti di accesso a: database Firebase e autenticazione Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    // elementi del file xml
    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView registerBtnTextView, resetPassword;

    // oggetto della classe User che useremo per reperire informazioni durante le queries
    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(LoginActivity.this)){
            Utility.showToast(LoginActivity.this, getString(R.string.no_network));
        }


        // salvataggio dei riferimenti dei relativi componenti xml
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);
        registerBtnTextView = findViewById(R.id.register_text_view_btn);
        resetPassword = findViewById(R.id.reset_password_text_view_btn);

        // otteniamo le istanze relative a Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // settiamo i listeners
        registerBtnTextView.setOnClickListener(this::onClick);
        loginBtn.setOnClickListener(this::onClick);
        resetPassword.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_text_view_btn:
                // se l'utente clicca su "registati" lo rimandiamo all'Activity di registrazione
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.login_btn:
                // se l'utente clicca su "login" facciamo una serie di operazioni definite in "loginUser"
                loginUser();
                break;
            case R.id.reset_password_text_view_btn:
                // se l'utente clicca su "reset password", lo rimandiamo all'activity dedicata
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
        }
    }

    //torna a welcome
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
    }


    // funzione invocata quando l'utente clicca "login"
    private void loginUser(){
        // recupero i valori inseriti e li verifico
        String email = this.emailEditText.getText().toString().trim();
        String password = this.passwordEditText.getText().toString().trim();

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

        //mostro la barra di caricamento
        changeInProgress(true);

        //procedo al login
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // se il login è andato a buon fine
                        redirectUserOrSuperuserToHome();
                    }
                    else {
                        changeInProgress(false);
                        //login fallito
                        //fa il check degli account, indica se non è presente una connessione ad internet o errore generico, controlla email e password e specifica l'errore
                        //Ho dovuto tradurre le stringhe di errore in quanto firebase le mostra solo in inglese, nonostante l'utilizzo di getLocalizedMessage
                        if (Objects.equals(Objects.requireNonNull(task.getException()).getLocalizedMessage(), "There is no user record corresponding to this identifier. The user may have been deleted.")){
                            Utility.showToast(LoginActivity.this, getString(R.string.email_no_exist));
                        }else if (String.valueOf(task.getException().getLocalizedMessage()).equals("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")){
                            Utility.showToast(LoginActivity.this, getString(R.string.no_network));
                        }else if (String.valueOf(task.getException().getLocalizedMessage()).equals("The password is invalid or the user does not have a password.")) {
                            Utility.showToast(LoginActivity.this, getString(R.string.password_invalid));
                        }else
                            Utility.showToast(LoginActivity.this, task.getException().getLocalizedMessage());
                    }
                });

    }

    private void redirectUserOrSuperuserToHome() {
        //controllo se l'email è verificata
        if(!Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()){
            Utility.showToast(LoginActivity.this, getString(R.string.no_verified_email));
            changeInProgress(false);
            return;
        }
        // recupero i dati dell'utente loggato
        db.getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()  && snapshot.getValue() != null) {
                            // rendiamo invisibile la progress bar
                            changeInProgress(false);
                            loggedUser = snapshot.getValue(User.class);
                            if (loggedUser != null && loggedUser.isSuperuser()){ //se è superuser apro la SuperHome
                                startActivity(new Intent(LoginActivity.this, SuperHomeActivity.class));
                            }
                            else{ //altrimenti apre la Home
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            }
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(LoginActivity.this, error.getMessage());
                    }
                });
    }

    //mostra barra di caricamento durante il caricamento dei dati di login, nascondendo il tasto di login
    void changeInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }


}