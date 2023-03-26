package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.firebase.database.FirebaseDatabase;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity è quella relativa all'aggiunta di un nuovo oggetto nella collection "items"

 Quello che fa è prendere in input tutti i valori che vanno associati alle chiavi del modello Item,
 così da creare un'istanza, appunto, della classe Item e salvarla nel database.
 **************************************************************************************************/

public class AddItemActivity extends AppCompatActivity {

    // elementi del file xml
    private EditText nameEt, priceEt, descriptionEt, informationEt, imageEt, mapyEt;
    private Button addButton;
    private ProgressBar progressBar;

    // punto di accesso al database Firebase
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(AddItemActivity.this)){
            Utility.showToast(AddItemActivity.this, getString(R.string.no_network));
        }

        // salvataggio dei riferimenti dei relativi componenti xml
        nameEt = findViewById(R.id.trail_name_text);
        priceEt = findViewById(R.id.trail_price_text);
        informationEt = findViewById(R.id.trail_information_link_text);
        mapyEt = findViewById(R.id.trail_mapy_link_text);
        imageEt = findViewById(R.id.image_link_text);
        descriptionEt = findViewById(R.id.trail_description_text);
        addButton = findViewById(R.id.confirm_btn);
        progressBar = findViewById(R.id.progress_bar_addTrail);

        // otteniamo l'istanza del database
        db = FirebaseDatabase.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //listener
        addButton.setOnClickListener(view -> addNewItemToDatabase());
    }

    private void addNewItemToDatabase() {

        //conservo le stringhe
        String name = this.nameEt.getText().toString().trim();
        String description = this.descriptionEt.getText().toString().trim();
        String information = this.informationEt.getText().toString().trim();
        String imageURL = this.imageEt.getText().toString().trim();
        String mapy = this.mapyEt.getText().toString().trim();
        String price = this.priceEt.getText().toString().trim();
        String itemKey;



        //controlli
        if (name.isEmpty()) {
            this.nameEt.setError(getString(R.string.name_empty));
            this.nameEt.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            this.descriptionEt.setError(getString(R.string.description_empty));
            this.descriptionEt.requestFocus();
            return;
        }

        if (imageURL.isEmpty()) {
            this.imageEt.setError(getString(R.string.image_empty));
            this.imageEt.requestFocus();
            return;
        }

        if (information.isEmpty()) {
            this.informationEt.setError(getString(R.string.information_empty));
            this.informationEt.requestFocus();
            return;

        }if (mapy.isEmpty()) {
            this.mapyEt.setError(getString(R.string.mapy_empty));
            this.mapyEt.requestFocus();
            return;
        }

        if (price.isEmpty()) {
            this.priceEt.setError(getString(R.string.price_empty));
            this.priceEt.requestFocus();
            return;
        }

        //mostra barra di caricamento
        changeInProgress(true);

        //converte in intero la stringa del prezzo
        Integer priceInteger = Integer.valueOf(price);

        //genero una key e la conservo
        itemKey = db.getReference("items").push().getKey();

        //creo il nuovo item
        Item newItem = new Item(name,description,imageURL,information,mapy,priceInteger, itemKey);

        //lo inserisco nel database
        assert itemKey != null;
        db.getReference("items")
                .child(itemKey)
                .setValue(newItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        changeInProgress(false);
                        Utility.showToast(AddItemActivity.this, getString(R.string.add_trail_success));
                        startActivity(new Intent(AddItemActivity.this, SuperHomeActivity.class));
                        finish();
                    }
                });
    }

    //mostra barra di caricamento durante il caricamento dei dati, nascondendo il tasto di conferma
    void changeInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0); //rimuove animazione
    }
}