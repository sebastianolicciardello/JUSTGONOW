package com.sebastiano.licciardello.justgonowtoadventure;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
 Quest'activity è quella relativa alla Home di un utente "normale" e non superuser/admin.

 Mostra la lista dei sentieri reperiti dalla collection "items" nel database all'interno di un RecyclerView.
 Per ogni sentiero viene mostrato il nome e il prezzo dell'escursione guidata. Ad ogni click sull' oggetto
 della recyclerview si rimanda all'ItemActivity.
 **************************************************************************************************/
public class HomeActivity extends AppCompatActivity {

    // otteniamo le istanze relative a Firebase
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference reference = db.getReference("items");
    final String userId = (Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

    // elementi del file xml
    private BottomNavigationView bottomNavigationView;

    // elementi del recyclerview
    private ItemAdapter itemAdapter; // Estrae i dati dei sentieri dal database
    private ArrayList<Item> arrayList;
    private LinearLayoutManager layoutManager;
    private RecyclerView contents;
    private int positionIndex;
    private int topView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(HomeActivity.this)){
            Utility.showToast(HomeActivity.this, getString(R.string.no_network));
        }

        //barra di navigazione
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        //Per la RecyclerView
        contents = findViewById(R.id.contentsRecyclerView);
        contents.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        contents.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, arrayList, userId);
        getData(); //aggiorna i dati della recycler

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


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onStart() {
        super.onStart();

        //gestisco la navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Trails:
                    break;
                case R.id.Favorites:
                    startActivity(new Intent(HomeActivity.this, FavoritesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Settings:
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
            }
            return false;
        });
        getData();

    }


    private void getData(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for ( DataSnapshot dataSnapshot : snapshot.getChildren() ) {
                    Item item = dataSnapshot.getValue(Item.class);
                    arrayList.add(item); //riempio l'array di items con i nuovi dati
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //esce dall'applicazione
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
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