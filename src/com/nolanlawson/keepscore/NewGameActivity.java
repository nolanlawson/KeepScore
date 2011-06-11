package com.nolanlawson.keepscore;

import android.app.Activity;
import android.os.Bundle;

public class NewGameActivity extends Activity {


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
	}

	
}
