package com.timesup.fra.timesup;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class DebugMode extends AppCompatActivity {

    DatabaseReference ref;
    FirebaseDatabase database;

    ArrayList<String> cardsToRemove;
    ArrayList<Integer> cardsIdToRemove;
    String valueOfId = "";

    boolean notInside = true;
    boolean dontCare = false;

    ArrayList<String> databaseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_mode_layout);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CardList");

        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });

        checkDoublons();
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

                                if (difference >= 0.7) // 80%
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
                        Toast.makeText(DebugMode.this, "Number of similar cards : " + cardsToRemove.size(), Toast.LENGTH_SHORT).show();

                        // initiateDeletion();
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