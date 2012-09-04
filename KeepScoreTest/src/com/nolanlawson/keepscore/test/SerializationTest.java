package com.nolanlawson.keepscore.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.keepscore.MainActivity;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.serialization.GamesBackup;
import com.nolanlawson.keepscore.serialization.GamesBackupSerializer;

/**
 * Tests for the XML serialization.
 * @author nolan
 *
 */
public class SerializationTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private Random random = new Random();
	
	public SerializationTest() {
		super("com.nolanlawson.keepscore", MainActivity.class);
	}
	
	public void testBasicSerialization() {
		GamesBackup gamesBackup = createRandomGamesBackup();
		
		testGamesBackup(gamesBackup);
	}
	
	public void testSingleGame() {
		GamesBackup gamesBackup = createRandomGamesBackup();
		gamesBackup.setGames(Collections.singletonList(gamesBackup.getGames().get(0)));
		
		testGamesBackup(gamesBackup);
	}
	
	public void testNoGames() {
		GamesBackup gamesBackup = createRandomGamesBackup();
		gamesBackup.setGames(Collections.<Game>emptyList());
		
		testGamesBackup(gamesBackup);
	}
	
	public void testNullsAndEmpties() {
		GamesBackup gamesBackup = createRandomGamesBackup();
		
		gamesBackup.getGames().get(0).setName(null);
		gamesBackup.getGames().get(1).setName("");
		
		gamesBackup.getGames().get(0).getPlayerScores().get(0).setName(null);
		gamesBackup.getGames().get(0).getPlayerScores().get(1).setName("");
		gamesBackup.getGames().get(0).getPlayerScores().get(0).setHistory(Collections.<Integer>emptyList());
				
		testGamesBackup(gamesBackup);
	}
	
	private void testGamesBackup(GamesBackup gamesBackup) {
		
		String xmlData = GamesBackupSerializer.serialize(gamesBackup);
		GamesBackup deserializedGamesBackup = GamesBackupSerializer.deserialize(xmlData);
		
		compareGamesBackups(gamesBackup, deserializedGamesBackup);
	}
	
	private void compareGamesBackups(GamesBackup first, GamesBackup second) {
		
		assertEquals(first.getDateSaved(), second.getDateSaved());
		assertEquals(first.getVersion(), second.getVersion());
		assertEquals(first.getGames().size(), second.getGames().size());
		assertEquals(first.getGameCount(), second.getGameCount());
		
		for (int i = 0; i < first.getGames().size(); i++) {
			compareGames(first.getGames().get(i), second.getGames().get(i));
		}
	}
	
	private void compareGames(Game first, Game second) {
		
		assertEquals(first.getDateSaved(), second.getDateSaved());
		assertEquals(first.getDateStarted(), second.getDateStarted());
		assertEqualsWithNulls(first.getName(), second.getName());
		assertEquals(first.getPlayerScores().size(), second.getPlayerScores().size());
		
		for (int i = 0; i < first.getPlayerScores().size(); i++) {
			comparePlayerScores(first.getPlayerScores().get(i), second.getPlayerScores().get(i));
		}
	}

	private void comparePlayerScores(PlayerScore first, PlayerScore second) {
		
		assertEqualsWithNulls(first.getName(), second.getName());
		assertEquals(first.getPlayerNumber(), second.getPlayerNumber());
		assertEquals(first.getScore(), second.getScore());
		assertEquals(first.getHistory(), second.getHistory());
	}

	private GamesBackup createRandomGamesBackup() {
		GamesBackup gamesBackup = new GamesBackup();
		gamesBackup.setGames(new ArrayList<Game>());
		
		gamesBackup.setDateSaved(2437934297L);
		gamesBackup.setVersion(1);
		
		int numGames = random.nextInt(10) + 2;
		for (int i = 0; i < numGames; i++) {
			
			Game game = new Game();
			
			game.setDateSaved(random.nextLong());
			game.setDateStarted(random.nextLong());
			game.setName(Long.toString(random.nextLong(), 16));
			game.setPlayerScores(createRandomPlayerScores());
			
			gamesBackup.getGames().add(game);
		}
		
		gamesBackup.setGameCount(gamesBackup.getGames().size());
		
		return gamesBackup;
	}

	private List<PlayerScore> createRandomPlayerScores() {
		int numPlayers = random.nextInt(8) + 2;
		
		List<PlayerScore> playerScores = new ArrayList<PlayerScore>();
		for (int i = 0; i < numPlayers; i++) {
			PlayerScore playerScore = new PlayerScore();
			
			int score = random.nextInt(100) + 30;
			playerScore.setScore(score);
			playerScore.setHistory(createRandomHistory(score));
			playerScore.setName(Long.toString(random.nextLong(), 16));
			playerScore.setPlayerNumber(i);
			
			playerScores.add(playerScore);
		}
		
		return playerScores;
	}

	private List<Integer> createRandomHistory(int score) {
		List<Integer> history = new ArrayList<Integer>();
		for (int delta, sum = 0; sum < score; sum += delta) {
			history.add(delta = Math.min(random.nextInt(15) + 1, score - sum));
		}
		return history;
	}
	
	private void assertEqualsWithNulls(String first, String second) {
		if (first == null) {
			assertNull(second);
		} else {
			assertEquals(first, second);
		}
	}

}
