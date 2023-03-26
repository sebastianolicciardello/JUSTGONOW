package com.sebastiano.licciardello.justgonowtoadventure.adapters;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.sebastiano.licciardello.justgonowtoadventure.Item;
import com.sebastiano.licciardello.justgonowtoadventure.R;
import com.sebastiano.licciardello.justgonowtoadventure.SuperItemActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
/***************************************************************************************************
 Estende ItemAdapter, è identico cambia solo che avvia la SuperItemActivity al posto del ItemActivity
 **************************************************************************************************/
public class SuperItemAdapter extends ItemAdapter {
    public SuperItemAdapter(Context context, ArrayList<Item> itemArrayList, String userId) {
        super(context, itemArrayList, userId);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
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
            Intent intent = new Intent(context, SuperItemActivity.class);
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
}
