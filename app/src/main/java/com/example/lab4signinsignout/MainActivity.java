package com.example.lab4signinsignout;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lab4signinsignout.activities.SignInActivity;
import com.example.lab4signinsignout.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Displays the authenticated user's email and allows signing out. */
public class MainActivity extends AppCompatActivity {
    /** Checks authentication before displaying the signed-in screen.
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            openSignIn();
            return;
        }
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.textSignedIn.setText(getString(R.string.signed_in_as, user.getEmail()));
        binding.buttonSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            openSignIn();
        });
    }

    /** Clears authenticated screens from the back stack and returns to sign in. */
    private void openSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
