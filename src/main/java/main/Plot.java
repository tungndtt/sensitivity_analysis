package main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.List;


public class Plot extends ApplicationFrame {

    /**
     * Construct Plot-instance to plot the line chart
     * @param title title of the plot application
     * @param meaning meaning of drawn lines
     * @param legendX meaning of X
     * @param legendY meaning of Y
     * @param points list of points to plot
     */
    public Plot(String title, String meaning, String legendX, String legendY, List<double[]> points) {

        super(title);
        final XYSeries series = new XYSeries(meaning);
        for(double[] point : points) {
            series.add(point[0], point[1]);
        }
        XYSeriesCollection data = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                legendX,
                legendY,
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
        setContentPane(chartPanel);
    }

    /**
     * Display the plotting chart
     */
    public void display() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}
