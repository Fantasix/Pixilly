import javax.swing.*;
import java.awt.*;

/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 14:09
 */
public interface Constants {
    // Window
    final static int WINDOW_WIDTH = 480;
    final static int WINDOW_HEIGHT = 480;

    // Map
    final static int MAX_MAP_WIDTH = 80;
    final static int MAX_MAP_HEIGHT = 40;

    final static int MIN_ROOM_SIZE = 0;
    final static int MAX_ROOM_SIZE = 1;
    final static int ROOM_RATIO = 2;
    final static int ROOM_BRANCHING = 3;
    final static int CORRIDOR_RATIO = 4;
    final static int MIN_CORRIDOR_SIZE = 5;
    final static int MAX_CORRIDOR_SIZE = 6;

    // Tiles
    final static int TILE_SIZE = 16;
    final static Image TILE_SET = new ImageIcon("dungeonTiles.png").getImage();

    final static int TILE_EMPTY = 0;
    final static int TILE_FLOOR = 100;
    final static int TILE_FLOOR_EDGE = 101;
    final static int TILE_WALL = 200;
    final static int TILE_WALL_CORNER = 201;
    final static int TILE_CORRIDOR_FLOOR = 300;
    final static int TILE_CORRIDOR_WALL = 301;

    public final int TILE_DOOR = 900;
    public final int TILE_UPSTAIRS = 901;
    public final int TILE_DOWNSTAIRS = 902;
    public final int TILE_CHEST = 903;
}
