/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 21/09/13
 * Time: 18:32
 */
public class Tile {
    private int type;
    private int x;
    private int y;

    public Tile(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
