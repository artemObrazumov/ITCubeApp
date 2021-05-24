package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    // View-элементы
    private EditText email_input;
    private Button submit_button;

    // Окно для уведомления о загрузке
    private ProgressDialog dialog;

    // Firebase
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        // Инициализация элементов
        email_input = findViewById(R.id.input_email);
        submit_button = findViewById(R.id.submit_button);

        dialog = new ProgressDialog(RecoverPasswordActivity.this);
        dialog.setMessage(getString(R.string.sending_email));

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecoverPassword();
            }
        });

        auth = FirebaseAuth.getInstance();
    }

    // Восстановление аккаунта
    private void RecoverPassword() {
        String email = email_input.getText().toString().trim();
        dialog.show();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_was_sent), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_sent_error), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.email_sent_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}