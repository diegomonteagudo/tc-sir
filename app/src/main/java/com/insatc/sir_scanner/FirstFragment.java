package com.insatc.sir_scanner;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.insatc.sir_scanner.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);



        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.buttonRapport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener le répertoire interne "Documents"
                File documentsDirectory = new File(requireActivity().getExternalFilesDir(null), "Documents");

                // Obtener la liste des fichiers CSV dans le répertoire
                List<String> csvFiles = getListOfCSVFiles(documentsDirectory);

                // Afficher la liste des fichiers dans la console
                for (String fileName : csvFiles) {
                    System.out.println(fileName);
                }

                // Exemple d'utilisation d'une boîte de dialogue
                showFileDialog(csvFiles);

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<String> getListOfCSVFiles(File directory) {
        List<String> csvFiles = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            // Liste tous les fichiers dans le répertoire
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                        // Ajoute le nom du fichier à la liste des fichiers CSV
                        csvFiles.add(file.getName());
                    }
                }
            }
        }

        return csvFiles;
    }

    private void showFileDialog(final List<String> fileList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sélectionnez un fichier CSV");

        // Convertit la liste en un tableau pour une utilisation dans la boîte de dialogue
        final CharSequence[] items = fileList.toArray(new CharSequence[fileList.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // L'utilisateur a sélectionné un fichier
                String selectedFileName = fileList.get(which);
                // Faites quelque chose avec le nom du fichier sélectionné, par exemple, le transmettre à une autre activité
                Toast.makeText(requireContext(), "Fichier sélectionné : " + selectedFileName, Toast.LENGTH_SHORT).show();
                File externalDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                String csvFilePath = new File(externalDir, selectedFileName).getAbsolutePath();
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("Path", csvFilePath);
                startActivity(intent);            }
        });

        builder.show();
    }

}