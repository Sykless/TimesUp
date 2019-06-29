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

public class ChooseGameMode extends AppCompatActivity
{
    TimesUpParameters app;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_game_mode_layout);
    }

    public void goToClassic(View view)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("CardList");

        app = (TimesUpParameters) getApplicationContext();

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                ArrayList<String> cardList = new ArrayList<>();
                long id = app.getCardFirstID();

                for (long i = id ; i < 40 + id ; i++)
                {
                    cardList.add(dataSnapshot.child(String.valueOf(i)).getValue(String.class));
                }

                app.setCardList(cardList);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });

        Intent intent = new Intent(this,PhaseSetup.class);
        intent.putExtra("phaseNumber",1);
        startActivity(intent);
    }

    public void goToPersonnalize(View view)
    {
        Intent intent = new Intent(this,CreateParty.class);
        startActivity(intent);
    }
}
