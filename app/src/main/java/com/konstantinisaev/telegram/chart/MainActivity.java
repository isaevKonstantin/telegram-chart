package com.konstantinisaev.telegram.chart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

	private ChartScrollerView chartScrollerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chartScrollerView = findViewById(R.id.scroller);
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
			chartScrollerView.bindData(chartData);
			Log.d(MainActivity.class.getSimpleName(),jsonArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private String loadJSONFromAsset() {
		String json = null;
		try {
			InputStream is = this.getAssets().open("chart_data.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}
}
