package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PhaseSetup extends AppCompatActivity
{
    String minutesText;
    String secondsText;
    int teamNumber;
    int phaseNumber;

    EditText timer;
    Button startButton;
    TextView phaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phase_setup_layout);

        timer = findViewById(R.id.timer);
        startButton = findViewById(R.id.startButton);
        phaseName = findViewById(R.id.phaseName);

        Intent intent = getIntent();
        teamNumber = intent.getIntExtra("teamNumber",0);
        phaseNumber = intent.getIntExtra("phaseNumber",1);

        phaseName.setText("Manche " + phaseNumber);

        if (phaseNumber == 1)
        {
             timer.setText("1:00");
        }
        else
        {
            timer.setText("0:30");
        }

        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        ArrayList<String> cardList = new ArrayList<String>(app.getCardList());
        Collections.shuffle(cardList);
        app.setCurrentCardList(cardList);
    }

    public void clickMinus(View view)
    {
        String time = timer.getText().toString();
        String[] parts = time.split(":");

        int minutes = Integer.valueOf(parts[0]);
        int seconds = Integer.valueOf(parts[1]);

        while (seconds % 15 != 0)
        {
            seconds++;
        }

        if (seconds == 0)
        {
            if (minutes == 0)
            {
                minutesText = "0";
                secondsText = "00";
            }
            else
            {
                minutesText = String.valueOf(minutes - 1);
                secondsText = "45";
            }
        }
        else if (seconds <= 15)
        {
            minutesText = parts[0];
            secondsText = "00";
        }
        else
        {
            minutesText = parts[0];
            secondsText = String.valueOf(seconds - 15);
        }

        if (minutesText.equals("0") && secondsText.equals("00"))
        {
            startButton.getBackground().setAlpha(128);
            startButton.setClickable(false);
        }

        timer.setText(minutesText + ":" + secondsText);
    }

    public void clickPlus(View view)
    {
        String time = timer.getText().toString();
        String[] parts = time.split(":");

        int minutes = Integer.valueOf(parts[0]);
        int seconds = Integer.valueOf(parts[1]);

        startButton.getBackground().setAlpha(255);
        startButton.setClickable(true);

        while (seconds % 15 != 0)
        {
            seconds--;
        }

        if (seconds == 45)
        {
            minutesText = String.valueOf(minutes + 1);
            secondsText = "00";
        }
        else
        {
            minutesText = parts[0];
            secondsText = String.valueOf(seconds + 15);
        }

        timer.setText(minutesText + ":" + secondsText);
    }

    public void goToStart(View view)
    {
        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        Intent intent;

        if (phaseNumber == 1)
        {
            Random rand = new Random();
            teamNumber = rand.nextInt(app.getTeamList().size() - 1);
        }

        intent = new Intent(this,PhaseBegin.class);
        intent.putExtra("phaseNumber",phaseNumber);
        intent.putExtra("teamNumber", teamNumber);
        intent.putExtra("timer",timer.getText().toString());
        startActivity(intent);
        finish();
    }
}
