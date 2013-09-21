/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 14:10
 */

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame implements Constants {

    private GameManager gameManager;

    private Thread mainThread;

    private JPanel mainContent;

    public GameWindow() {

        super("Pixilly");

        this.gameManager = new GameManager();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setResizable(false);

        mainContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                GameWindow.this.gameManager.rendering(g);
            }
        };

        mainContent.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        setContentPane(mainContent);

        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    GameWindow.this.gameManager.process();

                    mainContent.repaint();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //
                    }

                }
            }
        });

    }

    public Thread getMainThread() {
        return mainThread;
    }

    public JPanel getMainContent() {
        return mainContent;
    }
}
