package com.sebastiano.licciardello.justgonowtoadventure;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
import com.sebastiano.licciardello.justgonowtoadventure.adapters.SuperItemAdapter;
import com.sebastiano.licciardello.justgonowtoadventure.utility.CheckNetwork;
import com.sebastiano.licciardello.justgonowtoadventure.utility.Utility;

import java.util.ArrayList;
import java.util.Objects;

/***************************************************************************************************
 Quest'activity è quella relativa alla Home di un utente "superuser"

 E' possibile vedere quanti utenti sono iscritti e quanti sentieri sono stati pubblicati.

 E' presente un pulsante che consente l'aggiunta di un sentiero.

 E' presente la RecyclerView contenente i sentieri reperiti dalla collection "items" nel database.
 Al click di ogni oggetto si avvia la relativa SuperItemActivity.
 **************************************************************************************************/

public class SuperHomeActivity extends AppCompatActivity{

    // otteniamo le istanze relative a Firebase
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference reference = db.getReference("items");
    final String userId = (Objects.requireNonNull(mAuth.getCurrentUser()).getUid());


    // elementi del file xml
    private TextView numPeopleValue;
    private TextView numTrailsValue;
    private BottomNavigationView bottomNavigationView;

    // elementi del recyclerview
    private SuperItemAdapter itemAdapter; // Estrae i dati dei sentieri dal database
    private ArrayList<Item> arrayList;
    private LinearLayoutManager layoutManager;
    private RecyclerView contents;
    private int positionIndex;
    private int topView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_home);

        //controllo se è presente una connessione di rete
        if(CheckNetwork.isInternetAvailable(SuperHomeActivity.this)){
            Utility.showToast(SuperHomeActivity.this, getString(R.string.no_network));
        }

        // salvataggio dei riferimenti dei relativi componenti xml
        numPeopleValue = findViewById(R.id.numPeopleValue);
        numTrailsValue = findViewById(R.id.numTrailsValue);

        //barra di navigazione
        bottomNavigationView = findViewById(R.id.bottom_navigation_super);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        //Per la RecyclerView
        contents = findViewById(R.id.adminItemsRecyclerView);
        contents.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        contents.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();
        itemAdapter = new SuperItemAdapter(this, arrayList, userId);
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

        // riempiamo il contatore utenti registrati
        db.getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            numPeopleValue.setText(String.valueOf(snapshot.getChildrenCount()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(SuperHomeActivity.this,
                                error.getMessage());
                    }
                });

        //// riempiamo il contatore sentieri pubblicati
        db.getReference("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            numTrailsValue.setText(String.valueOf(snapshot.getChildrenCount()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utility.showToast(SuperHomeActivity.this,
                                error.getMessage());
                    }
                });

        //gestisco la navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.Trails:
                    break;
                case R.id.AddTrail:
                    startActivity(new Intent(SuperHomeActivity.this, AddItemActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
                case R.id.Settings:
                    startActivity(new Intent(SuperHomeActivity.this, SuperSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    break;
            }
            return false;
        });
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
