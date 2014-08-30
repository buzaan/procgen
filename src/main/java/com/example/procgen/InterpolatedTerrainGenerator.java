package com.example.procgen;

import java.util.Random;

/**
 *
 * @author buzaan
 */
public class InterpolatedTerrainGenerator implements IMapGenerator {
    private final int mapWidth;
    private final int mapHeight;
    private final InterpolationMethod method;

    private static int tileIntensity(int val) {
        return (val << 16 | val << 8 | val);
    }

    // This whole bit feels overdesigned...
    public interface Builder {
        void setWidth(int w);
        void setHeight(int h);
        InterpolationMethod build();
    }

    private static abstract class AbstractBuilder implements Builder {
        // 0 used as invalid dimension
        protected int width = 0;
        protected int height = 0;
        protected final int spacing;

        public AbstractBuilder(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public InterpolationMethod build() {
            if(width == 0 || height == 0) {
                throw new IllegalStateException("Dimensions not specified.");
            }
            return create();
        }

        @Override
        public void setWidth(int w) {
            if(w < 0) {
                throw new IllegalStateException("Negative width specified.");
            }
            width = w;
        }

        @Override
        public void setHeight(int h) {
            if(h < 0) {
                throw new IllegalStateException("Negative height specified.");
            }
            height = h;
        }

        protected abstract InterpolationMethod create();
    }

    public static class BicubicBuilder extends AbstractBuilder {
        public BicubicBuilder(int spacing) {
            super(spacing);
        }

        @Override
        protected InterpolationMethod create() {
            return new Bicubic(width, height, spacing);
        }
    }

    public static class BilinearBuilder extends AbstractBuilder {
        public BilinearBuilder(int spacing) {
            super(spacing);
        }

        @Override
        protected InterpolationMethod create() {
            return new Bilinear(width, height, spacing);
        }
    }

    public static class GradientBuilder extends AbstractBuilder {
        public GradientBuilder(int spacing) {
            super(spacing);
        }

        @Override
        protected InterpolationMethod create() {
            return new Gradient(width, height, spacing);
        }
    }

    public static Builder Bilinear(int spacing) {
        return new BilinearBuilder(spacing);
    }

    public static Builder Bicubic(int spacing) {
        return new BicubicBuilder(spacing);
    }

    public static Builder Gradient(int spacing) {
        return new GradientBuilder(spacing);
    }

    public interface InterpolationMethod extends Map.Functor {
        void randomizeLattice();
    }

    private static abstract class AbstractMethod implements InterpolationMethod {
        protected final Map lattice;
        protected final int spacing;
        protected final Random rnd = new Random();

        public AbstractMethod(int width, int height, int spacing) {
            lattice = new Map((width / spacing) + 1, (height / spacing) + 1);
            this.spacing = spacing;
        }

        @Override
        public void randomizeLattice() {
            lattice.apply(new Map.Functor() {
                @Override
                public int value(int x, int y) {
                    return rnd.nextInt(0xff);
                }
            });
        }
    }

    public static class Bilinear extends AbstractMethod {
        Bilinear(int width, int height, int spacing) {
            super(width, height, spacing);
        }

        @Override
        public int value(int x, int y) {
            // The lattice x & y values will be the floor of our map point
            // divided by the spacing.
            int lx = x / spacing;
            int ly = y / spacing;

            // The points themselves, using quasi-array notation.
            int q00 = lattice.getTile(lx, ly);
            int q10 = lattice.getTile(lx + 1, ly);
            int q01 = lattice.getTile(lx, ly + 1);
            int q11 = lattice.getTile(lx + 1, ly + 1);

            // How far the location is in each region is given by the modulus
            // of the x & y values and the spacing. "Interior" x & y
            int ix = x % spacing;
            int iy = y % spacing;
            int sum = (q11 * ix * iy)
                    + (q01 * (spacing - ix) * iy)
                    + (q10 * ix * (spacing - iy))
                    + (q00 * (spacing - ix) * (spacing - iy));
            int out = (int)(sum / (spacing * spacing));
            return tileIntensity(out);
        }
    }

    public static class Bicubic extends AbstractMethod {
        public Bicubic(int width, int height, int spacing) {
            super(width, height, spacing);
        }

        private double s(double x) {
            // pcgbook mentions this function is a common choice
            return (-2 * Math.pow(x, 3) + 3 * Math.pow(x, 2));
        }

        @Override
        public int value(int x, int y) {
            int lx = x / spacing;
            int ly = y / spacing;

            int ix = x % spacing;
            int iy = y % spacing;

            int q00 = lattice.getTile(lx, ly);
            int q10 = lattice.getTile(lx + 1, ly);
            int q01 = lattice.getTile(lx, ly + 1);
            int q11 = lattice.getTile(lx + 1, ly + 1);

            double px = (double)ix / spacing;
            double py = (double)iy / spacing;
            double v = ( q00 * s(1.0 - px) * s(1.0 - py)
                    + q10 * s(px) * s(1.0 - py)
                    + q01 * s(1.0 - px) * s(py)
                    + q11 * s(px) * s(py));
            return tileIntensity((int)v);
        }
    }

    public static class Gradient implements InterpolationMethod {
        // xs and ys stores the x & y slope at each point in the lattice.
        private final int[] xs;
        private final int[] ys;
        private final int lWidth;
        private final int spacing;
        private final Random rnd = new Random();

        public Gradient(int width, int height, int spacing) {
            int x = (width / spacing) + 1;
            int y = (height / spacing) + 1;
            lWidth = y;
            xs = new int[x * y];
            ys = new int[x * y];
            this.spacing = spacing;
        }

        @Override
        public void randomizeLattice() {
            for(int i = 0; i < xs.length; i++) {
                // Initializes each component with a random value between
                // [-spacing, spacing)
                xs[i] = (rnd.nextInt(spacing * 2) - spacing);
                ys[i] = (rnd.nextInt(spacing * 2) - spacing);
            }
        }

        private static double s(double x) {
            return (-2 * Math.pow(x, 3) + 3 * Math.pow(x, 2));
        }

        private int dotProduct(int lx, int ly, int vx, int vy) {
            int x = xs[lx + ly * lWidth];
            int y = ys[lx + ly * lWidth];
            return vx * x + vy * y;
        }

        @Override
        public int value(int x, int y) {
            int lx = x / spacing;
            int ly = y / spacing;
            int ix = x % spacing;
            int iy = y % spacing;

            int q00 = dotProduct(lx, ly, ix, iy);
            int q10 = dotProduct(lx + 1, ly, (spacing - ix), iy);
            int q01 = dotProduct(lx, ly + 1, ix, (spacing - iy));
            int q11 = dotProduct(lx + 1, ly + 1, (spacing - ix), (spacing - iy));
            // Perform bicubic interpolation on these points
            double px = (double)ix / spacing;
            double py = (double)iy / spacing;
            double v = ( q00 * s(1.0 - px) * s(1.0 - py)
                    + q10 * s(px) * s(1.0 - py)
                    + q01 * s(1.0 - px) * s(py)
                    + q11 * s(px) * s(py));

            // Since we may have negative heights due to the negative slopes,
            //terrain defaults to being in the middle of the domain we use.
            return tileIntensity((0xff / 2) + (int)v);
        }
    }

    InterpolatedTerrainGenerator(int width, int height, Builder methodBuilder) {
        mapWidth = width;
        mapHeight = height;
        methodBuilder.setWidth(width);
        methodBuilder.setHeight(height);
        method = methodBuilder.build();
    }

    @Override
    public Map generate() {
        Map out = new Map(mapWidth, mapHeight);
        method.randomizeLattice();
        try {
            out.apply(method);
        } catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("Baddness");
        }
        return out;
    }

}
