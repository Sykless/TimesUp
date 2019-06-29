package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Home extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;

    long databaseSize;
    boolean sizeSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

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

                System.out.println("Database size : " + databaseSize);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });
    }

    public void shuffleDatabase(View view)
    {
        ref.addListenerForSingleValueEvent(new ValueEventListener()
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
                    ref.child(String.valueOf(i)).setValue(cardList.get(i));
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

    public void goToAdd(View view)
    {
        Intent intent = new Intent(this,AddCard.class);
        startActivity(intent);
    }

    public void goToChoose(View view)
    {
        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        long checkpoint = app.getCheckpoint();

        if (checkpoint == -1)
        {
            Intent intent = new Intent(this,AddTeams.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this,CreateParty.class);
            startActivity(intent);
        }
    }
}

