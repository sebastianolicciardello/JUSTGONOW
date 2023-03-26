package com.sebastiano.licciardello.justgonowtoadventure.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sebastiano.licciardello.justgonowtoadventure.Item;
import com.sebastiano.licciardello.justgonowtoadventure.ItemActivity;
import com.sebastiano.licciardello.justgonowtoadventure.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/***************************************************************************************************
 Utilizzo di Recyclerview, una versione flessibile ed efficiente di ListView. È un contenitore
 per il rendering di set di dati di viste più grandi che possono essere riciclati e fatti scorrere
 in modo molto efficiente. Fa utilizzo della classe ItemAdapter con all’interno la classe ItemHolder,
 queste permettono il riciclo corretto dei dati nella view.
 **************************************************************************************************/

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {

    final Context context;
    final ArrayList<Item> itemArrayList;
    final String userId;


    public ItemAdapter(Context context, ArrayList<Item> itemArrayList, String userId){
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position){
        Item item = (Item) itemArrayList.toArray()[position];
        String s = "?"; //dove conservo la stringa della media delle recensioni
        holder.name.setText(item.getName());
        String priceString = context.getString(R.string.price_value, item.getPrice());
        holder.price.setText(priceString);
        // per quanto riguarda le recensioni, prendiamo tutti i valori, ne facciamo una media
        HashMap<String, Float> reviews = item.getReviews();
        int counter = 0;
        if (reviews != null) { // se il sentiero ha almeno una review
            ArrayList<Float> reviewsValues = new ArrayList<>(item.getReviews().values());
            double sum = 0.0;
            for (Float value: reviewsValues) {
                sum = sum + value;
                counter++;
            }
            float average = (float) sum/reviewsValues.size();
            s = String.format(Locale.getDefault(), "%.2f", average);
        }

        // immagine
        Glide.with(holder.image.getContext()).load(item.getImage()).into(holder.image);

        String finalS = s;
        int finalCounter = counter;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("Name", item.getName());
            intent.putExtra("Price", item.getPrice());
            intent.putExtra("Description", item.getDescription());
            intent.putExtra("ImageURL", item.getImage());
            intent.putExtra("InformationURL", item.getInformation());
            intent.putExtra("MapyURL", item.getMapy());
            intent.putExtra("Rating", finalS);
            intent.putExtra("userId", userId);
            intent.putExtra("reviewsNumber", finalCounter);
            intent.putExtra("Key", item.getKey());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }


    public static class ItemHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final ImageView image;
        public final TextView price;


        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameItem);
            image = itemView.findViewById(R.id.ImageViewItem);
            price = itemView.findViewById(R.id.priceItem);
        }
    }
}
