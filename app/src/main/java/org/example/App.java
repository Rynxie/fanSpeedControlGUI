package org.example;

import java.io.*;
import javax.swing.*; //arayüzü yaptığımız kütüphane 
import java.awt.*;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import org.eclipse.paho.client.mqttv3.*;

public class App {

   
    public static JLabel therm = new JLabel("Sıcaklık Buraya gelecek"); // espyi takmadan önceki yazı 
    public static chart thermChart = new chart(); // grafik 

    public static void main(String[] args) throws MqttException {

        // tema ayarlama
        FlatDarkLaf.setup();
        FlatAnimatedLafChange.showSnapshot(); // butonların renklerini vs değiştiriyor, sliderin şeklini değiştiriyor

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Cannot load the theme: " + e.getMessage()); // eğer eklenemezse hata veriyor 
        }

        // mqtt sunucusuna bağlantı ayarı
        MqttClient client = new MqttClient("tcp://tuna.sh:1884", "JavaApp");
        client.connect();
        System.out.println("Connected to server");

        // pencere ayarları
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // pencere kapatıldığında programı tamamen kapat, bu koyulmazsa arka planda program çalışmaya devam ediyor
        frame.setSize(500, 600); // pencerenin boyutu
        frame.setLayout(new GridLayout(3, 1));
        // frame.setUndecorated(true); // windowsun açma kapama tuuşları vs görünmesin diye 

        // ızgara hücrelerine eklenecek panellerin ayarları her biri için aytı fonksiyon tanımalndı
        JPanel topPanel = createTopPanel(client); // top panelin nereye içindilerin nereye geleceğini ayarlayan fonksiyon createTopPanel, client mqtt 
        JPanel midPanel = createMidPanel(); // grafiği alıp midPanel'in içine koyuyor 
        JPanel bottomPanel = createBottomPanel(client); // 

        // paneller pencereye ekleniyor
        frame.add(topPanel);
        frame.add(midPanel);
        frame.add(bottomPanel);

        frame.setVisible(true); // pencereyi görünür yapıyor

        // mqtt "esp/therm" kanalından sıcaklık okuması alınıp grafiğe ve anlık sıcaklık göstergesine yükleyen fonksiyon
        readTherm thread = new readTherm(); // thread'in açılma sebebi aynı anda mqtt üzerinden gelen sıcaklık verisinin okunması gerek hem de pencereyi açıyor iki işi aynı anda yapması için thread kullanılıyor 
        thread.start();
    }

    private static JPanel createTopPanel(MqttClient client) {
        JPanel topPanel = new JPanel(new GridLayout(1, 2)); // topPanel'i iki tane sütuna bölme 
        
        // Sol kısım fan hızı ayarlama
        JPanel topLeftPanel = new JPanel();
        topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS)); // topPanel'deki birinci sütun 

        JSlider slider = new JSlider(0, 255, 127); // topPanel'deki birinci sütunun ayarları 
        JLabel speedLabel = new JLabel("Max Hız");
        JButton setSpeedButton = new JButton("Onayla");

        speedLabel.setFont(new Font("Arial", Font.BOLD, 24)); 
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER); // yatayda ortalıyor, yazının nereye yaslı olduğu 
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // x düzleminde ortalıyor,

        slider.setAlignmentX(Component.CENTER_ALIGNMENT); // slider'ın x e göre ortalanmasını sağlıyor

        setSpeedButton.setAlignmentX(Component.CENTER_ALIGNMENT); // buton'un x e göre ortalanmasını sağlıyor
        setSpeedButton.addActionListener(e -> { // butona tıklandığında yapılacak şeyler 
            System.out.println("Fan hızı ayarlandı"); 
            int pwmDuty = slider.getValue(); // slider'dan 0 ile 250 arasında değer geliyor bu değer mqtt ile yollanıyor
            String payload = "" + pwmDuty; // interger'ı string yapmak için mqtt ile sadece string yollabildiği için sunucudan ne değer geldiğine bakılabiliyor  
            MqttMessage message = new MqttMessage(payload.getBytes()); // string'i byte'a çeviriyor mesaj objesi oluşturuyor, payload stringe çevirdiğimiz hali
            message.setQos(1); // bunun bilinmesi lazım 
            try { // mesaj gönderilmezse bilinmesi için try catch yapısı kullanıldı
                client.publish("esp/speed", message); // mesajı gönderen fonksiyon
            } catch (MqttException error) { 
                System.out.println("Sinyal gönderilemedi");
            } finally { // hata vermezse 
                System.out.println("Sinyal gönderildi");
            }
        });

        // oluşturulan yazı,slider,butonub panele eklenmesi yani LEFT SIDE 
        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 50))); // Box.createRigidArea yukarda 50 piksel boşluk ekliyor
        topLeftPanel.add(speedLabel); // maks hız yazısını ekliyor
        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 20))); //
        topLeftPanel.add(slider); // slider'ı ekliyor
        topLeftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topLeftPanel.add(setSpeedButton); // onayla butonunu ekliyor

        // Sağ kısım sıcaklık yazısının görünmesi 
        JPanel topRightPanel = new JPanel(new GridLayout(1, 1)); // bir satır bir sütun içinde olsun 
        therm.setFont(new Font("Arial", Font.BOLD, 24)); 
        therm.setHorizontalAlignment(SwingConstants.CENTER); // yazıyı ortalama 
        topRightPanel.add(therm); // yazıyı panele ekleme 
        
        // birinci satırın sağ ve sol panelinin eklenmesi 
        topPanel.add(topLeftPanel); 
        topPanel.add(topRightPanel);
        
        return topPanel; // oluşturulan top panel döndürülür
    }

    private static JPanel createMidPanel() { // midPaneli oluşturan fonksiyon 
        JPanel midPanel = new JPanel(new BorderLayout()); // midpanelin objesi oluşturulur 
        midPanel.add(thermChart.chartPanel, BorderLayout.CENTER); // grafiği orta panele ekleyip ortalıyor 
        return midPanel;
    }

    private static JPanel createBottomPanel(MqttClient client) { // bottomPaneli oluşturan fonksiyon 
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2)); // bottomPanel'e 1 satır, 2 sütun ekliyor 
        
        // esp'nin kodu mqtt'nin esp/signal kanalından gelen 1 ve 0 a göre fan açıp kapatılıyor
        JButton acButton = createButton("Fanı aç", "#42aa86", "1", client); 
        JButton kapatButton = createButton("Fanı kapat", "#fc4f25", "0", client);
        
        // aç kapat butonlarını bottomPanele ekliyor 
        bottomPanel.add(acButton); 
        bottomPanel.add(kapatButton);

        return bottomPanel; 
    }

    private static JButton createButton(String text, String color, String signal, MqttClient client) { 
        JButton button = new JButton(text); // textin yazılacağı buton objesi oluşturuluyor 
        button.setBackground(Color.decode(color)); // butonun arka plan rengi 
        button.setForeground(Color.WHITE); // butonun üstündeki yazının rengi
        button.setFocusPainted(false); // üstüne tıklandığında rengi değişmemesi için
        button.setBorderPainted(false); // çerçevesi olmaması için
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // oluşturulan butonun temadan etkilenmemesi için 

        button.addActionListener(e -> { // butona tıklandığında yapılacak şeyler
            System.out.println(text + " sinyali gönderiliyor..."); // "fan aç/kapat sinyali gönderiliyor..."
            MqttMessage message = new MqttMessage(signal.getBytes()); // 1 ve 0'ı byte'a çeviriyor
            message.setQos(1);
            try {
                client.publish("esp/signal", message); // mesajı gönderen fonksiyon 
            } catch (MqttException error) {
                System.out.println("Sinyal gönderilemedi");
            } finally { // hata vermezse bunu çalıştırıyor
                System.out.println("Sinyal gönderildi"); 
            }
        });

        return button; // oluşturulan buton döndürülür
    }
}
