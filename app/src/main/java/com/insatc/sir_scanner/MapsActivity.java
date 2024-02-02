package com.insatc.sir_scanner;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Map;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;

import android.Manifest;
import android.widget.Toast;

public class MapsActivity extends AppCompatActivity {

    public String TAG = "MapsActivity";
    private String csvPath;

    private ImageButton btnFullscreen;
    private CustomMapView map;
    private TextView legend;
    private LinearLayout reportLayout;
    private TextView genInfoTextView;
    private TextView cellAnalysisTextView;
    private PieChart cellPieChart;
    private Button btnSaveCsv;
    private Button btnSaveJson;

    private List<String[]>  csvToSave; //nécessaire pour saveCsv()
    private String jsonToSave; //nécessaire pour saveJson()

    private boolean mapIsFullScreen = false;
    private int originalMapWidth;
    private int originalMapHeight;

    //nécessaire pour le pie chart avec sortColors()
    private LinkedList<Integer> colorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, getPreferences(Activity.MODE_PRIVATE));

        setContentView(R.layout.activity_maps);

        /*TRES IMPORTANT : l'activité doit être appelée avec un extra "Path" qui est le chemin
        complet vers le csv*/
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Path")){
            csvPath = intent.getStringExtra("Path");
        } else {
            Log.d("MapsActivity", "ERREUR: AUCUN INTENT");
            //ancien nom de test si pas d'intent
            csvPath = getExternalFilesDir(null) +  "/Documents/csvAntenne.csv";
        }

        map = findViewById(R.id.mapView);
        legend = findViewById(R.id.legendTextView);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        reportLayout = findViewById(R.id.reportSection);
        genInfoTextView = findViewById(R.id.genInfoTextView);
        cellAnalysisTextView = findViewById(R.id.cellAnalysisTextView);
        cellPieChart = findViewById(R.id.cellPieChart);
        btnSaveCsv = findViewById(R.id.btnSaveCsv);
        btnSaveJson = findViewById(R.id.btnSaveJson);

        colorList = new LinkedList<Integer>();

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Centrer la carte sur une position
        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(45.799999 , 4.85); //!!!!Bien mettre le premier POINT !!!!!!!!
        mapController.setCenter(startPoint);


        //CustomPoint pt1 = new CustomPoint("100234223", new GeoPoint(45.799999, 4.85), "14:41 - 22/01/2024");
        //CustomPoint pt2 = new CustomPoint("100234224", new GeoPoint(45.799909 , 4.8509), "14:43 - 22/01/2024");

        // Lire les points depuis le fichier CSV et les ajouter à la carte
        Log.d("csv :",csvPath);
        try{
            CSVReader reader = new CSVReader(new FileReader(csvPath));
            List<String[]> rows = reader.readAll();
            csvToSave = rows;
            //lire le csv et ajouter les markeurs sur la carte
            addMarkersFromCSV(map, rows);
            //lire le csv et générer l'objet RecordingData
            RecordingData recData = generateRecordingDataObject(rows);

            //Récupérer le nom du fichier csv sans le chemin ni l'extension
            String[] pathSplit = csvPath.split("/");
            String csvName = pathSplit[pathSplit.length-1];
            csvName = csvName.split("\\.")[0];

            //générer le JSON et l'affichage
            generateJSON(recData, csvName);
            displayReport(recData);

            //click listener seulement après avoir généré le JSON
            btnSaveJson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!Environment.isExternalStorageManager()) {
                        //tentative de faire fonctionner sous Android 13+
                        Toast.makeText(MapsActivity.this, "Veuillez autoriser à l'application la manipulation de tous les fichiers", Toast.LENGTH_SHORT).show();
                        Intent intentPerm = new Intent();
                        intentPerm.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intentPerm);
                    } else {
                        saveJson(recData.nomEnregistrement);
                    }
                }
            });

            btnSaveCsv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!Environment.isExternalStorageManager()) {
                        //tentative de faire fonctionner sous Android 13+
                        Toast.makeText(MapsActivity.this, "Veuillez autoriser à l'application la manipulation de tous les fichiers", Toast.LENGTH_SHORT).show();
                        Intent intentPerm = new Intent();
                        intentPerm.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intentPerm);
                    } else {
                        saveCsv(recData.nomEnregistrement);
                    }
                }
            });
        } catch (Exception e){
            Log.d("MapsActivity","ERREUR chargement du csv");
            e.printStackTrace();
            genInfoTextView.setText("ERREUR chargement du CSV...");
        }

        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMapFullscreen();
            }
        });

    }

    /**
     * Fonction qui génère le RecordingData qui servira ensuite à GSON et à l'affichage
     * @param rows CSV sous forme de liste de tableaux de String
     * @return rec l'objet RecordingData contennant les infos utiles
     */
    private RecordingData generateRecordingDataObject(List<String[]> rows){
        RecordingData rec = new RecordingData();

        int index = 0;
        while (index < rows.size() && rows.get(index).length == 0) {
            index++;
        }

        //Récupération des données générales de l'enregistrement (premier header)
        for(int i = 0; i < rows.size(); i++){
            String[] row = rows.get(i);
            if(row[0].equals("Nom")){ //signifie qu'on est bien dans le header des infos générales
                row = rows.get(i+1);
                rec.nomEnregistrement = row[0];
                rec.technologieVisee = row[1];
                rec.operateur = row[2];
                rec.debut = row[3];
                rec.intervalleMesure = row[4];
                rec.dureePrevue = row[5];
                break;
            }
        }

        //Récupération du nombre de mesures et création des serving cells avec les infos faciles
        //ATTENTION requiert un format FIXE du CSV bien précis
        int nbMesures = 0;
        for(int i = 0; i < rows.size(); i++){
            String[] row = rows.get(i);
            if(row[0].contains("Date")) { //pas terrible, repose sur le fait que la première colonne de la ligne contient "Date"
                nbMesures += 1;

                //Serving cell
                String[] measureInfoRow = rows.get(i+1);
                String[] cellHeaderRow = rows.get(i+2);
                String[] cellInfoRow = rows.get(i+3);
                if(cellHeaderRow[0].equals("Cell Type")){
                    String tech = techToG(cellInfoRow[0]);
                    int id1 = Integer.parseInt(cellInfoRow[1]);
                    int id2 = Integer.parseInt(cellInfoRow[2]);
                    int xrfcn = Integer.parseInt(cellInfoRow[3]);
                    int mcc = Integer.parseInt(cellInfoRow[4]);
                    int mnc = Integer.parseInt(cellInfoRow[5]);
                    int tac = Integer.parseInt(cellInfoRow[6]);
                    if (tech.equals("4G")) {
                        rec.register4GServingCell(id1, id2, xrfcn, mcc, mnc, tac);
                    } else if (tech.equals("3G")) {
                        rec.register3GServingCell(id1, id2, xrfcn, mcc, mnc, tac);
                    } else if (tech.equals("2G")) {
                        rec.register2GServingCell(id1, id2, xrfcn, mcc, mnc, tac);
                    }

                    //données GPS
                    double lat = Double.parseDouble(measureInfoRow[2]);
                    double lon = Double.parseDouble(measureInfoRow[1]);
                    double alt = Double.parseDouble(measureInfoRow[4]);
                    double prec = Double.parseDouble(measureInfoRow[3]);
                    if(lat != 0.0 && lon != 0.0 && alt != 0.0 && prec != 0.0) {
                        rec.addGPSPointToServingCell(id1, id2, lat, lon, alt, prec);
                    }
                }
            }
        }
        rec.nombreMesures = nbMesures;
        rec.updateNbCellules();

        //Calcul des infos signal moyennes, seulement après avoir registre toutes les serving cells
        //autant de fois qu'elles servent
        for(int i = 0; i < rows.size(); i++){
            String[] row = rows.get(i);
            if(row[0].contains("Date")) {

                //Serving cell
                String[] cellHeaderRow = rows.get(i+2);
                String[] cellInfoRow = rows.get(i+3);
                if(cellHeaderRow[0].equals("Cell Type")){
                    int id1 = Integer.parseInt(cellInfoRow[1]);
                    int id2 = Integer.parseInt(cellInfoRow[2]);
                    double signalStrength = Double.parseDouble(cellInfoRow[7]);

                    //infos signal 4G
                    if(cellInfoRow[0].equals("LTE")){
                        double rsrq = Double.parseDouble(cellInfoRow[10]);
                        double rsrp = Double.parseDouble(cellInfoRow[11]);
                        if(signalStrength != 0.0 && rsrq != 0.0 && rsrp != 0.0
                                && signalStrength != -1.0 && rsrq != -1.0 && rsrp != -1.0){
                            rec.addSignalInfoTo4GServingCell(id1, id2, signalStrength, rsrq, rsrp);
                        }
                    } else {
                        //infos signal autre
                        rec.addSignalInfoToServingCell(id1, id2, signalStrength);
                    }
                }
            }
        }

        return rec;
    }

    /**
     * Fonction qui génère le fichier JSON "rapport" à partir de RecordingData. Sera sauvegardé dans
     * un dossier "Rapports" dans le dossier de stockage de l'application.
     * @param rec l'objet RecordingData contennant les infos
     * @param name le nom du JSON
     */
    private void generateJSON(RecordingData rec, String name){
        File dir = new File(getExternalFilesDir(null) + "/Rapports");
        if(!dir.exists()){
            dir.mkdir();
        }

        Gson gson = new Gson();
        String json = gson.toJson(rec);
        jsonToSave = json;
        try {
            FileWriter writer = new FileWriter(dir + "/" + name + ".json");
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Fonction qui s'occupe de l'affichage du rapport avec un string builder
     * @param rec l'objet RecordingData contennant les infos
     */
    private void displayReport(RecordingData rec) {
        StringBuilder genInfo = new StringBuilder();
        genInfo.append("Nom de l'enregistrement : " + rec.nomEnregistrement + "\n\n");
        genInfo.append("Technologie visée : " + rec.technologieVisee + "\n\n");
        genInfo.append("Opérateur : " + rec.operateur + "\n\n");
        genInfo.append("Début de l'enregistrement : " + rec.debut + "\n\n");
        genInfo.append("Intervalle de mesure : " + rec.intervalleMesure + "\n\n");
        genInfo.append("Nombre de mesures : " + rec.nombreMesures + "\n\n");
        genInfo.append("Nombre de cellules : " + rec.nombreCellulesTotal + "\n\n");
        genInfo.append("Zones traversées : ");
        for (int zone : rec.listeZones) {
            genInfo.append(zone + ", ");
        }
        genInfo.append("\n\n\n");
        genInfoTextView.setText(genInfo.toString());

        //Affichage du pourcentage de mesures où chaque cellule était serving
        rec.sortServingCells();
        LinkedHashMap<Integer, Double> rankedServingCells = rec.getServingCellsPercentage();
        StringBuilder cellAnalysis = new StringBuilder();
        cellAnalysis.append("Pourcentage de mesures où chaque cellule était Serving :\n\n");
        for (LinkedHashMap.Entry<Integer, Double> entry : rankedServingCells.entrySet()) {
            //Juste 3 chiffres après la virgule
            String value = String.format("%.3f", entry.getValue());
            cellAnalysis.append("Cellule " + entry.getKey() + " : " + value + "%\n");
        }
        cellAnalysis.append("\n\n\n");
        cellAnalysisTextView.setText(cellAnalysis.toString());


        //Affichage du pie chart
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        for (LinkedHashMap.Entry<Integer, Double> entry : rankedServingCells.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), "ID: " + entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Serving cells");
        sortColors();
        dataSet.setColors(colorList);
        cellPieChart.setData(new PieData(dataSet));
        cellPieChart.setCenterText("Serving %"); // Set your desired text
        cellPieChart.setCenterTextSize(16f); // Adjust text size
        cellPieChart.setDrawCenterText(true);
        cellPieChart.setEntryLabelTextSize(8f);
        cellPieChart.getDescription().setEnabled(false);
        cellPieChart.invalidate();

        Legend legend = cellPieChart.getLegend();
        legend.setEnabled(false);

    }

    /**
     * Ajouter toutes les mesures du csv sous forme de markeurs de couleurs différentes pour chaque
     * serving cell
     * @param map la carte OSM
     * @param rows CSV sous forme de liste de tableaux de String
     */
    private void addMarkersFromCSV(MapView map, List<String[]> rows) {
        String time = "";
        double latitude = 0.0;
        double longitude = 0.0;
        double precision = 0.0;
        double altitude = 0.0;
        String cellIdentity = "";

        boolean ignoreNextPair = false;
        boolean isStart = true;

        // Ignorer les lignes vides au début du fichier
        int index = 0;
        while (index < rows.size() && rows.get(index).length == 0) {
            index++;
        }

        for (; index < rows.size(); index++) {
            String[] row = rows.get(index);

            if (!row[0].equals("")) {
                if (row[0].contains("Date")) {
                    String[] infoRow = rows.get(index+1);
                    // Ligne avec date, longitude, latitude
                    time = infoRow[0];
                    latitude = Double.parseDouble(infoRow[2]);
                    longitude = Double.parseDouble(infoRow[1]);
                    precision = Double.parseDouble(infoRow[3]);
                    altitude = Double.parseDouble(infoRow[4]);
                    ignoreNextPair = false; // Réinitialiser le drapeau pour le nouveau relevé
                } else if (row[0].equals("Cell Type") && !ignoreNextPair) {
                    // Ligne avec les titres des colonnes (première paire)
                    cellIdentity = rows.get(index+1)[1]; // Assurez-vous que c'est la bonne colonne

                    //Centrer la map
                    if(isStart){
                        IMapController mapController = map.getController();
                        mapController.setZoom(15.0);
                        GeoPoint startPoint = new GeoPoint(latitude,longitude);
                        mapController.setCenter(startPoint);
                        isStart = false;
                    }

                    // Ajouter le marqueur à la carte
                    GeoPoint geoPoint = new GeoPoint(latitude,longitude,altitude);
                    addMarker(map, new CustomPoint(cellIdentity, geoPoint, time, precision));
                    ignoreNextPair = true; // Ignorer les paires suivantes pour ce relevé
                }
            }
        }
    }

    /**
     * Ajouter un markeur (= 1 position associée à 1 mesure) sur la carte
     * @param map la carte OSM
     * @param customPoint le point correspondant à la mesure
     */
    private void addMarker(MapView map, CustomPoint customPoint) {
        Marker marker = new Marker(map);
        GeoPoint point = customPoint.getGeoPoint();
        marker.setPosition(point);
        marker.setTitle("Antenne ID: " + customPoint.getId() + "\nHeure : "+ customPoint.getTime()
        + "\nPrécision : " + customPoint.getPrecision() + " m");
        marker.setIcon(getMarkerDrawable(customPoint.getId()));
        map.getOverlays().add(marker);
    }


    /**
     * Obtenir une icone pour les markers d'une couleur différente pour chaque id
     * @param id l'id de la cellule
     * @return un Drawable à à mettre dans le setIcon d'un marker
     */
    private Drawable getMarkerDrawable(String id) {
        // Fonction de hachage pour obtenir une valeur numérique à partir de l'identifiant
        int hash = Math.abs(id.hashCode());

        int alpha = 0xFF;
        int red = (hash & 0xFF0000) >> 16;
        int green = (hash & 0x00FF00) >> 8;
        int blue = hash & 0x0000FF;
        int colorInt = (alpha << 24) | (red << 16) | (green << 8) | blue;
        colorList.add(colorInt); //même si la couleur est déjà dans la liste on ajoute. Voir sortColors()

        int originalMarkerId = R.drawable.marker_default_circle;
        Drawable marker = getResources().getDrawable(originalMarkerId);
        marker.setTint(colorInt);
        return marker;
    }

    /**
     * Trier la liste des couleurs en fonction du nombre d'oocurences de chaque couleur
     * Forcément le nombre d'occurences est égal au nombre de mesures où la cellule était serving
     * Comme ça pas besoin d'établir une correspondance entre les couleurs et les cell ID
     * pour la pie chart. Petit stratagème mais ce n'est pas très beau
     */
    private void sortColors(){
        LinkedList<Integer> sortedColors = new LinkedList<Integer>();
        Map<Integer, Integer> colorOccurences = new LinkedHashMap<Integer, Integer>();
        for(int color : colorList){
            if(colorOccurences.containsKey(color)){
                colorOccurences.put(color, colorOccurences.get(color)+1);
            } else {
                colorOccurences.put(color, 1);
            }
        }
        while(!colorOccurences.isEmpty()){
            int maxColor = 0;
            int maxOccurences = 0;
            for(Map.Entry<Integer, Integer> entry : colorOccurences.entrySet()){
                if(entry.getValue() > maxOccurences){
                    maxColor = entry.getKey();
                    maxOccurences = entry.getValue();
                }
            }
            sortedColors.add(maxColor);
            colorOccurences.remove(maxColor);
        }
        colorList = sortedColors;
    }

    /**
     * Activer/désactiver la taille maximale de la carte. Appelé par le bouton correspondant
     * Attention : toutes les views de l'écran doivent disaparaitre/ré-apparaitre
     */
    private void toggleMapFullscreen(){
        ViewGroup.LayoutParams mapLayout = map.getLayoutParams();
        if(mapIsFullScreen){
            mapLayout.width = originalMapWidth;
            mapLayout.height = originalMapHeight;
            map.setLayoutParams(mapLayout);
            legend.setVisibility(View.VISIBLE);
            reportLayout.setVisibility(View.VISIBLE);
            btnSaveCsv.setVisibility(View.VISIBLE);
            btnSaveJson.setVisibility(View.VISIBLE);
        } else {
            originalMapHeight = mapLayout.height;
            originalMapWidth = mapLayout.width;
            mapLayout.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            mapLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            map.setLayoutParams(mapLayout);
            legend.setVisibility(View.GONE);
            reportLayout.setVisibility(View.GONE);
            btnSaveCsv.setVisibility(View.GONE);
            btnSaveJson.setVisibility(View.GONE);
        }
        mapIsFullScreen = !mapIsFullScreen;
    }

    /**
     * Fonction qui convertit le nom de la technologie en G (2G, 3G, 4G)
     * @param techName
     * @return le nom de la technologie en G
     */
    private String techToG(String techName){
        if(techName.equals("LTE")){
            return "4G";
        } else if (techName.equals("WCDMA")) {
            return "3G";
        } else if (techName.equals("GSM")){
            return "2G";
        } else {
            return "";
        }
    }

    /**
     * Fonction qui sauvegarde le JSON dans un dossier externe
     * @param name nom du fichier sans l'extension
     */
    private void saveJson(String name){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
            //code peu importe
        } else {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            try {
                FileWriter writer = new FileWriter(directory + "/" + name + "_rapport.json");
                writer.write(jsonToSave);
                writer.close();
                Toast.makeText(this, "JSON sauvegardé dans Downloads", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fonction qui sauvegarde le CSV dans un dossier externe
     * @param name nom du fichier sans l'extension
     */
    private void saveCsv(String name){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
            //code peu importe
        } else {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            try {
                CSVWriter writer = new CSVWriter(new FileWriter(directory + "/" + name + "_log.csv"));
                writer.writeAll(csvToSave);
                writer.close();
                Toast.makeText(this, "CSV sauvegardé dans Downloads", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}