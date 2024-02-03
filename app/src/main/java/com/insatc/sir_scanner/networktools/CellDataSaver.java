package com.insatc.sir_scanner.networktools;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.insatc.sir_scanner.activities.ReportActivity;

import android.content.Intent;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;

import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.util.Log;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.CheckBox;
import com.insatc.sir_scanner.R;

public class CellDataSaver implements Runnable {
    private Context context;
    private Button btnGetAntennaInfo;
    private Button btnVisualiser;
    private TextView displayTextView;
    private StringBuilder info;
    private List<CellInfo> cellInfoList;
    private Handler uiInteraction = new Handler(Looper.getMainLooper());
    private boolean recording = false;
    private boolean everythingAlreadySaved = false;
    private Thread thisThread;
    private long recordingTime; // in milliseconds
    private long measurementInterval; // in milliseconds
    private String recordingName; //SANS ".csv"
    private String technologie;
    private String operateur;
    private boolean forceGPS = false;
    private final long DEFAULT_RECORDING_TIME = Long.MAX_VALUE;
    private final long DEFAULT_MEASURE_INTERVAL = 20000;


    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public CellDataSaver(Context context, String tech, String op,
                         Button btnGetAntennaInfo, TextView displayTextView, Button btnVisualiser){
        this.context = context;
        this.btnGetAntennaInfo = btnGetAntennaInfo;
        this.displayTextView = displayTextView;
        this.btnVisualiser = btnVisualiser;
        this.technologie = tech;
        this.operateur = op;
    }

    @Override
    public void run() {
        thisThread = Thread.currentThread();
        showInputDialog(); // dialog
        long recordingStartTime = System.currentTimeMillis(); // Enregistrer le temps de début d'enregistrement
        long recordingTimeMillis = recordingTime; // Convertir la durée d'enregistrement en millisecondes
        Log.d("CellDataSaver", "Interval : "+measurementInterval);
        Log.d("CellDataSaver", "Temps : "+recordingTime);
        Log.d("CellDataSaver", "recordingStartTime : "+recordingStartTime);
        Log.d("CellDataSaver", "recordingTimeMillis : "+recordingTimeMillis);

        changerTexteBouton("Arreter enregistrement", this.btnGetAntennaInfo);
        cacherVisualisation(this.btnVisualiser);

        String csvFileName = recordingName+".csv"; // A CHANGER JUSTE UN TEST
        File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String csvFilePath = new File(externalDir, csvFileName).getAbsolutePath();

        CSVWriter csvWriter = null;
        try {
            csvWriter = new CSVWriter(new FileWriter(csvFilePath));
        } catch (IOException e) {
            Log.d("CellDataSaver","ERREUR: LE CSV NE FONCTIONNE PAS");
            throw new RuntimeException(e);
        }
        csvWriter.writeNext(new String[]{"Nom","Technologie","Operateur","Debut","Intervalle (s)","Durée prévue"});
        csvWriter.writeNext(new String[]{recordingName,technologie,operateur,
                getCurrentDateTime(),String.valueOf(measurementInterval/1000),
                recordingTime==Long.MAX_VALUE?"Arrêt manuel":String.valueOf(recordingTime/1000)});

        csvWriter.writeNext(new String[]{"", "", "", "", "", "", "", "", "", ""}); // Ajouter une ligne vide
        String[] headersGpsDateHeure = {"Date et Heure","Longitude ", "Latitude", "Précision", "Altitude"};
        String dateTime;
        Location location;

        //boucle qui recommence à chaque mesure
        while(recording && System.currentTimeMillis() - recordingStartTime < recordingTimeMillis){

            everythingAlreadySaved = false;
            info = new StringBuilder();
            info.append("DERNIÈRE SAUVEGARDE : "+getCurrentTime()+"\n\n");
            //Ecriture localisation, date et heure
            double latitude = 0;
            double longitude = 0;
            double precision = 0;
            double altitude = -1;
            if(forceGPS) {
                location = getGPSInfoHard();
            } else {
                location = getGPSInfoNormal();
            }
            if(location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if(location.hasAltitude()){
                    altitude = location.getAltitude();
                }
                if(location.hasAccuracy()){
                    precision = location.getAccuracy();
                }
            } else{
                info.append("Attention: position introuvable\n\n");
            }

            dateTime = getCurrentDateTime();

            csvWriter.writeNext(headersGpsDateHeure);
            String[] rowDataGpsDateH = {dateTime, String.valueOf(longitude),
                    String.valueOf(latitude), String.valueOf(precision), String.valueOf(altitude) };
            csvWriter.writeNext(rowDataGpsDateH);

            //traffic pas très propre, aucune garantie à 100% qu'on soit bien en connected au moment
            //de l'enregistrement. Paramètres très arbitraires. Sleep un peu brouillon.
            //Il y a sûrement du gachis de données mobiles en plus.
            InternetTraffic.generateTraffic(4, 1000);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            getAntennaInfo(csvWriter);
            changerTexteTextview(info.toString(), this.displayTextView);
            csvWriter.writeNext(new String[]{""}); // Ajouter une ligne vide
            toast("Mesure effectuée", Toast.LENGTH_SHORT);
            try {
                Thread.sleep(measurementInterval);
            } catch (InterruptedException e) {
                try {
                    csvWriter.close();
                } catch (IOException f) {
                    Log.d("CellDataSaver", "ERREUR d'écriture du csv");
                    f.printStackTrace();
                }
                toast("Fichier CSV généré avec succès", Toast.LENGTH_LONG);
                File csvFile = new File(csvFilePath);
                everythingAlreadySaved = true;
            }

        }

        /*si jamais l'utilisateur a arrêté l'enregistrement alors que le Thread n'était pas
        en train de dormir. Très sale. Dans presque tous les cas c'est plutôt dans le catch
        de l'interruption que le fichier se ferme*/
        if(!everythingAlreadySaved){
            // Fermeture du fichier CSV
            try {
                csvWriter.close();
            } catch (IOException e) {
                Log.d("CellDataSaver", "ERREUR d'écriture du csv");
                e.printStackTrace();
            }
            // Affichage d'un message à l'utilisateur
            toast("Fichier CSV généré avec succès", Toast.LENGTH_LONG);

            File csvFile = new File(csvFilePath);
            everythingAlreadySaved = true;
        }
        recording = false;
        proposerVisualisation(csvFilePath, btnVisualiser);
        changerTexteBouton("Commencer enregistrement", this.btnGetAntennaInfo);
    }


    /**
     * Afficher une boîte de dialogue pour obtenir les paramètres d'enregistrement choisis par l'utilisateur
     * Bloque le thread jusqu'à ce que la boîte de dialogue soit fermée
     */
    private void showInputDialog() {
        // Utiliser un objet (array) pour indiquer si la boîte de dialogue a été fermée
        final boolean[] dialogClosed = {false};
        uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                // Création de la boîte de dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Paramètres d'enregistrement");

                // Utilisation du layout inflator pour inflater le layout de la boîte de dialogue
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.recording_input_dialog, null);
                builder.setView(dialogView);

                // Récupération des références des vues dans le layout de la boîte de dialogue
                EditText recordingNameInput = dialogView.findViewById(R.id.recordingNameInput);
                EditText recordingTimeInput = dialogView.findViewById(R.id.recordingTimeInput);
                EditText intervalInput = dialogView.findViewById(R.id.intervalInput);
                CheckBox forceGPScheckbox = dialogView.findViewById(R.id.forceGPScheckbox);

                // Set up des buttons
                builder.setPositiveButton("Start Recording", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Parse input and start recording
                        startRecording(
                                recordingNameInput.getText().toString(),
                                parseInput(recordingTimeInput.getText().toString()),
                                parseInput(intervalInput.getText().toString())
                        );
                        forceGPS = forceGPScheckbox.isChecked();
                        dialogClosed[0] = true;
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialogClosed[0] = false;
                    }
                });

                builder.show();
            }
        });
        // Attendre la fermeture de la boîte de dialogue
        while (!dialogClosed[0]) {
            try {
                Thread.sleep(100); // Attendre 100 millisecondes avant de vérifier à nouveau
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Obtenir les informations sur les antennes du moment et les écrire dans le fichier CSV
     * @param csv le CSVWriter pour écrire les informations
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void getAntennaInfo(CSVWriter csv) {
        Log.d("CellDataSaver", "getAntennaInfo called");

        // Vérification des permissions
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                // Initialisation du StringBuilder et récupération des CellInfo
                cellInfoList = telephonyManager.getAllCellInfo();

                // Lire les informations de chaque cellule et écrire dans le fichier CSV
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        processGsmCellInfo((CellInfoGsm) cellInfo, csv);
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        processWcdmaCellInfo((CellInfoWcdma) cellInfo, csv);
                    } else if (cellInfo instanceof CellInfoLte) {
                        processLteCellInfo((CellInfoLte) cellInfo, csv);
                    } else {
                        Log.d("MainActivity", "Unknown CellInfo type");
                        // Ajoutez d'autres types de cellules au besoin
                    }

                    // Ajout d'un espace entre chaque antenne
                    info.append("\n\n");
                }

                changerTexteTextview(info.toString(), displayTextView);
            } else {
                changerTexteTextview("Unable to access TelephonyManager.", displayTextView);
            }
        }
    }


    /**
     * Changer le texte d'un bouton
     * @param texte nouveau texte
     * @param bouton bouton
     */
    private void changerTexteBouton(String texte, Button bouton){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                bouton.setText(texte);
            }
        });
    }

    /**
     * Arrêter l'enregistrement. Appelée par les autres Threads.
     * Très sale : se fini par un interrupt sur lui-même pour si jamais il est en Thread.sleep
     */
    public void stopPeriodicRecording() {
        if (recording) {
            // Arrêtez l'enregistrement périodique uniquement s'il est en cours
            recording = false;
            changerTexteBouton("Commencer enregistrement", this.btnGetAntennaInfo);
            thisThread.interrupt();
        }
    }

    /**
     * Obtenir les informations sur les cellules GSM (2G)
     * @param cellInfo les CellInfo gsm
     * @param csvWriter le CSVWriter pour écrire dans le csv
     */
    private void processGsmCellInfo(CellInfoGsm cellInfo, CSVWriter csvWriter) {
        // En-têtes Csv
        String[] headers = {"Cell Type", "Cell ID", "Base Station IC", "ARFCN",
                "MCC", "MNC", "LAC", "Signal Strength (dBm)", "ASU Level", "Level"};
        csvWriter.writeNext(headers);

        // Traitement des informations pour GSM
        CellIdentityGsm cellIdentity = ((CellInfoGsm) cellInfo).getCellIdentity();
        CellSignalStrengthGsm gsmSignalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();

        //informations sur l'identité de la cellule WCDMA
        int cid = -1;
        int bsic = -1;
        int mcc = -1;
        int mnc = -1;
        int lac = -1;
        int arfcn = -1;
        if (cellIdentity.getCid() != Integer.MAX_VALUE) {cid = cellIdentity.getCid();}
        if (cellIdentity.getBsic() != Integer.MAX_VALUE) {bsic = cellIdentity.getBsic();}
        if (cellIdentity.getMcc() != Integer.MAX_VALUE) {mcc = cellIdentity.getMcc();}
        if (cellIdentity.getMnc() != Integer.MAX_VALUE) {mnc = cellIdentity.getMnc();}
        if (cellIdentity.getLac() != Integer.MAX_VALUE) {lac = cellIdentity.getLac();}
        if (cellIdentity.getArfcn() != Integer.MAX_VALUE) {arfcn = cellIdentity.getArfcn();}

        // Informations sur la force du signal GSM
        int dbm = gsmSignalStrength.getDbm(); // Signal Strength en dBm
        int asuLevel = gsmSignalStrength.getAsuLevel(); // ASU Level
        int level = gsmSignalStrength.getLevel(); // Level

        //écriture écran
        info.append("Cell Type: GSM\n");
        info.append(appendIfDefined("GSM Cell Identity: ", cid));
        info.append(appendIfDefined("GSM Base Station IC: ", bsic));
        info.append(appendIfDefined("MCC: ", mcc));
        info.append(appendIfDefined("MNC: ", mnc));
        info.append(appendIfDefined("LAC: ", lac));
        info.append(appendIfDefined("ARFCN: ", arfcn));
        info.append(appendIfDefined("Signal Strength (GSM): ", dbm, " dBm"));
        info.append(appendIfDefined("ASU Level (GSM): ", asuLevel));
        info.append(appendIfDefined("Level (GSM): ", level));

        // Ecriture Csv
        String[] rowData = {"GSM", String.valueOf(cid), String.valueOf(bsic), String.valueOf(arfcn),
                String.valueOf(mcc), String.valueOf(mnc), String.valueOf(lac),
                String.valueOf(dbm), String.valueOf(asuLevel), String.valueOf(level)};
        csvWriter.writeNext(rowData);

    }


    /**
     * Obtenir les informations sur les cellules WCDMA (3G)
     * @param cellInfo les CellInfo wcdma
     * @param csvWriter le CSVWriter pour écrire dans le csv
     */
    private void processWcdmaCellInfo(CellInfoWcdma cellInfo, CSVWriter csvWriter) {
        String[] headers = {"Cell Type", "Cell ID", "PSC", "UARFCN",
                "MCC", "MNC", "LAC", "Signal Strength (dBm)", "ASU Level", "Level", "Ec/No"};
        csvWriter.writeNext(headers);

        // Traitement des informations pour WCDMA
        CellInfoWcdma wcdmaCellInfo = (CellInfoWcdma) cellInfo;
        CellIdentityWcdma cellIdentity = wcdmaCellInfo.getCellIdentity();
        CellSignalStrengthWcdma wcdmaSignalStrength = wcdmaCellInfo.getCellSignalStrength();

        //informations sur l'identité de la cellule WCDMA
        int cid = -1;
        int mcc = -1;
        int mnc = -1;
        int lac = -1;
        int uarfcn = -1;
        int psc = -1;
        if (cellIdentity.getCid() != Integer.MAX_VALUE) {cid = cellIdentity.getCid();}
        if (cellIdentity.getMcc() != Integer.MAX_VALUE) {mcc = cellIdentity.getMcc();}
        if (cellIdentity.getMnc() != Integer.MAX_VALUE) {mnc = cellIdentity.getMnc();}
        if (cellIdentity.getLac() != Integer.MAX_VALUE) {lac = cellIdentity.getLac();}
        if (cellIdentity.getUarfcn() != Integer.MAX_VALUE) {uarfcn = cellIdentity.getUarfcn();}
        if (cellIdentity.getPsc() != Integer.MAX_VALUE) {psc = cellIdentity.getPsc();}

        // Informations sur la force du signal WCDMA
        int dbm = wcdmaSignalStrength.getDbm(); // Signal Strength en dBm
        int asuLevel = wcdmaSignalStrength.getAsuLevel(); // ASU Level
        int level = wcdmaSignalStrength.getLevel(); // Level

        // Ec/No (Energy per chip over the noise spectral density)
        int ecno = wcdmaSignalStrength.getEcNo(); // En dBm

        info.append("Cell Type: WCDMA\n");
        info.append(appendIfDefined("WCDMA Cell Identity: ", cid));
        info.append(appendIfDefined("MCC: ", mcc));
        info.append(appendIfDefined("MNC: ", mnc));
        info.append(appendIfDefined("LAC: ", lac));
        info.append(appendIfDefined("UARFCN: ", uarfcn));
        info.append(appendIfDefined("PSC: ", psc));
        info.append(appendIfDefined("Signal Strength (WCDMA): ", dbm, " dBm"));
        info.append(appendIfDefined("ASU Level (WCDMA): ", asuLevel));
        info.append(appendIfDefined("Level (WCDMA): ", level));
        info.append(appendIfDefined("Ec/No (WCDMA): ", ecno));

        // Ecriture Csv
        String[] rowData = {"WCDMA", String.valueOf(cid), String.valueOf(psc), String.valueOf(uarfcn),
                String.valueOf(mcc), String.valueOf(mnc), String.valueOf(lac),
                String.valueOf(dbm), String.valueOf(asuLevel), String.valueOf(level), String.valueOf(ecno)};
        csvWriter.writeNext(rowData);
    }


    /**
     * Obtenir les informations sur les cellules LTE (4G)
     * @param cellInfo les CellInfo lte
     * @param csvWriter le CSVWriter pour écrire dans le csv
     */
    private void processLteCellInfo(CellInfoLte cellInfo, CSVWriter csvWriter) {
        String[] headers = {"Cell Type", "Cell ID", "PCI", "EARFCN",
                "MCC", "MNC", "TAC", "Signal Strength (dBm)", "ASU Level", "Level",
                "RSRP (dBm)", "RSRQ (dBm)", "Bande passante (kHz)"};
        csvWriter.writeNext(headers);

        // Traitement des informations pour LTE
        CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
        CellIdentityLte cellIdentity = lteCellInfo.getCellIdentity();
        CellSignalStrengthLte lteSignalStrength = lteCellInfo.getCellSignalStrength();

        // Informations sur l'identité de la cellule LTE
        int ci = -1;
        int mcc = -1;
        int mnc = -1;
        int pci = -1;
        int tac = -1;
        int bw = -1;
        int earfcn = -1;
        if (cellIdentity.getCi() != Integer.MAX_VALUE) {ci = cellIdentity.getCi();}
        if (cellIdentity.getMcc() != Integer.MAX_VALUE) {mcc = cellIdentity.getMcc();}
        if (cellIdentity.getMnc() != Integer.MAX_VALUE) {mnc = cellIdentity.getMnc();}
        if (cellIdentity.getPci() != Integer.MAX_VALUE) {pci = cellIdentity.getPci();}
        if (cellIdentity.getTac() != Integer.MAX_VALUE) {tac = cellIdentity.getTac();}
        if (cellIdentity.getBandwidth() != Integer.MAX_VALUE) {bw = cellIdentity.getBandwidth();}
        if (cellIdentity.getEarfcn() != Integer.MAX_VALUE) {earfcn = cellIdentity.getEarfcn();}

        // Informations sur la force du signal LTE
        int dbm = lteSignalStrength.getDbm(); // Signal Strength en dBm
        int asuLevel = lteSignalStrength.getAsuLevel(); // ASU Level
        int level = lteSignalStrength.getLevel(); // Level

        // Récupération du RSRP (Reference Signal Received Power)
        int rsrp = lteSignalStrength.getRsrp(); // En dBm

        // Récupération du RSRQ (Reference Signal Received Quality)
        int rsrq = lteSignalStrength.getRsrq(); // En dB

        // Affichage des informations dans le TextView
        info.append("Cell Type: LTE\n");
        info.append(appendIfDefined("LTE Cell Identity: ",ci));
        info.append(appendIfDefined("MCC: ", mcc));
        info.append(appendIfDefined("MNC: ", mnc));
        info.append(appendIfDefined("PCI: ", pci));
        info.append(appendIfDefined("TAC: ", tac));
        info.append(appendIfDefined("Bandwidth (LTE): ", bw, " kHz"));
        info.append(appendIfDefined("EARFCN: ", earfcn));
        info.append(appendIfDefined("Signal Strength (LTE): ", dbm, " dBm"));
        info.append(appendIfDefined("ASU Level (LTE): ", asuLevel));
        info.append(appendIfDefined("Level (LTE): ", level));
        info.append(appendIfDefined("RSRP (LTE): ", rsrp, " dBm"));
        info.append(appendIfDefined("RSRQ (LTE): ", rsrq, " dB"));

        // Ecriture Csv
        String[] rowData = {"LTE", String.valueOf(ci), String.valueOf(pci), String.valueOf(earfcn),
                String.valueOf(mcc), String.valueOf(mnc), String.valueOf(tac),
                String.valueOf(dbm), String.valueOf(asuLevel), String.valueOf(level),
                String.valueOf(rsrp), String.valueOf(rsrq), String.valueOf(bw)};
        csvWriter.writeNext(rowData);

    }


    /**
     * Obtenir la dernière position du cache
     * @return la Location (qui peut être nulle)
     */
    private Location getGPSInfoNormal() {
        Location gpsInfo = null;

        // Vérification des autorisations
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtention du gestionnaire de localisation
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                // Vérification si le GPS est activé
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // Obtention de la dernière position connue
                    gpsInfo = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        return gpsInfo;
    }


    /**
     * Obtenir la position en forçant à prendre une nouvelle mesure.
     * Très sale : BLOQUE le thread pendant 3 secondes pour attendre la position,
     * sinon timeout et renvoie null
     * @return la Location (qui peut être nulle)
     */
    private Location getGPSInfoHard() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Location[] locationResult = new Location[1];

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CancellationToken token = new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            };
            final Task<Location> locationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token);
            CountDownLatch latch = new CountDownLatch(1);

            locationTask.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    locationResult[0] = task.getResult();
                }
                latch.countDown(); // Release the latch to unblock the calling thread
            });

            try{
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    return null;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return locationResult[0];

        }

        return null;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        Date currentDate = new Date();
        return sdf.format(currentDate);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        return sdf.format(currentDate);
    }

    /**
     * Convertir une chaîne de caractères en long, utilisé juste pour les paramètres d'enregistrement
     * @param input la chaîne de caractères à convertir
     * @return le long correspondant à la chaîne de caractères, ou 0 si la conversion échoue
     */
    private long parseInput(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if parsing fails
        }
    }


    /**
     * Traiter les paramètres d'enregistrement et commencer l'enregistrement
     * @param name
     * @param recordingTime
     * @param measurementInterval
     */
    private void startRecording(String name, long recordingTime, long measurementInterval) {
        if(!name.equals("")){
            recordingName = name;
        } else {
            recordingName = "csvAntenne"+getCurrentDateTime();
        }

        //entrées invalides
        if((recordingTime < measurementInterval && recordingTime!=0) || recordingTime<0 || measurementInterval<0){
            this.recordingTime = DEFAULT_RECORDING_TIME;
            this.measurementInterval = DEFAULT_MEASURE_INTERVAL;
            toast("Erreur paramètres. Paramètres par défaut utilisés", Toast.LENGTH_SHORT);
        } else {
            //aucune entrée = valeurs par défaut
            if(recordingTime==0){
                this.recordingTime = DEFAULT_RECORDING_TIME;
            } else {
                this.recordingTime = recordingTime * 1000;
            }
            if(measurementInterval==0){
                this.measurementInterval = DEFAULT_MEASURE_INTERVAL;
            } else {
                this.measurementInterval = measurementInterval * 1000;
            }
        }
        recording = true;
    }


    /**
     * Faire apparaître le bouton de visualisation si le chemin n'est pas vide, à appeler
     * après l'enregistrement
     * @param path chemin du fichier CSV
     * @param btnVisualiser bouton de visualisation devant apparaître
     */
    private void proposerVisualisation(String path, Button btnVisualiser){
        if (!path.equals("") && !recording) {
            this.uiInteraction.post(new Runnable() {
                @Override
                public void run() {
                    btnVisualiser.setVisibility(View.VISIBLE);
                    btnVisualiser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ReportActivity.class);
                            intent.putExtra("Path", path);
                            context.startActivity(intent);
                        }
                    });
                }
            });
        }
    }


    /**
     * Cacher le bouton de visualisation quand on recommence un enregistrement
     * @param btnVisualiser bouton de visualisation devant disparaître
     */
    private void cacherVisualisation(Button btnVisualiser){
        if(btnVisualiser.getVisibility()!=View.GONE){
            this.uiInteraction.post(new Runnable() {
                @Override
                public void run() {
                    btnVisualiser.setVisibility(View.GONE);
                }
            });
        }
    }


    /**
     * Changer le texte d'un TextView
     * @param texte nouveau texte
     * @param textView la TextView
     */
    private void changerTexteTextview(String texte, TextView textView){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(texte);
            }
        });
    }


    /**
     * Afficher un toast
     * @param texte texte du toast
     * @param duration durée du toast avec Toast.LENGTH_SHORT ou Toast.LENGTH_LONG
     */
    private void toast(String texte, int duration){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, texte, duration).show();
            }
        });
    }


    /**
     * Renvoyer l'info d'une cellule si elle est définie, sinon renvoie une chaîne vide
     * @param label le label de l'info
     * @param variable la variable à vérifier et afficher
     * @param units les unités de la variable
     * @return la chaîne de caractères à ajouter au StringBuilder
     */
    private String appendIfDefined(String label, int variable, String units){
        if(variable != -1){
            return label+variable+units+"\n";
        } else {
            return "";
        }
    }

    /**
     * Identique à appendIfDefined mais sans unités
     * @param label le label de l'info
     * @param variable la variable à vérifier et afficher
     * @return la chaîne de caractères à ajouter au StringBuilder
     */
    private String appendIfDefined(String label, int variable) {
        return appendIfDefined(label, variable, "");
    }

}
