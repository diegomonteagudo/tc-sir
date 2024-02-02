package com.insatc.sir_scanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.insatc.sir_scanner.NetworkTools.CellDataSaver;
import com.insatc.sir_scanner.NetworkTools.CellDataScanner;
import com.insatc.sir_scanner.NetworkTools.InternetTraffic;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private CellDataScanner scanner;
    private boolean scanRunning = false;
    private boolean saveRunning = false;
    private CellDataSaver csvSaver;

    private Button btnGetAntennaInfo;
    private Button btnConnect;
    private Button btnScanToggle;
    private Button btnVisualiser;
    private TextView tvAntennaInfo;
    private TextView tvSaveInfo;
    private Context scanActivity = this;
    private String technologie;
    private String operateur;
    private StringBuilder info; // Déclaration de la variable info
    private List<CellInfo> cellInfoList; // Déclaration de la liste cellInfoList

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Technologie") && intent.hasExtra("Operateur")){
            technologie = intent.getStringExtra("Technologie");
            operateur = intent.getStringExtra("Operateur");
        } else {
            Log.d("ScanActivity", "ERREUR INTENT");
        }

        // Toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Scanner"); // Titre de la Toolbar

        // Initialisation des vues
        btnGetAntennaInfo = findViewById(R.id.btnGetAntennaInfo);
        tvAntennaInfo = findViewById(R.id.tvAntennaInfo);
        tvSaveInfo = findViewById(R.id.tvSaveInfo);
        btnConnect = findViewById(R.id.btnBottom1);
        btnScanToggle = findViewById(R.id.btnScanToggle);
        btnVisualiser = findViewById(R.id.btnVisualiser);

        scanner = new CellDataScanner(this, technologie, btnScanToggle, tvAntennaInfo);
        csvSaver = new CellDataSaver(this, technologie, operateur, btnGetAntennaInfo, tvSaveInfo, btnVisualiser);

        requestPermissions(REQUIRED_PERMISSIONS,1);

        //bouton lancer l'enregistrement (ou arrêter si en cours)
        btnGetAntennaInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!saveRunning){
                    saveRunning = true;
                    Thread saveThread = new Thread(csvSaver);
                    saveThread.start();
                } else {
                    saveRunning = false;
                    csvSaver.stopPeriodicRecording();
                }
            }
        });

        //activer le scan en continu
        btnScanToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!scanRunning){
                    scanRunning = true;
                    Thread scanThread = new Thread(scanner);
                    scanThread.start();
                } else {
                    scanRunning = false;
                    scanner.endScan();
                }
            }
        });

        //bouton générer du trafic HTTP pour passer en RRC_CONNECTED
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternetTraffic.generateTraffic(scanActivity, btnConnect);
            }
        });

    }

    public boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return true; // Pour les versions d'Android antérieures à M, les autorisations sont considérées comme accordées.
    }

}