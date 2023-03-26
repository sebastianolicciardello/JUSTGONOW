package com.sebastiano.licciardello.justgonowtoadventure;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

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
 - Pulsante aggiungi ai preferiti, si riempie l’icona e aggiunge ai preferiti il sentiero
 - Pulsante recensione: apre un dialog con la possibilità di dare un voto al sentiero,
    riempie l’icona del pulsante.
 **************************************************************************************************/

public class ItemActivity extends AppCompatActivity {

    // id del sentiero passato tramime l'intent dalla Home
    private String itemId;

    private TextView ratingText;
    private TextView reviewsNumber;
    private FloatingActionButton vote, favorite;

    // da utilizzare nelle funzioni
    private int userVote = 0;
    private int position = 0; //utilizzati dal dialog per votare
    private boolean voted = false;
    private String stringTitle;
    private String stringButton;

    //url di info e mapy
    private String infoUrl, mapyUrl;

    // componenti del file xml (statici)
    private FloatingActionButton information, mapy;

    //firebase
    protected final FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
    protected final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    protected final DatabaseReference reference = rootNode.getReference("items");
    final String userId = (Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
    final String userEmail = (Objects.requireNonNull(mAuth.getCurrentUser().getEmail()));

    // dialog per votare
    MaterialAlertDialogBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //controllo se è presente una connessione di rete
        if (CheckNetwork.isInternetAvailable(ItemActivity.this)) {
            Utility.showToast(ItemActivity.this, getString(R.string.no_network));
        }

        // prendiamo i riferimenti degli oggetti xml
        //componenti del file xml (dinamici)
        ImageView image = findViewById(R.id.ImageViewItem);
        information = findViewById(R.id.infoIcon);
        mapy = findViewById(R.id.mapyIcon);
        TextView priceValue = findViewById(R.id.priceValue);
        TextView description = findViewById(R.id.description);
        favorite = findViewById(R.id.favoriteButton);
        reviewsNumber = findViewById(R.id.reviewsNumber);
        vote = findViewById(R.id.voteButton);
        TextView name = findViewById(R.id.trail_name_text);
        ratingText = findViewById(R.id.rating);

        //preparo il dialog per votare
        builder = new MaterialAlertDialogBuilder(this);
        stringTitle = getString(R.string.add_review);
        stringButton = getString(R.string.add);


        //carico i dati da mostrare
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
        if (counter == 1) {
            reviewsNumber.setText(getString(R.string.reviews_number_one, counter));
        } else
            reviewsNumber.setText(getString(R.string.reviews_number, counter));

        // immagine
        Glide.with(ItemActivity.this)
                .load(imageURL)
                .into(image);
        // prezzo
        priceValue.setText(getString(R.string.price_value, price));
        // descrizione
        description.setText(descriptionS);
        //nome
        name.setText(nameS);


        // ci agganciamo all'oggetto nella collection "items" grazie all'Uid
        DatabaseReference itemReference = reference.child(itemId);
        itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    // review (se l'ha già messa)
                    HashMap<String, Float> reviews = Objects.requireNonNull(snapshot.getValue(Item.class)).getReviews();
                    if (reviews != null) { // se il sentiero ha almeno una review
                        for (String key : reviews.keySet()) {
                            // cicliamo le chiavi (che sono gli id degli utenti)
                            if (key.equals(userId)) {
                                // se troviamo tra le chiavi quella dell'utente loggato,
                                // allora l'utente ha già messo una review
                                userVote = Objects.requireNonNull(reviews.get(key)).intValue(); //conservo il valore
                                vote.setImageResource(R.drawable.ic_baseline_star_rate_24); //cambio icona
                                voted = true;

                                //Imposto la posizione del voto nel dialog
                                switch (userVote) {
                                    case 5:
                                        position = 0;
                                        break;
                                    case 4:
                                        position = 1;
                                        break;
                                    case 3:
                                        position = 2;
                                        break;
                                    case 2:
                                        position = 3;
                                        break;
                                    case 1:
                                        position = 4;
                                        break;
                                }
                                break;
                            }


                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utility.showToast(ItemActivity.this, error.getMessage());
            }

        });


        //controllo se è già nei preferiti
        itemReference.child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    // se l'item ha una favorites list con qualcosa dentro, allora controlliamo se ha una recensione dell'utente loggato
                    if (snapshot.hasChild(userId)) {
                        // ha già il sentiero nella lista dei preferiti
                        // settiamo l'immagine piena
                        favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utility.showToast(ItemActivity.this, error.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //LISTENERS

        mapy.setOnClickListener(this::onClick);
        information.setOnClickListener(this::onClick);
        vote.setOnClickListener(this::onClick);
        favorite.setOnClickListener(this::onClick);

    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.infoIcon:
                Intent iInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(infoUrl));
                startActivity(Intent.createChooser(iInfo, getString(R.string.open_url_with)));
                break;
            case R.id.mapyIcon:
                Intent iMapy = new Intent(Intent.ACTION_VIEW, Uri.parse(mapyUrl));
                startActivity(Intent.createChooser(iMapy, getString(R.string.open_url_with)));
                break;
            case R.id.voteButton:
                //carico la lista di opzioni
                String[] list;
                if (voted) {
                    list = getResources().getStringArray(R.array.votes_again);
                    stringTitle = getString(R.string.edit_reviews);
                    stringButton = getString(R.string.edit);

                } else {
                    list = getResources().getStringArray(R.array.votes);
                }
                //mostro il dialog per votare
                builder.setTitle(stringTitle) //imposto come checked la recensione che avevo già dato
                        .setSingleChoiceItems(list, position, (dialogInterface, i) -> {
                            position = i;
                            //imposto il voto in base all'elemento scelto
                            switch (position) {
                                case 0:
                                    userVote = 5;
                                    break;
                                case 1:
                                    userVote = 4;
                                    break;
                                case 2:
                                    userVote = 3;
                                    break;
                                case 3:
                                    userVote = 2;
                                    break;
                                case 4:
                                    userVote = 1;
                                    break;
                                case 5:
                                    userVote = -1;
                                    break;
                            }
                        })
                        .setPositiveButton(stringButton, (dialogInterface, i) -> { //pressione del tasto conferma voto
                            if (userVote == 0) {
                                userVote = 5; //è il caso in cui non esistono recensioni, l'utente preme su vota e lascia 5 stelle come opzione senza premere altro.
                            }

                            if (userVote == -1) { //elimino la recensione
                                reference.child(itemId).child("reviews")
                                        .child(userId).removeValue().addOnCompleteListener(task -> {
                                            // settiamo l'immagine NON piena
                                            vote.setImageResource(R.drawable.ic_baseline_star_border_24);
                                            onVoteUpdateRatings(); //aggiorna la scritta delle recensioni
                                            Utility.showToast(ItemActivity.this, getString(R.string.review_removed));
                                        });
                            } else {
                                // quando l'utente valuta un sentiero, dobbiamo salvare la review nell'oggetto item nel db
                                reference.child(itemId)
                                        .child("reviews")
                                        .child(userId) // creiamo un nuovo "figlio" con l'id dell'utente
                                        .setValue(userVote).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Utility.showToast(ItemActivity.this, getString(R.string.review_added));
                                                onVoteUpdateRatings(); //aggiorna la scritta delle recensioni
                                                voted = true;
                                                vote.setImageResource(R.drawable.ic_baseline_star_rate_24); //cambio icona
                                            }

                                        });
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel()).show(); //tasto annulla
                break;
            case R.id.favoriteButton:

                // quando clicco sul pulsante "preferiti" a prescindere che sia pieno o meno
                // quello che facciamo è accedere al campo "favorites" dell'item cliccato
                reference.child(itemId).child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            // se l'item ha una favorites list con qualcosa dentro, allora controlliamo se ha una recensione dell'utente loggato
                            if (!snapshot.hasChild(userId)) {
                                // se non c'è ancora la recensione con l'id la aggiungiamo
                                snapshot.getRef().child(userId).setValue(userEmail);
                                // settiamo l'immagine piena
                                favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                            } else {
                                // altrimenti, ha già il sentiero nella lista dei preferiti e lo vuole rimuovere
                                snapshot.child(userId).getRef().removeValue();
                                // settiamo l'immagine NON piena
                                favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);

                            }
                        } else {
                            // se la lista dei preferiti è vuota, allora non c'è sicuramente => la aggiungiamo
                            snapshot.getRef().child(userId).setValue(userEmail);
                            // settiamo l'immagine piena
                            favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(ItemActivity.this, error.getMessage());
                    }
                });

                break;
        }
    }


    private void onVoteUpdateRatings() { //per aggiornare la media delle recensioni
        DatabaseReference itemReference = reference.child(itemId);
        itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    HashMap<String, Float> reviews = Objects.requireNonNull(snapshot.getValue(Item.class)).getReviews();
                    int counter = 0;
                    if (reviews != null) { // se il sentiero ha almeno una review
                        ArrayList<Float> reviewsValues = new ArrayList<>(Objects.requireNonNull(snapshot.getValue(Item.class)).getReviews().values());
                        double sum = 0.0;
                        for (Float value : reviewsValues) {
                            sum = sum + value;
                            counter++;
                        }
                        float average = (float) sum / reviewsValues.size();
                        String s = String.format(Locale.getDefault(),"%.2f", average);
                        ratingText.setText(s);
                        if (counter == 1) {
                            reviewsNumber.setText(getString(R.string.reviews_number_one, counter));
                        } else
                            reviewsNumber.setText(getString(R.string.reviews_number, counter));
                    }else {
                        ratingText.setText(getString(R.string.noReview));
                        reviewsNumber.setText("");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}

