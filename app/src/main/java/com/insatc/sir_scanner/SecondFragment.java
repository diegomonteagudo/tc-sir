package com.insatc.sir_scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.insatc.sir_scanner.databinding.FragmentSecondBinding;
import com.insatc.sir_scanner.network.CellData;
import com.insatc.sir_scanner.network.InternetTraffic;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        binding.dataDispButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = CellData.getCellData(getActivity());
                binding.cellInfoTextView.setText(info);
            }
        });

        binding.connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InternetTraffic.generateTraffic(getActivity(), binding.connectionButton);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}