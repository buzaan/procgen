
package com.example.procgen;

public class Map {
    private final int width;
    private final int height;
    private final int[][] data;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        data = new int[width][height];
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    int getTile(int x, int y) throws IndexOutOfBoundsException {
        return data[x][y];
    }

    void setTile(int x, int y, int value) throws IndexOutOfBoundsException {
        data[x][y] = value;
    }
}
