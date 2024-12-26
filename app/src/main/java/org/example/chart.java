package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

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

    public void updateData(int value) { // yeni değerleri grafiğe ekler 
        series.addOrUpdate(new Second(), value); 
    }
}

