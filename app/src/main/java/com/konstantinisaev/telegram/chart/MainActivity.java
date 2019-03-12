package com.konstantinisaev.telegram.chart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

	private ChartView chartView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chartView = findViewById(R.id.chartView);
		parseJson();
	}

	private void parseJson() {
		String json = loadJSONFromAsset();
		if(TextUtils.isEmpty(json)){
			return;
		}
		try {
			JSONArray jsonArray = new JSONArray(json);
			List<ChartData> chartData = new ArrayList<>();
			for (int i = 0;i < jsonArray.length();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				chartData.add(ChartData.parseFromJson(jsonObject));
			}
			chartView.bindData(chartData);
			Log.d(MainActivity.class.getSimpleName(),jsonArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private String loadJSONFromAsset() {
		String json;
		try {
			InputStream is = this.getAssets().open("chart_data.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main,menu);
		return super.onCreateOptionsMenu(menu);
	}
}
