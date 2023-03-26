package com.sebastiano.licciardello.justgonowtoadventure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sebastiano.licciardello.justgonowtoadventure.adapters.ItemAdapter;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

import java.util.ArrayList;
import java.util.Objects;

/***************************************************************************************************
 Quest'activity è quella relativa alla visualizzazione della lista dei preferiti dell'utente.

 Questa è semplicemente la stessa RecyclerView usata nella home ma filtrata in funzione
 dei sentieri che l'utente ha aggiunto.
 **************************************************************************************************/

public class FavoritesActivity extends AppCompatActivity {

    // otteniamo le istanze relative a Firebase
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference reference = db.getReference("items");
    final String userId = (Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

    // elementi del file xml
    private BottomNavigationView bottomNavigationView;
    private ImageView noFavorites;

    // elementi del recyclerview
    private ItemAdapter itemAdapter; // Estrae i dati dei sentieri dal database
    private ArrayList<Item> arrayList;
    private LinearLayoutManager layoutManager;
    private RecyclerView contents;
    private int positionIndex;
    private int topView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(FavoritesActivity.this)){
            Utility.showToast(FavoritesActivity.this, getString(R.string.no_network));
        }

        //barra di navigazione
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        //sfondo lista vuota
        noFavorites = findViewById(R.id.no_favorites);

        //Per la RecyclerView:

        contents = findViewById(R.id.contentsRecyclerView);
        contents.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        contents.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, arrayList, userId);
        getData();

        contents.setAdapter(itemAdapter);

    }

    @Override
    protected void onResume(){
        super.onResume();
        getData();
        //riprendo la posizione della recyclerview
        if (positionIndex!=-1){
            layoutManager.scrollToPositionWithOffset(positionIndex, topView);
        }
    }

    private void getData(){

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                boolean empty = true; //per impostare la visibilità dell'icona di mancanza di preferiti
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (dataSnapshot.child("favorites").child(userId).getValue()!=null){
                        Item item = dataSnapshot.getValue(Item.class);
                        arrayList.add(item); //riempio l'array di items con i nuovi dati
                        empty = false;

                    }
                }
                if(empty){ //mostra l'immagine di lista vuota
                    noFavorites.setVisibility(View.VISIBLE);
                }

                itemAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onStart() {
        super.onStart();

        //gestisco la navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Trails:
                    startActivity(new Intent(FavoritesActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Favorites:
                    break;
                case R.id.Settings:
                    startActivity(new Intent(FavoritesActivity.this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //conservo la posizione della recyclerview
        positionIndex = layoutManager.findFirstCompletelyVisibleItemPosition();
        View startView = contents.getChildAt(0);
        topView = (startView == null) ? 0 : (startView.getTop() - contents.getPaddingTop());
    }

}