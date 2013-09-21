/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 15:01
 */
public class Cell implements Constants {
    private int x;
    private int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

