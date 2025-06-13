package com.example.dailyquest;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ChallengeRecord.class}, version = 1)
public abstract class ChallengeDatabase extends RoomDatabase {
    public abstract ChallengeDao challengeDao();
}
