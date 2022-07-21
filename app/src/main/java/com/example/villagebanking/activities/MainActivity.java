package com.example.villagebanking.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.villagebanking.databinding.ActivityMainBinding;



public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();

    }

    private void setListener()
    {
        binding.chatroomlayout.setOnClickListener( v -> startActivity(new Intent(getApplicationContext(), SignInActivity.class)));
    }



}
