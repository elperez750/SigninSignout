package com.example.lab4signinsignout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab4signinsignout.R;
import com.example.lab4signinsignout.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * Sign In screen. Lets existing users log in and navigate to Sign Up.
 */
public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    /**
     * Inflates the layout with view binding and sets up click listeners.
     *
     * @param savedInstanceState the saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_sign_in);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    /**
     * Sets the click listeners for the screen's views. Tapping the
     * "Create new account" text opens the Sign Up screen.
     */
    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        binding.buttonSignIn.setOnClickListener(v -> addDataToFireStore());
    }

    /**
     * Adds a test record to the "Users" collection in Firestore and shows a
     * toast on success or failure.
     */
    private void addDataToFireStore() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> data = new HashMap<>();
        data.put("first_name", "Steve");
        data.put("last_name", "Summers");

        database.collection("Users")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "data inserted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}