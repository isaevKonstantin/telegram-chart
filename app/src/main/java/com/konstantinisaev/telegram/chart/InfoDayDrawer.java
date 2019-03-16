package com.konstantinisaev.telegram.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.Pair;

import java.util.List;

import static com.konstantinisaev.telegram.chart.MainActivity.getContext;

public class InfoDayDrawer implements DrawComponent<InfoDayDrawer.InfoDayParams> {

	private Paint circlePaint;
	private InfoDayParams params;
	private Paint blueStrokePaint;


	public InfoDayDrawer() {
		circlePaint = new Paint();
		circlePaint.setStyle(Paint.Style.FILL);

		blueStrokePaint = new Paint();
		blueStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_scroller));
		blueStrokePaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void build(InfoDayParams params) {
		this.params = params;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(params == null){
			return;
		}
		canvas.drawPath(params.linePath,blueStrokePaint);
		for (Pair<Path, String> item : this.params.data) {
			circlePaint.setColor(Color.parseColor(item.second));
			canvas.drawPath(item.first,circlePaint);
		}
	}

	@Override
	public void clear() {
		params = null;
	}

	static class InfoDayParams {

		final List<Pair<Path,String>> data;
		private Path linePath;

		InfoDayParams(List<Pair<Path, String>> path, Path linePath) {
			this.data = path;
			this.linePath = linePath;
		}
	}
}
