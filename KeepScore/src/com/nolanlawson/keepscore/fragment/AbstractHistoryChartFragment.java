package com.nolanlawson.keepscore.fragment;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.widget.chart.LineChartView;

public abstract class AbstractHistoryChartFragment extends SherlockFragment {
    
    // valid scale values for the history item width when zooming in and out
    private static final List<Float> ZOOM_VALUES = Arrays.asList(
        0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.75F, 1.0F, 1.5F, 2.0F, 2.5F, 3.0F);
    
    private AtomicInteger currentZoomIdx = new AtomicInteger(ZOOM_VALUES.indexOf(1.0F));
    
    protected abstract LineChartView getChart();
    protected abstract View getContainer();
    
    protected List<Integer> createLineColors(Game game, final Activity activity) {
        
        return CollectionUtil.transform(game.getPlayerScores(), new Function<PlayerScore, Integer>(){

            @Override
            public Integer apply(PlayerScore obj) {
                return obj.getPlayerColor().toChartColor(activity);
            }
        });
    }
    

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.history_menu, menu);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        MenuItem zoomInMenuItem = menu.findItem(R.id.menu_zoom_in);
        MenuItem zoomOutMenuItem = menu.findItem(R.id.menu_zoom_out);
        
        
        boolean atMin = currentZoomIdx.get() == 0;
        boolean atMax = currentZoomIdx.get() == ZOOM_VALUES.size() - 1;
        
        zoomInMenuItem.setEnabled(!atMax);
        zoomInMenuItem.setVisible(true);
        zoomOutMenuItem.setEnabled(!atMin);
        zoomOutMenuItem.setVisible(true);
        
        //
        // set the icons to be grayed out if disabled.  It looks prettier that way.  See
        // http://stackoverflow.com/questions/9642990/is-it-possible-to-grey-out-not-just-disable-a-menuitem-in-android
        // for details.
        //
        Drawable zoomInIcon = getResources().getDrawable(R.drawable.action_zoom_in);
        Drawable zoomOutIcon = getResources().getDrawable(R.drawable.action_zoom_out);
        
        if (atMin) {
            zoomOutIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else if (atMax) {
            zoomInIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
        
        zoomInMenuItem.setIcon(zoomInIcon);
        zoomOutMenuItem.setIcon(zoomOutIcon);
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.menu_zoom_in:
                changeZoom(1);
                return true;
            case R.id.menu_zoom_out:
                changeZoom(-1);
                return true;
        }            
        return false;
    }
    
    private void changeZoom(int delta) {
        
        // change the zoom on the graph, i.e. update the width of the individual history items
        float zoomValue = ZOOM_VALUES.get(Math.min(ZOOM_VALUES.size() - 1, Math.max(0, currentZoomIdx.addAndGet(delta))));
        
        getChart().setZoomLevel(zoomValue);
        getChart().requestLayout();
        getChart().invalidate();
        getContainer().invalidate();
        
        getSherlockActivity().supportInvalidateOptionsMenu();
    }    
    
}
