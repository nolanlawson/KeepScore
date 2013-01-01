package com.nolanlawson.keepscore.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.serialization.GamesBackupSummary;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * Simple adapter for showing GamesBackupSummaries, when loading backups.
 * 
 * @author nolan
 * 
 */
public class GamesBackupSummaryAdapter extends ArrayAdapter<GamesBackupSummary> {

    private static final int LAYOUT_RES_ID = R.layout.games_backup_item;

    private static UtilLogger log = new UtilLogger(GamesBackupSummaryAdapter.class);
    
    private DateFormat dateFormat;
    private int gameCountMinWidth;

    public GamesBackupSummaryAdapter(Context context, DisplayMetrics displayMetrics, 
            List<GamesBackupSummary> objects) {
        super(context, LAYOUT_RES_ID, new ArrayList<GamesBackupSummary>(objects));

        dateFormat = new SimpleDateFormat(context.getString(R.string.text_backup_date_format));
        gameCountMinWidth = calculateGameCountMinWidth(context, displayMetrics, objects);
    }

    private int calculateGameCountMinWidth(Context context, DisplayMetrics displayMetrics,
            List<GamesBackupSummary> objects) {
        // ensure that the min width of the game count matches the max number of
        // games, e.g. if there are 130 games
        // in one of the saved games files, then
        // the minWidth should be 60 (20sp * 3 to support 3 characters in "130")
        int maxNumChars = CollectionUtil.maxValue(objects, new Function<GamesBackupSummary, Integer>() {

            public Integer apply(GamesBackupSummary obj) {
                return Integer.toString(obj.getGameCount()).length();
            }

        });

        log.d("max num chars is %d", maxNumChars);
        
        int spValue = 20 * maxNumChars;
        
        // Convert the sp to pixels
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(LAYOUT_RES_ID, null, false);
        }

        TextView autoOrManualTextView = (TextView) view.findViewById(R.id.text_auto_or_manual);
        TextView dateTextView = (TextView) view.findViewById(R.id.text_date_saved);
        TextView filenameTextView = (TextView) view.findViewById(R.id.text_filename);
        TextView numGamesTextView = (TextView) view.findViewById(R.id.text_num_games);

        GamesBackupSummary summary = getItem(position);

        autoOrManualTextView.setText(summary.isAutomatic() ? R.string.text_backup_automatic
                : R.string.text_backup_manual);
        dateTextView.setText(dateFormat.format(new Date(summary.getDateSaved())));
        filenameTextView.setText(summary.getFilename());
        numGamesTextView.setText(Integer.toString(summary.getGameCount()));

        log.d("setting minWidth to %d", gameCountMinWidth);
        numGamesTextView.setMinWidth(gameCountMinWidth);

        return view;
    }
}
