package com.hillman.sudokusolver.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.hillman.sudokusolver.R;

/**
 * Created by jeff on 12/18/13.
 */
public class PuzzlesActivity extends Activity {
    public static final String DATA_PUZZLE_NUMBER = "com.hillman.sudokusolver.activity.PuzzlesActivity.DATA_PUZZLE_NUMBER";
    public static final String DATA_PUZZLE_NAME = "com.hillman.sudokusolver.activity.PuzzlesActivity.DATA_PUZZLE_NAME";
    public static final String DATA_PUZZLE_NUMBERS = "com.hillman.sudokusolver.activity.PuzzlesActivity.DATA_PUZZLE_NUMBERS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_puzzles);
    }
}
