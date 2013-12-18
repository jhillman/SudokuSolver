package com.hillman.sudokusolver.model;
 
import android.content.ContentValues;
import android.database.Cursor;
   
import com.hillman.sudokusolver.database.table.PuzzleTable;
 
import java.util.ArrayList;
import java.util.List;
 
public class Puzzle {
    private long mRowId;
    private int mNumber; 
    private String mName; 
    private String mNumbers; 
  
    private ContentValues mValues = new ContentValues();
 
    public Puzzle() {}
 
    public Puzzle(final Cursor cursor) {
        this(cursor, false);
    }
 
    public Puzzle(final Cursor cursor, boolean prependTableName) {
        String prefix = prependTableName ? PuzzleTable.TABLE_NAME + "_" : "";
        setRowId(cursor.getLong(cursor.getColumnIndex(prefix + PuzzleTable._ID)));
        setNumber(cursor.getInt(cursor.getColumnIndex(prefix + PuzzleTable.NUMBER))); 
        setName(cursor.getString(cursor.getColumnIndex(prefix + PuzzleTable.NAME))); 
        setNumbers(cursor.getString(cursor.getColumnIndex(prefix + PuzzleTable.NUMBERS))); 
    }
  
    public final void setRowId(long _id) {
        mRowId = _id;
        mValues.put(PuzzleTable._ID, _id);
    }
 
    public final void setNumber(int number) {
        mNumber = number;
        mValues.put(PuzzleTable.NUMBER, number);
    }
 
    public final void setName(String name) {
        mName = name;
        mValues.put(PuzzleTable.NAME, name);
    }
 
    public final void setNumbers(String numbers) {
        mNumbers = numbers;
        mValues.put(PuzzleTable.NUMBERS, numbers);
    }
  
    public long getRowId() {
        return mRowId;
    }
 
    public int getNumber() {
        return mNumber;
    }
 
    public String getName() {
        return mName;
    }
 
    public String getNumbers() {
        return mNumbers;
    }
   
    public ContentValues getContentValues() {
        return mValues;
    }
  
    public static List<Puzzle> listFromCursor(Cursor cursor) {
        List<Puzzle> list = new ArrayList<Puzzle>();
 
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new Puzzle(cursor));
            } while (cursor.moveToNext());
        }
 
        return list;
    }
 
    // BEGIN PERSISTED SECTION - put custom methods here

    // END PERSISTED SECTION
}