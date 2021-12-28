package com.timesup.fra.timesup;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Home extends AppCompatActivity
{
    DatabaseReference refCardList;
    FirebaseDatabase database;

    Button shuffleButton;
    Button addCardButton;
    Button playButton;
    ImageButton debugButton;
    ImageView logo;

    FirebaseAuth mAuth;

    int logoClicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        shuffleButton = findViewById(R.id.shuffleButton);
        addCardButton = findViewById(R.id.addCardButton);
        playButton = findViewById(R.id.playButton);
        debugButton = findViewById(R.id.debugButton);
        logo = findViewById(R.id.logo);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // User is signed in
        if (user != null) {
            playButton.setVisibility(View.VISIBLE);
            shuffleButton.setVisibility(View.VISIBLE);

            // Admin user
            if (!user.isAnonymous()) {
                addCardButton.setVisibility(View.VISIBLE);
                debugButton.setVisibility(View.VISIBLE);
            }
        }
        // No user is signed in
        else {
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    playButton.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        shuffleButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(Home.this, "Échec de l'authentification.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        database = FirebaseDatabase.getInstance();
        refCardList = database.getReference("CardList");

        logo.setOnClickListener( new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            logoClicks++;

            if (logoClicks > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setTitle("Entrez le mot de passe admin :");

                // Set up the input
                final EditText input = new EditText(Home.this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(new PasswordTransformationMethod());

                FrameLayout container = new FrameLayout(Home.this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 100;
                params.rightMargin = 100;
                input.setLayoutParams(params);
                container.addView(input);

                builder.setView(container);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signInWithEmailAndPassword("admin@timesup.fra", input.getText().toString())
                                .addOnCompleteListener(Home.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            addCardButton.setVisibility(View.VISIBLE);
                                            debugButton.setVisibility(View.VISIBLE);
                                        } else {
                                            Toast.makeText(Home.this, "Échec de l'authentification.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });

                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        }
    });
    }

    public void shuffleDatabase(View view)
    {
        shuffleButton.setText("Mélange en cours...");
        shuffleButton.setClickable(false);
        shuffleButton.getBackground().setAlpha(128);

        refCardList.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<String> cardList = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    cardList.add(postSnapshot.getValue(String.class));
                }

                Collections.shuffle(cardList);

                for (int i = 0 ; i < cardList.size() ; i++)
                {
                    refCardList.child(String.valueOf(i)).setValue(cardList.get(i));
                }

                shuffleButton.setText("Mélanger les cartes");
                shuffleButton.setClickable(true);
                shuffleButton.getBackground().setAlpha(255);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });
    }

    public void goToAdd(View view)
    {
        Intent intent = new Intent(this,AddCard.class);
        startActivity(intent);
    }

    public void goToPhaseSetup(View view)
    {
        Intent intent = new Intent(this,AddTeams.class);
        startActivity(intent);
    }

    public void goToDebugMode(View view)
    {
        Intent intent = new Intent(this,DebugMode.class);
        startActivity(intent);
    }
}

