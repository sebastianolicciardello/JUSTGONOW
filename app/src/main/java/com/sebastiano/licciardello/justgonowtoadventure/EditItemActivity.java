package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

/***************************************************************************************************
 Quest'activity è quella relativa alla modifica di un oggetto dalla collection "items"

 Quello che fa è mostrare le informazioni correnti del sentiero all'interno delle EditText,
 così da creare un'istanza della classe Item con le nuove informazioni per poi inserirla nel db
 sovrascrivendo quella già esistente in quanto il nuovo nodo avrà lo stesso id di quello precedente
 **************************************************************************************************/

public class EditItemActivity extends AppCompatActivity {

    // componenti del file xml
    private EditText name, price, description, information, image, mapy;
    private Button editButton;
    private ProgressBar progressBar;

    // punto di accesso al database Firebase
    private FirebaseDatabase db;

    // id del sentiero passato tramite l'intent dalla SuperHome
    private String itemId;
    // item, da reperire dal db grazie all'id (stringa) passato dalla precedente activity
    private Item item;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(EditItemActivity.this)){
            Utility.showToast(EditItemActivity.this, getString(R.string.no_network));
        }

        // salvataggio dei riferimenti dei relativi componenti xml

        name = findViewById(R.id.trail_name_text);
        price = findViewById(R.id.trail_price_text);
        information = findViewById(R.id.trail_information_link_text);
        mapy = findViewById(R.id.trail_mapy_link_text);
        image = findViewById(R.id.image_link_text);
        description = findViewById(R.id.trail_description_text);
        editButton = findViewById(R.id.confirm_btn);
        progressBar = findViewById(R.id.progress_bar_editTrail);

        //conservo l'id dell'item
        itemId = getIntent().getStringExtra("Key");

        // otteniamo l'istanza del db
        db = FirebaseDatabase.getInstance();

        // accediamo alle informazioni del sentiero mostrato e le inseriamo nei campi editabili
        db.getReference("items").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            item = snapshot.getValue(Item.class);
                            if (item != null) {
                                name.setText(item.getName());
                                price.setText(item.getPrice().toString());
                                information.setText(item.getInformation());
                                mapy.setText(item.getMapy());
                                image.setText(item.getImage());
                                description.setText(item.getDescription());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(EditItemActivity.this, error.getMessage());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //setto il listener
        editButton.setOnClickListener(view -> editItemInDatabase());
    }

    private void editItemInDatabase() {
        //conservo le stringhe modificate
        String name = this.name.getText().toString().trim();
        String description = this.description.getText().toString().trim();
        String informationURL = this.information.getText().toString().trim();
        String imageURL = this.image.getText().toString().trim();
        String mapyURL = this.mapy.getText().toString().trim();
        Integer price = Integer.valueOf(this.price.getText().toString().trim());

        //controllo che non siano vuote

        if (name.isEmpty()) {
            this.name.setError(getString(R.string.name_empty));
            this.name.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            this.description.setError(getString(R.string.description_empty));
            this.description.requestFocus();
            return;
        }

        if (imageURL.isEmpty()) {
            this.image.setError(getString(R.string.image_empty));
            this.image.requestFocus();
            return;
        }

        if (informationURL.isEmpty()) {
            this.information.setError(getString(R.string.information_empty));
            this.information.requestFocus();
            return;

        }if (mapyURL.isEmpty()) {
            this.mapy.setError(getString(R.string.mapy_empty));
            this.mapy.requestFocus();
            return;
        }

        if (price.toString().isEmpty()) {
            this.price.setError(getString(R.string.price_empty));
            this.price.requestFocus();
            return;
        }

        //mostro la barra di caricamento
        changeInProgress(true);

        //creo l'item da sovrascrivere a quello precedente
        Item newItem = new Item(name,description,imageURL,informationURL,mapyURL,price, item.getKey());

        //conservo le recensioni e i preferiti inserendole nel nuovo item
        newItem.setReviews(item.getReviews());
        newItem.setFavorites(item.getFavorites());

        //imposto le modifiche nel database
        db.getReference("items").child(itemId).setValue(newItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utility.showToast(EditItemActivity.this, getString(R.string.edit_trail_success));
                    }
                });

        changeInProgress(false); //ritorno alla superhome chiudendo l'activity di modifica item
        startActivity(new Intent(EditItemActivity.this, SuperHomeActivity.class));
        finish();
    }

    //mostra barra di caricamento durante il caricamento dei dati, nascondendo il tasto di conferma
    void changeInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        }
    }


}