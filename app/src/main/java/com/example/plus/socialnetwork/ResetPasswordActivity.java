package com.example.plus.socialnetwork;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPasswordActivity extends AppCompatActivity {


    private Button resetPasswordEmailSendButton;
    private EditText resetEmailInput;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth=FirebaseAuth.getInstance();


        mToolbar=findViewById(R.id.forget_pasword_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resetPasswordEmailSendButton=findViewById(R.id.reset_password_email_button);
        resetEmailInput=findViewById(R.id.reset_password_EMAIL);


        resetPasswordEmailSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String useremmail=resetEmailInput.getText().toString();
                if(TextUtils.isEmpty(useremmail))
                {
                    Toast.makeText(ResetPasswordActivity.this, "Enter an e mail", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(useremmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(ResetPasswordActivity.this, "Check your E-mail inbox", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                            }
                            else
                            {
                                String message=task.getException().getMessage().toString();
                                Toast.makeText(ResetPasswordActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
}
