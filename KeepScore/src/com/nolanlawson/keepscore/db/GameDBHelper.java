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

import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

public class GameDBHelper extends SQLiteOpenHelper {

	private static UtilLogger log = new UtilLogger(GameDBHelper.class);
	
	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 2;

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
	
	private static final String JOINED_TABLES = TABLE_GAMES + " g join " + TABLE_PLAYER_SCORES + " ps ON "
			+ "g." + COLUMN_ID + "=ps." + COLUMN_GAME_ID;
	private static final String[] JOINED_COLUMNS = 	new String[]{
		"g." + COLUMN_ID, 
		"g." + COLUMN_DATE_STARTED, 
		"g." + COLUMN_DATE_SAVED,
		"g." + COLUMN_NAME,
		"ps." + COLUMN_ID, 
		"ps." + COLUMN_NAME, 
		"ps." + COLUMN_SCORE, 
		"ps." + COLUMN_PLAYER_NUMBER,
		"ps." + COLUMN_HISTORY};
	
	private ThreadLocal<SQLiteStatement> updateGame = new ThreadLocal<SQLiteStatement>(){

		@Override
		protected SQLiteStatement initialValue() {
			String sql = "update " + TABLE_GAMES + " set " 
					+ COLUMN_DATE_STARTED + "=?,"
					+ COLUMN_DATE_SAVED + "=?,"
					+ COLUMN_NAME + "=? "
					+ "where " + COLUMN_ID + "=?";
			return db.compileStatement(sql);
		}
	};
	
	private ThreadLocal<SQLiteStatement> updatePlayerScore = new ThreadLocal<SQLiteStatement>(){

		@Override
		protected SQLiteStatement initialValue() {
			String sql = "update " + TABLE_PLAYER_SCORES + " set " 
					+ COLUMN_NAME + "=?,"
					+ COLUMN_SCORE + "=?,"
					+ COLUMN_PLAYER_NUMBER + "=?,"
					+ COLUMN_HISTORY + "=? "
					+ "where " + COLUMN_ID + "=?";
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
		String createSql1 = "create table if not exists " + TABLE_GAMES + " ("
				+ COLUMN_ID + " integer not null primary key autoincrement, "
				+ COLUMN_NAME + " text, "
				+ COLUMN_AUTOSAVED + " int not null, "
				+ COLUMN_DATE_STARTED + " int not null, " + COLUMN_DATE_SAVED
				+ " int not null);";
		
		db.execSQL(createSql1);
		
		String createSql2 = "create table if not exists " + TABLE_PLAYER_SCORES + " ("
				+ COLUMN_ID + " integer not null primary key autoincrement, "
				+ COLUMN_NAME + " text not null, " + COLUMN_SCORE
				+ " int not null, " + COLUMN_PLAYER_NUMBER + " int not null, "
				+ COLUMN_HISTORY + " text, "
				+ COLUMN_GAME_ID + " int not null);";
		
		db.execSQL(createSql2);
		
		String indexSql1 = "create index if not exists index_game_id on " + TABLE_PLAYER_SCORES
				+ " (" + COLUMN_GAME_ID + ");";
		
		db.execSQL(indexSql1);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// add an index for the startTime
		String index = "create index if not exists index_date_started on " + TABLE_GAMES
				+ "(" + COLUMN_DATE_STARTED + ");";
		
		db.execSQL(index);
	}
	
	/**
	 * Return true if a game with that dateStarted value exists.
	 * @param dateStarted
	 * @return
	 */
	public boolean existsByDateStarted(long dateStarted) {
		synchronized (GameDBHelper.class) {
			Cursor cursor = null;
			try {
				cursor = db.query(TABLE_GAMES, new String[]{COLUMN_ID}, "dateStarted=" + dateStarted, 
						null, null, null, null);
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
			String[] columns = {"count(" + COLUMN_ID +")"};
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
				cursor = db.query(TABLE_GAMES, new String[]{COLUMN_ID}, null, null, null, null, orderBy);
				
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
				
				String sql = new StringBuilder("select ")
					.append(TextUtils.join(",", JOINED_COLUMNS))
					.append(" from ")
					.append(JOINED_TABLES)
					.append(" order by ")
					.append(COLUMN_DATE_SAVED)
					.append(" desc ")
					.append(" limit 1")
					.toString();
				
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
	 * @param game
	 * @return
	 */
	public void saveGame(Game game) {
		saveGame(game, true);
	}
	
	/**
	 * save a game, optionally updating its 'dateSaved' value
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
			contentValues.put(COLUMN_AUTOSAVED, 1); // legacy "autosaved" column that must be specified
			
			db.insert(TABLE_GAMES, null, contentValues);
			
			game.setId(newGameId);
			log.d("new game id is %s", newGameId);
		}
		
		savePlayerScores(game.getId(), game.getPlayerScores());
		
	}
	
	private int getMaxPlayerScoreId() {
		Cursor cursor = null;
		try {
			cursor = db.query(TABLE_PLAYER_SCORES, new String[]{"max(" + COLUMN_ID + ")"}, null, null, null, null, null);
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
			cursor = db.query(TABLE_GAMES, new String[]{"max(" + COLUMN_ID + ")"}, null, null, null, null, null);
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
				
				String historyAsString = playerScore.getHistory() != null
						? TextUtils.join(",", playerScore.getHistory())
						: null;

				
				if (playerScore.getId() != -1) {
					// already exists; update
					
					updatePlayerScore(playerScore.getId(), playerScore.getName(), playerScore.getScore(), 
							playerScore.getPlayerNumber(), historyAsString);
					
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
					values.put(COLUMN_HISTORY, historyAsString);
					values.put(COLUMN_NAME, playerScore.getName());
					values.put(COLUMN_PLAYER_NUMBER, playerScore.getPlayerNumber());
					values.put(COLUMN_SCORE, playerScore.getScore());
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

	public void updateGameName(Game game, String newName) {
		synchronized (GameDBHelper.class) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_NAME, newName);
			
			db.update(TABLE_GAMES, values, COLUMN_ID + "=" + game.getId(), null);
		}
	}

	public List<String> findDistinctPlayerNames() {
		synchronized (GameDBHelper.class) {
			List<String> result = new ArrayList<String>();
			String[] columns = {"distinct " + COLUMN_NAME};
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

	public void deleteGames(Collection<Game> games) {
		synchronized (GameDBHelper.class) {
			try {
				db.beginTransaction();
				String where = " in (" 
						+ TextUtils.join(",", CollectionUtil.transform(games, new Function<Game,Integer>() {

							@Override
							public Integer apply(Game obj) {
								return obj.getId();
							}
				})) + ")"; 
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
			
			if (currentGame == null || currentGame.getId() != currentId) { // new Game
				
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
				playerScore.setHistory(
						CollectionUtil.stringsToInts(
						StringUtil.split(StringUtil.nullToEmpty(cursor.getString(8)), ',')));
				playerScores.add(playerScore);
				
			} while (cursor.moveToNext());
			
			
			Collections.sort(playerScores, PlayerScore.sortByPlayerNumber());
			
			currentGame.setPlayerScores(playerScores);
		}
		
		return result;
	}
	
	/**
	 * convenience method for updating games, using prepared statements for performance boosts.
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
	
	private void updatePlayerScore(int id, String name, long score, int playerNumber, String history) {
		SQLiteStatement statement = updatePlayerScore.get();
		
		bindStringOrNull(statement, 1, name);
		statement.bindLong(2, score);
		statement.bindLong(3, playerNumber);
		bindStringOrNull(statement, 4, history);
		statement.bindLong(5, id);
		
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
