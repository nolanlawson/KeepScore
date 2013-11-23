package com.nolanlawson.keepscore;


public class PortraitGameActivity extends GameActivity {

    @Override
    public void hideAbsentPlayers() {
        // no need to do anything; viewstub takes care of not inflating anything that doesn't
        // need to be inflated
    }
    
    @Override
    public boolean getShouldShowOnscreenDeltaButtons() {
        return false; // don't show on portrait; it looks weird and I'm too lazy to fix it.
    }
}