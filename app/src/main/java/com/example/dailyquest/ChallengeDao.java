package com.example.dailyquest;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChallengeDao {

    @Insert
    void insert(ChallengeRecord record);

    @Query("SELECT * FROM ChallengeRecord ORDER BY id DESC")
    List<ChallengeRecord> getAllHistory();
}
