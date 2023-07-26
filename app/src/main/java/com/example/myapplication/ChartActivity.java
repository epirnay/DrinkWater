package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartActivity extends AppCompatActivity {

    // variable for our bar chart
    BarChart barChart;

    // variable for our bar data set.
    private BarDataSet waterIntakeLastWeek, waterIntakeThisWeek;

    private BarChart stepsBarChart;

    private BarDataSet stepCountLastWeek, stepCountThisWeek;
    Button buttonToSettings;

    // array list for storing entries.
    ArrayList barEntries;

    // creating a string array for displaying days.
    String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    GraphView graphViewWater;
    GraphView graphViewStep;
    MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);

        buttonToSettings = (Button) findViewById(R.id.button_ToSetting);

        // initializing variable for bar chart.
        graphViewWater = findViewById(R.id.idGraphView);
        graphViewStep = findViewById(R.id.idGraphView2);

        buttonToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChartActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        myDatabaseHelper = MyDatabaseHelper.getInstance(this);

        Map<String, Integer> dailyWaterConsumptionMap = myDatabaseHelper.getDailyWaterConsumption();


        Map<String, Integer> stepMap = myDatabaseHelper.getDailyStepCount();

        // Create an array of DataPoint to hold the data
        DataPoint[] dataPoints = new DataPoint[dailyWaterConsumptionMap.size()];
        DataPoint[] dataPoints2 = new DataPoint[stepMap.size()];
        int index = 0;
        Map<String, Integer> treeMap2 = new TreeMap<String, Integer>(stepMap);
        for (Map.Entry<String, Integer> entry : treeMap2.entrySet()) {
            String time = entry.getKey();
            time=time.substring(0,2);

            int dateInt=Integer.parseInt(time);
            //if(dateInt==15){
            // dateInt=13;
            //}
            int totalStep = entry.getValue();
            dataPoints2[index] = new DataPoint(dateInt, totalStep);
            index++;
        }

        index = 0;
        Map<String, Integer> treeMap = new TreeMap<String, Integer>(dailyWaterConsumptionMap);
        for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
            String time = entry.getKey();
            time=time.substring(0,2);

            int dateInt=Integer.parseInt(time);
            //if(dateInt==15){
            // dateInt=13;
            //}
            int totalWaterConsumed = entry.getValue();
            dataPoints[index] = new DataPoint(dateInt, totalWaterConsumed);
            index++;
        }

        // Create a LineGraphSeries using the DataPoint array
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        LineGraphSeries<DataPoint> seriesStep = new LineGraphSeries<>(dataPoints2);
        // Set the title of the graph
        graphViewWater.setTitle("Daily Water Consumption");

        // Add the LineGraphSeries to the GraphView
        graphViewWater.addSeries(series);

        // on below line we are setting
        // text color to our graph view.
        graphViewWater.setTitleColor(com.google.android.material.R.color.design_default_color_error);

        // on below line we are setting
        // our title text size.
        graphViewWater.setTitleTextSize(14);

        // on below line we are adding
        // data series to our graph view.
        graphViewWater.addSeries(series);
        graphViewWater.getViewport().setXAxisBoundsManual(true);
        graphViewWater.getViewport().setMinX(0);  // starting hour
        graphViewWater.getViewport().setMaxX(24); // end hour


        graphViewStep.setTitle("Daily Step Count");

        // Add the LineGraphSeries to the GraphView
        graphViewStep.addSeries(seriesStep);

        // on below line we are setting
        // text color to our graph view.
        graphViewStep.setTitleColor(com.google.android.material.R.color.design_default_color_error);

        // on below line we are setting
        // our title text size.
        graphViewStep.setTitleTextSize(14);

        // on below line we are adding
        // data series to our graph view.
        graphViewStep.addSeries(seriesStep);

        graphViewStep.getViewport().setXAxisBoundsManual(true);
        graphViewStep.getViewport().setMinX(0);  // starting hour
        graphViewStep.getViewport().setMaxX(24); // end hour



        barChart = findViewById(R.id.idBarChart);

        // creating a new bar data set.

        waterIntakeLastWeek = new BarDataSet(getBarEntries(myDatabaseHelper.getLastWeekWaterConsumption()), "Last Week");
        waterIntakeLastWeek.setColor(getApplicationContext().getResources().getColor(com.google.android.material.R.color.cardview_dark_background));
        waterIntakeThisWeek = new BarDataSet(getBarEntries(myDatabaseHelper.getWeeklyWaterConsumption()), "This Week");
        waterIntakeThisWeek.setColor(Color.BLUE);

        // below line is to add bar data set to our bar data.
        BarData data = new BarData(waterIntakeLastWeek, waterIntakeThisWeek);

        // after adding data to our bar data we
        // are setting that data to our bar chart.
        barChart.setData(data);

        // below line is to remove description
        // label of our bar chart.
        barChart.getDescription().setEnabled(false);

        // below line is to get x axis
        // of our bar chart.
        XAxis xAxis = barChart.getXAxis();

        // below line is to set value formatter to our x-axis and
        // we are adding our days to our x axis.
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        // below line is to set center axis
        // labels to our bar chart.
        xAxis.setCenterAxisLabels(true);

        // below line is to set position
        // to our x-axis to bottom.
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // below line is to set granularity
        // to our x axis labels.
        xAxis.setGranularity(1);

        // below line is to enable
        // granularity to our x axis.
        xAxis.setGranularityEnabled(true);

        // below line is to make our
        // bar chart as draggable.
        barChart.setDragEnabled(true);

        // below line is to make visible
        // range for our bar chart.
        barChart.setVisibleXRangeMaximum(3);

        // below line is to add bar
        // space to our chart.
        float barSpace = 0.1f;

        // below line is use to add group
        // spacing to our bar chart.
        float groupSpace = 0.5f;

        // we are setting width of
        // bar in below line.
        data.setBarWidth(0.15f);

        // below line is to set minimum
        // axis to our chart.
        barChart.getXAxis().setAxisMinimum(0);

        // below line is to
        // animate our chart.
        barChart.animate();

        // below line is to group bars
        // and add spacing to it.
        barChart.groupBars(0, groupSpace, barSpace);

        // below line is to invalidate
        // our bar chart.
        barChart.invalidate();


        ///////////////////////////////////////////////////////////////////////////////////////////
        // BAR CHARTS FOR STEPS
        ///////////////////////////////////////////////////////////////////////////////////////////
        stepsBarChart = findViewById(R.id.idBarChartStep);
        stepCountLastWeek = new BarDataSet(getBarEntries(myDatabaseHelper.getLastWeekStepCount()), "Last Week");
        stepCountLastWeek.setColor(getApplicationContext().getResources().getColor(com.google.android.material.R.color.cardview_dark_background));
        stepCountThisWeek = new BarDataSet(getBarEntries(myDatabaseHelper.getWeeklyStepCount()), "This Week");
        stepCountThisWeek.setColor(Color.BLUE);
        BarData stepsData = new BarData(stepCountLastWeek, stepCountThisWeek);
        stepsBarChart.setData(stepsData);
        stepsBarChart.getDescription().setEnabled(false);
        XAxis stepsXAxis = stepsBarChart.getXAxis();
        stepsXAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        stepsXAxis.setCenterAxisLabels(true);
        stepsXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        stepsXAxis.setGranularity(1);

        // below line is to enable
        // granularity to our x axis.
        stepsXAxis.setGranularityEnabled(true);

        // below line is to make our
        // bar chart as draggable.
        stepsBarChart.setDragEnabled(true);

        // below line is to make visible
        // range for our bar chart.
        stepsBarChart.setVisibleXRangeMaximum(3);



        // we are setting width of
        // bar in below line.
        stepsData.setBarWidth(0.15f);

        // below line is to set minimum
        // axis to our chart.
        stepsBarChart.getXAxis().setAxisMinimum(0);

        // below line is to
        // animate our chart.
        stepsBarChart.animate();

        // below line is to group bars
        // and add spacing to it.
        stepsBarChart.groupBars(0, groupSpace, barSpace);

        // below line is to invalidate
        // our bar chart.
        stepsBarChart.invalidate();
    }

    // array list for first set
    private ArrayList<BarEntry> getBarEntries(List<WeeklyEntry> weeklyConsumption) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        // Iterate over weeklyConsumption and convert to BarEntry
        for (WeeklyEntry entry : weeklyConsumption) {
            String dayString = entry.getDate();
            int consumption = entry.getValue();

            // Convert the day string to a float for the x-axis
            Float dayIndex = Float.valueOf(dayString);

            barEntries.add(new BarEntry(dayIndex, consumption));
        }

        // Sort the bar entries by the day index to ensure they're in order


        return barEntries;
    }




    // array list for second set.

}