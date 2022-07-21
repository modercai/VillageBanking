package com.example.villagebanking.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.villagebanking.databinding.ActivitySignInBinding;
import com.example.villagebanking.utilities.Constants;
import com.example.villagebanking.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener()
    {
        binding.signupid.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.loginButton.setOnClickListener(v ->
        {
            if(isValidSignInDetails())
            {
                signIn();
            }
        });
    }

    private void signIn()
    {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL,binding.emailadd.getText().toString()).whereEqualTo(Constants.KEY_PASSWORD,binding.pass.getText().toString()).get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
            {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                Intent intent = new Intent(getApplicationContext(),Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else
            {
                loading(false);
                showToast("Login Error");
            }
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails()
    {
        if(binding.emailadd.getText().toString().trim().isEmpty())
        {
            showToast("Enter Email address");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.emailadd.getText().toString()).matches())
        {
            showToast("Enter Correct Email Address");
            return false;
        }else if(binding.pass.getText().toString().isEmpty())
        {
            showToast("Enter Password");
            return false;
        }else
        {
            return true;
        }

    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.loginButton.setVisibility(View.INVISIBLE);
            binding.loginprogressbar.setVisibility(View.VISIBLE);
        }else
        {
            binding.loginButton.setVisibility(View.VISIBLE);
            binding.loginprogressbar.setVisibility(View.INVISIBLE);
        }
    }
}