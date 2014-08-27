package com.example.procgen;

import java.util.Random;

/**
 *
 * @author buzaan
 */
public class InterpolatedTerrainGenerator implements IMapGenerator {
    private final int mapWidth;
    private final int mapHeight;
    private final Map.Functor method;

    private static int tileIntensity(int val) {
        return (val << 16 | val << 8 | val);
    }

    public static class Bilinear implements Map.Functor {
        private final Map lattice;
        private final int ratio;
        private final Random rnd = new Random();

        Bilinear(int width, int height, int ratio) {
            this.ratio = ratio;
            lattice = new Map((width / ratio) + 1, (height / ratio) + 1);
            lattice.apply(new Map.Functor() {
                @Override
                public int value(int x, int y) {
                    return rnd.nextInt(0xff);
                }
            });
        }

        @Override
        public int value(int x, int y) {
            // The lattice x & y values will be the floor of our map point
            // divided by the ratio.
            int lx = x / ratio;
            int ly = y / ratio;

            // The points themselves, using quasi-array notation.
            int q00 = lattice.getTile(lx, ly);
            int q10 = lattice.getTile(lx + 1, ly);
            int q01 = lattice.getTile(lx, ly + 1);
            int q11 = lattice.getTile(lx + 1, ly + 1);

            // How far the location is in each region is given by the modulus
            // of the x & y values and the ratio. "Interior" x & y
            int ix = x % ratio;
            int iy = y % ratio;
            int sum = (q11 * ix * iy)
                    + (q01 * (ratio - ix) * iy)
                    + (q10 * ix * (ratio - iy))
                    + (q00 * (ratio - ix) * (ratio - iy));
            int out = (int)(sum / (ratio * ratio));
            return tileIntensity(out);
        }
    }

    public class Bicubic implements Map.Functor {
        @Override
        public int value(int x, int y) {
            return 0;
        }
    }

    InterpolatedTerrainGenerator(int width, int height, Map.Functor method) {
        mapWidth = width;
        mapHeight = height;
        this.method = method;
    }

    @Override
    public Map generate() {
        Map out = new Map(mapWidth, mapHeight);
        out.apply(method);
        return out;
    }

}
