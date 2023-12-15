package com.insatc.sir_scanner;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellIdentityLte;
import android.telephony.TelephonyManager;
import android.widget.Toast;
public class CellData {
    public static void getCellData(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        boolean conditionPermission = context.checkSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if(conditionPermission){
            for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();

                    int cellId = cellIdentityLte.getPci();
                    int mcc = cellIdentityLte.getMcc();

                    Toast.makeText(context, "Cell ID: " + cellId + "\nMCC: " + mcc, Toast.LENGTH_LONG).show();
                    //Rappel MCC France = 208
                    //Pour les cellules auxquelles ont est pas associé, il affichera le max int 32
                }
            }
        }
    }
}