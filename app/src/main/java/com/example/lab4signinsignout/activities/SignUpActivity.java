package com.example.lab4signinsignout.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab4signinsignout.R;
import com.example.lab4signinsignout.databinding.ActivitySignUpBinding;

/**
 * Sign Up screen. Lets new users register and navigate back to Sign In.
 */
public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    /**
     * Inflates the layout with view binding and sets up click listeners.
     *
     * @param savedInstanceState the saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_sign_up);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    /**
     * Sets the click listeners for the screen's views. Tapping the
     * "Sign in" text goes back to the previous screen (Sign In).
     */
    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
    }


}