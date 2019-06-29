package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Scoreboard extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;
    TimesUpParameters app;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoreboard_layout);

        app = (TimesUpParameters) getApplicationContext();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("CardList");

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                long databaseSize = dataSnapshot.getChildrenCount();

                if (app.getCardFirstID() + 40 < databaseSize)
                {
                    app.setCardFirstID(app.getCardFirstID() + 40);
                }
                else
                {
                    Toast.makeText(app, "Nombre de cartes maximum atteint.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("samarchpa", "Failed to read value.", error.toException());
            }
        });

        ArrayList<Team> teamList = app.getTeamList();

        ((TextView) findViewById(R.id.team1)).setText(teamList.get(0).getTeamName() + " : " + (teamList.get(0).getScorePhaseOne() + teamList.get(0).getScorePhaseTwo() + teamList.get(0).getScorePhaseThree()));
        ((TextView) findViewById(R.id.team2)).setText(teamList.get(1).getTeamName() + " : " + (teamList.get(1).getScorePhaseOne() + teamList.get(1).getScorePhaseTwo() + teamList.get(1).getScorePhaseThree()));
        ((TextView) findViewById(R.id.textView2)).setText("Manche 1 : " + teamList.get(0).getScorePhaseOne());
        ((TextView) findViewById(R.id.textView3)).setText("Manche 2 : " + teamList.get(0).getScorePhaseTwo());
        ((TextView) findViewById(R.id.textView4)).setText("Manche 3 : " + teamList.get(0).getScorePhaseThree());
        ((TextView) findViewById(R.id.textView6)).setText("Manche 1 : " + teamList.get(1).getScorePhaseOne());
        ((TextView) findViewById(R.id.textView7)).setText("Manche 2 : " + teamList.get(1).getScorePhaseTwo());
        ((TextView) findViewById(R.id.textView8)).setText("Manche 3 : " + teamList.get(1).getScorePhaseThree());
    }

    public void goToHome(View view)
    {
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }
}
