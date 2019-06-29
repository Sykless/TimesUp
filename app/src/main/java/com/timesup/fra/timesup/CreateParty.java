package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreateParty extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;

    long databaseSize;
    long originalSize = -1;
    boolean sizeSetup = false;
    boolean customSetup = false;

    TextView numberOfCards;
    TextView numberOfCardsText;
    TextView completedTextView;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove title bar
        setContentView(R.layout.loading);

        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        final long checkpoint = app.getCheckpoint();

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
                    setContentView(R.layout.create_party_layout);

                    numberOfCards = findViewById(R.id.numberOfCards);
                    numberOfCardsText = findViewById(R.id.numberOfCardsText);
                    completedTextView = findViewById(R.id.completedTextView);
                    startButton = findViewById(R.id.startButton);

                    ref.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (checkpoint == -1)
                            {
                                startButton.setText("Commencer la recherche");
                                numberOfCards.setVisibility(View.INVISIBLE);
                                numberOfCardsText.setVisibility(View.INVISIBLE);
                                originalSize = databaseSize;
                            }
                            else
                            {
                                startButton.setText("Annuler la recherche");
                                originalSize = checkpoint;

                                long totalNumber = databaseSize - originalSize;
                                numberOfCardsText.setVisibility(View.VISIBLE);
                                numberOfCards.setVisibility(View.VISIBLE);
                                numberOfCards.setText(String.valueOf(totalNumber));

                                if (totalNumber >= 1)
                                {
                                    completedTextView.setVisibility(View.VISIBLE);
                                    startButton.setText("Commencer à jouer");
                                }
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
                else
                {
                    long totalNumber = databaseSize - originalSize;
                    numberOfCards.setText(String.valueOf(totalNumber));

                    if (totalNumber >= 2)
                    {
                        completedTextView.setVisibility(View.VISIBLE);
                        startButton.setText("Commencer à jouer");
                    }
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

    public void launchResearch(View view)
    {
        final TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        long checkpoint = app.getCheckpoint();

        if (checkpoint == -1)
        {
            numberOfCardsText.setVisibility(View.VISIBLE);
            numberOfCards.setVisibility(View.VISIBLE);
            numberOfCards.setText("0");
            startButton.setText("Annuler la recherche");

            app.setCheckpoint(databaseSize);
            originalSize = databaseSize;
        }
        else
        {
            long totalNumber = databaseSize - originalSize;

            if (totalNumber >= 2)
            {
                ref.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        ArrayList<String> cardList = new ArrayList<>();

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        {
                            if (Integer.valueOf(postSnapshot.getKey()) >= originalSize)
                            {
                                cardList.add(postSnapshot.getValue(String.class));
                            }
                        }

                        app.setCardList(cardList);

                        Intent intent = new Intent(getApplicationContext(),PhaseSetup.class);
                        intent.putExtra("phaseNumber",1);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError error)
                    {
                        // Failed to read value
                        Log.w("samarchpa", "Failed to read value.", error.toException());
                    }
                });
            }
            else
            {
                startButton.setText("Commencer la recherche");
                app.setCheckpoint(-1);
                originalSize = -1;
                numberOfCardsText.setVisibility(View.INVISIBLE);
                numberOfCards.setVisibility(View.INVISIBLE);
            }
        }
    }
}

