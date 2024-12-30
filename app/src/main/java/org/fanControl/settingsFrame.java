package org.fanControl;

import java.io.*;
import javax.swing.*; //arayüzü yaptığımız kütüphane 
import java.awt.*;

public class settingsFrame extends JFrame{
        
        JTextField minTempField         = new JTextField();
        JTextField autoTimeStartClock   = new JTextField();
        JTextField autoTimeStopClock    = new JTextField();
        JButton submitButton            = new JButton("Zaman emiri yolla");

        JLabel minTempFieldLabel        = new JLabel("Minimum sıcaklık");
        JLabel autoTimeStartClockLabel  = new JLabel("Başlangıç saati");
        JLabel autoTimeStopClockLabel   = new JLabel("Bitiş saati");



        public settingsFrame(){

            this.setLayout(new GridBagLayout());
            this.setSize(300, 200);
            minTempField.setText("" + org.fanControl.readTherm.minTemp);
            submitButton.setHorizontalAlignment(SwingConstants.CENTER);
            submitButton.addActionListener(e->{

                org.fanControl.App.sendMqttPackage(org.fanControl.App.client, "esp/timing", minTempField.getText() + ";" + autoTimeStartClock.getText() + ";" + autoTimeStopClock.getText() + ";");
                JOptionPane.showMessageDialog(null, "Fan, " + autoTimeStartClock.getText() + "-" + autoTimeStopClock.getText() + " Saatleri arasında minimum " + minTempField.getText() + "C° sıcaklında çalışacak şekilde ayarlandı");
            });
        

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 0;

            this.add(minTempFieldLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 0;

            this.add(minTempField, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 1;

            this.add(autoTimeStartClockLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 1;

            this.add(autoTimeStartClock, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 0;
            gbc.gridy = 2;

            this.add(autoTimeStopClockLabel,gbc);

            gbc.weightx = 0.7;
            gbc.gridx = 1;
            gbc.gridy = 2;

            this.add(autoTimeStopClock, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;

        
            this.add(submitButton, gbc);

            this.setVisible(true);


        }
}
