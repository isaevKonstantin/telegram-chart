package com.konstantinisaev.telegram.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import java.util.List;
import java.util.Set;

import static com.konstantinisaev.telegram.chart.MainActivity.getContext;

public class UpperDateDrawer implements DrawComponent<UpperDateDrawer.UpperDateDrawerParams> {

	private Paint headerPaint;
	private UpperDateDrawerParams params;

	public UpperDateDrawer() {
		headerPaint = new Paint();
		headerPaint.setStyle(Paint.Style.FILL);
		headerPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
		headerPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.text12));
	}

	@Override
	public void build(UpperDateDrawerParams params) {
		this.params = params;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(params == null){
			return;
		}
		int position = 0;
		for (String date : params.date) {
			canvas.drawText(date,params.positions.get(position),params.bottomLine, headerPaint);
			position += 1;
		}
	}

	@Override
	public void clear() {}

	static class UpperDateDrawerParams {

		final Set<String> date;
		final List<Float> positions;
		final float bottomLine;

		UpperDateDrawerParams(Set<String> date, List<Float> positions, float bottomLine) {
			this.date = date;
			this.positions = positions;
			this.bottomLine = bottomLine;
		}
	}
}
