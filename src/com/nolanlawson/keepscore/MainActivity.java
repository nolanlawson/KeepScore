package com.nolanlawson.keepscore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

	private Button newGameButton, resumeGameButton, loadGameButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		
		newGameButton = (Button) findViewById(android.R.id.button1);
		resumeGameButton = (Button) findViewById(android.R.id.button2);
		loadGameButton = (Button) findViewById(android.R.id.button3);
		
		for (Button button : new Button[]{newGameButton, resumeGameButton, loadGameButton}) {
			button.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case android.R.id.button1:
			Intent intent = new Intent(this, NewGameActivity.class);
			startActivity(intent);
			break;
		case android.R.id.button2:
			break;
		case android.R.id.button3:
			break;
		}
	}
}