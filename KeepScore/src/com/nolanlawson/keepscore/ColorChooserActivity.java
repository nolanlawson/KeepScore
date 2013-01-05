package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;

import com.nolanlawson.keepscore.helper.PlayerColor;

/**
 * Activity for choosing a PlayerColor.
 * @author nolan
 *
 */
public class ColorChooserActivity extends Activity implements OnClickListener {
    
    List<View> colorViews = new ArrayList<View>();
    List<LinearLayout> rows = new ArrayList<LinearLayout>();
    
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_chooser_dialog);
        
        setUpWidgets();
        
    }

    private void setUpWidgets() {
        
        for (int resId : new int[]{R.id.linear_layout_row_1, R.id.linear_layout_row_2,
                R.id.linear_layout_row_3, R.id.linear_layout_row_4}) {
            LinearLayout row = (LinearLayout) findViewById(resId);
            Iterator<PlayerColor> playerColors = Arrays.asList(PlayerColor.values()).iterator();
            for (int i = 0; i < row.getChildCount(); i++) {
                View view = row.getChildAt(i);
                colorViews.add(view);
                
                PlayerColor playerColor = playerColors.next();
                view.setTag(playerColor);
                view.setBackgroundResource(playerColor.getSelectorResId());
                view.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        for (View otherView : colorViews) {
            otherView.setSelected(view.getId() == otherView.getId());
        }
    }
}
