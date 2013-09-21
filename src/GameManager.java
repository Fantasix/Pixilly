/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 14:10
 */

import java.awt.*;

public class GameManager implements Constants {

    public GameManager() {

    }

    public void process() {

    }

    public void rendering(Graphics g) {
        g.drawImage(Pixilly.getTheMap().getMapImage(), 0, 0, null);
    }
}

