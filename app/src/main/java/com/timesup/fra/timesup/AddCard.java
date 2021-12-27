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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AddCard extends AppCompatActivity
{
    public static final int SIMILARITY_PERCENTAGE_LIMIT = 50;

    DatabaseReference ref;
    FirebaseDatabase database;

    EditText addCardEditText;
    Button addCardButton;

    long databaseSize;
    boolean sizeSetup;
    boolean notInside = true;
    boolean dontCare = false;

    ArrayList<String> cardsToRemove;
    Map<String, Integer> similarCards = new HashMap<String, Integer>();
    String valueOfId = "";

    Runnable createCard;
    Context context;

    ArrayList<String> databaseList;
    ArrayList<String> closeCardsList = new ArrayList<>();

    String databaseCard;
    String closestCard;
    int biggestDifference;

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

    void createNewCard(final String cardName)
    {
        final Handler registeredHandler = new Handler();
        final Handler checkHandler = new Handler();

        dontCare = false;
        similarCards.clear();
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
                            databaseCard = postSnapshot.getValue(String.class);
                            int similarityPercentage = (int) Math.floor(100*similarity(cardName, databaseCard));

                            if (similarityPercentage > SIMILARITY_PERCENTAGE_LIMIT)
                            {
                                similarCards.put(databaseCard, similarityPercentage);
                            }
                        }

                        if (similarCards.isEmpty() || dontCare)
                        {
                            Toast.makeText(getApplicationContext(), "Nouvelle carte " + cardName + " ajoutée avec succès !", Toast.LENGTH_LONG).show();

                            final long originalID = databaseSize;
                            System.out.println("Trying to connect with number " + originalID + " with name " + cardName);

                            if (dataSnapshot.child(""+originalID).getValue() == null)
                            {
                                ref.child(""+originalID).setValue(cardName);
                                System.out.println("Registering " + cardName + "...");

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
                                                if (dataSnapshot.child(""+originalID).getValue(String.class).equals(cardName))
                                                {
                                                    System.out.println("Checking verified with " + cardName + ", everything went right.");
                                                }
                                                else
                                                {
                                                    System.out.println("Checking verified with " + cardName + ", problem encountered. Trying again...");
                                                    createNewCard(cardName);
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
                                            Toast.makeText(getApplicationContext(), "La carte " + cardName + " n'a pas été ajoutée.", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            };

                            Object[] sortedSimilarCards = getSortedCardList(similarCards);
                            String similarCardsString = "";

                            for (Object element : sortedSimilarCards) {
                                similarCardsString += " - " + ((Map.Entry<String, Integer>) element).getKey() + " (" + ((Map.Entry<String, Integer>) element).getValue() + "%)" + "\n";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("La carte " + cardName + " est similaires aux cartes existantes suivantes : " + "\n" + similarCardsString + "Voulez-vous vraiment l'ajouter ?").setPositiveButton("Oui", dialogClickListener)
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

        registeredHandler.post(createCard);
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

    private Object[] getSortedCardList(Map<String, Integer> cardList) {
        Object[] sortedCardList = cardList.entrySet().toArray();

        Arrays.sort(sortedCardList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });

        return sortedCardList;
    }
}