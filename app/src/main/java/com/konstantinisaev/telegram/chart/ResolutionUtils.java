package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class ResolutionUtils {

	public static int dpToPx(Context context, float dp) {
		Resources r = context.getResources();
		return (int) (TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()) + 0.5f);
	}
}
