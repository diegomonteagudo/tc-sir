package com.insatc.sir_scanner.networktools;

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
     * passer en RRC Connected.
     * Gère aussi l'apparence du bouton. C'est donc celle à appeler pour le traffic initié par
     * l'utilisateur via un bouton
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
                    requeteHttp();
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

    /**
     * Fonction qui créée un thread qui génère du traffic en fond (simples requêtes HTTP) pour
     * passer en RRC Connected.
     * Aucun élément UI. C'est donc à utiliser pour le traffic généré automatiquement,
     * sans intervention de l'utilisateur.
     * @param nbRequetes Nombre de requêtes à faire
     * @param attenteEnMs Attente en milisecondes entre chaque début de requête
     */
    public static void generateTraffic(int nbRequetes, int attenteEnMs) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                //nombre de requêtes HTTP à faire
                for (int i = 0; i < nbRequetes; i++) {
                    requeteHttp();
                    try{
                        Thread.sleep(attenteEnMs);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        });

        thread.start();
    }

    /**
     * Faire 1 requête HTTP quelconque
     */
    public static void requeteHttp(){
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
    }
}
