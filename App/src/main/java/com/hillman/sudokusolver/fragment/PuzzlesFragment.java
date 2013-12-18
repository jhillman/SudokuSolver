package com.hillman.sudokusolver.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hillman.sudokusolver.R;
import com.hillman.sudokusolver.activity.PuzzlesActivity;
import com.hillman.sudokusolver.adapter.PuzzleAdapter;
import com.hillman.sudokusolver.database.table.PuzzleTable;
import com.hillman.sudokusolver.model.Puzzle;
import com.hillman.sudokusolver.provider.PuzzlesProvider;

/**
 * Created by jeff on 12/18/13.
 */
public class PuzzlesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private PuzzleAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_puzzles, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new PuzzleAdapter(getActivity());
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        Puzzle puzzle = new Puzzle(cursor);

        Intent data = new Intent();
        data.putExtra(PuzzlesActivity.DATA_PUZZLE_NAME, puzzle.getName());
        data.putExtra(PuzzlesActivity.DATA_PUZZLE_NUMBERS, puzzle.getNumbers());

        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), PuzzlesProvider.PUZZLE_CONTENT_URI, null, null, null, PuzzleTable.NUMBER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
