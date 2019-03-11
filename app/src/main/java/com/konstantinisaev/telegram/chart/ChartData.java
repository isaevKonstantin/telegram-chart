package com.konstantinisaev.telegram.chart;

public class ChartData {

	private String title;
	private String x;
	private String y;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "ChartData{" +
			"title='" + title + '\'' +
			", x='" + x + '\'' +
			", y='" + y + '\'' +
			'}';
	}
}
