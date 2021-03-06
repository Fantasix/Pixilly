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
            put(FIRST_ROOM_SIZE_RATIO, 1);

            put(ROOM_EXPANSION, 4);
            put(TRIES_PER_EXPANSION, 6);
            put(MIN_EXPANSION_SIZE, 3);
            put(MAX_EXPANSION_SIZE, 6);

            put(CORRIDOR_RATIO, 5);
            put(MIN_CORRIDOR_SIZE, 2);
            put(MAX_CORRIDOR_SIZE, 6);

            put(OPACITY_SWITCH, 1);
            put(ROOM_ID_SWITCH, 0);
            put(CLEAN_WALLS_SWITCH, 1);
        }
    };

    private int[][] mapTiles;
    private ArrayList<Cell> mapFreeWalls = new ArrayList<Cell>();
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

    public int getMaxDepth() {
        int d = 0;

        for (Room r : mapRooms) {
            d = r.getDepth() > d ? r.getDepth() : d;
        }

        return d;
    }

    public Room getDeepestRoom() {
        int rId = 0;
        int depth = 0;
        for (Room r : mapRooms) {
            if (r.getDepth() > depth && !r.isCorridor()) {
                depth = r.getDepth();
                rId = r.getId();
            }
        }
        return mapRooms.get(rId);
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
        Room firstRoom = makeRoom((int) Math.floor(sizeX / 2), (int) Math.floor(sizeY / 2));
        for (int i = 0; i < getParam(ROOM_EXPANSION); i++) {
            expandRoom(firstRoom);
        }
        finishingFixes(firstRoom);

        while (roomCount < getParam(ROOM_AMOUNT) && roomTries < getParam(TRIES_PER_ROOM) * (getParam(ROOM_AMOUNT) - 1)) {
            // Choose between Room & Corridor
            // Room
            if (getRand(0, 100) > getParam(CORRIDOR_RATIO)) {
                Room newRoom = newRandomRoom();
                if (newRoom != null) {
                    for (int i = 0; i < getParam(ROOM_EXPANSION); i++) {
                        int tries = 0;
                        while (!expandRoom(newRoom) && tries < getParam(TRIES_PER_EXPANSION)) tries++;
                    }
                    finishingFixes(newRoom);
                }
                roomTries++;
            }
            // Corridor
            else {
                Room corridor = newRandomCorridor();
                if (corridor != null) {
                    finishingFixes(corridor);
                }
                roomTries++;
            }
        }

        // Adding in and out stairs
        placeUpStairs();
        placeDownStairs();

        System.out.println("Rooms : " + mapRooms.size() + "/" + getParam(ROOM_AMOUNT));
        System.out.println("Max Depth : " + getMaxDepth());

        return true;
    }

    public boolean placeUpStairs() {
        boolean success = true;
        Room upStairsRoom = mapRooms.get(0);
        Tile upStairsTile;

        if (upStairsRoom.getFreeTiles().size() == 0) {
            ArrayList<Tile> edgeTilesCopy = (ArrayList<Tile>) upStairsRoom.getEdgeTiles().clone();
            upStairsTile = (Tile) chooseRandom(edgeTilesCopy);
            int x = upStairsTile.getX();
            int y = upStairsTile.getY();

            while (edgeTilesCopy.size() > 0 && (getTile(x + 1, y) == TILE_DOOR || getTile(x - 1, y) == TILE_DOOR || getTile(x, y + 1) == TILE_DOOR || getTile(x, y - 1) == TILE_DOOR)) {
                edgeTilesCopy.remove(upStairsTile);
                if (edgeTilesCopy.size() > 0) {
                    upStairsTile = (Tile) chooseRandom(edgeTilesCopy);
                    x = upStairsTile.getX();
                    y = upStairsTile.getY();
                } else {
                    success = false;
                }
            }

            if (success) {
                upStairsRoom.removeEdgeTile(upStairsTile.getX(), upStairsTile.getY());
                upStairsRoom.setTile(upStairsTile.getX(), upStairsTile.getY(), TILE_UPSTAIRS);
                setTile(upStairsTile.getX(), upStairsTile.getY(), TILE_UPSTAIRS);
            }
        } else {
            upStairsTile = (Tile) chooseRandom(upStairsRoom.getFreeTiles());
            upStairsRoom.removeFreeTile(upStairsTile.getX(), upStairsTile.getY());
            upStairsRoom.setTile(upStairsTile.getX(), upStairsTile.getY(), TILE_UPSTAIRS);
            setTile(upStairsTile.getX(), upStairsTile.getY(), TILE_UPSTAIRS);
        }

        return success;
    }

    public boolean placeDownStairs() {
        boolean success = true;
        Room downStairsRoom = getDeepestRoom();
        Tile downStairsTile;

        if (downStairsRoom.getFreeTiles().size() == 0) {
            ArrayList<Tile> edgeTilesCopy = (ArrayList<Tile>) downStairsRoom.getEdgeTiles().clone();
            downStairsTile = (Tile) chooseRandom(edgeTilesCopy);
            int x = downStairsTile.getX();
            int y = downStairsTile.getY();

            while (edgeTilesCopy.size() > 0 && (getTile(x + 1, y) == TILE_DOOR || getTile(x - 1, y) == TILE_DOOR || getTile(x, y + 1) == TILE_DOOR || getTile(x, y - 1) == TILE_DOOR)) {
                edgeTilesCopy.remove(downStairsTile);
                if (edgeTilesCopy.size() > 0) {
                    downStairsTile = (Tile) chooseRandom(edgeTilesCopy);
                    x = downStairsTile.getX();
                    y = downStairsTile.getY();
                } else {
                    success = false;
                }
            }

            if (success) {
                downStairsRoom.removeEdgeTile(downStairsTile.getX(), downStairsTile.getY());
                downStairsRoom.setTile(downStairsTile.getX(), downStairsTile.getY(), TILE_DOWNSTAIRS);
                setTile(downStairsTile.getX(), downStairsTile.getY(), TILE_DOWNSTAIRS);
            }
        } else {
            downStairsTile = (Tile) chooseRandom(downStairsRoom.getFreeTiles());
            downStairsRoom.removeFreeTile(downStairsTile.getX(), downStairsTile.getY());
            downStairsRoom.setTile(downStairsTile.getX(), downStairsTile.getY(), TILE_DOWNSTAIRS);
            setTile(downStairsTile.getX(), downStairsTile.getY(), TILE_DOWNSTAIRS);
        }

        return success;
    }

    public void finishingFixes(Room room) {
        if (room != null) {
            for (int x = 1; x < sizeX - 1; x++) {
                for (int y = 1; y < sizeY; y++) {

                    // Wall fix (3 walls + 1 empty)
                    if (getTile(x, y) == TILE_WALL) {

                        int wallX = 0;
                        int wallY = 0;
                        int cornerX = 0;
                        int cornerY = 0;

                        if (getTile(x + 1, y) == TILE_WALL) wallX++;
                        if (getTile(x - 1, y) == TILE_WALL) wallX++;
                        if (getTile(x, y + 1) == TILE_WALL) wallY++;
                        if (getTile(x, y - 1) == TILE_WALL) wallY++;

                        if (getTile(x + 1, y) == TILE_WALL_CORNER) cornerX++;
                        if (getTile(x - 1, y) == TILE_WALL_CORNER) cornerX++;
                        if (getTile(x, y + 1) == TILE_WALL_CORNER) cornerY++;
                        if (getTile(x, y - 1) == TILE_WALL_CORNER) cornerY++;

                        if (cornerX == 2 && cornerY == 2) {
                            setTile(x, y, TILE_WALL_CORNER);
                            room.setTile(x, y, TILE_WALL_CORNER);
                        } else if (wallX == 1 && (cornerY + wallY == 2)) {
                            if (getTile(x + 1, y) == TILE_WALL || getTile(x + 1, y) == TILE_WALL_CORNER) {
                                if (getTile(x + 2, y) == TILE_WALL || getTile(x + 2, y) == TILE_WALL_CORNER) {
                                    setTile(x, y, TILE_WALL_CORNER);
                                    room.setTile(x, y, TILE_WALL_CORNER);
                                }
                            } else {
                                if (getTile(x - 2, y) == TILE_WALL || getTile(x - 2, y) == TILE_WALL_CORNER) {
                                    setTile(x, y, TILE_WALL_CORNER);
                                    room.setTile(x, y, TILE_WALL_CORNER);
                                }
                            }
                        } else if (wallY == 1 && (cornerX + wallX == 2)) {
                            if (getTile(x, y + 1) == TILE_WALL || getTile(x, y + 1) == TILE_WALL_CORNER) {
                                if (getTile(x, y + 2) == TILE_WALL || getTile(x, y + 2) == TILE_WALL_CORNER) {
                                    setTile(x, y, TILE_WALL_CORNER);
                                    room.setTile(x, y, TILE_WALL_CORNER);
                                }
                            } else {
                                if (getTile(x, y - 2) == TILE_WALL || getTile(x, y - 2) == TILE_WALL_CORNER) {
                                    setTile(x, y, TILE_WALL_CORNER);
                                    room.setTile(x, y, TILE_WALL_CORNER);
                                }
                            }
                        }
                    }

                    // Wall fix (2 Edges)
                    if (getTile(x, y) == TILE_WALL) {
                        int edgeCount = 0;
                        int xCount = 0;
                        int yCount = 0;
                        if (getTile(x + 1, y) == TILE_FLOOR_EDGE) {
                            edgeCount++;
                            xCount++;
                        }
                        if (getTile(x - 1, y) == TILE_FLOOR_EDGE) {
                            edgeCount++;
                            xCount++;
                        }
                        if (getTile(x, y + 1) == TILE_FLOOR_EDGE) {
                            edgeCount++;
                            yCount++;
                        }
                        if (getTile(x, y - 1) == TILE_FLOOR_EDGE) {
                            edgeCount++;
                            yCount++;
                        }

                        if (edgeCount > 1 && xCount > 0 && yCount > 0) {
                            mapFreeWalls.remove(new Cell(x, y));
                            setTile(x, y, TILE_WALL_CORNER);
                            room.setTile(x, y, TILE_WALL_CORNER);
                        }
                    }
                }
            }
        }
    }

    public boolean canPlaceRoom(int roomX, int roomY, int roomWidth, int roomHeight) {

        if (roomX < 1 || roomY < 1 || roomX + roomWidth > sizeX - 2 || roomY + roomHeight > sizeY - 2) {
            return false;
        }

        for (int x = roomX; x < roomX + roomWidth; x++) {

            for (int y = roomY; y < roomY + roomHeight; y++) {

                if (getTile(x, y) != TILE_EMPTY && getTile(x, y) != TILE_WALL && getTile(x, y) != TILE_WALL_CORNER) {
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

                        } else if (getTile(x, y) == TILE_WALL_CORNER && getParam(CLEAN_WALLS_SWITCH) == 1) {
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
                                room.setTile(x, y, TILE_FLOOR_EDGE);
                                room.addEdgeTile(x, y, TILE_FLOOR_EDGE);
                                mapFreeWalls.remove(new Cell(x, y));
                            }
                        } else {
                            if (getTile(x, y) == TILE_EMPTY || getTile(x, y) == TILE_WALL || getTile(x, y) == TILE_WALL_CORNER || getTile(x, y) == TILE_FLOOR_EDGE) {
                                setTile(x, y, TILE_FLOOR);
                                room.addTile(x, y, TILE_FLOOR);
                                room.addFreeTile(x, y, TILE_FLOOR);
                                room.removeEdgeTile(x, y);
                                mapFreeWalls.remove(new Cell(x, y));
                            }
                        }
                    }
                }
            }

            for (Tile t : room.getEdgeTiles()) {

                setTile(t.getX(), t.getY(), TILE_FLOOR);
                room.setTile(t.getX(), t.getY(), TILE_FLOOR);

            }
            room.clearEdgeTiles();

            for (int x = 1; x < sizeX - 2; x++) {

                for (int y = 1; y < sizeY - 2; y++) {

                    if (getTile(x, y) == TILE_WALL || getTile(x, y) == TILE_WALL_CORNER) {
                        // Check the tiles around
                        for (int x1 = -1; x1 < 2; x1++) {
                            for (int y1 = -1; y1 < 2; y1++) {
                                if ((x1 != 0 || y1 != 0) && getTile(x + x1, y + y1) == TILE_FLOOR) {
                                    setTile(x + x1, y + y1, TILE_FLOOR_EDGE);
                                    room.setTile(x + x1, y + y1, TILE_FLOOR);
                                    room.addEdgeTile(x + x1, y + y1, TILE_FLOOR_EDGE);
                                }
                            }
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
        int depth = 0;
        int roomWidth = getRand(getParam(MIN_ROOM_SIZE), getParam(MAX_ROOM_SIZE));
        int roomHeight = getRand(getParam(MIN_ROOM_SIZE), getParam(MAX_ROOM_SIZE));

        // It's the first room
        if (roomCount == 0) {
            roomWidth = (int) Math.floor(roomWidth * getParam(FIRST_ROOM_SIZE_RATIO));
            roomHeight = (int) Math.floor(roomHeight * getParam(FIRST_ROOM_SIZE_RATIO));
            roomX -= Math.floor(roomWidth / 2);
            roomY -= Math.floor(roomHeight / 2);
        } else {

            Room origin = getRoom(roomX, roomY);
            depth = origin != null ? origin.getDepth() + 1 : 0;

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

        // Checking if the base cell is available
        if (canPlaceRoom(roomX, roomY, roomWidth, roomHeight)) {

            newRoom = new Room(mapRooms.size(), depth);

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
                                newRoom.addTile(x, y, TILE_FLOOR);
                                newRoom.addFreeTile(x, y, TILE_FLOOR);
                            }
                        }

                    }

                }

            }

            mapRooms.add(newRoom);
            roomCount++;
        }

        return newRoom;
    }

    public Room newRandomRoom() {
        Room newRoom = null;

        Cell theDoor = (Cell) chooseRandom(mapFreeWalls);

        int offsetX = 0;
        int offsetY = 0;
        int doorX = theDoor.getX(), doorY = theDoor.getY();
        // East
        if (getTile(doorX + 1, doorY) == TILE_EMPTY) {
            offsetX = 1;
        }
        // West
        else if (getTile(doorX - 1, doorY) == TILE_EMPTY) {
            offsetX = -1;
        }
        // North
        else if (getTile(doorX, doorY - 1) == TILE_EMPTY) {
            offsetY = -1;
        }
        // South
        else if (getTile(doorX, doorY + 1) == TILE_EMPTY) {
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

    public Room makeCorridor(int cX, int cY, int offsetX, int offsetY) {
        Room newCorridor = null;
        int depth = 0;
        int maxLength = getRand(getParam(MIN_CORRIDOR_SIZE), getParam(MAX_CORRIDOR_SIZE));
        int length = 0;
        int width = 0;
        int height = 0;
        int pX = 0, pY = 0;
        boolean canPlace = false;

        Room origin = getRoom(cX, cY);
        depth = origin != null ? origin.getDepth() + 1 : 0;

        if (offsetX == 1) {
            for (int i = 1; i < maxLength + 1; i++) {
                if (i == maxLength || cX + i >= sizeX - 1 || (getTile(cX + i, cY) != TILE_EMPTY && getTile(cX + i, cY) != TILE_WALL && getTile(cX + i, cY) != TILE_WALL_CORNER)) {
                    length = i - 1;
                    width = length;
                    height = 3;
                    pX = cX;
                    pY = cY - 1;
                    if (length > getParam(MIN_CORRIDOR_SIZE)) canPlace = true;
                    break;
                }
            }
        } else if (offsetX == -1) {
            for (int i = 1; i < maxLength + 1; i++) {
                if (i == maxLength || cX - i <= 1 || (getTile(cX - i, cY) != TILE_EMPTY && getTile(cX - i, cY) != TILE_WALL && getTile(cX - i, cY) != TILE_WALL_CORNER)) {
                    length = i - 1;
                    width = length;
                    height = 3;
                    pX = cX - length + 1;
                    pY = cY - 1;
                    if (length > getParam(MIN_CORRIDOR_SIZE)) canPlace = true;
                    break;
                }
            }
        } else if (offsetY == 1) {
            for (int i = 1; i < maxLength + 1; i++) {
                if (i == maxLength || cY + i >= sizeY - 1 || (getTile(cX, cY + i) != TILE_EMPTY && getTile(cX, cY + i) != TILE_WALL && getTile(cX, cY + i) != TILE_WALL_CORNER)) {
                    length = i - 1;
                    width = 3;
                    height = length;
                    pX = cX - 1;
                    pY = cY;
                    if (length > getParam(MIN_CORRIDOR_SIZE)) canPlace = true;
                    break;
                }
            }
        } else if (offsetY == -1) {
            for (int i = 1; i < maxLength + 1; i++) {
                if (i == maxLength || cY - i <= 1 || (getTile(cX, cY - i) != TILE_EMPTY && getTile(cX, cY - i) != TILE_WALL && getTile(cX, cY - i) != TILE_WALL_CORNER)) {
                    length = i - 1;
                    width = 3;
                    height = length;
                    pX = cX - 1;
                    pY = cY - length + 1;
                    if (length > getParam(MIN_CORRIDOR_SIZE)) canPlace = true;
                    break;
                }
            }
        }

        // Placing the tiles
        if (canPlace) {
            newCorridor = new Room(mapRooms.size(), depth);
            newCorridor.setCorridor(true);

            for (int x = pX; x < pX + width; x++) {

                for (int y = pY; y < pY + height; y++) {

                    if (x == pX || x == pX + width - 1 || y == pY || y == pY + height - 1) {

                        // North-West
                        if (x == pX && y == pY) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newCorridor.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-West
                        else if (x == pX && y == pY + height - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newCorridor.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // South-East
                        else if (x == pX + width - 1 && y == pY + height - 1) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newCorridor.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // North-East
                        else if (x == pX + width - 1 && y == pY) {
                            setTile(x, y, TILE_WALL_CORNER);
                            newCorridor.addTile(x, y, TILE_WALL_CORNER);
                        }
                        // Wall
                        else if (getTile(x, y) == TILE_EMPTY) {
                            mapFreeWalls.add(new Cell(x, y));
                            setTile(x, y, TILE_WALL);
                            newCorridor.addTile(x, y, TILE_WALL);
                        }

                    } else {

                        if (getTile(x, y) == TILE_EMPTY) {
                            setTile(x, y, TILE_FLOOR_EDGE);
                            newCorridor.addTile(x, y, TILE_FLOOR_EDGE);
                            newCorridor.addEdgeTile(x, y, TILE_FLOOR_EDGE);
                        }

                    }

                }

            }

            mapRooms.add(newCorridor);
            roomCount++;
        }

        return newCorridor;
    }

    // A corridor is a 3 wide room
    public Room newRandomCorridor() {
        Room newCorridor = null;

        Cell theDoor = (Cell) chooseRandom(mapFreeWalls);

        int offsetX = 0;
        int offsetY = 0;
        int doorX = theDoor.getX(), doorY = theDoor.getY();
        // East
        if (getTile(doorX + 1, doorY) == TILE_EMPTY) {
            offsetX = 1;
        }
        // West
        else if (getTile(doorX - 1, doorY) == TILE_EMPTY) {
            offsetX = -1;
        }
        // North
        else if (getTile(doorX, doorY - 1) == TILE_EMPTY) {
            offsetY = -1;
        }
        // South
        else if (getTile(doorX, doorY + 1) == TILE_EMPTY) {
            offsetY = 1;
        }

        if (offsetX == 0 && offsetY == 0) {
            mapFreeWalls.remove(theDoor);
        } else {
            newCorridor = makeCorridor(theDoor.getX(), theDoor.getY(), offsetX, offsetY);
            if (newCorridor != null) {
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

        return newCorridor;
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
            case TILE_UPSTAIRS:
                return 24;
            case TILE_DOWNSTAIRS:
                return 32;
            default:
                return 0;
        }
    }

    // Return the dungeon map
    public void generateMapImage() {
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
                        double opacity = Math.floor(((double) theRoom.getDepth() / (getMaxDepth() + 1)) * 255);
                        g2.setColor(new Color(0, 0, 0, (int) opacity));
                        g2.fillRect(x * TILE_SIZE, y * TILE_SIZE, 16, 16);
                    }
                }

                // Room ids
                if (getParam(ROOM_ID_SWITCH) == 1 && getTile(x, y) != TILE_EMPTY) {
                    for (Room r : mapRooms) {
                        int index = r.hasTile(x, y);
                        if (index != -1) {
                            g2.drawString("" + r.getId(), x * TILE_SIZE, y * TILE_SIZE + 10);
                            break;
                        }
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
