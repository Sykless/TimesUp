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

import java.util.ArrayList;
import java.util.Collections;

public class AddCard extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;

    EditText addCardEditText;
    Button addCardButton;

    long databaseSize;
    boolean sizeSetup;
    boolean notInside = true;
    boolean dontCare = false;

    ArrayList<String> cardsToRemove;
    ArrayList<Integer> cardsIdToRemove;
    String valueOfId = "";

    Runnable createCard;
    Context context;

    ArrayList<String> databaseList;

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

        // checkDoublons();

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

    void checkDoublons()
    {
        final Handler registeredHandler = new Handler();

        dontCare = false;
        notInside = true;
        cardsToRemove = new ArrayList<>();
        cardsIdToRemove = new ArrayList<>();

        Runnable checkDoublonsProcedure = new Runnable()
        {
            @Override
            public void run()
            {
                ref.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot)
                    {
                        long databaseSize = dataSnapshot.getChildrenCount();

                        for (long i = 0 ; i < databaseSize ; i++)
                        {
                            String cardReference = dataSnapshot.child(String.valueOf(i)).getValue(String.class);
                            String card = dataSnapshot.child(String.valueOf(1000)).getValue(String.class);

                            if (card != null)
                            {
                                System.out.println("OUI");
                            }

                            for (long j = i + 1 ; j < databaseSize ; j++)
                            {
                                valueOfId = String.valueOf(j);

                                String cardToCompare = dataSnapshot.child(String.valueOf(j)).getValue(String.class);
                                double difference = similarity(cardReference, cardToCompare);

                                if (difference == 1)
                                {
                                    if (!cardsToRemove.contains(cardReference) && !cardsToRemove.contains(cardToCompare))
                                    {
                                        cardsToRemove.add(cardReference);
                                        cardsIdToRemove.add((int) i);
                                        System.out.println("Card " + cardReference + " (id = " + i + ") similar to card " + cardToCompare + " (id = " + j + ") at " + Math.round(100 * difference) + "%");
                                    }
                                }
                            }
                        }

                        Collections.sort(cardsToRemove, String.CASE_INSENSITIVE_ORDER);
                        Collections.reverse(cardsToRemove);

                        System.out.println("cardToRemove : " + cardsToRemove);
                        System.out.println("cardIdsToRemove : " + cardsIdToRemove);
                        System.out.println();
                        System.out.println("Number of similar cards : " + cardsToRemove.size());
                        Toast.makeText(AddCard.this, "Number of similar cards : " + cardsToRemove.size(), Toast.LENGTH_SHORT).show();

                        initiateDeletion();
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

        registeredHandler.postDelayed(checkDoublonsProcedure, 0);
    }

    void initiateDeletion()
    {
        databaseList = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                long databaseSize = dataSnapshot.getChildrenCount();

                for (long i = 0 ; i < cardsIdToRemove.size() ; i++)
                {
                    dataSnapshot.child(""+cardsIdToRemove.get((int) i)).getRef().setValue(null);
                }

                for (long i = 0 ; i < databaseSize ; i++)
                {
                    String card = dataSnapshot.child(String.valueOf(i)).getValue(String.class);

                    if (card != null && !cardsIdToRemove.contains((int) i))
                    {
                        databaseList.add(card);
                        dataSnapshot.child(""+i).getRef().setValue(null);
                    }
                }

                for (long i = 0 ; i < databaseList.size() ; i++)
                {
                    dataSnapshot.child(""+i).getRef().setValue(databaseList.get((int) i));
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
                                                    System.out.println("Checking verified with " + name + ", everything went right.");
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


    public String removeAccents(String card)
    {
        String newCard = "";

        for (char character : card.toCharArray())
        {
            switch (character)
            {
                case 'é':
                    newCard += 'e';
                    break;
                case 'è':
                    newCard += 'e';
                    break;
                case 'ê':
                    newCard += 'e';
                    break;
                case 'ë':
                    newCard += 'e';
                    break;
                case 'ô':
                    newCard += 'o';
                    break;
                case 'ö':
                    newCard += 'o';
                    break;
                case 'î':
                    newCard += 'i';
                    break;
                case 'ï':
                    newCard += 'i';
                    break;
                case 'â':
                    newCard += 'a';
                    break;
                case 'à':
                    newCard += 'a';
                    break;
                case 'ä':
                    newCard += 'a';
                    break;
                case 'û':
                    newCard += 'u';
                    break;
                case 'ü':
                    newCard += 'u';
                    break;
                case 'ù':
                    newCard += 'u';
                    break;
                case 'ç':
                    newCard += 'c';
                    break;
                default:
                    newCard += character;
            }
        }

        return newCard;
    }

    public double similarity(String s1, String s2)
    {
        String longer = s1, shorter = s2;

        if (s1.length() < s2.length())
        {
            longer = s2; shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    public int editDistance(String s1, String s2)
    {
        s1 = removeAccents(s1.toLowerCase());
        s2 = removeAccents(s2.toLowerCase());

        int[] costs = new int[s2.length() + 1];

        for (int i = 0; i <= s1.length() ; i++)
        {
            int lastValue = i;
            for (int j = 0; j <= s2.length() ; j++)
            {
                if (i == 0)
                {
                    costs[j] = j;
                }
                else
                {
                    if (j > 0)
                    {
                        int newValue = costs[j - 1];

                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                        {
                            newValue = Math.min(Math.min(newValue, lastValue),costs[j]) + 1;
                        }

                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
            {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}