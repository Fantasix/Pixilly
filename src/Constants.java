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

    // Map Params
    final static int MIN_ROOM_SIZE = 0;
    final static int MAX_ROOM_SIZE = 1;
    final static int ROOM_AMOUNT = 2;
    final static int TRIES_PER_ROOM = 3;
    final static int ROOM_EXPANSION = 4;
    final static int CORRIDOR_RATIO = 5;
    final static int MIN_CORRIDOR_SIZE = 6;
    final static int MAX_CORRIDOR_SIZE = 7;
    final static int OPACITY_SWITCH = 8;
    final static int MIN_EXPANSION_SIZE = 9;
    final static int MAX_EXPANSION_SIZE = 10;
    final static int ROOM_ID_SWITCH = 11;
    final static int TRIES_PER_EXPANSION = 12;
    final static int CLEAN_WALLS_SWITCH = 13;

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
