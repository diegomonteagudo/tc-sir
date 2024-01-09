package com.example.v1netmonster;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnGetAntennaInfo;
    private TextView tvAntennaInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Scanner v1"); // Titre de la Toolbar


        btnGetAntennaInfo = findViewById(R.id.btnGetAntennaInfo);
        tvAntennaInfo = findViewById(R.id.tvAntennaInfo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }

        btnGetAntennaInfo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked");
                if (checkPermissions()) {
                    Log.d("MainActivity", "Permissions granted");
                    getAntennaInfo();
                } else {
                    Log.d("MainActivity", "Permissions not granted");
                    // Informez l'utilisateur que les autorisations sont nécessaires.
                    Toast.makeText(MainActivity.this, "Permissions required.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAntennaInfo() {
        Log.d("MainActivity", "getAntennaInfo called");

        // Vérifiez si la permission ACCESS_FINE_LOCATION est accordée
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {

                StringBuilder info = new StringBuilder();
                List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

                if (cellInfoList != null) {
                    String csvFileName = "antennaaaaaaa_info.csv"; // Nom du fichier CSV
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String csvFilePath = new File(downloadsDir, csvFileName).getAbsolutePath();

                    try {
                        // Créer le fichier CSV
                        CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFilePath));

                        // En-têtes du fichier CSV
                        String[] headers = {"Cell Type", "Signal Strength (dBm)", "MCC", "MNC", "CI", "PCI", "TAC", "ASU Level", "Signal Level"};
                        csvWriter.writeNext(headers);

                        // Lire les informations de chaque cellule et écrire dans le fichier CSV
                        for (CellInfo cellInfo : cellInfoList) {
                            // ... (Votre code existant pour extraire les informations de la cellule)
                            if (cellInfo instanceof CellInfoGsm) {
                                Log.d("MainActivity", "CellInfo type: GSM");
                                CellSignalStrengthGsm gsmSignalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                                info.append("Signal Strength (GSM): ").append(gsmSignalStrength.getDbm()).append(" dBm\n");
                            } else if (cellInfo instanceof CellInfoCdma) {
                                Log.d("MainActivity", "CellInfo type: CDMA");
                                CellSignalStrengthCdma cdmaSignalStrength = ((CellInfoCdma) cellInfo).getCellSignalStrength();
                                info.append("Signal Strength (CDMA): ").append(cdmaSignalStrength.getDbm()).append(" dBm\n");
                            } else if (cellInfo instanceof CellInfoLte) {
                                CellInfoLte lteCellInfo = (CellInfoLte) cellInfo;
                                CellIdentityLte cellIdentity = lteCellInfo.getCellIdentity();
                                CellSignalStrengthLte lteSignalStrength = lteCellInfo.getCellSignalStrength();

                                // Informations sur l'identité de la cellule
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    String mcc1 = cellIdentity.getMccString();
                                }
                                int ci = cellIdentity.getCi();
                                int mcc = cellIdentity.getMcc();
                                int mnc = cellIdentity.getMnc();
                                int pci = cellIdentity.getPci();
                                int tac = cellIdentity.getTac();

                                // Informations sur la force du signal LTE
                                int dbm = lteSignalStrength.getDbm();
                                int asuLevel = lteSignalStrength.getAsuLevel();
                                int level = lteSignalStrength.getLevel();

                                info.append("LTE Cell Identity: ").append(ci).append("\n");
                                info.append("MCC: ").append(mcc).append("\n");
                                info.append("MNC: ").append(mnc).append("\n");
                                info.append("PCI: ").append(pci).append("\n");
                                info.append("TAC: ").append(tac).append("\n");

                                info.append("Signal Strength (LTE): ").append(dbm).append(" dBm\n");
                                info.append("ASU Level (LTE): ").append(asuLevel).append("\n");
                                info.append("Level (LTE): ").append(level).append("\n");

                                // Exemple d'écriture dans le fichier CSV à l'intérieur de la boucle
                                String[] rowData = {"LTE", String.valueOf(dbm), String.valueOf(mcc), String.valueOf(mnc), String.valueOf(ci),
                                        String.valueOf(pci), String.valueOf(tac), String.valueOf(asuLevel), String.valueOf(level)};
                                csvWriter.writeNext(rowData);
                            } else {
                                Log.d("MainActivity", "Unknown CellInfo type");
                                // Ajoutez d'autres types de cellules au besoin
                            }
                        }

                        // Fermer le fichier CSV
                        csvWriter.close();

                        // Afficher un message à l'utilisateur
                        File csvFile = new File(csvFilePath);
                        String fileName = csvFile.getName();
                        Toast.makeText(this, "Fichier CSV généré avec succès : " + fileName, Toast.LENGTH_LONG).show();

                        // Afficher le message toast
                        Toast.makeText(this, "Fichier CSV généré avec succès. Consultez les logs pour le contenu complet.", Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors de la création du fichier CSV", Toast.LENGTH_SHORT).show();
                    }

                }

                Log.d("MainActivity", info.toString());
                tvAntennaInfo.setText(info.toString());
            } else {
                tvAntennaInfo.setText("Unable to access TelephonyManager.");
            }
        } else {
            // La permission ACCESS_FINE_LOCATION n'est pas accordée, demandez-la à l'utilisateur si nécessaire.
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int networkStatePermission = checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE);
            int phoneStatePermission = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            int fineLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);

            return networkStatePermission == PackageManager.PERMISSION_GRANTED &&
                    phoneStatePermission == PackageManager.PERMISSION_GRANTED &&
                    fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                    coarseLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pour les versions d'Android antérieures à M, les autorisations sont considérées comme accordées.
    }

}