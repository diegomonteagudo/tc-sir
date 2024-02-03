package com.insatc.sir_scanner.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.insatc.sir_scanner.R;
import com.insatc.sir_scanner.databinding.FragmentTechChoiceBinding;


public class TechChoiceFragment extends Fragment {

    private FragmentTechChoiceBinding binding;

    private boolean isUpdating = true;

    // Déclarer le Handler en tant que variable membre de la classe
    private final Handler handler = new Handler();



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = FragmentTechChoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vérifier les autorisations si nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 1);
        } else {
            changeButtonColor();
            updateOperatorName();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) { // Vérifier si le fragment est attaché
                        changeButtonColor();
                        handler.postDelayed(this, 2000); // Mise à jour toutes les 2 secondes
                    }
                }
            }, 2000);
        }

        binding.button2G.setOnClickListener(v -> {
            openNetworkSettings("2G");
        });

        binding.button3G.setOnClickListener(v -> {
            openNetworkSettings("3G");
        });

        binding.button4G.setOnClickListener(v -> {
            openNetworkSettings("4G");
        });

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager telephonyManager = (TelephonyManager) requireContext().getSystemService(Context.TELEPHONY_SERVICE);

                String operatorName = telephonyManager.getNetworkOperatorName();
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                intent.putExtra("Technologie", getCurrentTechnology());
                intent.putExtra("Operateur", operatorName);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isUpdating = false;
        handler.removeCallbacksAndMessages(null); // Supprimer tous les callbacks du Handler
        binding = null;
    }

    private void openNetworkSettings(String technology) {
        Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        startActivity(intent);
    }
    private void changeButtonColor() {
        int colorGrey = ContextCompat.getColor(requireContext(), R.color.grey);
        binding.button2G.setBackgroundColor(colorGrey);
        binding.button3G.setBackgroundColor(colorGrey);
        binding.button4G.setBackgroundColor(colorGrey);

        String currentTechnology = getCurrentTechnology();

        if (currentTechnology.equals(binding.button2G.getText().toString())) {
            int color = ContextCompat.getColor(requireContext(), R.color.green);
            binding.button2G.setBackgroundColor(color);
        }
        else if (currentTechnology.equals(binding.button3G.getText().toString())) {
            int color = ContextCompat.getColor(requireContext(), R.color.green);
            binding.button3G.setBackgroundColor(color);
        }
        else if(currentTechnology.equals(binding.button4G.getText().toString())) {
            int color = ContextCompat.getColor(requireContext(), R.color.green);
            binding.button4G.setBackgroundColor(color);
        }
    }

    private String getCurrentTechnology() {
        TelephonyManager telephonyManager = (TelephonyManager) requireContext().getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission") int networkType = telephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }

    }
    @SuppressLint("SetTextI18n")
    private void updateOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) requireContext().getSystemService(Context.TELEPHONY_SERVICE);

        String operatorName = telephonyManager.getNetworkOperatorName();

        binding.textviewOperator.setText("Opérateur : " + operatorName);
    }

}


