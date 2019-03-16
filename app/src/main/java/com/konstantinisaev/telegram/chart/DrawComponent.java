package com.konstantinisaev.telegram.chart;

import android.graphics.Canvas;

public interface DrawComponent<T> {

	void build(T params);

	void onDraw(Canvas canvas);

	void clear();
}
