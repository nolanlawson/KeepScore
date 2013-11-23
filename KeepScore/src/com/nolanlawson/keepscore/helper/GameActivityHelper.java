package com.nolanlawson.keepscore.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.LandscapeGameActivity;
import com.nolanlawson.keepscore.NewGameActivity;
import com.nolanlawson.keepscore.PortraitGameActivity;
import com.nolanlawson.keepscore.db.Game;

public class GameActivityHelper {

    public static void openGame(Context context, Game game) {
        Intent intent = new Intent(context, getGameActivityClass(context, game.getPlayerScores().size()));
        intent.putExtra(GameActivity.EXTRA_GAME_ID, game.getId());

        context.startActivity(intent);
    }
    
    public static void newGame(Context context, String[] playerNames, String[] playerColors) {
        Intent intent = new Intent(context, getGameActivityClass(context, playerNames.length));

        intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
        intent.putExtra(GameActivity.EXTRA_PLAYER_COLORS, playerColors);

        context.startActivity(intent);
    }
    
    public static void newGameWithClearTop(Activity context, Game game) {
        Intent intent = new Intent(context, getGameActivityClass(context, game.getPlayerScores().size()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GameActivity.EXTRA_GAME, game);

        context.startActivity(intent);

        CompatibilityHelper.overridePendingTransition(context, android.R.anim.fade_in,
                android.R.anim.fade_out);
    }
    
    private static Class<?> getGameActivityClass(Context context, int numPlayers) {
        
        if (numPlayers > NewGameActivity.TYPICAL_MAX_NUMBER_OF_PLAYERS // landscape mode can't handle >8 players 
                || PreferenceHelper.getOrientation(context) == Orientation.Portrait) {
            return PortraitGameActivity.class;
        }
        return LandscapeGameActivity.class;
    }
    
}
