package com.hillman.sudokusolver.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hillman.sudokusolver.R;
import com.hillman.sudokusolver.model.Puzzle;

/**
 * Created by jeff on 12/18/13.
 */
public class PuzzleAdapter extends CursorAdapter {
    private final LayoutInflater mInflater;
    private final Gson mGson;

    public PuzzleAdapter(Context context) {
        super(context, null, false);

        mInflater = LayoutInflater.from(context);
        mGson = new Gson();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.list_item_puzzle, viewGroup, false);

        PuzzleHolder holder = new PuzzleHolder();
        holder.name = (TextView)view.findViewById(R.id.name);
        holder.puzzle = (TextView)view.findViewById(R.id.puzzle);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Puzzle puzzle = new Puzzle(cursor);
        PuzzleHolder holder = (PuzzleHolder) view.getTag();

        holder.name.setText(puzzle.getName());

        int[] numbers = mGson.fromJson(puzzle.getNumbers(), int[].class);
        StringBuilder puzzleBuilder = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                puzzleBuilder.append(numbers[i * 9 + j] == 0 ? "-" : numbers[i * 9 + j]);

                if (j < 8) {
                    puzzleBuilder.append(" ");
                }
            }

            if (i < 8) {
                puzzleBuilder.append("\n");
            }
        }

        holder.puzzle.setText(puzzleBuilder.toString());
    }

    private static class PuzzleHolder {
        TextView name;
        TextView puzzle;
    }
}
