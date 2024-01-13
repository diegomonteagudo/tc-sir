package com.insatc.sir_scanner.network;

import android.content.Context;
import android.widget.Button;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InternetTraffic {
    /**
     * Fonction qui créée un thread qui génère du traffic en fond (simples requêtes HTTP) pour
     * passer en RRC Connected. Attention cela gère aussi l'apparence du bouton...
     * @param context l'activité contenant le bouton qui lance cette fonction
     * @param button le bouton qui lance cette fonction
     */
    public static void generateTraffic(Context context, Button button) {
        button.setEnabled(false);
        String originalText = button.getText().toString();
        button.setText("Traffic en cours...");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                //nombre de requêtes HTTP à faire, à rendre personnalisable par la suite
                for (int i = 0; i < 5; i++) {
                    try {
                        //URL quelconque
                        URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/dog");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        in.close();
                        urlConnection.disconnect();
                        System.out.println("HTTP fait");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println(e.getClass().toString());
                    }
                    try{
                        //attendre 1 seconde entre chaque requête... Peut-être améliorable, plus propre
                        Thread.sleep(1000);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

                //à la fin du traffic, on récupère le thread UI pour réactiver le bouton
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        button.setEnabled(true);
                        button.setText(originalText);
                    }
                });

            }
        });

        thread.start();
    }
}
