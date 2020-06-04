package com.timesup.fra.timesup;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PhaseOne extends AppCompatActivity
{
    int currentCard = 0;
    int timer;
    int finalTimer;
    int bubble;
    int teamNumber;
    boolean hasClicked = false;
    String timerString;
    String lastCardName = "";

    TextView cardName;
    ArrayList<String> cardList;
    ArrayList<String> validateCards = new ArrayList<>();
    ArrayList<Team> teamList;
    LinearLayout bubbleList;
    Button validateButton;

    TimesUpParameters app;
    Handler timerHandler;
    Handler hideValidate = new Handler();
    Handler launchBeginPhase = new Handler();

    MediaPlayer validateSound;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phase_one_layout);

        app = (TimesUpParameters) getApplicationContext();
        cardList = app.getCurrentCardList();

        System.out.println("Phase one card list : " + cardList);

        teamList = app.getTeamList();
        bubbleList = findViewById(R.id.bubbleList);
        validateButton = findViewById(R.id.validateButton);
        cardName = findViewById(R.id.cardName);
        cardName.setText(cardList.remove(0));

        validateSound = MediaPlayer.create(this, R.raw.coin);

        Intent intent = getIntent();
        timerString = intent.getStringExtra("timer");
        teamNumber = intent.getIntExtra("teamNumber",0);
        String[] timerParts = timerString.split(":");

        finalTimer = 60*Integer.valueOf(timerParts[0]) + Integer.valueOf(timerParts[1]);
        timer = finalTimer;
        bubble = timer / 12;

        timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                timer--;

                int id = (timer + bubble - 1) / bubble;

                if (id < 12)
                {
                    bubbleList.getChildAt(id).setVisibility(View.INVISIBLE);
                }

                if (timer == 0)
                {
                    lastCardName = cardName.getText().toString();
                    cardName.setText("Terminé !");

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    else
                    {
                        v.vibrate(2000); // Deprecated in API 26
                    }

                    hideValidate = new Handler();
                    launchBeginPhase = new Handler();

                    hideValidate.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            validateButton.getBackground().setAlpha(128);
                            validateButton.setClickable(false);

                            if (!hasClicked)
                            {
                                cardList.add(lastCardName);
                                app.setCurrentCardList(cardList);
                            }

                            teamList.get(teamNumber).setScorePhaseOne(validateCards.size() + teamList.get(teamNumber).getScorePhaseOne());
                            teamNumber = (teamNumber + 1) % teamList.size();
                        }
                    },2000);

                    launchBeginPhase.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            Intent intent = new Intent(getApplicationContext(),PhaseBegin.class);
                            intent.putExtra("teamNumber",teamNumber);
                            intent.putExtra("timer",timerString);
                            startActivity(intent);
                            finish();
                        }
                    },5000);
                }
                else
                {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    public void nextCard(View view)
    {
        validateSound.start();

        System.out.println("Validate (" + validateCards.size() + ") : " +  validateCards);

        if (lastCardName.length() > 0)
        {
            hasClicked = true;

            validateCards.add(lastCardName);
            validateButton.getBackground().setAlpha(128);
            validateButton.setClickable(false);
        }
        else
        {
            validateCards.add(cardName.getText().toString());
        }

        if (cardList.size() > 0)
        {
            if (lastCardName.length() == 0)
            {
                cardName.setText(cardList.remove(0));
            }
        }
        else
        {
            teamList.get(teamNumber).setScorePhaseOne(validateCards.size() + teamList.get(teamNumber).getScorePhaseOne());

            cardName.setText("Fin de la manche !");
            bubbleList.setVisibility(View.INVISIBLE);
            validateButton.setVisibility(View.INVISIBLE);

            timerHandler.removeCallbacksAndMessages(null);
            hideValidate.removeCallbacksAndMessages(null);
            launchBeginPhase.removeCallbacksAndMessages(null);

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else
            {
                v.vibrate(2000); // Deprecated in API 26
            }

            new Handler().postDelayed(new Runnable()
            {
                public void run()
                {
                    if ( timer / (double) finalTimer < 0.75)
                    {
                        teamNumber = (teamNumber + 1) % teamList.size();
                    }

                    Intent intent = new Intent(getApplicationContext(),PhaseSetup.class);
                    intent.putExtra("phaseNumber",2);
                    intent.putExtra("teamNumber",teamNumber);
                    startActivity(intent);
                    finish();
                }
            },5000);
        }
    }
}
