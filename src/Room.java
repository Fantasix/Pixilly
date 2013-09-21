import java.util.ArrayList;

/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 21/09/13
 * Time: 00:15
 */
public class Room {
    private int id;
    private int depth = 0;
    private ArrayList<Tile> tiles = new ArrayList<Tile>();
    private ArrayList<Tile> freeTiles = new ArrayList<Tile>();
    private ArrayList<Tile> edgeTiles = new ArrayList<Tile>();

    public Room(int id) {
        this.id = id;
    }

    public Room(int id, int depth) {
        this.id = id;
        this.depth = depth;
    }

    public int getId() {
        return id;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void setTile(int x, int y, int t) {
        int index = hasTile(x, y);
        if (index != -1) {
            tiles.get(index).setType(t);
        }
    }

    public ArrayList<Tile> getFreeTiles() {
        return freeTiles;
    }

    public ArrayList<Tile> getEdgeTiles() {
        return edgeTiles;
    }

    public void addTile(int x, int y, int type) {
        tiles.add(new Tile(x, y, type));
    }

    public void addFreeTile(int x, int y, int type) {
        freeTiles.add(new Tile(x, y, type));
    }

    public void addEdgeTile(int x, int y, int type) {
        edgeTiles.add(new Tile(x, y, type));
    }

    public int hasTile(int x, int y) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getX() == x && tiles.get(i).getY() == y) {
                return i;
            }
        }
        return -1;
    }

    public Tile getTile(int x, int y) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getX() == x && tiles.get(i).getY() == y) {
                return tiles.get(i);
            }
        }

        return null;
    }

    public boolean removeTile(Cell cell) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t.getX() == cell.getX() && t.getY() == cell.getY()) {
                tiles.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeFreeTile(Cell cell) {
        for (int i = 0; i < freeTiles.size(); i++) {
            Tile t = freeTiles.get(i);
            if (t.getX() == cell.getX() && t.getY() == cell.getY()) {
                freeTiles.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeEdgeTile(Cell cell) {
        for (int i = 0; i < edgeTiles.size(); i++) {
            Tile t = edgeTiles.get(i);
            if (t.getX() == cell.getX() && t.getY() == cell.getY()) {
                edgeTiles.remove(i);
                return true;
            }
        }
        return false;
    }

    public void clearEdgeTiles() {
        edgeTiles.clear();
        edgeTiles = new ArrayList<Tile>();
    }
}
