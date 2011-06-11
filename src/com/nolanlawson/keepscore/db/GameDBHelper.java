package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.StringUtil;

public class GameDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 1;

	private static final String TABLE_GAMES = "Games";
	private static final String TABLE_PLAYER_SCORES = "PlayerScores";
	
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_DATE_STARTED = "dateStarted";
	private static final String COLUMN_DATE_SAVED = "dateSaved";
	private static final String COLUMN_AUTOSAVED = "autosaved";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_SCORE = "score";
	private static final String COLUMN_PLAYER_NUMBER = "playerNumber";
	private static final String COLUMN_GAME_ID = "gameId";
	private static final String COLUMN_HISTORY = "history";
	
	private static final String JOINED_TABLES = TABLE_GAMES + " g join " + TABLE_PLAYER_SCORES + " ps ON "
			+ "g." + COLUMN_ID + "=ps." + COLUMN_GAME_ID;
	private static final String[] JOINED_COLUMNS = 	new String[]{
		"g." + COLUMN_ID, 
		"g." + COLUMN_DATE_STARTED, 
		"g." + COLUMN_DATE_SAVED,
		"g." + COLUMN_AUTOSAVED, 
		"g." + COLUMN_NAME,
		"ps." + COLUMN_ID, 
		"ps." + COLUMN_NAME, 
		"ps." + COLUMN_SCORE, 
		"ps." + COLUMN_PLAYER_NUMBER,
		"ps." + COLUMN_HISTORY};
	
	
	private Context context;
	private SQLiteDatabase db;

	public GameDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createSql1 = "create table if not exists " + TABLE_GAMES + " ("
				+ COLUMN_ID + " integer not null primary key autoincrement, "
				+ COLUMN_NAME + " text, "
				+ COLUMN_DATE_STARTED + " int not null, " + COLUMN_DATE_SAVED
				+ " int not null, " + COLUMN_AUTOSAVED + " int not null);";
		
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
		
		String indexSql2 = "create index if not exists index_autosaved on " + TABLE_GAMES 
				+ "(" + COLUMN_AUTOSAVED + ");";
		
		db.execSQL(indexSql2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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
	
	public Game findMostRecentGame() {
		synchronized (GameDBHelper.class) {
			Cursor cursor = null;
			try {
				String orderBy = COLUMN_DATE_SAVED + " desc";
				cursor = db.query(JOINED_TABLES, JOINED_COLUMNS, null, null, null, null, orderBy);
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
	 * return true if a new game was saved
	 * @param game
	 * @return
	 */
	public boolean saveGame(Game game, boolean autosaved) {
		synchronized (GameDBHelper.class) {
			long dateSaved = System.currentTimeMillis();
			game.setDateSaved(dateSaved);
			game.setAutosaved(autosaved);
			
			ContentValues contentValues = new ContentValues();
			
			contentValues.put(COLUMN_DATE_STARTED, game.getDateStarted());
			contentValues.put(COLUMN_DATE_SAVED, dateSaved);
			contentValues.put(COLUMN_NAME, game.getName());
			contentValues.put(COLUMN_AUTOSAVED, autosaved);
			
			if (game.getId() != -1) { // might be a game that was already saved, so try to overwrite
				contentValues.put(COLUMN_ID, game.getId());
	
				int updated = db.update(TABLE_GAMES, contentValues, COLUMN_ID + "=" + game.getId(), null);
				
				if (updated != 0) {
					savePlayerScores(game.getId(), game.getPlayerScores());
					
					return false;
				}
			}
			// else create a new row in the table
			
			db.insert(TABLE_GAMES, null, contentValues);
			int newId;
			
			Cursor cursor = null;
			try {
				cursor = db.query(TABLE_GAMES, new String[]{COLUMN_ID}, "rowid=last_insert_rowid()", null, null, null, null);
				cursor.moveToNext();
				newId = cursor.getInt(0);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			
			game.setId(newId);
			
			savePlayerScores(newId, game.getPlayerScores());
			
			return true;
		}
	}
	
	private void savePlayerScores(int gameId, List<PlayerScore> playerScores) {
		synchronized (GameDBHelper.class) {
			
			for (PlayerScore playerScore : playerScores) {
				
				ContentValues values = new ContentValues();
				
				values.put(COLUMN_GAME_ID, gameId);
				if (playerScore.getHistory() != null) {
					// don't include deltas of 0
					List<Integer> filteredHistory = CollectionUtil.filter(playerScore.getHistory(), IntegerUtil.isNonZero());
					values.put(COLUMN_HISTORY, TextUtils.join(",", filteredHistory));
				} else {
					values.put(COLUMN_HISTORY, (String)null);
				}
				values.put(COLUMN_NAME, playerScore.getName());
				values.put(COLUMN_PLAYER_NUMBER, playerScore.getPlayerNumber());
				values.put(COLUMN_SCORE, playerScore.getScore());
				
				if (playerScore.getId() != -1) { // try to update
					
					values.put(COLUMN_ID, playerScore.getId());
					
					int updated = db.update(TABLE_PLAYER_SCORES, values, COLUMN_ID + "=" + playerScore.getId(), null);
					
					if (updated != 0) {
						continue;
					}
					
				}
				// else create new rows in the table
				
				db.insert(TABLE_PLAYER_SCORES, null, values);
				
				
				// set the new id on the PlayerScore
				int newId;
				
				Cursor cursor = null;
				try {
					cursor = db.query(TABLE_PLAYER_SCORES, new String[]{COLUMN_ID}, "rowid=last_insert_rowid()", null, null, null, null);
					cursor.moveToNext();
					newId = cursor.getInt(0);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
				
				playerScore.setId(newId);
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
			int id = game.getId();
			
			db.delete(TABLE_GAMES, COLUMN_ID + "=" + id, null);
			db.delete(TABLE_PLAYER_SCORES, COLUMN_GAME_ID + "=" + id, null);
		}
	}

	public void updateGameName(Game game, String newName) {
		synchronized (GameDBHelper.class) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_NAME, newName);
			
			db.update(TABLE_GAMES, values, COLUMN_ID + "=" + game.getId(), null);
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
				currentGame.setDateSaved(cursor.getInt(2));
				currentGame.setAutosaved(cursor.getInt(3) != 0);
				currentGame.setName(cursor.getString(4));
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
				
				playerScore.setId(cursor.getInt(5));
				playerScore.setName(cursor.getString(6));
				playerScore.setScore(cursor.getLong(7));
				playerScore.setPlayerNumber(cursor.getInt(8));
				playerScore.setHistory(
						CollectionUtil.stringsToInts(
						StringUtil.split(StringUtil.nullToEmpty(cursor.getString(9)), ',')));
				playerScores.add(playerScore);
				
			} while (cursor.moveToNext());
			
			
			Collections.sort(playerScores, PlayerScore.sortByPlayerNumber());
			
			currentGame.setPlayerScores(playerScores);
		}
		
		return result;
	}
}
