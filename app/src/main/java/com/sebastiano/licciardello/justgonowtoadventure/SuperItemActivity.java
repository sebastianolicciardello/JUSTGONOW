package com.sebastiano.licciardello.justgonowtoadventure;


import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity è quella relativa alla visualizzazione delle informazioni di un singolo sentiero con cui
 è possibile interagire.

 Le informazioni mostrate sono:
 - immagine sentiero
 - nome sentiero
 - descrizione (scroll view)
 - media recensioni degli utenti, con numero di recensioni date
 - prezzo escursione guidata
 - pulsante informazioni aggiuntive (ho solo inserito l’avvio di un video youtube, ma si può inserire qualunque url,)
 - pulsante mappa (permette l’apertura del sentiero tramite il sito o l’app di Mapy ).

 Le interazioni sono:
 - Pulsante di modifica apre Edit Item Activity
 - Pulsante di eliminazione: apre un avviso in cui si chiede la conferma per eliminare il sentiero.

 **************************************************************************************************/

public class SuperItemActivity extends AppCompatActivity {

    // id del sentiero passato tramime l'intent dalla SuperHome
    private String itemId;

    //url di info e mapy
    private String infoUrl, mapyUrl;

    // componenti del file xml (statici)
    private FloatingActionButton information, mapy, edit, delete;

    //firebase
    protected final FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
    protected  final DatabaseReference reference = rootNode.getReference("items");

    // messaggio di avvertimento per l'eliminazione del sentiero
    MaterialAlertDialogBuilder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_item);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(SuperItemActivity.this)){
            Utility.showToast(SuperItemActivity.this, getString(R.string.no_network));
        }

        // prendiamo i riferimenti degli oggetti xml
        // componenti del file xml (dinamici)
        ImageView image = findViewById(R.id.ImageViewItem);
        information = findViewById(R.id.infoIcon);
        mapy = findViewById(R.id.mapyIcon);
        TextView priceValue = findViewById(R.id.priceValue);
        TextView description = findViewById(R.id.description);
        TextView ratingText = findViewById(R.id.rating);
        TextView reviewsNumber = findViewById(R.id.reviewsNumber);
        edit = findViewById(R.id.editButton);
        delete = findViewById(R.id.deleteButton);
        TextView name = findViewById(R.id.trail_name_text);

        //preparo il messaggio di avvertimento per l'eliminazione del sentiero
        builder = new MaterialAlertDialogBuilder(this);

        //conservo i valori presi dalla recyclerview
        String nameS = getIntent().getStringExtra("Name");
        int price = getIntent().getIntExtra("Price", 0);
        String descriptionS = getIntent().getStringExtra("Description");
        String imageURL = getIntent().getStringExtra("ImageURL");
        infoUrl = getIntent().getStringExtra("InformationURL");
        mapyUrl = getIntent().getStringExtra("MapyURL");
        itemId = getIntent().getStringExtra("Key");
        String ratingS = getIntent().getStringExtra("Rating");
        int counter = getIntent().getIntExtra("reviewsNumber", 0);


        //Imposto gli elementi

        //recensioni
        ratingText.setText(ratingS);
        if (counter == 1){
            reviewsNumber.setText(getString(R.string.reviews_number_one, counter));
        }else
            reviewsNumber.setText(getString(R.string.reviews_number, counter));

        // immagine
        Glide.with(SuperItemActivity.this)
                .load(imageURL)
                .into(image);
        // prezzo
        priceValue.setText(getString(R.string.price_value, price));
        // descrizione
        description.setText(descriptionS);
        //nome
        name.setText(nameS);

    }


    @Override
    protected void onStart() {
        super.onStart();

        //listeners
        mapy.setOnClickListener(this::onClick);
        information.setOnClickListener(this::onClick);
        edit.setOnClickListener(this::onClick);
        delete.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch(view.getId()) {
            case R.id.infoIcon:
                Intent iInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl));
                startActivity(Intent.createChooser(iInfo, getString(R.string.open_url_with)));
                break;
            case R.id.mapyIcon:
                Intent iMapy = new Intent(Intent.ACTION_VIEW, Uri.parse(mapyUrl));
                startActivity(Intent.createChooser(iMapy, getString(R.string.open_url_with)));
                break;
            case R.id.editButton:
                startActivity(new Intent(SuperItemActivity.this, EditItemActivity.class).putExtra("Key", itemId));
                break;
            case R.id.deleteButton:
                builder.setTitle(getString(R.string.warning))
                                .setMessage(getString(R.string.warning_message)).setCancelable(true).
                        setPositiveButton(getString(R.string.delete), (dialogInterface, i) -> reference.child(itemId).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Utility.showToast(SuperItemActivity.this, getString(R.string.trail_deleted));
                                        startActivity(new Intent(SuperItemActivity.this, SuperHomeActivity.class));
                                    }
                                }))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel()).show();

                }
    }

}