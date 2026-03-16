import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties; // 追加
import java.io.*; // 追加

public class CalendarApp {
    private static Point mouseDownCompCoords = null;
    private static Set<String> holidays = new HashSet<>();
    private static final String CONFIG_FILE = "calendar_config.properties"; // 保存ファイル名

    public static void main(String[] args) {
        // --- 祝日データ登録（省略なし） ---
        holidays.add("2026-01-01"); holidays.add("2026-01-12"); holidays.add("2026-02-11");
        holidays.add("2026-02-23"); holidays.add("2026-03-20"); holidays.add("2026-04-29");
        holidays.add("2026-05-03"); holidays.add("2026-05-04"); holidays.add("2026-05-05");
        holidays.add("2026-05-06"); holidays.add("2026-07-20"); holidays.add("2026-08-11");
        holidays.add("2026-09-21"); holidays.add("2026-09-22"); holidays.add("2026-10-12");
        holidays.add("2026-11-03"); holidays.add("2026-11-23");

        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setType(Window.Type.UTILITY);
        frame.setFocusableWindowState(false);

        String mainFont = "Yu Gothic UI Light"; 

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                float opacity = 0.85f; 
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

                LocalDate today = LocalDate.now();
                LocalTime nowTime = LocalTime.now();
                YearMonth yearMonth = YearMonth.from(today);

                // 時刻
                g2d.setFont(new Font(mainFont, Font.PLAIN, 18));
                g2d.setColor(Color.WHITE);
                String timeText = nowTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                g2d.drawString(timeText, 310, 40);

                // 年月
                g2d.setFont(new Font(mainFont, Font.BOLD, 32));
                g2d.drawString(today.getYear() + " / " + String.format("%02d", today.getMonthValue()), 45, 40);

                // 曜日
                String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                g2d.setFont(new Font(mainFont, Font.PLAIN, 14));
                for (int i = 0; i < days.length; i++) {
                    if (i == 0) g2d.setColor(new Color(255, 120, 120));
                    else if (i == 6) g2d.setColor(new Color(120, 180, 255));
                    else g2d.setColor(Color.WHITE);
                    g2d.drawString(days[i], 50 + i * 50, 80);
                }

                // 日付
                int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();
                if (firstDayOfWeek == 7) firstDayOfWeek = 0;

                int xCount = firstDayOfWeek;
                int yCount = 0;
                g2d.setFont(new Font(mainFont, Font.PLAIN, 22));
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate currentDoc = yearMonth.atDay(day);
                    String dateKey = currentDoc.toString();

                    if (day == today.getDayOfMonth()) {
                        g2d.setColor(new Color(255, 230, 100)); 
                        g2d.fillOval(42 + xCount * 50, 95 + yCount * 40, 35, 35); 
                        g2d.setColor(Color.BLACK); 
                    } else if (xCount == 0 || holidays.contains(dateKey)) { 
                        g2d.setColor(new Color(255, 120, 120));
                    } else if (xCount == 6) {
                        g2d.setColor(new Color(120, 180, 255));
                    } else {
                        g2d.setColor(Color.WHITE);
                    }

                    String dayStr = String.valueOf(day);
                    int offset = dayStr.length() > 1 ? 48 : 55;
                    g2d.drawString(dayStr, offset + xCount * 50, 122 + yCount * 40);
                    
                    xCount++;
                    if (xCount > 6) { xCount = 0; yCount++; }
                }
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(500, 450));

        // マウス操作と保存の連動
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseDownCompCoords = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { 
                mouseDownCompCoords = null; 
                saveLocation(frame.getLocation()); // ★手を離した時に保存
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });

        frame.add(panel);
        frame.pack();
        
        // --- ★起動時に場所を読み込む ---
        Point savedPoint = loadLocation();
        if (savedPoint != null) {
            frame.setLocation(savedPoint);
        } else {
            // 初回やエラー時は右下に表示
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(screenSize.width - 520, screenSize.height - 520);
        }

        frame.setVisible(true);

        Timer timer = new Timer(1000, e -> panel.repaint());
        timer.start();
    }

    // 場所を保存するメソッド
    private static void saveLocation(Point p) {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("x", String.valueOf(p.x));
            prop.setProperty("y", String.valueOf(p.y));
            prop.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 場所を読み込むメソッド
    private static Point loadLocation() {
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(in);
            int x = Integer.parseInt(prop.getProperty("x"));
            int y = Integer.parseInt(prop.getProperty("y"));
            return new Point(x, y);
        } catch (Exception e) {
            return null; // ファイルがない時などはnullを返す
        }
    }
}