import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PROG209 - Event Driven Programming
 * Basic Interactive GUI - Individual Assignment 1
 * Author : Abdulai Bah
 * Theme  : "Sunrise Studio" - an animated, mood-changing welcome window.
 *
 * KEY IDEAS (explained in simple words):
 *  1. INHERITANCE : SwingAssignment "extends JFrame", so our class IS a window
 *                   and inherits everything a JFrame can do.
 *  2. CONSTRUCTOR : the SwingAssignment() constructor builds the whole window.
 *  3. EVENTS      : the button (ActionListener), a menu (ActionListener),
 *                   and two Timers (ActionListener) are all "events".
 */
public class SwingAssignment extends JFrame {

    private static final long serialVersionUID = 1L;

    // ---- The three components the assignment asks for ----------------------
    private JFrame frame;   // the window (it refers to this very object)
    private JPanel panel;   // the container that holds everything
    private JLabel label;   // the welcome text

    // ---- Extra components (the "Addition" and the "more") ------------------
    private JButton button;       // changes the label text and panel colour
    private JLabel statusLabel;   // shows mood, click count and a live clock
    private ImageIcon appIcon;    // my own application icon

    // ---- A "Mood" = one text message + one colour theme --------------------
    private static class Mood {
        final String name, message;
        final Color top, bottom, text, accent;
        Mood(String name, String message, Color top, Color bottom, Color text, Color accent) {
            this.name = name; this.message = message;
            this.top = top; this.bottom = bottom; this.text = text; this.accent = accent;
        }
    }

    private final Mood[] moods = {
        new Mood("Sunrise",
                "Welcome to Java Swing First Individual Assignment by Abdulai Bah",
                new Color(0xFFE9B8), new Color(0xFFB48A), new Color(0x3B1F0B), new Color(0xE8590C)),
        new Mood("Ocean",
                "Smooth waves, smooth code: this text changed by an ActionListener!",
                new Color(0xD6F1FF), new Color(0x7CC8E8), new Color(0x08324A), new Color(0x0B7BA7)),
        new Mood("Forest",
                "Event driven programming: the program reacts when you act.",
                new Color(0xE0F5DC), new Color(0x8FD19E), new Color(0x0F3D1E), new Color(0x2E8B57)),
        new Mood("Twilight",
                "Built with Java Swing by Abdulai Bah. Thank you for visiting!",
                new Color(0xE8DDFB), new Color(0xF5A9C8), new Color(0x2A1250), new Color(0x7B3FE4))
    };

    // ---- Animation state ---------------------------------------------------
    private int current = 0;          // which mood is showing
    private int clicks = 0;           // how many times the button was clicked
    private double phase = 0;         // moves the waves and the sun
    private double progress = 1.0;    // 0..1 smooth colour change between moods
    private Mood from = moods[0];     // mood we are changing from
    private Mood to = moods[0];       // mood we are changing to
    private Color curTop = moods[0].top, curBottom = moods[0].bottom;
    private Color curText = moods[0].text, curAccent = moods[0].accent;

    // =======================================================================
    //  CONSTRUCTOR : builds the window
    // =======================================================================
    public SwingAssignment() {
        super("Java Swing Assignment");          // call the JFrame (parent) constructor
        frame = this;                            // "frame" is this JFrame
        appIcon = loadIcon("icon.png");          // my own icon (ImageIcon)

        // ---- JPanel: paints a soft gradient sky, a sun and animated waves ----
        panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintScenery((Graphics2D) g.create(), getWidth(), getHeight());
            }
        };
        panel.setBackground(curBottom);

        // ---- JLabel: the required welcome text, with my icon above it --------
        label = new JLabel(moods[0].message, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 20));
        label.setForeground(curText);
        label.setIcon(appIcon);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setIconTextGap(18);

        // ---- JButton: the "Addition" (changes label text + panel colour) -----
        button = new JButton("Change Text and Color") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = curAccent;
                if (getModel().isPressed())       c = c.darker();
                else if (getModel().isRollover()) c = brighter(c, 0.18f);
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                g2.dispose();
                super.paintComponent(g);          // draws the button text
            }
        };
        button.setFont(new Font("Tahoma", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMnemonic(KeyEvent.VK_C);        // Alt+C also works
        button.setToolTipText("Click, press Enter, or press Alt+C");
        button.addActionListener(e -> nextMood()); // EVENT: runs on every click

        // ---- Status line: mood, clicks and a live clock ------------------------
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        statusLabel.setForeground(curText);
        updateStatus();

        // ---- Put the components inside the panel -------------------------------
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 20, 28, 20);
        gbc.gridy = 0; panel.add(label, gbc);               // label  -> panel
        gbc.gridy = 1; panel.add(button, gbc);              // button -> panel
        gbc.gridy = 2; gbc.insets = new Insets(0, 20, 0, 20);
        panel.add(statusLabel, gbc);                        // status -> panel

        // ---- Put the panel inside the frame ------------------------------------
        frame.add(panel);                                   // panel -> frame

        // ---- Menu bar (File / Help) ---------------------------------------------
        frame.setJMenuBar(buildMenuBar());

        // ---- Frame settings required by the assignment -------------------------
        frame.setSize(800, 800);                                 // 800 x 800
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // close button ends the app
        frame.setIconImage(appIcon.getImage());                  // window icon
        frame.getRootPane().setDefaultButton(button);            // Enter key = click
        frame.setLocationRelativeTo(null);                       // centre on screen

        // ---- Two Timers: animation (~30 fps) and live clock (every second) -----
        new Timer(33, e -> animate()).start();
        new Timer(1000, e -> updateStatus()).start();

        frame.setVisible(true);
    }

    // =======================================================================
    //  EVENT HANDLERS
    // =======================================================================

    /** Runs when the button is clicked: moves to the next mood. */
    private void nextMood() {
        clicks++;
        changeTo((current + 1) % moods.length);
    }

    private void changeTo(int index) {
        from = new Mood("now", "", curTop, curBottom, curText, curAccent);
        current = index;
        to = moods[index];
        progress = 0.0;                         // start the smooth colour change
        label.setText(to.message);              // change the LABEL text
        updateStatus();
    }

    /** Called ~30 times per second by a Timer. */
    private void animate() {
        phase += 0.06;
        if (progress < 1.0) {
            progress = Math.min(1.0, progress + 0.06);
            curTop = blend(from.top, to.top, progress);
            curBottom = blend(from.bottom, to.bottom, progress);
            curText = blend(from.text, to.text, progress);
            curAccent = blend(from.accent, to.accent, progress);
            panel.setBackground(curBottom);     // change the PANEL background colour
            label.setForeground(curText);
            statusLabel.setForeground(curText);
        }
        panel.repaint();
    }

    private void updateStatus() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE d MMM yyyy  |  HH:mm:ss"));
        statusLabel.setText("Mood: " + moods[current].name + " (" + (current + 1) + "/" + moods.length
                + ")   |   Clicks: " + clicks + "   |   " + time);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem reset = new JMenuItem("Reset to Sunrise");
        reset.addActionListener(e -> { clicks = 0; changeTo(0); });
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        file.add(reset);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Java Swing Assignment\nPROG209 Event Driven Programming\n\n"
                + "Created by Abdulai Bah\n"
                + "Press the button to change the label text and the panel colour.",
                "About", JOptionPane.INFORMATION_MESSAGE, appIcon));
        help.add(about);

        bar.add(file);
        bar.add(help);
        return bar;
    }

    // =======================================================================
    //  DRAWING : gradient sky, glowing sun and three animated waves
    // =======================================================================
    private void paintScenery(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // sky gradient
        g2.setPaint(new GradientPaint(0, 0, curTop, 0, h, curBottom));
        g2.fillRect(0, 0, w, h);

        // glowing sun that floats up and down gently
        double bob = Math.sin(phase * 0.7) * 8;
        double cx = w / 2.0, cy = h - 85 + bob;
        for (int i = 4; i >= 1; i--) {
            double r = 70 + i * 30;
            g2.setColor(withAlpha(curAccent, 22));
            g2.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        }
        g2.setColor(withAlpha(brighter(curAccent, 0.35f), 235));
        g2.fill(new Ellipse2D.Double(cx - 70, cy - 70, 140, 140));

        // three layers of waves
        for (int layer = 0; layer < 3; layer++) {
            Path2D wave = new Path2D.Double();
            wave.moveTo(0, h);
            for (int x = 0; x <= w; x += 8) {
                double y = h - 95 + layer * 30
                        + Math.sin(x / 90.0 + phase * (1.0 + layer * 0.35) + layer) * (15 - layer * 2);
                wave.lineTo(x, y);
            }
            wave.lineTo(w, h);
            wave.closePath();
            g2.setColor(withAlpha(curAccent, 70 + layer * 45));
            g2.fill(wave);
        }
        g2.dispose();
    }

    // =======================================================================
    //  SMALL HELPERS
    // =======================================================================
    private static Color blend(Color a, Color b, double t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    private static Color brighter(Color c, float amount) {
        return blend(c, Color.WHITE, amount);
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Loads the icon file. Tries the folder first, then the class path,
     *  and finally draws a simple icon so the program never fails. */
    private static ImageIcon loadIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() > 0) return icon;

        java.net.URL url = SwingAssignment.class.getResource("/" + path);
        if (url != null) return new ImageIcon(url);

        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x261859));
        g.fillOval(4, 4, 120, 120);
        g.setColor(new Color(0xFFD264));
        g.fillOval(34, 40, 60, 60);
        g.dispose();
        return new ImageIcon(img);
    }

    // =======================================================================
    //  The program starts here
    // =======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingAssignment::new);
    }
}
