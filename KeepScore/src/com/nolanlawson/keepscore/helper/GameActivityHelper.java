package com.nolanlawson.keepscore.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nolanlawson.keepscore.GameActivity;
import com.nolanlawson.keepscore.PortraitGameActivity;
import com.nolanlawson.keepscore.db.Game;

public class GameActivityHelper {

    public static void openGame(Context context, int gameId) {
        Intent intent = new Intent(context, getGameActivityClass());
        intent.putExtra(GameActivity.EXTRA_GAME_ID, gameId);

        context.startActivity(intent);
    }
    
    public static void newGame(Context context, String[] playerNames, String[] playerColors) {
        Intent intent = new Intent(context, getGameActivityClass());

        intent.putExtra(GameActivity.EXTRA_PLAYER_NAMES, playerNames);
        intent.putExtra(GameActivity.EXTRA_PLAYER_COLORS, playerColors);

        context.startActivity(intent);
    }
    
    public static void newGameWithClearTop(Activity context, Game game) {
        Intent intent = new Intent(context, getGameActivityClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(GameActivity.EXTRA_GAME, game);

        context.startActivity(intent);

        CompatibilityHelper.overridePendingTransition(context, android.R.anim.fade_in,
                android.R.anim.fade_out);
    }
    
    private static Class<?> getGameActivityClass() {
        return PortraitGameActivity.class;
    }
    
}
