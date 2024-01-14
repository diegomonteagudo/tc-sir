package com.insatc.sir_scanner.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;

import java.util.List;

public class CellData {
    public static String getCellData(Context context) {
        String texteFinal = "Aucun";
        String technologie = "";
        String essaiRRC = "Inconnu";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean conditionPermission = context.checkSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if(conditionPermission) {
            essaiRRC = connectivityManager.isDefaultNetworkActive()?"Probablement CONNECTED":"IDLE";
            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
            //CellInfo firstCell = allCellInfo.get(0);
            for (CellInfo cellInfo : allCellInfo) {
                if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    CellSignalStrengthLte cellSignalLte = cellInfoLte.getCellSignalStrength();

                    StringBuilder res = new StringBuilder();
                    res.append(appendIfDefined("PCI", cellIdentityLte.getPci()));
                    res.append(appendIfDefined("CI", cellIdentityLte.getCi()));
                    res.append(appendIfDefined("MCC", cellIdentityLte.getMccString()));
                    res.append(appendIfDefined("MNC", cellIdentityLte.getMncString()));
                    res.append(appendIfDefined("EARFCN", cellIdentityLte.getEarfcn()));
                    res.append(appendIfDefined("TAC", cellIdentityLte.getTac()));
                    res.append(appendIfDefined("Puissance (en dBm)", cellSignalLte.getDbm()));

                    if (texteFinal.equals("Aucun")) {
                        texteFinal = res.toString();
                        technologie += "4G ";
                    } else {
                        texteFinal += "\n\n" + res.toString();
                    }

                } else if (cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    CellSignalStrengthWcdma cellSignalWcdma = cellInfoWcdma.getCellSignalStrength();

                    StringBuilder res = new StringBuilder();
                    res.append(appendIfDefined("CID : ", cellIdentityWcdma.getCid()));
                    res.append(appendIfDefined("MCC : ", cellIdentityWcdma.getMccString()));
                    res.append(appendIfDefined("MNC : ", cellIdentityWcdma.getMncString()));
                    res.append(appendIfDefined("UARFCN : ", cellIdentityWcdma.getUarfcn()));
                    res.append(appendIfDefined("LAC : ", cellIdentityWcdma.getLac()));
                    res.append(appendIfDefined("Puissance (en dBm) : ", cellSignalWcdma.getDbm()));

                    if (texteFinal.equals("Aucun")) {
                        texteFinal = res.toString();
                        technologie += "3G ";
                    } else {
                        texteFinal += "\n\n" + res.toString();
                    }

                } else if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    CellSignalStrengthGsm cellSignalGsm = cellInfoGsm.getCellSignalStrength();

                    StringBuilder res = new StringBuilder();
                    res.append(appendIfDefined("CID : ", cellIdentityGsm.getCid()));
                    res.append(appendIfDefined("BSID : ", cellIdentityGsm.getBsic()));
                    res.append(appendIfDefined("MCC : ", cellIdentityGsm.getMccString()));
                    res.append(appendIfDefined("MNC : ", cellIdentityGsm.getMncString()));
                    res.append(appendIfDefined("ARFCN : ", cellIdentityGsm.getArfcn()));
                    res.append(appendIfDefined("LAC : ", cellIdentityGsm.getLac()));
                    res.append(appendIfDefined("Puissance (en dBm) : ", cellSignalGsm.getDbm()));

                    if (texteFinal.equals("Aucun")) {
                        texteFinal = res.toString();
                        technologie += "2G ";
                    } else {
                        texteFinal += "\n\n" + res.toString();
                    }

                }
            }
            return ("Etat RRC : "+essaiRRC+"\n\n"
                    +"Technologie : " + technologie + "\n\n\n\n"
                    + "Cellules :\n\n" + texteFinal);
        } else {
            return ("Erreur: permissions non accordées");
        }
    }

    public static String appendIfDefined(String label, String value) {
        if (value != null && !value.equals(String.valueOf(Integer.MAX_VALUE))) {
            return label + " : " + value + "\n";
        }
        return "";
    }

    public static String appendIfDefined(String label, int value) {
        return appendIfDefined(label, Integer.toString(value));
    }
}