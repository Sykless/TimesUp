package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

public class Scoreboard extends AppCompatActivity
{
    DatabaseReference ref;
    FirebaseDatabase database;
    TimesUpParameters app;

    LinearLayout scoreBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoreboard_layout);

        scoreBoard = findViewById(R.id.scoreBoard);

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
                    Toast.makeText(app, "Nombre de cartes maximum atteint, mélangez les cartes.", Toast.LENGTH_SHORT).show();
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

        LinearLayout.LayoutParams teamParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        teamParams.setMargins(32,32,10,10);

        LinearLayout.LayoutParams roundParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        roundParams.setMargins(64,5,10,10);


        for (Team team : teamList)
        {
            int[] teamScore = {team.getScorePhaseOne(), team.getScorePhaseTwo(), team.getScorePhaseThree()};
            TextView teamName = new TextView(this);

            teamName.setText(team.getTeamName() + " : " + (teamScore[0] + teamScore[1] + teamScore[2]));
            teamName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80);
            teamName.setTypeface(ResourcesCompat.getFont(this,R.font.fontdiner_swanky));
            teamName.setTextColor(getResources().getColor(R.color.yellow));
            teamName.setLayoutParams(teamParams);

            scoreBoard.addView(teamName);

            for (int phase = 0 ; phase < 3 ; phase++)
            {
                TextView score = new TextView(this);

                score.setText("Manche " + (phase + 1) + " : " + teamScore[phase]);
                score.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
                score.setTypeface(ResourcesCompat.getFont(this,R.font.fontdiner_swanky));
                score.setTextColor(getResources().getColor(R.color.yellow));
                score.setLayoutParams(roundParams);

                scoreBoard.addView(score);
            }


        }
    }

    public void goToHome(View view)
    {
        Intent intent = new Intent(this,Home.class);
        startActivity(intent);
    }
}
