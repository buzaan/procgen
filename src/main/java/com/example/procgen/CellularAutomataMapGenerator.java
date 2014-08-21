
package com.example.procgen;

/**
 * Generates a map using simple CA rules.
 * Uses a cellular automaton like Conway's game of life or Bryan's brain to
 * generate a cave-like map. The initial state is seeded by a RandomMapGenerator
 * and then a CA rule is applied to each cell for a number of generations.
 *
 * The generated map may be disjoint, but this is possibility is ignored for now.
 * @author buzaan
 */
public class CellularAutomataMapGenerator implements IMapGenerator {
    private final int mapWidth;
    private final int mapHeight;
    private static final int NUM_GENERATIONS = 2;
    private final Map tempMap;

    CellularAutomataMapGenerator(int width, int height) {
        mapWidth = width;
        mapHeight = height;
        tempMap = new Map(width, height);
    }

    int rule(Map m, int x, int y) {
        int wallNeighbors = 0;
        for(int xn = x - 1; xn <= x + 1; xn++) {
            for(int yn = y - 1; yn <= y + 1; yn++) {
                // Only process neighbors, not the tile in question
                if(!(xn == x && yn == y)) {
                    if(m.getTile(xn, yn) == Map.WALL) {
                        wallNeighbors++;
                    }
                }
            }
        }
        return wallNeighbors >= 5 ? Map.WALL : Map.SPACE;
    }

    void copyMap(Map src, Map dst) {
        for(int x = 0; x < src.getWidth(); x++){
            for(int y = 0; y < src.getHeight(); y++) {
                dst.setTile(x, y, src.getTile(x, y));
            }
        }
    }

    private void step(Map m) {
        for(int x = 1; x < m.getWidth() - 1; x++) {
            for(int y = 1; y < m.getHeight() - 1; y++) {
                tempMap.setTile(x, y, rule(m, x, y));
            }
        }
        Map.copy(tempMap, m);
    }

    @Override
    public Map generate() {
        RandomMapGenerator initialGen = new RandomMapGenerator(mapWidth, mapHeight);
        Map map = initialGen.generate();
        for(int i = 0; i < NUM_GENERATIONS; i++) {
            step(map);
        }
        return map;
    }

}
