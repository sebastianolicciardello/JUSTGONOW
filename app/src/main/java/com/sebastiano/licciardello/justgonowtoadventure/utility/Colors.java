package com.sebastiano.licciardello.justgonowtoadventure.utility;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

/***************************************************************************************************
 Questa classe permette l'implementazione del Material You con Dynamic Colors, sfrutta le API di
 Android 12 e 13 per cambiare colore dell’interfaccia in base allo sfondo e alla palette colori
 impostate dall’utente. Bisogna includerla nel manifest inserendola sopra tutte le activities.
 **************************************************************************************************/

public class Colors extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
