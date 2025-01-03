package org.fanControl;



import javax.swing.*; //arayüzü yaptığımız kütüphane 
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;


public class App {

    public static JLabel therm = new JLabel("ESP offline...");// espyi takmadan önceki yazı 
    public static TempChart thermChart = new TempChart(); // grafik 
    public static JButton acButton;
    public static JButton kapatButton;
    public static JSlider slider;
    public static JCheckBox autoModCheckBox;
    public static ActionListener checkBoxListener;
    public static MQTTClient mqttThread;
 

    public static void main(String[] args){

        // tema ayarlama
        FlatDarkLaf.setup();
        FlatAnimatedLafChange.showSnapshot(); // butonların renklerini vs değiştiriyor, sliderin şeklini değiştiriyor

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Tema yuklenemedi " + e.getMessage()); // eğer eklenemezse hata veriyor 
        }

        // pencere ayarları
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // pencere kapatıldığında programı tamamen kapat, bu koyulmazsa arka planda program çalışmaya devam ediyor
        frame.setSize(500, 600); // pencerenin boyutu
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // ızgara hücrelerine eklenecek panellerin ayarları her biri için ayrı fonksiyon tanımlandı
        JPanel topPanel = createTopPanel();
        JPanel midPanel = createMidPanel();
        JPanel bottomPanel = createBottomPanel();

        // panelleri pencereye ekliyoruz
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.1;
        frame.add(topPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.7;
        frame.add(midPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.2;
        frame.add(bottomPanel, gbc); // pencereyi görünür yapıyor
        changeInteractionStatus(false,false,false,true);
        frame.setVisible(true);
        // mqtt "esp/therm" kanalından sıcaklık okuması alınıp grafiğe ve anlık sıcaklık göstergesine yükleyen fonksiyon
        mqttThread = new MQTTClient(); // thread'in açılma sebebi aynı anda mqtt üzerinden gelen sıcaklık verisinin okunması gerek hem de pencereyi açıyor iki işi aynı anda yapması için thread kullanılıyor 
        mqttThread.start();
    }
    public static void changeInteractionStatus(boolean enableAcButton, boolean enableKapatButton, boolean enableAutoBox, boolean disableAll){

        acButton.setEnabled(!disableAll);
        kapatButton.setEnabled(!disableAll);
        slider.setEnabled(!disableAll);
        autoModCheckBox.setEnabled(!disableAll);
        
        acButton.setBackground(Color.decode("#c9b5b5"));
        kapatButton.setBackground(Color.decode("#c9b5b5"));

       if(enableAcButton){
            acButton.setBackground(Color.decode("#c9b5b5"));
            kapatButton.setBackground(Color.decode("#fc4f25"));
            kapatButton.setEnabled(true);
            acButton.setEnabled(false);
       }
       if (enableKapatButton) {
            
            acButton.setBackground(Color.decode("#42aa86"));
            kapatButton.setBackground(Color.decode("#c9b5b5"));
            kapatButton.setEnabled(false);
            acButton.setEnabled(true);

       }
       if(enableAutoBox){
        autoModCheckBox.setEnabled(true);
       }

    }
   
    private static JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(1, 2)); 
    
        JPanel topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new GridBagLayout()); 
    
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH;
        
    
        slider = new JSlider(0, 255, 127);
        JLabel speedLabel = new JLabel("Hız");
        JLabel autoModLabel = new JLabel("Otomatik mod");
        autoModCheckBox = new JCheckBox();

        
    
        speedLabel.setFont(new Font("Arial", Font.BOLD, 16)); 
        autoModLabel.setFont(new Font("Arial", Font.BOLD, 16)); 
    
        slider.addChangeListener(e -> { 
            System.out.println("Fan hızı ayarlandı"); 
            int pwmDuty = slider.getValue();
            MQTTClient.sendMqttPackage(mqttThread.client,"esp/speed",pwmDuty);
        });
        
        autoModCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        checkBoxListener = e -> {
            System.out.println("Auto mod değişti"); 
            boolean statusBool = autoModCheckBox.isSelected();
            
            if (statusBool) {
                changeInteractionStatus(false, false, true, true);
            }else{
                changeInteractionStatus(false, false, true, false);
            }

            
            MQTTClient.sendMqttPackage(mqttThread.client, "esp/auto", statusBool ? 1 : 0);

            
        };
        autoModCheckBox.addActionListener(checkBoxListener);
    
        JButton settingsButton = new JButton("Ayarlar");

        settingsButton.addActionListener(e -> {
            
            settingsFrame settings = new settingsFrame();
            settingsButton.setEnabled(false);

            settings.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    settingsButton.setEnabled(true);
                    settings.dispose();
                }
            });
        });
    
        gbcTop.weighty = 0.4;
        gbcTop.weightx = 0.1;
        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        topLeftPanel.add(speedLabel, gbcTop);
    
        gbcTop.weightx = 2;
        gbcTop.gridx = 1;
        gbcTop.gridy = 0;
        topLeftPanel.add(slider, gbcTop);
    
        gbcTop.weightx = 0.2;
        gbcTop.gridx = 0;
        gbcTop.gridy = 1;
        topLeftPanel.add(autoModLabel, gbcTop);
    
        gbcTop.weightx = 0.2;
        gbcTop.gridx = 1;
        gbcTop.gridy = 1;
        topLeftPanel.add(autoModCheckBox, gbcTop);
    
        gbcTop.weightx = 1.0;
        gbcTop.weighty = 0.2;
        gbcTop.gridx = 0;
        gbcTop.gridy = 2;
        gbcTop.gridwidth = 2;
        topLeftPanel.add(settingsButton, gbcTop);
    
        JPanel topRightPanel = new JPanel(new GridLayout(1, 1)); 
      
        therm.setFont(new Font("Arial", Font.BOLD, 24)); 
        therm.setHorizontalAlignment(SwingConstants.CENTER); 
        therm.setForeground(Color.WHITE);
        topRightPanel.add(therm); 
    
        topPanel.add(topLeftPanel); 
        topPanel.add(topRightPanel);
        
        return topPanel;
    }
    
    private static JPanel createMidPanel() { // midPaneli oluşturan fonksiyon 
        JPanel midPanel = new JPanel(new BorderLayout()); // midpanelin objesi oluşturulur 
        midPanel.add(thermChart.chartPanel, BorderLayout.CENTER); // grafiği orta panele ekleyip ortalıyor 
        return midPanel;
    }

    private static JPanel createBottomPanel() { // bottomPaneli oluşturan fonksiyon 
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2)); // bottomPanel'e 1 satır, 2 sütun ekliyor 
        
        // esp'nin kodu mqtt'nin esp/signal kanalından gelen 1 ve 0 a göre fan açıp kapatılıyor
        acButton = createButton("Fanı aç", "#42aa86", 1); 
        kapatButton = createButton("Fanı kapat", "#fc4f25", 0);
        
        // aç kapat butonlarını bottomPanele ekliyor 
        bottomPanel.add(acButton); 
        bottomPanel.add(kapatButton);

        return bottomPanel; 
    }

    private static JButton createButton(String text, String color, int signal) { 
        JButton button = new JButton(text); // textin yazılacağı buton objesi oluşturuluyor 
        button.setBackground(Color.decode(color)); // butonun arka plan rengi 
        button.setForeground(Color.WHITE); // butonun üstündeki yazının rengi
        button.setFocusPainted(false); // üstüne tıklandığında rengi değişmemesi için
        button.setBorderPainted(false); // çerçevesi olmaması için
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // oluşturulan butonun temadan etkilenmemesi için 

        button.addActionListener(e -> { // butona tıklandığında yapılacak şeyler
            
            MQTTClient.sendMqttPackage(mqttThread.client,"esp/signal",signal);

            switch (signal) {
                case 1:
                    changeInteractionStatus(false,true,false,false);
                    break;
                case 0:
                    changeInteractionStatus(true,false,false,false);
                default:
                    break;
            }
        });

        return button; // oluşturulan buton döndürülür
    }
}
