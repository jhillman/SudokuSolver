package com.hillman.sudokusolver.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hillman.sudokusolver.R;
import com.hillman.sudokusolver.Sudoku;
import com.hillman.sudokusolver.SudokuResult;
import com.hillman.sudokusolver.model.Puzzle;
import com.hillman.sudokusolver.provider.PuzzlesProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jeff on 12/17/13.
 */
public class MainActivity extends Activity {
    private static final int GRID_SIZE = 9;

    private static final int REQUEST_CODE_PUZZLES = 1;

    private int mBlack;
    private int mGray;
    private int[] mPuzzle;
    private TextView[][] mTextViewGrid;
    private Gson mGson;
    private int mCurrentPuzzleNumber = 1;
    private boolean mPuzzleChosen;

    private native SudokuResult solve(int[] puzzle);

    static {
        System.loadLibrary("sudoku");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        Cursor cursor = null;
        boolean readPuzzles = false;

        try {
            cursor = getContentResolver().query(PuzzlesProvider.PUZZLE_CONTENT_URI, null, null, null, null, null);

            readPuzzles = (cursor == null || !cursor.moveToFirst());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (readPuzzles) {
            readPuzzles();
        }

        mBlack = getResources().getColor(android.R.color.black);
        mGray = getResources().getColor(android.R.color.darker_gray);

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPuzzleChosen = false;

                ((TextView)findViewById(R.id.puzzle_name)).setText("");
                ((TextView)findViewById(R.id.native_time)).setText("");
                ((TextView)findViewById(R.id.java_time)).setText("");
                ((TextView)findViewById(R.id.strategy)).setText("");

                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        TextView textView = mTextViewGrid[i][j];
                        setPuzzleValue(i, j, 0);

                        textView.setText("?");
                        textView.setTextColor(mGray);
                        textView.setTypeface(null, Typeface.NORMAL);
                    }
                }
            }
        });

        findViewById(R.id.puzzles).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PuzzlesActivity.class);
                intent.putExtra(PuzzlesActivity.DATA_PUZZLE_NUMBER, mCurrentPuzzleNumber);

                startActivityForResult(intent, REQUEST_CODE_PUZZLES);
            }
        });

        findViewById(R.id.solve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TextView)findViewById(R.id.native_time)).setText("");
                ((TextView)findViewById(R.id.java_time)).setText("");

                new NativeSolutionTask().execute(mPuzzle);
                new JavaSolutionTask().execute(mPuzzle);
            }
        });

        mGson = new Gson();

        mPuzzle = new int[GRID_SIZE * GRID_SIZE];
        mTextViewGrid = new TextView[GRID_SIZE][GRID_SIZE];
        View.OnClickListener cellOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mPuzzleChosen) {
                    return;
                }

                final int viewI = (int) view.getTag(R.id.key_i);
                final int viewJ = (int) view.getTag(R.id.key_j);

                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                Menu menu = popup.getMenu();
                popup.getMenuInflater().inflate(R.menu.numbers, menu);

                for (int i = 0; i < GRID_SIZE; i++) {
                    if (i != viewJ && getPuzzleValue(viewI, i) != 0) {
                        removeMenuItem(menu, getPuzzleValue(viewI, i));
                    }

                    if (i != viewI && getPuzzleValue(i, viewJ) != 0) {
                        removeMenuItem(menu, getPuzzleValue(i, viewJ));
                    }
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        TextView textView = (TextView)view;

                        textView.setText(menuItem.getTitle());

                        if (menuItem.getTitle().equals("?")) {
                            setPuzzleValue(viewI, viewJ, 0);
                            textView.setTextColor(mGray);
                            textView.setTypeface(null, Typeface.NORMAL);
                        } else {
                            setPuzzleValue(viewI, viewJ, Integer.parseInt(menuItem.getTitle().toString()));
                            textView.setTextColor(mBlack);
                            textView.setTypeface(null, Typeface.BOLD);
                        }

                        return true;
                    }
                });

                popup.show();
            }

            private void removeMenuItem(Menu menu, int number) {
                int[] numberItems = {0, R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine};

                menu.removeItem(numberItems[number]);
            }
        };

        int oneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        float numberTextSize = getResources().getDimension(R.dimen.number_text_size);

        ViewGroup content = (ViewGroup) findViewById(R.id.puzzle_holder);
        TextView firstTextView = null;

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        mainLayoutParams.setMargins(oneDp, oneDp, oneDp, oneDp);
        mainLayout.setLayoutParams(mainLayoutParams);

        for (int i = 0; i < 3; i++) {
            LinearLayout mainRowLayout = new LinearLayout(this);
            mainRowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams mainRowLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
            );
            mainRowLayoutParams.weight = 1;
            mainRowLayout.setLayoutParams(mainRowLayoutParams);

            for (int j = 0; j < 3; j++) {
                LinearLayout gridLayout = new LinearLayout(this);
                gridLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams gridLayoutParams = new LinearLayout.LayoutParams(
                        0,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                gridLayoutParams.weight = 1;
                gridLayoutParams.setMargins(oneDp, oneDp, oneDp, oneDp);
                gridLayout.setLayoutParams(gridLayoutParams);


                for (int k = 0; k < 3; k++) {
                    LinearLayout rowLayout = new LinearLayout(this);
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0
                    );
                    rowLayoutParams.weight = 1;
                    rowLayout.setLayoutParams(rowLayoutParams);

                    for (int l = 0; l < 3; l++) {
                        TextView cellTextView = new TextView(this);
                        cellTextView.setTextColor(mGray);
                        cellTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, numberTextSize);
                        LinearLayout.LayoutParams cellTextViewParams = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cellTextViewParams.weight = 1;
                        cellTextViewParams.setMargins(oneDp, oneDp, oneDp, oneDp);
                        cellTextView.setLayoutParams(cellTextViewParams);
                        cellTextView.setGravity(Gravity.CENTER);
                        cellTextView.setText("?");
                        cellTextView.setTag(R.id.key_i, (i * 3) + k);
                        cellTextView.setTag(R.id.key_j, (j * 3) + l);

                        cellTextView.setBackgroundResource(R.drawable.white_selector);
                        cellTextView.setClickable(true);
                        cellTextView.setOnClickListener(cellOnClickListener);

                        rowLayout.addView(cellTextView);

                        setPuzzleValue((i * 3) + k, (j * 3) + l, 0);

                        mTextViewGrid[(i * 3) + k][(j * 3) + l] = cellTextView;

                        if (firstTextView == null) {
                            firstTextView = cellTextView;
                        }
                    }

                    gridLayout.addView(rowLayout);
                }

                mainRowLayout.addView(gridLayout);
            }

            mainLayout.addView(mainRowLayout);
        }

        content.addView(mainLayout);

        final TextView finalFirstTextView = firstTextView;

        finalFirstTextView.post(new Runnable() {
            @Override
            public void run() {
                setHeights(findViewById(R.id.puzzle_holder), finalFirstTextView.getWidth());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            mPuzzleChosen = true;
            mCurrentPuzzleNumber = data.getIntExtra(PuzzlesActivity.DATA_PUZZLE_NUMBER, 1);

            ((TextView)findViewById(R.id.puzzle_name)).setText(data.getStringExtra(PuzzlesActivity.DATA_PUZZLE_NAME));
            ((TextView)findViewById(R.id.native_time)).setText("");
            ((TextView)findViewById(R.id.java_time)).setText("");
            ((TextView)findViewById(R.id.strategy)).setText("");

            mPuzzle = mGson.fromJson(data.getStringExtra(PuzzlesActivity.DATA_PUZZLE_NUMBERS), int[].class);

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    TextView textView = mTextViewGrid[i][j];
                    int number = getPuzzleValue(i, j);

                    textView.setText(number == 0 ? "?" : Integer.toString(number));
                    textView.setTextColor(number == 0 ? mGray : mBlack);
                    textView.setTypeface(null, number == 0 ? Typeface.NORMAL : Typeface.BOLD);
                }
            }
        }
    }

    private void readPuzzles() {
        AssetManager assetManager = getAssets();
        Gson gson = new Gson();

        try {
            int puzzleNumber = 1;

            for (String file : assetManager.list("puzzles")) {
                String numberLine;
                Puzzle puzzle = new Puzzle();

                BufferedReader puzzleReader = new BufferedReader(new InputStreamReader(assetManager.open("puzzles/" + file)));

                puzzle.setNumber(puzzleNumber++);
                puzzle.setName(puzzleReader.readLine());

                numberLine = puzzleReader.readLine();

                int[] numbers = new int[GRID_SIZE * GRID_SIZE];
                int numberIndex = 0;

                while (numberLine != null) {
                    for (int i = 0; i < GRID_SIZE; i++) {
                        numbers[numberIndex++] = Integer.parseInt(numberLine.substring(i, i + 1));
                    }

                    numberLine = puzzleReader.readLine();
                }

                puzzle.setNumbers(gson.toJson(numbers, int[].class));

                getContentResolver().insert(PuzzlesProvider.PUZZLE_CONTENT_URI, puzzle.getContentValues());

                puzzleReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getPuzzleValue(int i, int j) {
        return mPuzzle[i * GRID_SIZE + j];
    }

    private void setPuzzleValue(int i, int j, int value) {
        mPuzzle[i * GRID_SIZE + j] = value;
    }

    private void setHeights(View view, int height) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setHeights(viewGroup.getChildAt(i), height);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setHeight(height);
        }
    }

    private class NativeSolutionTask extends AsyncTask<int[], Void, SudokuResult> {
        private long mStart;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mStart = System.currentTimeMillis();
        }

        @Override
        protected SudokuResult doInBackground(int[]... puzzles) {
            return solve(puzzles[0]);
        }

        @Override
        protected void onPostExecute(SudokuResult nativeResult) {
            super.onPostExecute(nativeResult);

            long time = System.currentTimeMillis() - mStart;

            ((TextView)findViewById(R.id.native_time)).setText(time + " milliseconds");

            setSolution(nativeResult);
        }
    }

    private class JavaSolutionTask extends AsyncTask<int[], Void, SudokuResult> {
        private long mStart;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mStart = System.currentTimeMillis();
        }

        @Override
        protected SudokuResult doInBackground(int[]... puzzles) {
            return Sudoku.solve(puzzles[0]);
        }

        @Override
        protected void onPostExecute(SudokuResult javaResult) {
            super.onPostExecute(javaResult);

            long time = System.currentTimeMillis() - mStart;

            ((TextView)findViewById(R.id.java_time)).setText(time + " milliseconds");

            setSolution(javaResult);
        }
    }

    private void setSolution(SudokuResult sudokuResult) {
        if (sudokuResult.solutionFound()) {
            int[] solution = sudokuResult.getSolution();

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    TextView textview = mTextViewGrid[i][j];
                    int number = solution[i * GRID_SIZE + j];

                    if (textview.getText().equals("?")) {
                        textview.setText(Integer.toString(solution[i * GRID_SIZE + j]));
                        textview.setTextColor(number == 0 ? mGray : mBlack);
                    }
                }
            }
        }

        ((TextView)findViewById(R.id.strategy)).setText(sudokuResult.getSolutionStrategy().toString());
    }
}
