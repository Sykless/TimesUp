package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PhaseSetup extends AppCompatActivity
{
    TimesUpParameters app;

    String minutesText;
    String secondsText;
    int teamNumber;
    int phaseNumber;
    int cardsNumber;

    EditText timer;
    Button startButton;
    TextView phaseName;
    TextView cardsNumberView;
    Button buttonRemoveCards;
    Button buttonAddCards;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phase_setup_layout);

        timer = findViewById(R.id.timer);
        startButton = findViewById(R.id.startButton);
        phaseName = findViewById(R.id.phaseName);
        cardsNumberView = findViewById(R.id.cardsNumber);
        buttonRemoveCards = findViewById(R.id.buttonRemoveCards);
        buttonAddCards = findViewById(R.id.buttonAddCards);

        Intent intent = getIntent();
        teamNumber = intent.getIntExtra("teamNumber",0);
        phaseNumber = intent.getIntExtra("phaseNumber",1);

        System.out.println("Phase number " + phaseNumber);

        phaseName.setText("Manche " + phaseNumber);

        if (phaseNumber == 1)
        {
            timer.setText("1:00");
            cardsNumberView.setVisibility(View.VISIBLE);
            buttonRemoveCards.setVisibility(View.VISIBLE);
            buttonAddCards.setVisibility(View.VISIBLE);
        }
        else
        {
            timer.setText("0:45");
            cardsNumberView.setVisibility(View.GONE);
            buttonRemoveCards.setVisibility(View.GONE);
            buttonAddCards.setVisibility(View.GONE);
        }
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

    public void clickMinusCard(View view)
    {
        buttonAddCards.getBackground().setAlpha(255);
        buttonAddCards.setClickable(true);

        String cardsNumberString = cardsNumberView.getText().toString();
        int cards = Integer.parseInt(cardsNumberString.split(" ")[0]);

        if (cards == 30)
        {
            buttonRemoveCards.getBackground().setAlpha(128);
            buttonRemoveCards.setClickable(false);
        }

        cardsNumberView.setText((cards - 10) + " cartes");
    }

    public void clickPlusCard(View view)
    {
        buttonRemoveCards.getBackground().setAlpha(255);
        buttonRemoveCards.setClickable(true);

        String cardsNumberString = cardsNumberView.getText().toString();
        int cards = Integer.parseInt(cardsNumberString.split(" ")[0]);

        if (cards == 90)
        {
            buttonAddCards.getBackground().setAlpha(128);
            buttonAddCards.setClickable(false);
        }

        cardsNumberView.setText((cards + 10) + " cartes");
    }

    public void goToStart(View view)
    {
        app = (TimesUpParameters) getApplicationContext();

        System.out.println("PhaseNumber : " + phaseNumber);

        if (phaseNumber != 1)
        {
            launchPhase();
        }
        else
        {
            Random rand = new Random();
            teamNumber = rand.nextInt(app.getTeamList().size());

            System.out.println("Debug cards number : " + Integer.parseInt(cardsNumberView.getText().toString().split(" ")[0]));
            app.setCardsNumber(Integer.parseInt(cardsNumberView.getText().toString().split(" ")[0]));

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("CardList");

            ref.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    ArrayList<String> cardList = new ArrayList<>();
                    long id = app.getCardFirstID();

                    System.out.println("Cards number app : " + app.getCardsNumber());

                    for (long i = id ; i < app.getCardsNumber() + id ; i++)
                    {
                        System.out.println(dataSnapshot.child(String.valueOf(i)).getValue(String.class));
                        cardList.add(dataSnapshot.child(String.valueOf(i)).getValue(String.class));
                    }

                    app.setCardList(cardList);
                    launchPhase();
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Failed to read value
                    Log.w("samarchpa", "Failed to read value.", error.toException());
                }
            });
        }
    }

    public void launchPhase()
    {
        System.out.println("Cards number : " + app.getCardList().size());
        System.out.println("Timer : " + timer.getText().toString());
        System.out.println("PhaseNumber : " + phaseNumber);

        ArrayList<String> cardList = new ArrayList<>(app.getCardList());
        Collections.shuffle(cardList);

        app.setCurrentCardList(cardList);

        Intent intent = new Intent(this,PhaseBegin.class);
        intent.putExtra("phaseNumber",phaseNumber);
        intent.putExtra("teamNumber", teamNumber);
        intent.putExtra("timer",timer.getText().toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        return;
    }
}
