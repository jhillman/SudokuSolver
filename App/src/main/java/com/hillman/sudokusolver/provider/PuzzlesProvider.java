package com.hillman.sudokusolver.provider;
 
import com.hillman.sudokusolver.database.PuzzlesDatabase;
 
import com.hillman.sudokusolver.database.table.*;
 
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.content.ContentUris;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
 
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
 
import java.util.ArrayList;
import java.util.List;
 
public class PuzzlesProvider extends ContentProvider {
 
    public static final String AUTHORITY = "com.hillman.sudokusolver.provider.puzzles";
  
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String TAG = "PuzzlesProvider";
 
    public static final Uri PUZZLE_CONTENT_URI = Uri.withAppendedPath(PuzzlesProvider.AUTHORITY_URI, PuzzleContent.CONTENT_PATH);
   
    private static final UriMatcher URI_MATCHER;
    private PuzzlesDatabase mDatabase;
 
    private static final int PUZZLE_DIR = 0;
    private static final int PUZZLE_ID = 1;
   
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 
        URI_MATCHER.addURI(AUTHORITY, PuzzleContent.CONTENT_PATH, PUZZLE_DIR);
        URI_MATCHER.addURI(AUTHORITY, PuzzleContent.CONTENT_PATH + "/#",    PUZZLE_ID);
     }
 
    private static class PuzzleContent implements BaseColumns {
        private PuzzleContent() {}
 
        public static final String CONTENT_PATH = "puzzle";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.puzzles.puzzle";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.puzzles.puzzle";
    }
   
    @Override
    public final boolean onCreate() {
        mDatabase = new PuzzlesDatabase(getContext());
        return true;
    }
 
    @Override
    public final String getType(final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case PUZZLE_DIR:
                return PuzzleContent.CONTENT_TYPE;
            case PUZZLE_ID:
                return PuzzleContent.CONTENT_ITEM_TYPE;
   
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
 
    @Override
    public final Cursor query(final Uri uri, String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        final SQLiteDatabase dbConnection = mDatabase.getReadableDatabase();
 
        switch (URI_MATCHER.match(uri)) {
            case PUZZLE_ID:
                queryBuilder.appendWhere(PuzzleTable._ID + "=" + uri.getLastPathSegment());
                break;
            case PUZZLE_DIR:
                queryBuilder.setTables(PuzzleTable.TABLE_NAME);
                break;
   
            default :
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
 
        Cursor cursor = queryBuilder.query(dbConnection, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
        return cursor;
 
    }
 
    @Override
    public final Uri insert(final Uri uri, final ContentValues values) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
                case PUZZLE_DIR:
                case PUZZLE_ID:
                    final long puzzleId = dbConnection.insertOrThrow(PuzzleTable.TABLE_NAME, null, values);
                    final Uri newPuzzleUri = ContentUris.withAppendedId(PUZZLE_CONTENT_URI, puzzleId);
                    getContext().getContentResolver().notifyChange(newPuzzleUri, null); 
                    return newPuzzleUri;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        return null;
    }
 
    @Override
    public final int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
        int updateCount = 0;
        List<Uri> joinUris = new ArrayList<Uri>();
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
               case PUZZLE_DIR :
                   updateCount = dbConnection.update(PuzzleTable.TABLE_NAME, values, selection, selectionArgs);
  
                   break;
               case PUZZLE_ID :
                   final long puzzleId = ContentUris.parseId(uri);
                   updateCount = dbConnection.update(PuzzleTable.TABLE_NAME, values, 
                       PuzzleTable._ID + "=" + puzzleId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
  
                   break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
 
            for (Uri joinUri : joinUris) {
                getContext().getContentResolver().notifyChange(joinUri, null);
            }
        }
 
        return updateCount;
 
    }
 
    @Override
    public final int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
        int deleteCount = 0;
        List<Uri> joinUris = new ArrayList<Uri>();
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
                case PUZZLE_DIR :
                    deleteCount = dbConnection.delete(PuzzleTable.TABLE_NAME, selection, selectionArgs);
  
                    break;
                case PUZZLE_ID :
                    deleteCount = dbConnection.delete(PuzzleTable.TABLE_NAME, PuzzleTable.WHERE_ID_EQUALS, new String[] { uri.getLastPathSegment() });
  
                    break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
 
            for (Uri joinUri : joinUris) {
                getContext().getContentResolver().notifyChange(joinUri, null);
            }
        }
 
        return deleteCount;
    }
}