import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Project : Pixilly
 * Package : PACKAGE_NAME
 * User: Fantasix
 * Date: 20/09/13
 * Time: 14:43
 */
public class DungeonMap implements Constants {

    private Image mapImage;

    private int sizeX;
    private int sizeY;
    private long seed;
    private int roomCount = 0;

    // Params
    private HashMap<Integer, Integer> params = new HashMap<Integer, Integer>() {
        {
            put(MIN_ROOM_SIZE, 3);
            put(MAX_ROOM_SIZE, 10);
            put(ROOM_AMOUNT, 10);
            put(TRIES_PER_ROOM, 10);
            put(ROOM_BRANCHING, 4);
            put(CORRIDOR_RATIO, 5);
            put(MIN_CORRIDOR_SIZE, 2);
            put(MAX_CORRIDOR_SIZE, 6);
        }
    };

    // private Room[][] mapRooms;
    private int[][] mapTiles;
    private ArrayList<Cell> mapFreeWalls = new ArrayList<Cell>();
    private ArrayList<Cell> mapFreeFloors = new ArrayList<Cell>();

    public DungeonMap(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int[][] getMapTiles() {
        return mapTiles;
    }

    public int getTile(int x, int y) {
        return mapTiles[x][y];
    }

    public void setTile(int x, int y, int tile) {
        mapTiles[x][y] = tile;
    }

    public int isWall(int x, int y) {
        Cell c = new Cell(x, y);
        return mapFreeWalls.indexOf(c);
    }

    public int isFloor(int x, int y) {
        Cell c = new Cell(x, y);
        return mapFreeFloors.indexOf(c);
    }

    public Cell chooseRandom(ArrayList<Cell> array) {
        return array.get(getRand(0, array.size() - 1));
    }

    private int getRand(int min, int max) {
        //the seed is based on current date and the old, already used seed
        Date now = new Date();
        long newSeed = now.getTime() + seed;
        seed = newSeed;

        Random rdm = new Random(seed);
        int n = max - min + 1;
        int i = rdm.nextInt(n);
        if (i < 0) {
            i = -i;
        }
        return min + i;
    }

    private int getParam(int param) {
        return params.get(param);
    }

    public boolean generate() {
        return this.generate(new HashMap<Integer, Integer>());
    }

    public boolean generate(HashMap<Integer, Integer> params, int seed) {
        this.seed = seed;

        return this.generate(params);
    }

    public boolean generate(HashMap<Integer, Integer> inParams) {
        // Params
        for (Integer key : inParams.keySet()) {
            params.remove(key);
            params.put(key, inParams.get(key));
        }

        // Variables
        int roomTries = 0;

        // Map Init
        mapTiles = new int[sizeX][sizeY];

        // Filling with tile 0
        for (int y = 0; y < sizeY; y++) {

            for (int x = 0; x < sizeX; x++) {

                setTile(x, y, TILE_EMPTY);
            }

        }

        // First Room
        makeRoom((int) Math.floor(sizeX / 2), (int) Math.floor(sizeY / 2));

        while (roomCount < getParam(ROOM_AMOUNT) && roomTries < getParam(TRIES_PER_ROOM) * (getParam(ROOM_AMOUNT) - 1)) {
            newRandomRoom();
            roomTries++;
        }
        return true;
    }

    public boolean canPlaceRoom(int roomX, int roomY, int roomWidth, int roomHeight) {

        if (roomX < 1 || roomY < 1 || roomX + roomWidth > sizeX - 2 || roomY + roomHeight > sizeY - 2) {
            return false;
        }

        for (int x = roomX; x < roomX + roomWidth; x++) {

            for (int y = roomY; y < roomY + roomHeight; y++) {

                if (mapTiles[x][y] != TILE_EMPTY && mapTiles[x][y] != TILE_WALL && mapTiles[x][y] != TILE_WALL_CORNER) {
                    return false;
                }

            }

        }

        return true;
    }

    public boolean makeRoom(int roomX, int roomY) {
        return this.makeRoom(roomX, roomY, 0, 0);
    }

    public boolean makeRoom(int roomX, int roomY, int offsetX, int offsetY) {
        boolean success = true;
        int roomWidth = getRand(getParam(MIN_ROOM_SIZE), getParam(MAX_ROOM_SIZE));
        int roomHeight = getRand(getParam(MIN_ROOM_SIZE), getParam(MAX_ROOM_SIZE));

        // It's the first room
        if (roomCount == 0) {
            roomX -= Math.floor(roomWidth / 2);
            roomY -= Math.floor(roomHeight / 2);
        } else {

            if (offsetX == 1) {
                roomY = getRand(roomY - roomHeight + 2, roomY - 1);
            } else if (offsetX == -1) {
                roomX -= roomWidth - 1;
                roomY = getRand(roomY - roomHeight + 2, roomY - 1);
            } else if (offsetY == 1) {
                roomX = getRand(roomX - roomWidth + 2, roomX - 1);
            } else if (offsetY == -1) {
                roomY -= roomHeight - 1;
                roomX = getRand(roomX - roomWidth + 2, roomX - 1);
            }
        }

        // System.out.println("Tentative de création d'une salle à " + roomX + "-" + roomY + " (" + roomWidth + "x" + roomHeight + ")");
        // Checking if the base cell is available
        if (canPlaceRoom(roomX, roomY, roomWidth, roomHeight)) {

            for (int x = roomX; x < roomX + roomWidth; x++) {

                for (int y = roomY; y < roomY + roomHeight; y++) {

                    if (x == roomX || x == roomX + roomWidth - 1 || y == roomY || y == roomY + roomHeight - 1) {

                        // North-West
                        if (x == roomX && y == roomY) {
                            setTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-West
                        else if (x == roomX && y == roomY + roomHeight - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-East
                        else if (x == roomX + roomWidth - 1 && y == roomY + roomHeight - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                        }
                        // North-East
                        else if (x == roomX + roomWidth - 1 && y == roomY) {
                            setTile(x, y, TILE_WALL_CORNER);
                        }
                        // Wall
                        else if (getTile(x, y) == TILE_EMPTY) {
                            mapFreeWalls.add(new Cell(x, y));
                            setTile(x, y, TILE_WALL);
                        }

                    } else {
                        if (x == roomX + 1 || x == roomX + roomWidth - 2 || y == roomY + 1 || y == roomY + roomHeight - 2) {
                            if (getTile(x, y) == TILE_EMPTY) setTile(x, y, TILE_FLOOR_EDGE);
                            mapFreeFloors.add(new Cell(x, y));
                        } else {
                            if (getTile(x, y) == TILE_EMPTY) setTile(x, y, TILE_FLOOR);
                        }

                    }

                }

            }

        } else {
            success = false;
        }

        if (success) roomCount++;
        return success;
    }

    public boolean newRandomRoom() {
        boolean success = true;

        Cell theDoor = chooseRandom(mapFreeWalls);

        int offsetX = 0;
        int offsetY = 0;
        int doorX = theDoor.getPosX(), doorY = theDoor.getPosY();
        // East
        if (mapTiles[doorX + 1][doorY] == TILE_EMPTY) {
            offsetX = 1;
        }
        // West
        else if (mapTiles[doorX - 1][doorY] == TILE_EMPTY) {
            offsetX = -1;
        }
        // North
        else if (mapTiles[doorX][doorY - 1] == TILE_EMPTY) {
            offsetY = -1;
        }
        // South
        else if (mapTiles[doorX][doorY + 1] == TILE_EMPTY) {
            offsetY = 1;
        }

        if (offsetX == 0 && offsetY == 0) {
            success = false;
            mapFreeWalls.remove(theDoor);
        } else {
            if (makeRoom(theDoor.getPosX(), theDoor.getPosY(), offsetX, offsetY)) {
                setTile(theDoor.getPosX(), theDoor.getPosY(), TILE_DOOR);
                mapFreeWalls.remove(theDoor);

                // Turn top and bottom walls of the door to corners
                if (offsetX == 1 || offsetX == -1) {
                    setTile(theDoor.getPosX(), theDoor.getPosY() - 1, TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getPosX(), theDoor.getPosY() - 1));
                    setTile(theDoor.getPosX(), theDoor.getPosY() + 1, TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getPosX(), theDoor.getPosY() + 1));
                }
                // Turn left and right walls of the door to corners
                else {
                    setTile(theDoor.getPosX() - 1, theDoor.getPosY(), TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getPosX() - 1, theDoor.getPosY()));
                    setTile(theDoor.getPosX() + 1, theDoor.getPosY(), TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getPosX() + 1, theDoor.getPosY()));
                }

            }
        }

        return success;
    }

    // Get the corresponding tile number
    public int getTileNumber(int x, int y) {
        int tileType = getTile(x, y);

        switch (tileType) {
            case TILE_FLOOR:
                return getRand(12, 14);
            case TILE_FLOOR_EDGE:
                return getRand(12, 14);
            case TILE_WALL:

                int x1 = 0;
                int x2 = 0;
                // Check on left
                while (getTile(x - x1, y) == TILE_WALL) x1++;
                // Check on right
                while (getTile(x + x2, y) == TILE_WALL) x2++;

                if (getTile(x - x1, y) == TILE_WALL_CORNER && getTile(x + x2, y) == TILE_WALL_CORNER) {
                    return 5;
                } else {
                    return 6;
                }

            case TILE_WALL_CORNER:
                return 4;
            case TILE_DOOR:
                return 33;
            default:
                return 0;
        }
    }

    // Return the dungeon map
    public void generateMapImage() {
        System.out.println("Génération de l'image...");
        BufferedImage dungeonImage = new BufferedImage(sizeX * TILE_SIZE, sizeY * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = dungeonImage.createGraphics();

        for (int y = 0; y < sizeY; y++) {

            for (int x = 0; x < sizeX; x++) {

                int tile = getTileNumber(x, y);

                int tileSetXTiles = TILE_SET.getWidth(null) / TILE_SIZE;
                int tileSetYTiles = TILE_SET.getHeight(null) / TILE_SIZE;
                int srcXTile = tile % tileSetXTiles;
                int srcYTile = (int) Math.floor(tile / tileSetYTiles);
                int srcX = srcXTile * TILE_SIZE;
                int srcY = srcYTile * TILE_SIZE;


                g2.drawImage(TILE_SET, x * TILE_SIZE, y * TILE_SIZE, x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE + TILE_SIZE, srcX, srcY, srcX + TILE_SIZE, srcY + TILE_SIZE, null);

            }

        }
        g2.dispose();

        this.mapImage = dungeonImage;
    }

    public Image getMapImage() {
        if (mapImage == null) {
            generateMapImage();
        }

        return mapImage;
    }
}
