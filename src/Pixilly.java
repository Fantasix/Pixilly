import java.awt.*;
import java.util.HashMap;

/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 14:14
 */
public class Pixilly implements Constants {

    private static DungeonMap theMap;

    public static void main(String[] args) {
        System.out.println("Start");
        // Dungeon Map init
        theMap = new DungeonMap(MAX_MAP_WIDTH, MAX_MAP_HEIGHT);

        HashMap<Integer, Integer> params = new HashMap<Integer, Integer>() {
            {
                put(ROOM_AMOUNT, 30);
                put(TRIES_PER_ROOM, 10);
                put(MIN_ROOM_SIZE, 4);
                put(MAX_ROOM_SIZE, 8);

                put(ROOM_EXPANSION, 4);
                put(TRIES_PER_EXPANSION, 10);
                put(MIN_EXPANSION_SIZE, 4);
                put(MAX_EXPANSION_SIZE, 8);

                put(OPACITY_SWITCH, 0);
                put(ROOM_ID_SWITCH, 1);
                put(CLEAN_WALLS_SWITCH, 1);
            }
        };

        theMap.generate(params);

        // Window init
        GameWindow window = new GameWindow();

        window.getMainContent().setPreferredSize(new Dimension(MAX_MAP_WIDTH * TILE_SIZE, MAX_MAP_HEIGHT * TILE_SIZE));

        window.pack();

        window.setLocationRelativeTo(null);

        window.setVisible(true);

        window.getMainThread().start();
    }

    public static DungeonMap getTheMap() {
        return theMap;
    }

}
