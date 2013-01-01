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

    public GamesBackupSummaryAdapter(Context context, DisplayMetrics displayMetrics, List<GamesBackupSummary> objects) {
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

        // view wrapper optimization per Romain Guy
        final Context context = parent.getContext();
        ViewWrapper viewWrapper;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(LAYOUT_RES_ID, parent, false);
            viewWrapper = new ViewWrapper(view);
            view.setTag(viewWrapper);
        } else {
            viewWrapper = (ViewWrapper) view.getTag();
        }
        
        GamesBackupSummary summary = getItem(position);

        viewWrapper.getAutoOrManualTextView().setText(summary.isAutomatic() ? R.string.text_backup_automatic
                : R.string.text_backup_manual);
        viewWrapper.getDateTextView().setText(dateFormat.format(new Date(summary.getDateSaved())));
        viewWrapper.getFilenameTextView().setText(summary.getFilename());
        viewWrapper.getNumGamesTextView().setText(Integer.toString(summary.getGameCount()));

        log.d("setting minWidth to %d", gameCountMinWidth);
        viewWrapper.getNumGamesTextView().setMinWidth(gameCountMinWidth);

        return view;
    }

    /**
     * ViewWrapper optimization per Romain Guy on some blog post.
     * 
     * @author nolan
     * 
     */
    private static class ViewWrapper {

        private View view;
        private TextView autoOrManualTextView, dateTextView, filenameTextView, numGamesTextView;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public TextView getAutoOrManualTextView() {
            if (autoOrManualTextView == null) {
                autoOrManualTextView = (TextView) view.findViewById(R.id.text_auto_or_manual);
            }
            return autoOrManualTextView;
        }

        public TextView getDateTextView() {
            if (dateTextView == null) {
                dateTextView = (TextView) view.findViewById(R.id.text_date_saved);
            }
            return dateTextView;
        }

        public TextView getFilenameTextView() {
            if (filenameTextView == null) {
                filenameTextView = (TextView) view.findViewById(R.id.text_filename);
            }
            return filenameTextView;
        }

        public TextView getNumGamesTextView() {
            if (numGamesTextView == null) {
                numGamesTextView = (TextView) view.findViewById(R.id.text_num_games);
            }
            return numGamesTextView;
        }

    }
}
