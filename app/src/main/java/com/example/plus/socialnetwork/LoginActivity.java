package com.example.plus.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {


    private Button loginButton;
    private EditText userEmail,userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;
    //anim için..
    TextView logo1,logo2;
    LinearLayout layout1;

    //

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private ImageView googleSignInButton;
    private static final int RC_SIGN_IN=1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG="LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


/////Google..
        googleSignInButton=findViewById(R.id.google_signin_button);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this, "Connection to Google sign in failed...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


//////Google END...

        needNewAccountLink=findViewById(R.id.textView);
        loginButton=findViewById(R.id.login_button);
        userEmail=findViewById(R.id.login_email);
        userPassword=findViewById(R.id.login_password);
        forgetPasswordLink=findViewById(R.id.forget_pasword_link);


        ///anim
        layout1=findViewById(R.id.linearLayout);
        logo1=findViewById(R.id.txtlogo1);
        logo2=findViewById(R.id.txtlogo2);
        //


        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);


        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });


        forgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

    }


    ///////////////////////////Google
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN)
        {

            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait,while you are connecting with your Google account");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();



            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess())
            {
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please wait, while we are getting your information", Toast.LENGTH_SHORT).show();

            }
            else
            {
                Toast.makeText(this, "Can't get information", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }
    }




    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainActivity();
                            loadingBar.dismiss();

                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Can't connect, try again", Toast.LENGTH_SHORT).show();
                            sendUserToLoginnActivity();
                            loadingBar.dismiss();

                        }

                    }
                });
    }


    ///////////////////////////Google end









    private void AllowingUserToLogin()
    {
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please write your E mail", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please write your Password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {

                                sendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "You are logged in succesfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error occured : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null)
            sendUserToMainActivity();
        else
            animasyonlariCalistir();


        super.onStart();
    }


    private void animasyonlariCalistir() {

      /*  Animation animation = AnimationUtils.loadAnimation(this, R.anim.yukari);
        animation.reset();
        userEmail.clearAnimation();
        userEmail.startAnimation(animation);
        userPassword.clearAnimation();
        userPassword.startAnimation(animation); */


        Animation animation4 = AnimationUtils.loadAnimation(this, R.anim.push_left);
        animation4.reset();
        userPassword.clearAnimation();
        userPassword.startAnimation(animation4);


        Animation animation5 = AnimationUtils.loadAnimation(this, R.anim.push_right);
        animation5.reset();
        userEmail.clearAnimation();
        userEmail.startAnimation(animation5);


        Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.yukari);
        animation2.reset();
        loginButton.clearAnimation();
        loginButton.startAnimation(animation2);


        Animation animation3 = AnimationUtils.loadAnimation(this, R.anim.animation_yuklenme);
        animation3.reset();
        logo1.clearAnimation();
        logo1.startAnimation(animation3);
        logo2.clearAnimation();
        logo2.startAnimation(animation3);
        forgetPasswordLink.clearAnimation();
        forgetPasswordLink.startAnimation(animation3);
        needNewAccountLink.clearAnimation();
        needNewAccountLink.startAnimation(animation3);
        layout1.clearAnimation();
        layout1.startAnimation(animation3);


    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void sendUserToLoginnActivity()
    {
        Intent selfintent=new Intent(LoginActivity.this,LoginActivity.class);
        selfintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfintent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);

    }
}
