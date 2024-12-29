package org.fanControl;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.formdev.flatlaf.FlatDarkLaf;


import javax.swing.*;
import java.awt.*;

public class chart {
    
    private TimeSeries series = new TimeSeries("Sıcaklık durumu"); 
    TimeSeriesCollection dataset = new TimeSeriesCollection(series); // verileri timeseries diye bir şeyin içine ekliyoruz kütüphane istiyor

        // Çizgi grafiği oluştur
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Sıcaklık", // Başlık
                "Zaman", // X ekseni etiketi
                "Sıcaklık", // Y ekseni etiketi
                dataset, // Veri seti
                false, // Legend
                true, // Tooltips
                false // URLs
    );
    
    public ChartPanel chartPanel = new ChartPanel(chart);
    
    public chart(){
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Cannot load the theme for chart: " + e.getMessage()); // eğer eklenemezse hata veriyor 
        }
        
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
        System.out.println(UIManager.getColor("Panel.background"));
        
       
        chart.getTitle().setPaint(UIManager.getColor("Label.foreground"));

        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;

            xyPlot.setBackgroundPaint(UIManager.getColor("Panel.background"));
            xyPlot.setDomainGridlinePaint(UIManager.getColor("Label.foreground"));
            xyPlot.setRangeGridlinePaint(UIManager.getColor("Label.foreground"));

            xyPlot.getDomainAxis().setLabelPaint(UIManager.getColor("Label.foreground"));
            xyPlot.getDomainAxis().setTickLabelPaint(UIManager.getColor("Label.foreground"));
            xyPlot.getRangeAxis().setLabelPaint(UIManager.getColor("Label.foreground"));
            xyPlot.getRangeAxis().setTickLabelPaint(UIManager.getColor("Label.foreground"));
        }


    }
    public void updateData(int value) { // yeni değerleri grafiğe ekler 
        series.addOrUpdate(new Second(), value); 
    }
}

