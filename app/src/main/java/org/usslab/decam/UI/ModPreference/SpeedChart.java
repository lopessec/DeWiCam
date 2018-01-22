package org.usslab.decam.UI.ModPreference;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by pip on 2017/5/15.
 */

public class SpeedChart extends LineChartView {
    public static final int MAX_COUNTS = 20;
    protected double dataPassedIndex = 0;
    private boolean hasInited = false;

    private List<PointValue> originalDataSets = new ArrayList<>();
    private Line line = new Line(originalDataSets).setCubic(true)
            .setFilled(true).setHasPoints(false).setColor(Color.DKGRAY).setStrokeWidth(1);
    private Axis axisX = new Axis().setHasLines(true).setInside(true);
    private Axis axisY = new Axis().setHasLines(true).setInside(true);

    private List<Line> lines = new ArrayList<>();
    private LineChartData data = new LineChartData(lines);


    public SpeedChart(Context context) {
        super(context);
        init();
    }

    public SpeedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (hasInited)
            return;
        lines.clear();
        lines.add(line);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
    }

    public void reset() {
        this.originalDataSets.clear();
        dataPassedIndex = 0;
    }

    public void addPointDelta(double y, double delta) {

        addPoint(dataPassedIndex++, y);
        //dataPassedIndex+=delta;
    }

    public void addPoint(double x, double y) {
        PointValue value = new PointValue((float) x, (float) y);
        originalDataSets.add(value);
        if (originalDataSets.size() > MAX_COUNTS) {
            //limit chart length;
            originalDataSets.remove(0);
        }
    }

    public void updateView() {
        setLineChartData(data);
    }

}
