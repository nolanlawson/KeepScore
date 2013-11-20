package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;

import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.widget.SquareImage;

/**
 * Activity for choosing a PlayerColor.
 * @author nolan
 *
 */
public class ColorChooserActivity extends Activity implements OnClickListener {
    
    public static final String EXTRA_SELECTED_COLOR = "selectedColor";
    
    List<SquareImage> colorViews = new ArrayList<SquareImage>();
    
    int selectedColor;
    
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        selectedColor = savedInstanceState.getInt(EXTRA_SELECTED_COLOR);
        
        setContentView(R.layout.color_chooser_dialog);
        
        setUpWidgets();
        
    }

    private void setUpWidgets() {
        
        int colorCounter = 0;
        for (int rowId : new int[]{R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4}) {
            View row = findViewById(rowId);
            for (int columnId : new int[]{R.id.column_1, R.id.column_2, R.id.column_3, R.id.column_4}) {
                SquareImage squareImage = (SquareImage)(row.findViewById(columnId));
                PlayerColor playerColor = PlayerColor.values()[colorCounter++];
                squareImage.setTag(playerColor);
                squareImage.setImageResource(playerColor.getSelectorResId());
                squareImage.setOnClickListener(this);
                colorViews.add(squareImage);
            }
        }
        
    }

    @Override
    public void onClick(View view) {
        selectedColor = ((PlayerColor)(view.getTag())).ordinal();
    }
}
