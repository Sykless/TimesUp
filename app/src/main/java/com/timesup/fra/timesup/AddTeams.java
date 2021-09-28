package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class AddTeams extends AppCompatActivity
{
    LinearLayout teamList;
    Button validateButton;

    int teamNumber = 0;

    String[] animalNames = {"Écureuils", "Cachalots","Chatons","Canards","Albatros","Babouins","Koalas",
            "Caribous","Chamois","Dindons","Sangliers","Mulots","Opossums","Ouistitis","Alpagas",
            "Pélicans","Poulets","Suricates","Yacks","Bouquetins","Paresseux","Paons",
            "Lamas", "Canartichos"};
    String[] adjectifs = {"bicurieux","mal-aimés","frustrés","malades","perturbés","sales","moyens",
            "sexy","boiteux","moelleux","drogués","asociaux","assistés","nazes","racistes","radins",
            "rebelles","castrés","planqués","paumés", "scandinaves", "rutilants","péremptoires",
            "ni de droite ni de gauche","vegans","bourlingueurs","indisposés"};

    // "Loutres", "Belettes"

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_teams_layout);

        teamList = findViewById(R.id.teamList);
        validateButton = findViewById(R.id.validateButton);

        for (int i = 0 ; i < 2 ; i++)
        {
            addTeam();
        }
    }

    public void goToChoose(View view)
    {
        TimesUpParameters app = (TimesUpParameters) getApplicationContext();
        ArrayList<Team> teamNameList = new ArrayList<>();

        for (int i = 0 ; i < teamNumber ; i++)
        {
            RelativeLayout rectangle = (RelativeLayout) teamList.getChildAt(i);
            TextView name = (TextView) rectangle.getChildAt(0);

            teamNameList.add(new Team(name.getText().toString()));
        }

        app.setTeamList(teamNameList);

        Intent intent = new Intent(this,ChooseGameMode.class);
        startActivity(intent);
    }

    public void hitPlus(View view)
    {
        addTeam();
    }

    public void hitMinus(View view)
    {
        if (teamNumber > 0)
        {
            teamNumber--;
            teamList.removeViewAt(teamNumber);

            if (teamNumber >= 2)
            {
                validateButton.getBackground().setAlpha(255);
                validateButton.setClickable(true);
            }
            else
            {
                validateButton.getBackground().setAlpha(128);
                validateButton.setClickable(false);
            }
        }
    }

    public void addTeam()
    {
        // Create a new RelativeLayout
        RelativeLayout newButton = new RelativeLayout(this);

        // Defining the RelativeLayout layout parameters
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);

        buttonParams.setMargins(0, 32, 0, 0);
        buttonParams.height = 100;

        // Creating a new TextView
        TextView teamName = new TextView(this);
        Random rand = new Random();
        String name = animalNames[rand.nextInt(animalNames.length)] + " " + adjectifs[rand.nextInt(adjectifs.length)];

        teamName.setText(name);
        teamName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
        teamName.setTypeface(ResourcesCompat.getFont(this,R.font.fontdiner_swanky));
        teamName.setTextColor(getResources().getColor(R.color.blue));
        teamName.setLines(1);

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams textParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParameters.setMarginStart(8);

        // Setting the parameters on the TextView
        teamName.setLayoutParams(textParameters);

        // Adding the TextView to the RelativeLayout as a child and make the layout clickable
        newButton.addView(teamName, 0);
        newButton.setGravity(Gravity.CENTER_VERTICAL);
        newButton.setLayoutParams(buttonParams);
        newButton.setBackgroundColor(getResources().getColor(R.color.yellow));

        // Add the RelativeLayout to the main LinearLayout
        teamList.addView(newButton, teamNumber);
        teamNumber++;

        if (teamNumber >= 2)
        {
            validateButton.getBackground().setAlpha(255);
            validateButton.setClickable(true);
        }
        else
        {
            validateButton.getBackground().setAlpha(128);
            validateButton.setClickable(false);
        }
    }
}
