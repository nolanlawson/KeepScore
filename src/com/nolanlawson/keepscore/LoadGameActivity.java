package com.nolanlawson.keepscore;

import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.nolanlawson.keepscore.data.SavedGameAdapter;
import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.util.StringUtil;

public class LoadGameActivity extends ListActivity implements OnItemLongClickListener {

	private SavedGameAdapter adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        List<Game> games = getAllGames();
        
        Collections.sort(games, Game.byRecentlySaved());
        
        adapter = new SavedGameAdapter(this, games);
        
        setListAdapter(adapter);
        
        setContentView(R.layout.load_game);
        
        getListView().setOnItemLongClickListener(this);
	}

	private List<Game> getAllGames() {
		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(this);
			return dbHelper.findAllGames();
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Game game = adapter.getItem(position);
		
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra(GameActivity.EXTRA_GAME_ID, game.getId());
		
		startActivity(intent);
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		
		showOptionsMenu(position);
		
		return true;
	}

	private void showOptionsMenu(final int position) {
		
		CharSequence[] options = new CharSequence[]{
				getString(R.string.text_delete), 
				getString(R.string.text_edit_name),
				getString(R.string.menu_history)};
		
		new AlertDialog.Builder(this)
			.setCancelable(true)
			.setItems(options, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					switch (which) {
					case 0: // delete
						showDeleteDialog(position);
						break;
					case 1: // edit name
						showEditGameNameDialog(position);
						break;
					case 2: // history
						showHistory(position);
						break;
					}
				}
			})
			.show();
		
	}

	protected void showHistory(int position) {
		Game game = adapter.getItem(position);
		
		Intent intent = new Intent(this, HistoryActivity.class);
		intent.putExtra(HistoryActivity.EXTRA_GAME, game);
		
		startActivity(intent);
		
	}

	private void showEditGameNameDialog(int position) {
		final Game game = adapter.getItem(position);
		

		final EditText editText = new EditText(this);
		editText.setHint(R.string.hint_game_name);
		editText.setText(StringUtil.nullToEmpty(game.getName()));
		editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		editText.setSingleLine();
		new AlertDialog.Builder(this)
			.setTitle(R.string.title_edit_game_name)
			.setView(editText)
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(final DialogInterface dialog, int which) {

					final String newName = StringUtil.nullToEmpty(editText.getText().toString());
					
					// update database in the background to avoid jankiness
					new AsyncTask<Void, Void, Void>(){

						@Override
						protected Void doInBackground(Void... params) {
							GameDBHelper dbHelper = null;
							try {
								dbHelper = new GameDBHelper(LoadGameActivity.this);
								dbHelper.updateGameName(game, newName);
							} finally {
								if (dbHelper != null) {
									dbHelper.close();
								}
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							super.onPostExecute(result);
							
							
							game.setName(newName.trim());
							adapter.notifyDataSetChanged();
							
							dialog.dismiss();
						}
						
						
						
					}.execute((Void)null);
					
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();

		
	}
	
	
	private void showDeleteDialog(final int position) {
		new AlertDialog.Builder(this)
			.setCancelable(true)
			.setTitle(R.string.title_confirm)
			.setMessage(R.string.text_game_will_be_deleted)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					deleteGame(position);
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}
	private void deleteGame(final int position) {
		
		final Game game = adapter.getItem(position);

		
		// do in background to avoid jankiness
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				
				
				GameDBHelper dbHelper = null;
				try {
					dbHelper = new GameDBHelper(LoadGameActivity.this);
					
					dbHelper.deleteGame(game);
					
				} finally {
					if (dbHelper != null) {
						dbHelper.close();
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				
				Toast.makeText(LoadGameActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
				adapter.remove(game);
				
			}
			
			
			
		}.execute((Void)null);
	}
}
