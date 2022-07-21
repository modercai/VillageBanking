package com.example.villagebanking.activities;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.villagebanking.databinding.ActivitySignUpBinding;
import com.example.villagebanking.utilities.Constants;
import com.example.villagebanking.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;


public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener()
    {
        binding.loginid.setOnClickListener(v -> onBackPressed());
        binding.signupButton.setOnClickListener(v -> {
            if (isValidSignUpDetails()){
                signUp();
            }
        });
        binding.imagelayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickimage.launch(intent);
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void signUp()
    {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.nameinput.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.emailaddress.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.password.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        db.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
            loading(false);
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
            preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
            preferenceManager.putString(Constants.KEY_NAME,binding.nameinput.getText().toString());
            Intent intent = new Intent(getApplicationContext(),Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(exception -> {
            loading(false);
            showToast(exception.getMessage());
        });
    }

    private String encodedImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);

    }

    private final ActivityResultLauncher<Intent> pickimage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.userimage.setImageBitmap(bitmap);
                            binding.imagetxt.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }catch(FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails()
    {
        if (encodedImage == null)
        {
            showToast("select Dp image");
            return false;
        }else if (binding.nameinput.getText().toString().trim().isEmpty())
        {
            showToast("Enter Your Name");
            return false;
        }else if (binding.emailaddress.getText().toString().trim().isEmpty())
        {
            showToast("Enter Email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailaddress.getText().toString()).matches())
        {
            showToast("Enter Correct Email");
            return false;
        }else if (binding.password.getText().toString().trim().isEmpty())
        {
            showToast("Enter Password");
            return false;
        }else if (binding.password1.getText().toString().trim().isEmpty())
        {
            showToast("Confirm Password");
            return false;
        }else if (!binding.password.getText().toString().equals(binding.password1.getText().toString()))
        {
            showToast("Password and Confirm Password must be the same");
            return false;
        }else {
            return true;
        }


        //setting the visibility if the of the progressbar
    }
    private void loading(Boolean isLoading)
    {
        if (isLoading)
        {
            binding.signupButton.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }else
        {
            binding.progressbar.setVisibility(View.INVISIBLE);
            binding.signupButton.setVisibility(View.VISIBLE);
        }
    }
}