package com.hillman.sudokusolver.database.table;
 
public final class PuzzleTable {
    private PuzzleTable() {}
 
    public static final String TABLE_NAME = "puzzle";
 
    public static final String _ID = "_id"; 
 
    public static final String NUMBER = "number"; 
 
    public static final String NAME = "name"; 
 
    public static final String NUMBERS = "numbers"; 
  
    public static final String[] ALL_COLUMNS = new String[] { _ID, NUMBER, NAME, NUMBERS };
 
    public static final String SQL_CREATE = "CREATE TABLE puzzle ( _id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER, name TEXT, numbers TEXT )";
 
    public static final String SQL_INSERT = "INSERT INTO puzzle ( number, name, numbers ) VALUES ( ?, ?, ? )";
 
    public static final String SQL_DROP = "DROP TABLE IF EXISTS puzzle";
 
    public static final String WHERE_ID_EQUALS = _ID + "=?";
}