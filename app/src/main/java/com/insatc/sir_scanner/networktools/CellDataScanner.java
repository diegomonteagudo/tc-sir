package com.insatc.sir_scanner.networktools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.concurrent.Executors;

public class CellDataScanner implements Runnable {
    private static final long REFRESH_INTERVAL = 1000; // Interval de rafraîchissement en millisecondes
    private boolean isRunning = false;
    private Context context;
    private Button toggleScanButton;
    private TextView displayTextView;
    private StringBuilder info;
    private List<CellInfo> cellInfoList;
    private Handler uiInteraction = new Handler(Looper.getMainLooper());
    private String technologie;
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public CellDataScanner(Context context, String techno, Button toggleScanButton, TextView displayTextView){
        this.context = context;
        this.toggleScanButton = toggleScanButton;
        this.displayTextView = displayTextView;
        this.technologie = techno;
    }

    @Override
    public void run() {
        isRunning = true;
        changerTexteBouton("Désactiver scan", this.toggleScanButton);

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            while(isRunning){
                getAntennaInfo();
                refreshCellInfo();
                changerTexteTextview(info.toString(), this.displayTextView);
                try {
                    Thread.sleep(REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            changerTexteTextview("Erreur: veuillez autoriser la localisation", displayTextView);
            changerTexteBouton("Activer scan", this.toggleScanButton);
        }
    }

    // méthode pour rafraîchir les informations de la cellule
    private void refreshCellInfo() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Executors.newSingleThreadExecutor() comme Executor
                telephonyManager.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
                    @Override
                    public void onCellInfo(List<CellInfo> cellInfoList) {
                        if(cellInfoList.size()>0) {
                            getAntennaInfo();
                        }
                    }
                });
            }
        }
    }

    /**
     * Méthode pour récupérer les informations de la cellule et les afficher dans le TextView
     */
    private void getAntennaInfo() {
        Log.d("CellDataScanner", "getAntennaInfo called");

        // Vérification des permissions
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (telephonyManager != null) {
                // Initialisation du StringBuilder et récupération des CellInfo
                info = new StringBuilder();
                String RRCstate = connectivityManager.isDefaultNetworkActive()?"~CONNECTED":"IDLE";
                info.append("État RRC :\n"+RRCstate+"\n\n\n");
                cellInfoList = telephonyManager.getAllCellInfo();

                // Lire les informations de chaque cellule
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        if(!technologie.equals("2G")){
                            info.append("Attention : cellule 2G\n");
                        }
                        processGsmCellInfo((CellInfoGsm) cellInfo);
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if(!technologie.equals("3G")){
                            info.append("Attention : cellule 3G\n");
                        }
                        processWcdmaCellInfo((CellInfoWcdma) cellInfo);
                    } else if (cellInfo instanceof CellInfoLte) {
                        if(!technologie.equals("4G")){
                            info.append("Attention : cellule 4G\n");
                        }
                        processLteCellInfo((CellInfoLte) cellInfo);
                    } else {
                        Log.d("MainActivity", "Unknown CellInfo type");
                        info.append("Technologie inconnue");
                    }

                    // Ajout d'un espace entre chaque antenne
                    info.append("\n\n");
                }

            } else {
                changerTexteTextview("TelephonyManager inaccessible. Aucune fonctionalité n'est" +
                        "donc disponible.", displayTextView);
            }
        } else {
            changerTexteTextview("Erreur: veuillez autoriser la localisation", displayTextView);
        }
    }

    /**
     * Méthode pour traiter les informations des cellules GSM
     * @param cellInfo la cellule 2G
     */
    private void processGsmCellInfo(CellInfoGsm cellInfo) {
        // Traitement des informations pour GSM
        CellInfoGsm gsmCellInfo = (CellInfoGsm) cellInfo;
        CellIdentityGsm cellIdentity = gsmCellInfo.getCellIdentity();
        CellSignalStrengthGsm gsmSignalStrength = gsmCellInfo.getCellSignalStrength();

        //informations sur l'identité de la cellule GSM
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

        // Affichage des informations dans le TextView
        if(!info.toString().contains("Serving")){
            info.append("\"Serving\" cell :\n");
        }
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

    }

    /**
     * Méthode pour traiter les informations des cellules WCDMA
     * @param cellInfo la cellule 3G
     */
    private void processWcdmaCellInfo(CellInfoWcdma cellInfo) {
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

        // Affichage des informations dans le TextView
        /*OLD
        info.append("Cell Type: WCDMA\n");
        info.append("WCDMA Cell Identity: ").append(cid).append("\n");
        info.append("MCC: ").append(mcc).append("\n");
        info.append("MNC: ").append(mnc).append("\n");
        info.append("LAC: ").append(lac).append("\n");
        info.append("UARFCN: ").append(uarfcn).append("\n");
        info.append("PSC:").append(psc).append("\n");
        info.append("Signal Strength (WCDMA): ").append(dbm).append(" dBm\n");
        info.append("ASU Level (WCDMA): ").append(asuLevel).append("\n");
        info.append("Level (WCDMA): ").append(level).append("\n");
        info.append("Ec/No (WCDMA): ").append(ecno).append("\n");
         */
        if(!info.toString().contains("Serving")){
            info.append("\"Serving\" cell :\n");
        }
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

    }

    /**
     * Méthode pour traiter les informations des cellules LTE
     * @param cellInfo la cellule 4G
     */
    private void processLteCellInfo(CellInfoLte cellInfo) {
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
        /*OLD
        info.append("Cell Type: LTE\n");
        info.append("LTE Cell Identity: ").append(ci).append("\n");
        info.append("MCC: ").append(mcc).append("\n");
        info.append("MNC: ").append(mnc).append("\n");
        info.append("PCI: ").append(pci).append("\n");
        info.append("TAC: ").append(tac).append("\n");
        info.append("Bandwidth (LTE): ").append(bw).append(" kHz\n");
        info.append("EARFCN: ").append(earfcn).append("\n");
        info.append("Signal Strength (LTE): ").append(dbm).append(" dBm\n");
        info.append("ASU Level (LTE): ").append(asuLevel).append("\n");
        info.append("Level (LTE): ").append(level).append("\n");
        info.append("RSRP (LTE): ").append(rsrp).append(" dBm\n");
        info.append("RSRQ (LTE): ").append(rsrq).append(" dB\n");
         */
        if(!info.toString().contains("Serving")){
            info.append("\"Serving\" cell :\n");
        }
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


    }

    /**
     * Arrêter le scan en continu
     */
    public void endScan(){
        if(isRunning){
            isRunning = false;
            changerTexteBouton("Activer scan", this.toggleScanButton);
        }
    }

    public void changerTexteBouton(String texte, Button bouton){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                bouton.setText(texte);
            }
        });
    }

    public void changerTexteTextview(String texte, TextView textView){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                //textView.setTextColor(0xFFAAAAAA);
                textView.setText(texte);
            }
        });
    }

    /**
     * Méthode pour afficher un toast
     * @param texte le texte à afficher
     * @param duration la durée d'affichage (Toast.LENGTH_SHORT ou Toast.LENGTH_LONG)
     */
    public void toast(String texte, int duration){
        this.uiInteraction.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, texte, duration).show();
            }
        });
    }

    public String appendIfDefined(String label, int variable, String units){
        if(variable != -1){
            return label+variable+units+"\n";
        } else {
            return "";
        }
    }

    public String appendIfDefined(String label, int variable) {
        return appendIfDefined(label, variable, "");
    }
}