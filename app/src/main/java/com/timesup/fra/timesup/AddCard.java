package com.timesup.fra.timesup;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddCard extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;

    EditText addCardEditText;
    Button addCardButton;

    long databaseSize;
    boolean sizeSetup;
    boolean valueRegistered = false;
    boolean notInside = true;
    boolean dontCare = false;

    Runnable createCard;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_card_layout);

        context = this;

        addCardEditText = findViewById(R.id.addCardEditText);
        addCardButton = findViewById(R.id.addCardButton);

        addCardEditText.requestFocus();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CardList");

        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                databaseSize = dataSnapshot.getChildrenCount();

                if (!sizeSetup)
                {
                    sizeSetup = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });
    }

    public void launchCreateNewCard(View view)
    {
        String name = addCardEditText.getText().toString();

        if (sizeSetup && name.length() > 0)
        {
            addCardEditText.setText("");
            createNewCard(name);
        }
    }

    void createNewCard(final String name)
    {
        final Handler registeredHandler = new Handler();
        final Handler checkHandler = new Handler();

        dontCare = false;
        notInside = true;
        createCard = new Runnable()
        {
            @Override
            public void run()
            {
                ref.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        {
                            if (postSnapshot.getValue(String.class).toLowerCase().equals(name.toLowerCase()))
                            {
                                notInside = false;
                                break;
                            }
                        }

                        if (notInside || dontCare)
                        {
                            Toast.makeText(getApplicationContext(), "Nouvelle carte " + name + " ajoutée avec succès !", Toast.LENGTH_LONG).show();

                            final long originalID = databaseSize;
                            System.out.println("Trying to connect with number " + originalID + " with name " + name);

                            if (dataSnapshot.child(""+originalID).getValue() == null)
                            {
                                ref.child(""+originalID).setValue(name);
                                System.out.println("Registering " + name + "...");

                                checkHandler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        ref.addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.child(""+originalID).getValue(String.class).equals(name))
                                                {
                                                    System.out.println("Checking verified with " + name + ", everything went good.");
                                                }
                                                else
                                                {
                                                    System.out.println("Checking verified with " + name + ", problem encountered. Trying again...");
                                                    createNewCard(name);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error)
                                            {
                                                // Failed to read value
                                                Log.w("samarchpa", "Failed to read value.", error.toException());
                                            }
                                        });
                                    }
                                }, 1000);
                            }
                            else
                            {
                                registeredHandler.postDelayed(createCard,1000);
                            }
                        }
                        else
                        {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            dontCare = true;
                                            registeredHandler.postDelayed(createCard,1000);
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            dontCare = false;
                                            Toast.makeText(getApplicationContext(), "La carte " + name + " n'a pas été ajoutée.", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("La carte " + name + " existe déjà dans la base de données. Voulez-vous vraiment l'ajouter ?").setPositiveButton("Oui", dialogClickListener)
                                    .setNegativeButton("Non", dialogClickListener).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error)
                    {
                        // Failed to read value
                        Log.w("samarchpa", "Failed to read value.", error.toException());
                    }
                });
            }
        };

        registeredHandler.postDelayed(createCard, 0);
    }
}

