package com.insatc.sir_scanner.network;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellIdentityGsm;
import android.telephony.TelephonyManager;
import android.widget.Toast;
public class CellData {
    public static String getCellData(Context context) {
        String infoLTE = "Aucun";
        String infoWCDMA = "Aucun";
        String infoGSM = "Aucun";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        boolean conditionPermission = context.checkSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if(conditionPermission){
            for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    if(infoLTE == "Aucun") {
                        infoLTE = cellIdentityLte.toString();
                    } else {
                        infoLTE += "\n\n"+cellIdentityLte.toString();
                    }

                } else if (cellInfo instanceof CellInfoWcdma){
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    if(infoWCDMA == "Aucun") {
                        infoWCDMA = cellIdentityWcdma.toString();
                    } else {
                        infoWCDMA += "\n\n"+cellIdentityWcdma.toString();
                    }

                } else if (cellInfo instanceof CellInfoGsm){
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    if(infoGSM == "Aucun") {
                        infoGSM = cellIdentityGsm.toString();
                    } else {
                        infoGSM += "\n\n"+cellIdentityGsm.toString();
                    }

                }
            }
        }

        //Très brouillon et en plus, je crois qu'on ne peut avoir qu'une technologie à la fois donc
        //ça ne sert à rien de faire comme ça. C'est juste temporaire (même le code au dessus)
        return ("4G:\n\n"+infoLTE+"\n\n\n\n"+"3G:\n\n"+infoWCDMA+"\n\n\n\n"+
                "2G:\n\n"+infoGSM);
    }
}