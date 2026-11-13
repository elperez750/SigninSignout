package com.example.lab4signinsignout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lab4signinsignout.MainActivity;
import com.example.lab4signinsignout.R;
import com.example.lab4signinsignout.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;

/** Authenticates existing users with Firebase email/password authentication. */
public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private FirebaseAuth auth;
    private boolean loading;

    /** Initializes view binding and authentication listeners.
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> signIn());
    }

    /** Validates entered credentials before requesting Firebase authentication. */
    private void signIn() {
        if (loading) return;
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();
        if (email.isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.setError(getString(R.string.invalid_email));
            binding.inputEmail.requestFocus();
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                binding.inputPassword.setText("");
                Toast.makeText(this, R.string.sign_in_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.sign_in_failed,
                        task.getException() == null ? getString(R.string.try_again)
                                : (Object) task.getException().getLocalizedMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Updates the progress indicator and prevents duplicate requests.
     * @param busy whether authentication is in progress
     */
    private void setLoading(boolean busy) {
        loading = busy;
        binding.progressBar.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
        binding.buttonSignIn.setVisibility(busy ? View.INVISIBLE : View.VISIBLE);
        binding.buttonSignIn.setEnabled(!busy);
        binding.textCreateNewAccount.setEnabled(!busy);
        binding.inputEmail.setEnabled(!busy);
        binding.inputPassword.setEnabled(!busy);
    }
}
