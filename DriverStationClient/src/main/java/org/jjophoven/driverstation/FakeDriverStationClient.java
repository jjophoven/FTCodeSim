package org.jjophoven.driverstation;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FakeDriverStationClient extends JFrame {
    private static final Color BG_DARK       = new Color(0x1A, 0x1A, 0x1A);
    private static final Color BG_PANEL      = new Color(0x2A, 0x2A, 0x2A);
    private static final Color BG_TELEM      = new Color(0x12, 0x12, 0x12);
    private static final Color ACCENT_GREEN  = new Color(0x4C, 0xAF, 0x50);
    private static final Color ACCENT_RED    = new Color(0xF4, 0x43, 0x36);
    private static final Color ACCENT_YELLOW = new Color(0xFF, 0xC1, 0x07);
    private static final Color ACCENT_BLUE   = new Color(0x21, 0x96, 0xF3);
    private static final Color TEXT_PRIMARY  = new Color(0xEE, 0xEE, 0xEE);
    private static final Color TEXT_MUTED    = new Color(0x88, 0x88, 0x88);
    private static final Color BORDER_COLOR  = new Color(0x3A, 0x3A, 0x3A);

    private volatile OpModeState dsState = OpModeState.WAIT_FOR_INIT;

    private JTextArea telemetryArea;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JLabel opModeLabel;
    private JLabel connectionLabel;
    private JButton mainButton;
    private JButton stopButton;

    private Timer swingTimer;
    private long timerStartMs;

    private java.util.List<String> opModes;

    private final DriverStationConnection connection;

    public FakeDriverStationClient(int port, java.util.List<String> opModes) {
        super("FTC Driver Station");

        this.opModes = opModes;
        initUI();

        connection = new DriverStationConnection(
                port,
                telemetryArea::setText,
                () -> {
                    connectionLabel.setText("● CONNECTED");
                    connectionLabel.setForeground(ACCENT_GREEN);
                },
                () -> {
                    connectionLabel.setText("● DISCONNECTED");
                    connectionLabel.setForeground(ACCENT_RED);
                    dispose();
                    System.exit(0);
                }
        );
    }

    private void initUI() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                connection.close();
                dispose();
                System.exit(0);
            }
        });

        setSize(480, 700);
        setMinimumSize(new Dimension(400, 560));
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildTopBar(),     BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomBar(),  BorderLayout.SOUTH);

        applyKeyDispatcher();
        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel title = new JLabel("FTC DRIVER STATION");
        title.setFont(new Font("Dialog", Font.BOLD, 13));
        title.setForeground(TEXT_MUTED);
        title.setOpaque(false);

        connectionLabel = new JLabel("● DISCONNECTED");
        connectionLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        connectionLabel.setForeground(ACCENT_RED);
        connectionLabel.setOpaque(false);

        bar.add(title, BorderLayout.WEST);
        bar.add(connectionLabel, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 12, 8, 12));

        panel.add(buildStatusBlock());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildButtonRow());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildTelemetryBlock());

        return panel;
    }

    private JPanel buildStatusBlock() {
        JPanel block = new JPanel(new GridBagLayout());
        block.setBackground(BG_PANEL);
        block.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        opModeLabel = new JLabel("No Op Mode Selected");
        opModeLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        opModeLabel.setForeground(TEXT_PRIMARY);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        block.add(opModeLabel, c);

        statusLabel = new JLabel("STOPPED");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        statusLabel.setForeground(BG_PANEL);
        statusLabel.setBackground(TEXT_MUTED);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
        block.add(statusLabel, c);

        timerLabel = new JLabel("0:00");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        timerLabel.setForeground(ACCENT_YELLOW);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        block.add(timerLabel, c);

        return block;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        mainButton = makeMainButton("INIT", ACCENT_GREEN);
        mainButton.addActionListener(e -> onMainButton());

        stopButton = makeMainButton("STOP", ACCENT_RED);
        stopButton.addActionListener(e -> onStop());
        stopButton.setEnabled(false);

        row.add(mainButton);
        row.add(stopButton);
        return row;
    }

    private JButton makeMainButton(String label, Color color) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled()
                        ? (getModel().isPressed() ? color.darker() : color)
                        : new Color(0x44, 0x44, 0x44);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 56));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildTelemetryBlock() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setBackground(BG_DARK);

        JLabel header = new JLabel("TELEMETRY");
        header.setFont(new Font("Dialog", Font.BOLD, 10));
        header.setForeground(TEXT_MUTED);
        wrapper.add(header, BorderLayout.NORTH);

        telemetryArea = new JTextArea();
        telemetryArea.setEditable(false);
        telemetryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        telemetryArea.setBackground(BG_TELEM);
        telemetryArea.setForeground(new Color(0x80, 0xFF, 0x80));
        telemetryArea.setCaretColor(new Color(0x80, 0xFF, 0x80));
        telemetryArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(telemetryArea);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(BG_TELEM);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        return bar;
    }

    private JLabel keyHint(String key, String desc) {
        JLabel lbl = new JLabel(key + " → " + desc);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 10));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private void onMainButton() {
        switch (dsState) {
            case WAIT_FOR_INIT:
                transitionTo(OpModeState.INITIALIZING);
                break;
            case INITIALIZING:
                transitionTo(OpModeState.RUNNING);
                break;
            case RUNNING:
                // shouldn't happen; STOP button handles this
                break;
        }
    }

    private void onStop() {
        transitionTo(OpModeState.STOPPED);
    }

    private void transitionTo(OpModeState next) {
        dsState = next;
        SwingUtilities.invokeLater(() -> {
            switch (next) {
                case INITIALIZING:
                    statusLabel.setText("INIT");
                    statusLabel.setBackground(ACCENT_BLUE);
                    mainButton.setText("START");
                    stopButton.setEnabled(true);
                    opModeLabel.setText("TeleOp");
                    timerLabel.setText("0:00");
                    break;

                case RUNNING:
                    statusLabel.setText("RUNNING");
                    statusLabel.setBackground(ACCENT_GREEN);
                    mainButton.setText("RUNNING");
                    mainButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    startTimer();
                    break;

                case STOPPED:
                    statusLabel.setText("STOPPED");
                    statusLabel.setBackground(TEXT_MUTED);
                    mainButton.setText("INIT");
                    mainButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    stopTimer();
                    timerLabel.setForeground(ACCENT_YELLOW);
                    break;
            }
            connection.sendState(dsState);
            mainButton.repaint();
            stopButton.repaint();
        });
    }

    private void startTimer() {
        timerStartMs = System.currentTimeMillis();
        swingTimer = new Timer(500, e -> updateTimer());
        swingTimer.start();
    }

    private void updateTimer() {
        long elapsed = (System.currentTimeMillis() - timerStartMs) / 1000;
        long mins = elapsed / 60;
        long secs = elapsed % 60;
        timerLabel.setText(String.format("%d:%02d", mins, secs));

        // Flash red in last 10 s (for a 120 s period — adjust if needed)
        timerLabel.setForeground(elapsed >= 110 ? ACCENT_RED : ACCENT_YELLOW);
    }

    private void stopTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    private final Set<Integer> pressedKeys = new HashSet<>();

    private void applyKeyDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.isConsumed()) return false;

                    int code = e.getKeyCode();

                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        if (code == KeyEvent.VK_ESCAPE) {
                            onStop();
                            return false;
                        }

                        if (pressedKeys.add(code)) {
                            connection.sendKey(code, true);
                        }

                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        if (pressedKeys.remove(code)) {
                            connection.sendKey(code, false);
                        }
                    }

                    return false;
                });
    }

    public static void main(String[] args) {
        // TODO send opmodes
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        java.util.List<String> opModes = Arrays.asList(args);

        SwingUtilities.invokeLater(() -> new FakeDriverStationClient(port, opModes));
    }
}