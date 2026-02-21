import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class EmojiToiletCatchGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🕊️💩🚽 Emoji Catch Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel(600, 600);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.start();
        });
    }

    static class GamePanel extends JPanel {
        // 画面サイズ
        private final int W, H;
        private final int groundY; // 地面の落下失敗ライン

        // ゲーム状態
        private boolean gameOver = false;
        private int flushedCount = 0;

        // 便器
        private double toiletX;
        private final double toiletY;
        private final double toiletSpeed = 7;

        // 鳩
        private double pigeonX;
        private double pigeonY;
        private boolean pigeonVisible = false;

        // うんち
        private boolean poopActive = false;
        private double poopX;
        private double poopY;
        private double poopVy;

        // 乱数・タイマー
        private final Random rnd = new Random();
        private Timer timer;

        // フォント
        private final Font emojiFont = new Font("SansSerif", Font.PLAIN, 40);
        private final Font uiFont = new Font("SansSerif", Font.BOLD, 24);

        // 背景絵文字
        private final String CLOUD = "☁️";
        private final String TREE  = "🌲";
        private final String MOUNTAIN = "⛰️";

        // 主役絵文字
        private final String PIGEON = "🕊️";
        private final String POOP   = "💩";
        private final String TOILET = "🚽";

        // 難易度
        private final int spawnCheckIntervalFrames = 20;
        private int frameCount = 0;
        private double spawnChance = 0.25;

        // 入力
        private boolean leftDown = false;
        private boolean rightDown = false;

        GamePanel(int w, int h) {
            this.W = w;
            this.H = h;
            setPreferredSize(new Dimension(W, H));
            setFocusable(true);

            int groundHeight = 150;
            this.groundY = H - groundHeight;

            this.toiletY = groundY + 40;
            this.toiletX = W / 2.0;

            setupKeyBindings();
        }

        void start() {
            timer = new Timer(16, e -> onTick()); // 約60fps
            timer.start();
            requestFocusInWindow();
        }

        private void setupKeyBindings() {
            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "leftPressed");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "rightPressed");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "leftReleased");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "rightReleased");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "restart");

            am.put("leftPressed", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { leftDown = true; }
            });
            am.put("rightPressed", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { rightDown = true; }
            });
            am.put("leftReleased", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { leftDown = false; }
            });
            am.put("rightReleased", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { rightDown = false; }
            });
            am.put("restart", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (gameOver) resetGame();
                }
            });
        }

        private void resetGame() {
            gameOver = false;
            flushedCount = 0;
            poopActive = false;
            pigeonVisible = false;
            toiletX = W / 2.0;
            repaint();
        }

        private void onTick() {
            if (gameOver) {
                repaint();
                return;
            }

            // 便器移動、左右
            if (leftDown)  toiletX -= toiletSpeed;
            if (rightDown) toiletX += toiletSpeed;

            // 便器の範囲制限
            toiletX = Math.max(20, Math.min(W - 60, toiletX));

            // 鳩＆うんちが出る
            frameCount++;
            if (!poopActive && frameCount % spawnCheckIntervalFrames == 0) {
                if (rnd.nextDouble() < spawnChance) {
                    spawnPigeonAndPoop();
                }
            }

            // うんち落下
            if (poopActive) {
                poopVy += 0.35; // 重力
                poopY  += poopVy;

                // キャッチの判定はこちら
                if (checkCatch()) {
                    poopActive = false;
                    pigeonVisible = false;
                    flushedCount++;
                } else {
                    // 地面に落ちたらゲームオーバー
                    if (poopY > groundY + 10) {
                        gameOver = true;
                    }
                }
            }

            repaint();
        }

        private void spawnPigeonAndPoop() {
            pigeonVisible = true;

            pigeonX = 50 + rnd.nextInt(W - 100);
            pigeonY = 80;

            poopActive = true;
            poopX = pigeonX + 10;
            poopY = pigeonY + 35;
            poopVy = 0.0;
        }

        private boolean checkCatch() {
            Rectangle poopRect = new Rectangle((int)poopX, (int)poopY, 36, 36);
            Rectangle toiletRect = new Rectangle((int)toiletX, (int)(toiletY - 30), 60, 60);
            return poopRect.intersects(toiletRect);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // 背景：空
            g2.setColor(new Color(90, 200, 255));
            g2.fillRect(0, 0, W, H);

            // 地面：草原
            g2.setColor(new Color(120, 200, 120));
            g2.fillRect(0, groundY, W, H - groundY);

            // 背景絵文字
            g2.setFont(emojiFont);
            drawEmoji(g2, CLOUD, 60,  80);
            drawEmoji(g2, CLOUD, 500, 90);
            drawEmoji(g2, TREE,  80,  groundY - 20);
            drawEmoji(g2, TREE,  500, groundY - 20);
            drawEmoji(g2, MOUNTAIN, 280, groundY - 10);

            // 鳩（灰色フチ）
            if (pigeonVisible) {
                drawPigeon(g2, PIGEON, (int)pigeonX, (int)pigeonY);
            }

            // うんち（茶色フチ）
            if (poopActive) {
                drawPoop(g2, POOP, (int)poopX, (int)poopY);
            }

            // 便器（背景つき）
            drawToiletWithHighlight(g2, TOILET, (int)toiletX, (int)toiletY);

            // 右上スコア
            g2.setFont(uiFont);
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("流した数 %03d", flushedCount), W - 200, 40);

            // ゲームオーバー
            if (gameOver) {
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, W, H);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 42));
                g2.drawString("GAME OVER", 170, 260);

                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                g2.drawString("最終スコア: " + flushedCount, 200, 310);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                g2.drawString("Rキーでリスタート", 230, 350);
            }
        }



        private void drawEmoji(Graphics2D g2, String s, int x, int y) {
            g2.drawString(s, x, y);
        }

        // 💩：茶色アウトライン（8方向）
        private void drawPoop(Graphics2D g2, String s, int x, int y) {
            Color outline = new Color(90, 40, 0); // 濃茶
            drawOutlinedEmoji(g2, s, x, y, outline);
        }

        // 🕊：灰色アウトライン（8方向）
        private void drawPigeon(Graphics2D g2, String s, int x, int y) {
            Color outline = new Color(120, 120, 120); // 灰色
            drawOutlinedEmoji(g2, s, x, y, outline);
        }

        // 🚽：背景ハイライト＋絵文字
        private void drawToiletWithHighlight(Graphics2D g2, String s, int x, int y) {
            // 半透明の黒い下敷き（見えにくかったので改善）
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x - 10, y - 40, 70, 70, 20, 20);

            // 絵文字本体
            g2.setColor(Color.WHITE); // ※絵文字自体の色はフォント依存
            g2.drawString(s, x, y);
        }

        // 共通：アウトライン描画（8方向）
        private void drawOutlinedEmoji(Graphics2D g2, String s, int x, int y, Color outlineColor) {
            g2.setColor(outlineColor);

            int d = 2;
            // 4方向
            g2.drawString(s, x - d, y);
            g2.drawString(s, x + d, y);
            g2.drawString(s, x, y - d);
            g2.drawString(s, x, y + d);
            // 斜4方向
            g2.drawString(s, x - d, y - d);
            g2.drawString(s, x + d, y - d);
            g2.drawString(s, x - d, y + d);
            g2.drawString(s, x + d, y + d);

            // 本体
            g2.setColor(Color.WHITE);
            g2.drawString(s, x, y);
        }
    }
}