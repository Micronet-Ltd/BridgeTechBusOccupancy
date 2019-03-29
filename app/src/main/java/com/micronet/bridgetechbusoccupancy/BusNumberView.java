package com.micronet.bridgetechbusoccupancy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by austin.oneil on 2/5/2019.
 */

public class BusNumberView extends TextView {

    public BusNumberView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackground(getResources().getDrawable(R.drawable.bus));
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
    }
}
