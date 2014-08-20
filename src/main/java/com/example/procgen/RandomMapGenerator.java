package com.example.procgen;

import java.util.Random;

/**
 *
 * @author buzaan
 */
public class RandomMapGenerator implements IMapGenerator {
    private final Random rnd = new Random();
    private final int xSize;
    private final int ySize;

    RandomMapGenerator(int x, int y) {
        xSize = x;
        ySize = y;
    }

    @Override
    public Map generate() {
        Map out = new Map(xSize, ySize);
        for(int x = 0; x < xSize; x++) {
            for(int y = 0; y < ySize; y++) {
                out.setTile(x, y, rnd.nextBoolean() ? Map.SPACE : Map.WALL);
            }
        }
        return out;
    }
}
