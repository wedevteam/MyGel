package com.wedev.mygel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.wedev.mygel.R;
import com.wedev.mygel.SignInActivity;

public class RegistrazioneEffettuataFragment extends Fragment {

    Button entra;
    View view;

    public RegistrazioneEffettuataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registrazione_effettuata, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        setUI();
    }
    private void setUI() {
        entra = view.findViewById(R.id.entra);
        entra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInActivity signin = (SignInActivity)getActivity();
                assert signin != null;
                signin.goMain();
            }
        });
    }
}