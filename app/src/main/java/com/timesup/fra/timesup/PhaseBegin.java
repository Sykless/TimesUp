package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class PhaseBegin extends AppCompatActivity
{
    TextView teamName;
    String timer;
    int phaseNumber;
    int teamNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phase_begin_layout);

        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        ArrayList<Team> teamList = app.getTeamList();

        Intent intent = getIntent();
        timer = intent.getStringExtra("timer");
        teamNumber = intent.getIntExtra("teamNumber",0);
        phaseNumber = intent.getIntExtra("phaseNumber",1);

        teamName = findViewById(R.id.teamName);
        teamName.setText(teamList.get(teamNumber).getTeamName());
    }

    public void goToStart(View view)
    {
        Intent intent;

        if (phaseNumber == 1)
        {
            intent = new Intent(this,PhaseOne.class);
        }
        else
        {
            intent = new Intent(this,PhaseTwoThree.class);
        }

        intent.putExtra("phaseNumber",phaseNumber);
        intent.putExtra("teamNumber",teamNumber);
        intent.putExtra("timer",timer);
        startActivity(intent);
        finish();
    }
}
