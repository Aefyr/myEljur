package com.af.myeljur;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Peter on 14.03.2017.
 */

public class BriefMaximumHeightListView extends ListView {
    public BriefMaximumHeightListView(Context context) {
        super(context);
    }
    public BriefMaximumHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public BriefMaximumHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
