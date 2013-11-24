package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.SparseArray;

import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.util.Pair;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

public class GameDBHelper extends SQLiteOpenHelper {

    private static UtilLogger log = new UtilLogger(GameDBHelper.class);

    private static final String DB_NAME = "games.db";
    private static final int DB_VERSION = 4;

    private static final String TABLE_GAMES = "Games";
    private static final String TABLE_PLAYER_SCORES = "PlayerScores";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE_STARTED = "dateStarted";
    private static final String COLUMN_DATE_SAVED = "dateSaved";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_AUTOSAVED = "autosaved"; // legacy
    private static final String COLUMN_PLAYER_NUMBER = "playerNumber";
    private static final String COLUMN_GAME_ID = "gameId";
    private static final String COLUMN_HISTORY = "history";
    private static final String COLUMN_LAST_UPDATE = "lastUpdate";
    private static final String COLUMN_HISTORY_TIMESTAMPS = "historyTimestamps";
    private static final String COLUMN_COLOR = "color";

    // my crazy system for using sqlite's group_concat, since it's not ambiguous to use regular old commas
    private static final String GROUP_CONCAT_SEPARATOR = "^__%^%__";
    private static final String GROUP_CONCAT_INNER_SEPARATOR = "$__%$%__";
    
    private static final String JOINED_TABLES = TABLE_GAMES + " g join " + TABLE_PLAYER_SCORES + " ps ON " + "g."
            + COLUMN_ID + "=ps." + COLUMN_GAME_ID;
    private static final String[] JOINED_COLUMNS = new String[] { 
        "g." + COLUMN_ID, 
        "g." + COLUMN_DATE_STARTED,
        "g." + COLUMN_DATE_SAVED, 
        "g." + COLUMN_NAME, 
        "ps." + COLUMN_ID, 
        "ps." + COLUMN_NAME, 
        "ps." + COLUMN_SCORE,
        "ps." + COLUMN_PLAYER_NUMBER, 
        "ps." + COLUMN_HISTORY, 
        "ps." + COLUMN_HISTORY_TIMESTAMPS,
        "ps." + COLUMN_LAST_UPDATE,
        "ps." + COLUMN_COLOR
        };

    private ThreadLocal<SQLiteStatement> updateGame = new ThreadLocal<SQLiteStatement>() {

        @Override
        protected SQLiteStatement initialValue() {
            String sql = "update " + TABLE_GAMES + " set " + COLUMN_DATE_STARTED + "=?," + COLUMN_DATE_SAVED + "=?,"
                    + COLUMN_NAME + "=? " + "where " + COLUMN_ID + "=?";
            return db.compileStatement(sql);
        }
    };

    private ThreadLocal<SQLiteStatement> updatePlayerScore = new ThreadLocal<SQLiteStatement>() {

        @Override
        protected SQLiteStatement initialValue() {
            String sql = "update " + TABLE_PLAYER_SCORES + " set " + COLUMN_NAME + "=?," + COLUMN_SCORE + "=?,"
                    + COLUMN_PLAYER_NUMBER + "=?," + COLUMN_HISTORY + "=?," + COLUMN_HISTORY_TIMESTAMPS + "=?,"
                    + COLUMN_LAST_UPDATE + "=?," 
                    + COLUMN_COLOR + "=? "
                    + "where "
                    + COLUMN_ID + "=?";
            return db.compileStatement(sql);
        }
    };
    
    private SQLiteDatabase db;

    public GameDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql1 = "create table if not exists " + TABLE_GAMES + " (" + COLUMN_ID
                + " integer not null primary key autoincrement, " + COLUMN_NAME + " text, " + COLUMN_AUTOSAVED
                + " int not null, " + COLUMN_DATE_STARTED + " int not null, " + COLUMN_DATE_SAVED + " int not null);";

        db.execSQL(createSql1);

        String createSql2 = "create table if not exists " + TABLE_PLAYER_SCORES + " (" + COLUMN_ID
                + " integer not null primary key autoincrement, " + COLUMN_NAME + " text not null, " + COLUMN_SCORE
                + " int not null, " + COLUMN_PLAYER_NUMBER + " int not null, " + COLUMN_HISTORY + " text, "
                + COLUMN_LAST_UPDATE + " int not null default 0, "
                + COLUMN_HISTORY_TIMESTAMPS + " text, "
                + COLUMN_COLOR + " string, "
                + COLUMN_GAME_ID + " int not null);";

        db.execSQL(createSql2);

        String indexSql1 = "create index if not exists index_game_id on " + TABLE_PLAYER_SCORES + " (" + COLUMN_GAME_ID
                + ");";

        db.execSQL(indexSql1);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion <= 1) {
        
            // add an index for the startTime
            String index = "create index if not exists index_date_started on " + TABLE_GAMES + "(" + COLUMN_DATE_STARTED
                    + ");";
    
            db.execSQL(index);
        }
        
        if (oldVersion <= 2) {
            // add a lastUpdate time for each playerScore
            String addColumn = "alter table " + TABLE_PLAYER_SCORES + " add column " + COLUMN_LAST_UPDATE 
                    + " int not null default 0";
            db.execSQL(addColumn);
        }
        
        if (oldVersion <= 3) {
            // add history timestamps and color
            db.execSQL("alter table " + TABLE_PLAYER_SCORES + " add column " + COLUMN_HISTORY_TIMESTAMPS
                    + " text;");
            db.execSQL("alter table " + TABLE_PLAYER_SCORES + " add column " + COLUMN_COLOR
                    + " string;");
            
            // older versions of keepscore only had 8 players, and there are 16 colors, so using the player
            // number as an ordinal is fine here
            db.execSQL("update " + TABLE_PLAYER_SCORES + " set " + COLUMN_COLOR + "=" + COLUMN_PLAYER_NUMBER + ";");
        }
        
    }

    /**
     * Return true if a game with that dateStarted value exists.
     * 
     * @param dateStarted
     * @return
     */
    public boolean existsByDateStarted(long dateStarted) {
        synchronized (GameDBHelper.class) {
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_GAMES, new String[] { COLUMN_ID }, "dateStarted=" + dateStarted, null, null,
                        null, null);
                return cursor.moveToNext();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }
    }

    public Game findGameById(int gameId) {
        synchronized (GameDBHelper.class) {
            Cursor cursor = null;
            try {
                String where = "g." + COLUMN_ID + "=" + gameId;
                cursor = db.query(JOINED_TABLES, JOINED_COLUMNS, where, null, null, null, null);
                List<Game> result = convertToGames(cursor);

                return result.isEmpty() ? null : result.get(0);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public int findGameCount() {
        synchronized (GameDBHelper.class) {
            String[] columns = { "count(" + COLUMN_ID + ")" };
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_GAMES, columns, null, null, null, null, null);
                if (cursor.moveToNext()) {
                    return cursor.getInt(0);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return 0;
        }
    }

    public int findMostRecentGameId() {
        synchronized (GameDBHelper.class) {
            Cursor cursor = null;
            try {
                String orderBy = COLUMN_DATE_SAVED + " desc";
                cursor = db.query(TABLE_GAMES, new String[] { COLUMN_ID }, null, null, null, null, orderBy);

                if (cursor.moveToNext()) {
                    return cursor.getInt(0);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return -1;
    }

    public Game findMostRecentGame() {
        synchronized (GameDBHelper.class) {
            Cursor cursor = null;
            try {

                String sql = new StringBuilder("select ").append(TextUtils.join(",", JOINED_COLUMNS)).append(" from ")
                        .append(JOINED_TABLES).append(" order by ").append(COLUMN_DATE_SAVED).append(" desc ")
                        .append(" limit 1").toString();

                cursor = db.rawQuery(sql, null);
                List<Game> result = convertToGames(cursor);

                return result.isEmpty() ? null : result.get(0);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    /**
     * save a game, updating its 'dateSaved' value
     * 
     * @param game
     * @return
     */
    public void saveGame(Game game) {
        saveGame(game, true);
    }

    /**
     * save a game, optionally updating its 'dateSaved' value
     * 
     * @param game
     * @return
     */
    public void saveGame(Game game, boolean updateDateSaved) {
        synchronized (GameDBHelper.class) {
            db.beginTransaction();
            try {
                saveGameWithinTransaction(game, updateDateSaved);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void saveGameWithinTransaction(Game game, boolean updateDateSaved) {

        long dateSaved = updateDateSaved ? System.currentTimeMillis() : game.getDateSaved();
        game.setDateSaved(dateSaved);

        if (game.getId() != -1) {
            // game was already saved, so try to overwrite

            updateGame(game.getId(), game.getDateStarted(), game.getDateSaved(), game.getName());
        } else {
            // else create a new row in the table

            int newGameId = getMaxGameId() + 1;

            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_DATE_STARTED, game.getDateStarted());
            contentValues.put(COLUMN_DATE_SAVED, dateSaved);
            contentValues.put(COLUMN_NAME, game.getName());
            contentValues.put(COLUMN_ID, newGameId);
            contentValues.put(COLUMN_AUTOSAVED, 1); // legacy "autosaved" column
                                                    // that must be specified

            db.insert(TABLE_GAMES, null, contentValues);

            game.setId(newGameId);
            log.d("new game id is %s", newGameId);
        }

        savePlayerScores(game.getId(), game.getPlayerScores());

    }

    private int getMaxPlayerScoreId() {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYER_SCORES, new String[] { "max(" + COLUMN_ID + ")" }, null, null, null, null,
                    null);
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private int getMaxGameId() {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_GAMES, new String[] { "max(" + COLUMN_ID + ")" }, null, null, null, null, null);
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private void savePlayerScores(int gameId, List<PlayerScore> playerScores) {
        synchronized (GameDBHelper.class) {

            int newId = -1;

            for (PlayerScore playerScore : playerScores) {

                Pair<String, String> historyAsStrings = Delta.toJoinedStrings(playerScore.getHistory());

                if (playerScore.getId() != -1) {
                    // already exists; update

                    updatePlayerScore(playerScore.getId(), playerScore.getName(), playerScore.getScore(),
                            playerScore.getPlayerNumber(), historyAsStrings.getFirst(), 
                            historyAsStrings.getSecond(), playerScore.getLastUpdate(), 
                            PlayerColor.serialize(playerScore.getPlayerColor()));

                } else {
                    // else insert new rows in the table

                    if (newId == -1) {
                        newId = getMaxPlayerScoreId() + 1;
                    } else {
                        newId++;
                    }

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ID, newId);
                    values.put(COLUMN_GAME_ID, gameId);
                    values.put(COLUMN_HISTORY, historyAsStrings.getFirst());
                    values.put(COLUMN_HISTORY_TIMESTAMPS, historyAsStrings.getSecond());
                    values.put(COLUMN_NAME, playerScore.getName());
                    values.put(COLUMN_PLAYER_NUMBER, playerScore.getPlayerNumber());
                    values.put(COLUMN_SCORE, playerScore.getScore());
                    values.put(COLUMN_COLOR, PlayerColor.serialize(playerScore.getPlayerColor()));
                    values.put(COLUMN_LAST_UPDATE, playerScore.getLastUpdate());
                    db.insert(TABLE_PLAYER_SCORES, null, values);

                    // set the new id on the PlayerScore
                    playerScore.setId(newId);

                    log.d("new playerScore id is %s", newId);
                }
            }
        }
    }
    
    public List<Game> findAllGames() {
        synchronized (GameDBHelper.class) {
            String orderBy = COLUMN_DATE_SAVED;

            Cursor cursor = null;

            try {

                cursor = db.query(JOINED_TABLES, JOINED_COLUMNS, null, null, null, null, orderBy);

                return convertToGames(cursor);

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }    

    public List<GameSummary> findAllGameSummaries() {
        synchronized (GameDBHelper.class) {
            
            String[] columns = {
                    "g." + COLUMN_ID, 
                    "g." + COLUMN_NAME, 
                    "g." + COLUMN_DATE_SAVED, 
                    // player names; the "separator" is a trick to ensure that we can cleanly separate the response,
                    // and put it into the proper order, since group_concat is always unordered in sqlite
                    "group_concat((ps.name || '" + GROUP_CONCAT_INNER_SEPARATOR +"' || ps.playerNumber), '" + GROUP_CONCAT_SEPARATOR + "')",
                    "max(length(ps.history) - length(replace(ps.history, ',', '')) + 1)" // num rounds
                    };
            
            String table = TABLE_GAMES + " g join " + TABLE_PLAYER_SCORES + " ps " + 
                    " on g." + COLUMN_ID + " = ps." + COLUMN_GAME_ID;
            String groupBy = "g." + COLUMN_ID;
            
            Cursor cursor = null;
            
            try {

                cursor = db.query(table, columns, null, null, groupBy, null, null);
                
                List<GameSummary> result = new ArrayList<GameSummary>();
                
                // re-use sparse array for performance
                SparseArray<String> playerNumbersToNames = new SparseArray<String>();
                
                while (cursor.moveToNext()) {
                    GameSummary gameSummary = new GameSummary();
                    
                    gameSummary.setId(cursor.getInt(0));
                    gameSummary.setName(cursor.getString(1));
                    gameSummary.setDateSaved(cursor.getLong(2));
                    
                    String playerNumbersAndNames = cursor.getString(3);
                    // sort by player number, get player names in order (no way to do this in sqlite, unfortunately)
                    
                    playerNumbersToNames.clear();
                    for (String playerNumberAndName : StringUtil.split(playerNumbersAndNames, GROUP_CONCAT_SEPARATOR)) {
                        int idx = playerNumberAndName.indexOf(GROUP_CONCAT_INNER_SEPARATOR);
                        String playerName = playerNumberAndName.substring(0, idx);
                        int playerNumber = Integer.parseInt(playerNumberAndName.substring(
                                idx + GROUP_CONCAT_INNER_SEPARATOR.length()));
                        playerNumbersToNames.put(playerNumber, playerName);
                    }
                    List<String> playerNames = new ArrayList<String>(playerNumbersToNames.size());
                    for (int i = 0, len = playerNumbersToNames.size(); i < len; i++) {
                        int playerNumber = playerNumbersToNames.keyAt(i);
                        playerNames.add(playerNumbersToNames.get(playerNumber));
                    }
                    gameSummary.setPlayerNames(playerNames);
                    
                    gameSummary.setNumRounds(cursor.getInt(4));
                    
                    result.add(gameSummary);
                }
                
                return result;

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void deleteGame(Game game) {
        synchronized (GameDBHelper.class) {
            try {
                db.beginTransaction();

                int id = game.getId();
                db.delete(TABLE_GAMES, COLUMN_ID + "=" + id, null);
                db.delete(TABLE_PLAYER_SCORES, COLUMN_GAME_ID + "=" + id, null);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public void updateGameName(int gameId, String newName) {
        synchronized (GameDBHelper.class) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, newName);

            db.update(TABLE_GAMES, values, COLUMN_ID + "=" + gameId, null);
        }
    }

    public List<String> findDistinctPlayerNames() {
        synchronized (GameDBHelper.class) {
            List<String> result = new ArrayList<String>();
            String[] columns = { "distinct " + COLUMN_NAME };
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_PLAYER_SCORES, columns, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return result;
        }
    }

    public void deleteGames(Collection<Integer> gameIds) {
        synchronized (GameDBHelper.class) {
            try {
                db.beginTransaction();
                String where = " in ("
                        + TextUtils.join(",", gameIds) + ")";
                db.delete(TABLE_GAMES, COLUMN_ID + where, null);
                db.delete(TABLE_PLAYER_SCORES, COLUMN_GAME_ID + where, null);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private List<Game> convertToGames(Cursor cursor) {
        List<Game> result = new ArrayList<Game>();

        Game currentGame = null;

        while (cursor.moveToNext()) {

            int currentId = cursor.getInt(0);

            if (currentGame == null || currentGame.getId() != currentId) { // new
                                                                           // Game

                currentGame = new Game();
                currentGame.setId(currentId);
                currentGame.setDateStarted(cursor.getLong(1));
                currentGame.setDateSaved(cursor.getLong(2));
                currentGame.setName(cursor.getString(3));
                result.add(currentGame);
            }

            List<PlayerScore> playerScores = new ArrayList<PlayerScore>();

            // build up all the PlayerScores
            do {

                if (cursor.getInt(0) != currentId) {
                    cursor.moveToPrevious(); // went too far
                    break;
                }

                PlayerScore playerScore = new PlayerScore();

                playerScore.setId(cursor.getInt(4));
                playerScore.setName(cursor.getString(5));
                playerScore.setScore(cursor.getLong(6));
                playerScore.setPlayerNumber(cursor.getInt(7));
                playerScore.setHistory(Delta.fromJoinedStrings(
                        StringUtil.nullToEmpty(cursor.getString(8)),
                        StringUtil.nullToEmpty(cursor.getString(9))));
                playerScore.setLastUpdate(cursor.getLong(10));
                playerScore.setPlayerColor(PlayerColor.deserialize(cursor.getString(11)));
                playerScores.add(playerScore);

            } while (cursor.moveToNext());

            Collections.sort(playerScores, PlayerScore.sortByPlayerNumber());

            currentGame.setPlayerScores(playerScores);
        }

        return result;
    }

    /**
     * convenience method for updating games, using prepared statements for
     * performance boosts.
     * 
     * @param id
     * @param dateStarted
     * @param dateSaved
     * @param name
     * @return
     */
    private void updateGame(int id, long dateStarted, long dateSaved, String name) {
        SQLiteStatement statement = updateGame.get();

        statement.bindLong(1, dateStarted);
        statement.bindLong(2, dateSaved);
        bindStringOrNull(statement, 3, name);
        statement.bindLong(4, id);

        statement.execute();
    }

    private void updatePlayerScore(int id, String name, long score, int playerNumber, String history, 
            String historyTimestamps, long lastUpdate, String color) {
        SQLiteStatement statement = updatePlayerScore.get();

        bindStringOrNull(statement, 1, name);
        statement.bindLong(2, score);
        statement.bindLong(3, playerNumber);
        bindStringOrNull(statement, 4, history);
        bindStringOrNull(statement, 5, historyTimestamps);
        statement.bindLong(6, lastUpdate);
        statement.bindString(7, color);
        statement.bindLong(8, id);

        statement.execute();
    }

    private void bindStringOrNull(SQLiteStatement statement, int index, String str) {
        if (str == null) {
            statement.bindNull(index);
        } else {
            statement.bindString(index, str);
        }
    }
}
