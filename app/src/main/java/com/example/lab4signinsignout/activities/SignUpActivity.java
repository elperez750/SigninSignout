package com.example.lab4signinsignout.activities;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.lab4signinsignout.R;
import com.example.lab4signinsignout.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Registers Firebase accounts and stores profile information without passwords. */
public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private boolean loading;
    private FirebaseUser pendingUser;
    private Map<String, Object> pendingProfile;
    private String encodedImage;
    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && !loading && pendingUser == null) selectImage(uri);
            });

    /** Initializes the registration form and its listeners.
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        binding.textSignIn.setOnClickListener(v -> finish());
        binding.buttonSignUp.setOnClickListener(v -> signUp());
        binding.layoutImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        if (savedInstanceState != null) {
            encodedImage = savedInstanceState.getString("encoded_image");
            if (encodedImage != null) {
                byte[] bytes = Base64.decode(encodedImage, Base64.NO_WRAP);
                binding.imageProfile.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                binding.textAddImage.setVisibility(View.GONE);
            }
        }
    }

    /** Retains the selected thumbnail when the activity is recreated.
     * @param outState bundle receiving the encoded thumbnail
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("encoded_image", encodedImage);
    }

    /** Loads a sampled thumbnail, encodes it, and updates the profile preview.
     * @param uri image selected through the system content picker
     */
    private void selectImage(Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(input, null, options);
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                throw new IOException("Unsupported image");
            }
            options.inSampleSize = 1;
            while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 512) {
                options.inSampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            Bitmap bitmap;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(input, null, options);
            }
            if (bitmap == null) throw new IOException("Cannot decode image");
            String image = encodeImage(bitmap);
            encodedImage = image;
            binding.imageProfile.setImageBitmap(bitmap);
            binding.textAddImage.setVisibility(View.GONE);
        } catch (IOException | RuntimeException exception) {
            showMessage(getString(R.string.image_load_failed));
        }
    }

    /** Resizes and compresses a bitmap into a Base64 JPEG for the profile document.
     * @param bitmap non-null bitmap to encode
     * @return Base64 JPEG without line breaks, with a maximum dimension of 256 pixels
     * @throws IllegalArgumentException if the bitmap is null or cannot be compressed
     */
    private String encodeImage(Bitmap bitmap) {
        if (bitmap == null) throw new IllegalArgumentException("Bitmap is required");
        float scale = Math.min(1f, 256f / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,
                Math.max(1, Math.round(bitmap.getWidth() * scale)),
                Math.max(1, Math.round(bitmap.getHeight() * scale)), true);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!resized.compress(Bitmap.CompressFormat.JPEG, 70, output)) {
                throw new IllegalArgumentException("Cannot compress bitmap");
            }
            return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
        } finally {
            if (resized != bitmap) resized.recycle();
        }
    }

    /** Validates all required text fields and creates an email/password account. */
    private void signUp() {
        if (loading) return;
        // Retry the profile write instead of attempting to create the same account twice.
        if (pendingUser != null) {
            saveProfile();
            return;
        }
        String firstName = binding.inputFirstName.getText().toString().trim();
        String lastName = binding.inputLastName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();
        String confirmation = binding.inputConfirmPassword.getText().toString();
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || password.trim().isEmpty() || confirmation.trim().isEmpty()) {
            showMessage(getString(R.string.fill_all_fields));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.setError(getString(R.string.invalid_email));
            binding.inputEmail.requestFocus();
            showMessage(getString(R.string.invalid_email));
            return;
        }
        if (password.length() < 6) {
            binding.inputPassword.setError(getString(R.string.password_too_short));
            binding.inputPassword.requestFocus();
            showMessage(getString(R.string.password_too_short));
            return;
        }
        if (!password.equals(confirmation)) {
            binding.inputConfirmPassword.setError(getString(R.string.password_mismatch));
            binding.inputConfirmPassword.requestFocus();
            showMessage(getString(R.string.password_mismatch));
            return;
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("first_name", firstName);
        profile.put("last_name", lastName);
        profile.put("email", email);
        if (encodedImage != null) profile.put("image", encodedImage);
        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setLoading(false);
                showMessage(getString(R.string.sign_up_failed,
                        task.getException() == null ? getString(R.string.try_again)
                                : (Object) task.getException().getLocalizedMessage()));
                return;
            }
            pendingUser = task.getResult().getUser();
            pendingProfile = profile;
            binding.inputPassword.setText("");
            binding.inputConfirmPassword.setText("");
            saveProfile();
        });
    }

    /** Saves the profile under the Auth UID; failed writes can be retried safely. */
    private void saveProfile() {
        setLoading(true);
        FirebaseFirestore.getInstance().collection("Users").document(pendingUser.getUid())
                .set(pendingProfile).addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        auth.signOut();
                        pendingUser = null;
                        showMessage(getString(R.string.sign_up_success));
                        finish();
                    } else {
                        binding.buttonSignUp.setText(R.string.retry_profile);
                        showMessage(getString(R.string.profile_save_failed));
                    }
                });
    }

    /** Shows validation and Firebase results.
     * @param message message to show in a toast
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /** Prevents repeated submissions and locks profile fields after account creation.
     * @param busy whether a Firebase request is in progress
     */
    private void setLoading(boolean busy) {
        loading = busy;
        binding.progressBar.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
        binding.buttonSignUp.setVisibility(busy ? View.INVISIBLE : View.VISIBLE);
        binding.buttonSignUp.setEnabled(!busy);
        binding.textSignIn.setEnabled(!busy);
        boolean editable = !busy && pendingUser == null;
        binding.layoutImage.setEnabled(editable);
        binding.inputFirstName.setEnabled(editable);
        binding.inputLastName.setEnabled(editable);
        binding.inputEmail.setEnabled(editable);
        binding.inputPassword.setEnabled(editable);
        binding.inputConfirmPassword.setEnabled(editable);
    }
}
