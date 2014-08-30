package buzaan.procgen;

public class Map {
    private final int width;
    private final int height;
    private final int[] data;
    public static final int WALL = 0x000000;
    public static final int SPACE = 0xffffff;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        data = new int[width * height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTile(int x, int y) {
        return data[x + y * width];
    }

    public void setTile(int x, int y, int value) {
        data[x + y * width] = value;
    }

    public void fillRegion(int x, int y, int w, int h, int c) {
        for(int i = x; i <= x + w; i++) {
            for(int j = y; j <= y + h; j++) {
                setTile(i, j, c);
            }
        }
    }

    public static void copy(Map src, Map dest) {
        System.arraycopy(src.data, 0, dest.data, 0, src.data.length);
    }

    public static interface Functor {
        int value(int x, int y);
    }

    public void apply(Functor f) {
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                setTile(x, y, f.value(x, y));
            }
        }
    }
}
