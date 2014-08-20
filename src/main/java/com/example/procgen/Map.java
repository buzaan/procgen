
package com.example.procgen;

public class Map {
    private final int width;
    private final int height;
    private final int[][] data;
    public static final int WALL = 0x000000;
    public static final int SPACE = 0xffffff;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        data = new int[width][height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTile(int x, int y) {
        try {
            return data[x][y];
        } catch(ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

    public void setTile(int x, int y, int value) {
        try {
            data[x][y] = value;
        } catch(ArrayIndexOutOfBoundsException e) {

        }
    }

    public void fillRegion(int x, int y, int w, int h, int c) {
        for(int i = x; i <= x + w; i++) {
            for(int j = y; j <= y + h; j++) {
                setTile(i, j, c);
            }
        }
    }
}
