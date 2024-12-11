package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class chart {
    
    private TimeSeries series = new TimeSeries("Sıcaklık durumu");
    TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        // Çizgi grafiği oluştur
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Gerçek Zamanlı Grafik", // Başlık
                "Zaman",                // X ekseni etiketi
                "Değer",                // Y ekseni etiketi
                dataset,                // Veri seti
                false,                  // Legend
                true,                   // Tooltips
                false                   // URLs
    );
    
    public ChartPanel chartPanel = new ChartPanel(chart);

    public void updateData(int value) {
        series.addOrUpdate(new Second(), value);
    }



}

