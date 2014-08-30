package buzaan.procgen;

import java.util.Random;

/**
 * Generates a map by recursively subdividing  the space.
 * This map generation strategy recursively divides the map space into two
 * segments. Once a minimum threshold is reached it places a room in that area
 * and then connects adjacent regions with a hallway.
 *
 * Tweaking the way it divides the space (currently is splits it right down the
 * middle), the shape of the room generated, and the way hallways are created
 * could make for more interesting maps.
 * @author buzaan
 */
public class BinaryPartitionMapGenerator implements IMapGenerator {

    private final int mapWidth;
    private final int mapHeight;
    private final int MIN_PARTITION_SIZE = 20;
    private final int MIN_ROOM_SIZE = 4;
    private final int PADDING = 2;
    private final Random rnd = new Random();

    BinaryPartitionMapGenerator(int width, int height) {
        mapWidth = width;
        mapHeight = height;
        rnd.setSeed(0);
    }

    private int nextIntBetween(int min, int max) {
        return rnd.nextInt(max - min) + min;
    }

    /**
     * @returns bounds of room generated.
     */
    private Rect genRoom(Map m, int x1, int y1, int x2, int y2) {
        int w = nextIntBetween(MIN_ROOM_SIZE, x2 - x1);
        int h = nextIntBetween(MIN_ROOM_SIZE, y2 - y1);
        int x = nextIntBetween(x1, x2 - w);
        int y = nextIntBetween(y1, y2 - h);
        m.fillRegion(x, y, w, h, Map.SPACE);
        return new Rect(x, y, x + w, y + h);
    }

    /**
     * Joins two segments split vertically with a horizontal hall. Starts on the
     * left segment and digs a hallway until it reaches an empty spot in the
     * right.
     *
     * @return Bounds of the joined segments.
     */
    private Rect joinV(Map m, Rect left, Rect right) {
        int midX = (left.x2 + right.x1) / 2;
        int leftY = (left.y2 + left.y1) / 2;
        int rightY = (right.y2 + right.y1) / 2;

        // Dig out a hall from the left...
        for (int x = left.x2 + 1; x <= midX; x++) {
            m.setTile(x, leftY, Map.SPACE);
        }

        for (int x = left.x2; x > 0 && m.getTile(x, leftY) == Map.WALL; x--) {
            m.setTile(x, leftY, Map.SPACE);
        }

        // ... and right...
        for (int x = right.x1 - 1; x >= midX; x--) {
            m.setTile(x, rightY, Map.SPACE);
        }
        for (int x = right.x1; x < m.getWidth() && m.getTile(x, rightY) == Map.WALL; x++) {
            m.setTile(x, rightY, Map.SPACE);
        }

        if(leftY != rightY) {
            // ... and then connect the two.
            int dir = leftY < rightY ? 1 : -1;
            for(int y = leftY; y != rightY + dir; y += dir) {
                m.setTile(midX, y, Map.SPACE);
            }
        }
        return Rect.enclosing(left, right);
    }

    private Rect joinH(Map m, Rect top, Rect bot) {
        int midY = (top.y2 + bot.y1) / 2;
        int topX = (top.x1 + top.x2) / 2;
        int botX = (bot.x1 + bot.x2) / 2;

        for(int y = top.y2 + 1; y <= midY; y++) {
            m.setTile(topX, y, Map.SPACE);
        }
        for(int y = top.y2; y > 0 && m.getTile(topX, y) == Map.WALL; y--) {
            m.setTile(topX, y, Map.SPACE);
        }

        for(int y = bot.y1 - 1; y >= midY; y--) {
            m.setTile(botX, y, Map.SPACE);
        }
        for(int y = bot.y1; y < m.getHeight() &&  m.getTile(botX, y) == Map.WALL; y++) {
            m.setTile(botX, y, Map.SPACE);
        }

        if(topX != botX) {
            int dir = topX < botX ? 1 : -1;
            for(int x = topX; x != botX + dir; x += dir) {
                m.setTile(x, midY, Map.SPACE);
            }
        }
        return Rect.enclosing(top, bot);
    }

    /**
     * @returns bounds of valid area in each partition.
     */
    private Rect partition(Map m, int x, int y, int w, int h) {
        if (w < MIN_PARTITION_SIZE || h < MIN_PARTITION_SIZE) {
            return genRoom(m,
                    x + PADDING,
                    y + PADDING,
                    x + w - PADDING,
                    y + h - PADDING);
        } else {
            boolean splitVert = w == h ? rnd.nextBoolean() : w > h;
            if (splitVert) {
                // Partition vertically
                int halfw = w / 2;
                Rect left = partition(m, x, y, halfw, h);
                Rect right = partition(m, x + halfw, y, halfw, h);
                return joinV(m, left, right);
            } else {
                // ... horizontally
                int halfh = h / 2;
                Rect top = partition(m, x, y, w, halfh);
                Rect bot = partition(m, x, y + halfh, w, halfh);
                return joinH(m, top, bot);
            }
        }
    }

    @Override
    public Map generate() {
        Map map = new Map(mapWidth, mapHeight);
        partition(map, 1, 1, map.getWidth() - 1, map.getHeight() - 1);
        return map;
    }
}
