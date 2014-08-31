package buzaan.procgen;

import java.util.Random;

/** 1/f terrain generation.
 * Generates terrain by applying one of the interpolated generators
 * at increasing levels of granularity and decreasing magnitudes.
 */
public class FractalTerrainGenerator implements IMapGenerator {
    private final int width;
    private final int height;
    private final int level;
    private final Random rnd = new Random();

    // Maximum tile height for lattice chosen here by experimentation.
    private final RandomTileValue randomValue = new RandomTileValue(0xaf);


    public FractalTerrainGenerator(int width, int height, int level) {
        if(width <= 0 || height <= 0 || level <= 0) {
            throw new IllegalArgumentException("Arguments must be positive.");
        }
        this.height = height;
        this.width = width;
        this.level = level;
    }

    private class RandomTileValue implements Map.Functor {
        private final int max;
        RandomTileValue(int max) {
            this.max = max;
        }
        @Override
        public int value(int x, int y) {
            return rnd.nextInt(max);
        }
    }

    private static int gcd(int a, int b) {
        if(a == 0) {
            return b;
        }
        return gcd(b, a & b);
    }
    /* TODO: would like to re-use interpolation methods from
    InterpolatedTerrainGenerator but they need modification to work in this
    context.
    */
    private void bicubic(final Map map,
            final int xsegs, final int ysegs,
            final double mag) {
        final int xspacing = map.getWidth() / xsegs;
        final int yspacing = map.getHeight() / ysegs;
        final Map lattice = new Map(
                (map.getWidth() / xspacing) + 1,
                (map.getHeight() / yspacing) + 1);
        lattice.apply(randomValue);

        // Apply bicubic interpolation at specified granularity & magnitude
        map.apply(new Map.Functor() {
            private double s(double x) {
                return 3 * Math.pow(x, 2) - 2 * Math.pow(x, 3);
            }

            @Override
            public int value(int x, int y) {
                int lx = x / xspacing;
                int ly = y / yspacing;
                int ix = x % xspacing;
                int iy = y % yspacing;
                int q00 = lattice.getTile(lx, ly);
                int q10 = lattice.getTile(lx + 1, ly);
                int q01 = lattice.getTile(lx, ly + 1);
                int q11 = lattice.getTile(lx + 1, ly + 1);
                double xp = (double)ix / xspacing;
                double yp = (double)iy / yspacing;
                double val = (q00 * s(1.0 - xp) * s(1.0 - yp)
                        + q10 * s(xp) * s(1.0 - yp)
                        + q01 * s(1.0 - xp) * s(yp)
                        + q11 * s(xp) * s(yp)) * mag;
                int v = map.getTile(x, y) + (int)val;
                return Integer.min(v, 0xff);
            }
        });
    }

    @Override
    public Map generate() {
        final Map map = new Map(width, height);
        for(int i = 1; i <= level; i *= 2) {
            bicubic(map, i, i, 1.0 / i);
        }

        //Convert tiles to greyscale
        map.apply(new Map.Functor() {
            @Override
            public int value(int x, int y) {
                int val = map.getTile(x, y) & 0xff;
                return (val << 16 | val << 8 | val);
            }
        });
        return map;
    }

}
