package com.example.dailyquest;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView challengeText;
    Button refreshButton;
    Spinner categorySpinner;
    Button completeButton;
    TextView pointsText;
    Button shareButton;
    ImageView photoView;
    Button photoButton;
    ChallengeDatabase db;
    TextView streakText;
// Add an ImageView to display the image


    // Categorized challenges
    String[][] categorizedChallenges = {
            // Health
            {
                    "Drink a glass of water",
                    "Stretch your arms above your head and breathe deeply three times",
                    "Do 10 jumping jacks or jog in place for 30 seconds",
                    "Walk around your space for 2 minutes",
                    "Eat a piece of fruit or a vegetable",
                    "Roll your shoulders and neck gently for 30 seconds",
                    "Smile at yourself in the mirror",
                    "Close your eyes and take five deep breaths",
                    "Sit or stand up straight for 30 seconds (check your posture)",
                    "Step outside for one minute of fresh air",
                    "Switch to a glass of water or unsweetened tea for your next drink"
            },
            // Creativity
            {
                    "Draw a quick doodle or sketch",
                    "Write a one-sentence story or poem",
                    "Make up a tongue twister and say it out loud",
                    "List three colors you see around you",
                    "Brainstorm three new uses for a common object nearby",
                    "Take a photo of something interesting around you",
                    "Rearrange one item on your desk in a creative way",
                    "Invent a funny sentence using a random word",
                    "Hum or sing a tune you just made up",
                    "Write one positive affirmation on a sticky note",
                    "Describe an everyday object in a magical way"
            },
            // Connection
            {
                    "Send a thank-you text or message to someone",
                    "Give someone a genuine compliment",
                    "Call or text a friend or family member to say hi",
                    "Tell someone one positive thing you appreciate about them",
                    "Ask someone how they're doing and listen attentively",
                    "Share a happy memory or funny story with someone",
                    "Send a motivational quote or kind note to a friend or colleague",
                    "Smile at someone and say hello",
                    "Write a short note or card of appreciation for someone",
                    "Make plans to meet a friend for a quick coffee or walk"
            },
            // Fun
            {
                    "Dance to your favorite song for 30 seconds",
                    "Tell a joke or riddle (to someone or out loud)",
                    "Make a silly face in the mirror",
                    "Watch a short funny video or meme online",
                    "Pretend to be a rock star and play air guitar for 20 seconds",
                    "Clap and give yourself a mini round of applause",
                    "Think of a cute animal, like a puppy, and smile",
                    "Make up a silly one-liner or pun",
                    "Do a quick victory dance for something you did well today",
                    "Laugh out loud for a few seconds"
            }
    };
    String[] categories = {"All", "Health", "Creativity", "Connection", "Fun"};


    SharedPreferences prefs;
    static final String PREFS_NAME = "DailyQuestPrefs";
    static final String KEY_LAST_DATE = "last_shown_date";
    static final String KEY_LAST_INDEX = "last_challenge_index";
    static final String KEY_LAST_CATEGORY = "last_category";
    static final String KEY_POINTS = "total_points";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String KEY_STREAK = "current_streak";
    static final String KEY_LAST_COMPLETED_DATE = "last_completed_date";




    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        challengeText = findViewById(R.id.challengeText);
        refreshButton = findViewById(R.id.refreshButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        completeButton = findViewById(R.id.completeButton);
        pointsText = findViewById(R.id.pointsText);
        shareButton = findViewById(R.id.shareButton);
        photoButton = findViewById(R.id.photoButton);
        photoView = findViewById(R.id.photoView);
        streakText = findViewById(R.id.streakText);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);



        db = Room.databaseBuilder(getApplicationContext(),
                        ChallengeDatabase.class, "challenge_db")
                .allowMainThreadQueries() // okay for now
                .build();






        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int totalPoints = prefs.getInt(KEY_POINTS, 0);
        pointsText.setText("Points: " + totalPoints + getBadge(totalPoints));
        int currentStreak = prefs.getInt(KEY_STREAK, 0);
        streakText.setText("🔥 Streak: " + currentStreak + " day(s)");

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                loadChallenge();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        refreshButton.setOnClickListener(v -> challengeText.setText("Come back tomorrow for a new challenge!"));

        completeButton.setOnClickListener(v -> {
            int currentPoints = prefs.getInt(KEY_POINTS, 0);
            currentPoints += 5; // +5 points per challenge

            SharedPreferences.Editor editor = prefs.edit(); // only one editor

            editor.putInt(KEY_POINTS, currentPoints); // update points
            pointsText.setText("Points: " + currentPoints + getBadge(currentPoints)); // update UI

            // Save challenge to database
            ChallengeRecord record = new ChallengeRecord();
            record.challengeText = challengeText.getText().toString();
            record.category = categorySpinner.getSelectedItem().toString();
            record.dateCompleted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            db.challengeDao().insert(record);

            // Handle streak logic
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String lastDate = prefs.getString(KEY_LAST_COMPLETED_DATE, "");

            int streak = prefs.getInt(KEY_STREAK, 0);
            if (today.equals(lastDate)) {
                // already counted today
            } else if (isYesterday(lastDate)) {
                streak += 1;
            } else {
                streak = 1;
            }

            editor.putString(KEY_LAST_COMPLETED_DATE, today);
            editor.putInt(KEY_STREAK, streak);

            // Apply all edits at once
            editor.apply();
            streakText.setText("🔥 Streak: " + streak + " day(s)");

        });


        shareButton.setOnClickListener(v -> {
            String challenge = challengeText.getText().toString();
            String message = "🎯 Today's DailyQuest: " + challenge + "\nJoin me in the challenge! 💪 #DailyQuestApp";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(shareIntent, "Share your quest via"));
        });

        photoButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        scheduleDailyNotification();

    }


    private void loadChallenge() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = prefs.getString(KEY_LAST_DATE, "");
        int savedIndex = prefs.getInt(KEY_LAST_INDEX, 0);
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String savedCategory = prefs.getString(KEY_LAST_CATEGORY, "");

        ArrayList<String> allChallenges = new ArrayList<>();

        if (selectedCategory.equals("All")) {
            for (String[] catGroup : categorizedChallenges) {
                for (String challenge : catGroup) {
                    allChallenges.add(challenge);
                }
            }
        } else {
            int catIndex = -1;
            for (int i = 1; i < categories.length; i++) {
                if (selectedCategory.equals(categories[i])) {
                    catIndex = i - 1;
                    break;
                }
            }
            if (catIndex >= 0) {
                for (String challenge : categorizedChallenges[catIndex]) {
                    allChallenges.add(challenge);
                }
            }
        }

        if (today.equals(savedDate) && selectedCategory.equals(savedCategory)) {
            challengeText.setText(allChallenges.get(savedIndex));
        } else {
            int index = (int) (Math.random() * allChallenges.size());
            challengeText.setText(allChallenges.get(index));

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LAST_DATE, today);
            editor.putInt(KEY_LAST_INDEX, index);
            editor.putString(KEY_LAST_CATEGORY, selectedCategory);
            editor.apply();
        }
    }

    private String getBadge(int points) {
        if (points >= 50) return " 🏅 Gold Badge";
        else if (points >= 25) return " 🥈 Silver Badge";
        else if (points >= 10) return " 🥉 Bronze Badge";
        else return "";
    }

    private void scheduleDailyNotification() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set time to 9:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If time is before now, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Show photo in an ImageView (you can add this to your layout)
            if (photoView != null) {
                photoView.setImageBitmap(imageBitmap);
            }
        }
    }

    private boolean isYesterday(String previousDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date yesterday = new Date(System.currentTimeMillis() - 86400000); // -1 day
            String formatted = sdf.format(yesterday);
            return formatted.equals(previousDate);
        } catch (Exception e) {
            return false;
        }
    }


}
