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
            put(OPACITY_SWITCH, 1);
            put(MIN_EXPANSION_SIZE, 3);
            put(MAX_EXPANSION_SIZE, 6);
        }
    };

    // private Room[][] mapRooms;
    private int[][] mapTiles;
    private ArrayList<Cell> mapFreeWalls = new ArrayList<Cell>();
    private ArrayList<Cell> mapFreeFloors = new ArrayList<Cell>();
    private ArrayList<Room> mapRooms = new ArrayList<Room>();

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

    public Room getRoom(int x, int y) {

        for (int i = 0; i < mapRooms.size(); i++) {
            Room theRoom = mapRooms.get(i);
            if (theRoom.hasTile(x, y) != -1) {
                return theRoom;
            }
        }

        return null;
    }

    public int isWall(int x, int y) {
        Cell c = new Cell(x, y);
        return mapFreeWalls.indexOf(c);
    }

    public int isFloor(int x, int y) {
        Cell c = new Cell(x, y);
        return mapFreeFloors.indexOf(c);
    }

    public Object chooseRandom(ArrayList array) {
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
        Room room = makeRoom((int) Math.floor(sizeX / 2), (int) Math.floor(sizeY / 2));
        for (int i = 0; i < getParam(ROOM_BRANCHING); i++) {
            expandRoom(room);
        }

        while (roomCount < getParam(ROOM_AMOUNT) && roomTries < getParam(TRIES_PER_ROOM) * (getParam(ROOM_AMOUNT) - 1)) {
            room = newRandomRoom();
            if (room != null) {
                for (int i = 0; i < getParam(ROOM_BRANCHING); i++) {
                    expandRoom(room);
                }

            }
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

    public boolean canExpandRoom(int exX, int exY, int exWidth, int exHeight, Room room) {

        if (exX < 1 || exY < 1 || exX + exWidth > sizeX - 2 || exY + exHeight > sizeY - 2) {
            return false;
        }

        for (int x = exX; x < exX + exWidth; x++) {

            for (int y = exY; y < exY + exHeight; y++) {

                // If it's a door : can't expand
                if (getTile(x, y) == TILE_DOOR) {
                    return false;
                }
                // If it's not tile from this room, and not a wall/corner/empty tile
                else if (room.hasTile(x, y) == -1 && !(getTile(x, y) == TILE_EMPTY || getTile(x, y) == TILE_WALL || getTile(x, y) == TILE_WALL_CORNER)) {
                    return false;
                }

            }

        }

        return true;
    }

    public boolean expandRoom(Room room) {
        boolean success = true;

        Tile tile = (Tile) chooseRandom(room.getEdgeTiles());
        int exWidth = getRand(getParam(MIN_EXPANSION_SIZE), getParam(MAX_EXPANSION_SIZE));
        int exHeight = getRand(getParam(MIN_EXPANSION_SIZE), getParam(MAX_EXPANSION_SIZE));

        int xOffset = (int) Math.floor(exWidth / 2);
        int yOffset = (int) Math.floor(exHeight / 2);

        int rX = tile.getX() - xOffset;
        int rY = tile.getY() - yOffset;

        if (canExpandRoom(rX, rY, exWidth, exHeight, room)) {
            System.out.println("Expansion :  " + rX + "-" + rY + " (" + exWidth + "x" + exHeight + ")");

            for (int x = rX; x < rX + exWidth; x++) {

                for (int y = rY; y < rY + exHeight; y++) {

                    if (x == rX || x == rX + exWidth - 1 || y == rY || y == rY + exHeight - 1) {

                        if (getTile(x, y) == TILE_EMPTY) {
                            // North-West
                            if (x == rX && y == rY) {
                                setTile(x, y, TILE_WALL_CORNER);
                                room.addTile(x, y, TILE_WALL_CORNER);
                            }
                            // South-West
                            else if (x == rX && y == rY + exHeight - 1) {
                                setTile(x, y, TILE_WALL_CORNER);
                                room.addTile(x, y, TILE_WALL_CORNER);
                            }
                            // South-East
                            else if (x == rX + exWidth - 1 && y == rY + exHeight - 1) {
                                setTile(x, y, TILE_WALL_CORNER);
                                room.addTile(x, y, TILE_WALL_CORNER);
                            }
                            // North-East
                            else if (x == rX + exWidth - 1 && y == rY) {
                                setTile(x, y, TILE_WALL_CORNER);
                                room.addTile(x, y, TILE_WALL_CORNER);
                            }
                            // Wall
                            else if (getTile(x, y) == TILE_EMPTY) {
                                mapFreeWalls.add(new Cell(x, y));
                                setTile(x, y, TILE_WALL);
                                room.addTile(x, y, TILE_WALL);
                            }

                        } else if (getTile(x, y) == TILE_WALL_CORNER) {
                            // If it's a corner, do not apply
                            if ((x == rX && y == rY)
                                    || (x == rX && y == rY + exHeight - 1)
                                    || (x == rX + exWidth - 1 && y == rY + exHeight - 1)
                                    || (x == rX + exWidth - 1 && y == rY)) {
                            } else {
                                mapFreeWalls.add(new Cell(x, y));
                                setTile(x, y, TILE_WALL);
                                room.addTile(x, y, TILE_WALL);
                            }
                        }
                    } else {

                        if (x == rX + 1 || x == rX + exWidth - 2 || y == rY + 1 || y == rY + exHeight - 2) {
                            if (getTile(x, y) == TILE_EMPTY) {
                                setTile(x, y, TILE_FLOOR_EDGE);
                                room.addTile(x, y, TILE_FLOOR_EDGE);
                                room.addEdgeTile(x, y, TILE_FLOOR_EDGE);

                            } else if (getTile(x, y) == TILE_WALL || getTile(x, y) == TILE_WALL_CORNER) {
                                setTile(x, y, TILE_FLOOR_EDGE);
                                room.addTile(x, y, TILE_FLOOR_EDGE);
                                room.addEdgeTile(x, y, TILE_FLOOR_EDGE);
                                mapFreeWalls.remove(new Cell(x, y));
                            }
                        } else {
                            if (getTile(x, y) == TILE_EMPTY || getTile(x, y) == TILE_WALL || getTile(x, y) == TILE_WALL_CORNER || getTile(x, y) == TILE_FLOOR_EDGE) {
                                setTile(x, y, TILE_FLOOR);
                                room.addTile(x, y, TILE_FLOOR);
                                room.removeEdgeTile(new Cell(x, y));
                                mapFreeWalls.remove(new Cell(x, y));
                                mapFreeFloors.add(new Cell(x, y));
                            }
                        }
                    }

                }

            }

            // Finishing touch for wall corners
            for (int x = rX; x < rX + exWidth; x++) {

                for (int y = rY; y < rY + exHeight; y++) {


                    if (getTile(x, y) == TILE_WALL) {
                        int edgeCount = 0;
                        if (getTile(x + 1, y) == TILE_FLOOR_EDGE) edgeCount++;
                        if (getTile(x - 1, y) == TILE_FLOOR_EDGE) edgeCount++;
                        if (getTile(x, y + 1) == TILE_FLOOR_EDGE) edgeCount++;
                        if (getTile(x, y - 1) == TILE_FLOOR_EDGE) edgeCount++;

                        if (edgeCount > 1) {
                            mapFreeWalls.remove(new Cell(x, y));
                            room.removeTile(new Cell(x, y));
                            setTile(x, y, TILE_WALL_CORNER);
                            room.addTile(x, y, TILE_WALL_CORNER);
                        }
                    }
                }
            }
        }

        return success;
    }

    public Room makeRoom(int roomX, int roomY) {
        return this.makeRoom(roomX, roomY, 0, 0);
    }

    public Room makeRoom(int roomX, int roomY, int offsetX, int offsetY) {
        Room newRoom = null;
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

            newRoom = new Room(mapRooms.size());
            System.out.println("Room :  " + roomX + "-" + roomY + " (" + roomWidth + "x" + roomHeight + ")");
            for (int x = roomX; x < roomX + roomWidth; x++) {

                for (int y = roomY; y < roomY + roomHeight; y++) {

                    if (x == roomX || x == roomX + roomWidth - 1 || y == roomY || y == roomY + roomHeight - 1) {

                        // North-West
                        if (x == roomX && y == roomY) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newRoom.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-West
                        else if (x == roomX && y == roomY + roomHeight - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newRoom.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-East
                        else if (x == roomX + roomWidth - 1 && y == roomY + roomHeight - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newRoom.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // North-East
                        else if (x == roomX + roomWidth - 1 && y == roomY) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newRoom.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // Wall
                        else if (getTile(x, y) == TILE_EMPTY) {
                            mapFreeWalls.add(new Cell(x, y));
                            setTile(x, y, TILE_WALL);
                            newRoom.addTile(x, y, TILE_WALL);
                        }

                    } else {
                        if (x == roomX + 1 || x == roomX + roomWidth - 2 || y == roomY + 1 || y == roomY + roomHeight - 2) {
                            if (getTile(x, y) == TILE_EMPTY) {
                                setTile(x, y, TILE_FLOOR_EDGE);
                                newRoom.addTile(x, y, TILE_FLOOR_EDGE);
                                newRoom.addEdgeTile(x, y, TILE_FLOOR_EDGE);
                            }

                        } else {
                            if (getTile(x, y) == TILE_EMPTY) {
                                setTile(x, y, TILE_FLOOR);
                                mapFreeFloors.add(new Cell(x, y));
                                newRoom.addTile(x, y, TILE_FLOOR);
                                newRoom.addFreeTile(x, y, TILE_FLOOR);
                            }
                        }

                    }

                }

            }

            mapRooms.add(newRoom);

        }

        if (newRoom != null) roomCount++;
        return newRoom;
    }

    public Room newRandomRoom() {
        Room newRoom = null;

        Cell theDoor = (Cell) chooseRandom(mapFreeWalls);

        int offsetX = 0;
        int offsetY = 0;
        int doorX = theDoor.getX(), doorY = theDoor.getY();
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
            mapFreeWalls.remove(theDoor);
        } else {
            newRoom = makeRoom(theDoor.getX(), theDoor.getY(), offsetX, offsetY);
            if (newRoom != null) {
                setTile(theDoor.getX(), theDoor.getY(), TILE_DOOR);
                mapFreeWalls.remove(theDoor);

                // Turn top and bottom walls of the door to corners
                if (offsetX == 1 || offsetX == -1) {
                    setTile(theDoor.getX(), theDoor.getY() - 1, TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getX(), theDoor.getY() - 1));
                    setTile(theDoor.getX(), theDoor.getY() + 1, TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getX(), theDoor.getY() + 1));
                }
                // Turn left and right walls of the door to corners
                else {
                    setTile(theDoor.getX() - 1, theDoor.getY(), TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getX() - 1, theDoor.getY()));
                    setTile(theDoor.getX() + 1, theDoor.getY(), TILE_WALL_CORNER);
                    mapFreeWalls.remove(new Cell(theDoor.getX() + 1, theDoor.getY()));
                }

            }
        }

        return newRoom;
    }

    // Get the corresponding tile number
    public int getTileNumber(int x, int y) {
        int tileType = getTile(x, y);

        switch (tileType) {
            case TILE_FLOOR:
                return getRand(12, 14);
            case TILE_FLOOR_EDGE:
                return getRand(9, 11);
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

                // Opacity
                if (getParam(OPACITY_SWITCH) == 1) {
                    Room theRoom = getRoom(x, y);
                    if (theRoom != null) {
                        double opacity = Math.floor(((double) theRoom.getId() / mapRooms.size()) * 255);
                        g2.setColor(new Color(0, 0, 0, (int) opacity));
                        g2.fillRect(x * TILE_SIZE, y * TILE_SIZE, 16, 16);
                    }
                }
            }

        }

        this.mapImage = dungeonImage;
    }

    public Image getMapImage() {
        if (mapImage == null) {
            generateMapImage();
        }

        return mapImage;
    }
}
