package com.example.dailyquest;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChallengeRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String challengeText;
    public String category;
    public String dateCompleted;
}
