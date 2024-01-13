package com.insatc.sir_scanner.network;

import android.content.Context;
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
        String texte = "Aucun";
        String technologie = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        boolean conditionPermission = context.checkSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if(conditionPermission) {
            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
            //CellInfo firstCell = allCellInfo.get(0);
            for (CellInfo cellInfo : allCellInfo) {
                if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    CellSignalStrengthLte cellSignalLte = cellInfoLte.getCellSignalStrength();

                    String PCI = "PCI : " + cellIdentityLte.getPci() + "\n";
                    String CI = "CI : " + cellIdentityLte.getCi() + "\n";
                    String MCC = "MCC : " + cellIdentityLte.getMccString() + "\n";
                    String MNC = "MNC : " + cellIdentityLte.getMncString() + "\n";
                    String EARFCN = "EARFCN :" + cellIdentityLte.getEarfcn() + "\n";
                    String TAC = "TAC : " + cellIdentityLte.getTac() + "\n";
                    String puissance = "Puissance (en dBm) : " + cellSignalLte.getDbm() + "\n";

                    String cellInfoTexteLte = PCI + CI + MCC + MNC + EARFCN + TAC + puissance + "\n\n\n";

                    if (texte.equals("Aucun")) {
                        texte = cellInfoTexteLte;
                        technologie += "4G ";
                    } else {
                        texte += "\n\n" + cellInfoTexteLte;
                    }

                } else if (cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    CellSignalStrengthWcdma cellSignalWcdma = cellInfoWcdma.getCellSignalStrength();

                    String CID = "CID : " + cellIdentityWcdma.getCid() + "\n";
                    String MCC = "MCC : " + cellIdentityWcdma.getMccString() + "\n";
                    String MNC = "MNC : " + cellIdentityWcdma.getMncString() + "\n";
                    String UARFCN = "UARFCN :" + cellIdentityWcdma.getUarfcn() + "\n";
                    String LAC = "LAC : " + cellIdentityWcdma.getLac() + "\n";
                    String puissance = "Puissance (en dBm) : " + cellSignalWcdma.getDbm() + "\n";

                    String cellInfoTexteWcdma = CID + MCC + MNC + UARFCN + LAC + puissance + "\n\n\n";

                    if (texte.equals("Aucun")) {
                        texte = cellInfoTexteWcdma;
                        technologie += "3G ";
                    } else {
                        texte += "\n\n" + cellInfoTexteWcdma;
                    }

                } else if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    CellSignalStrengthGsm cellSignalGsm = cellInfoGsm.getCellSignalStrength();

                    String CID = "CID : " + cellIdentityGsm.getCid() + "\n";
                    String BSID = "BSID : " + cellIdentityGsm.getBsic() + "\n";
                    String MCC = "MCC : " + cellIdentityGsm.getMccString() + "\n";
                    String MNC = "MNC : " + cellIdentityGsm.getMncString() + "\n";
                    String ARFCN = "ARFCN :" + cellIdentityGsm.getArfcn() + "\n";
                    String LAC = "LAC : " + cellIdentityGsm.getLac() + "\n";
                    String puissance = "Puissance (en dBm) : " + cellSignalGsm.getDbm() + "\n";

                    String cellInfoTexteGsm = CID + BSID + MCC + MNC + ARFCN + LAC + puissance + "\n\n\n";

                    if (texte.equals("Aucun")) {
                        texte = cellInfoTexteGsm;
                        technologie += "2G ";
                    } else {
                        texte += "\n\n" + cellInfoTexteGsm;
                    }

                }
            }
            return ("Technologie : " + technologie + "\n\n\n\n" + "Cellule :\n\n" + texte);
        } else {
            return ("Erreur: permissions non accordées");
        }
    }
}