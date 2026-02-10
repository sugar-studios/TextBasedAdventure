import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleGrid {
    private final ArrayList<Point> grid;
    private final int width;
    private final int height;

    public BattleGrid() {
        this(4, 4);
    }

    public BattleGrid(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        this.width = width;
        this.height = height;
        this.grid = generateGrid(width, height);
    }

    private ArrayList<Point> generateGrid(int width, int height) {
        ArrayList<Point> g = new ArrayList<>(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                g.add(new Point(x, y));
            }
        }
        return g;
    }

    private int indexOf(int x, int y) {
        return y * width + x;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isBlocked(int x, int y) {
        Point p = getPointAt(x, y);
        return p == null || p.getIsWall();
    }

    public boolean isOccupied(int x, int y) {
        Point p = getPointAt(x, y);
        return p != null && p.getOccupant() != null;
    }

    public boolean isPlaceable(int x, int y) {
        Point p = getPointAt(x, y);
        return p != null && !p.getIsWall() && p.getOccupant() == null;
    }


    //Bresenham's line algorithm
    public boolean hasLineOfSight(Point p1, Point p2) {
        if (p1 == null || p2 == null) return false;

        int x0 = p1.getX();
        int y0 = p1.getY();
        int x1 = p2.getX();
        int y1 = p2.getY();

        if (!inBounds(x0, y0) || !inBounds(x1, y1)) return false;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;

        int err = dx - dy;

        int x = x0;
        int y = y0;

        while (true) {
            if (!(x == x0 && y == y0) && !(x == x1 && y == y1)) {
                Point cell = getPointAt(x, y);
                if (cell != null && cell.getIsWall()) return false;
            }

            if (x == x1 && y == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int initialRow = grid.get(0).getY();
        int firstX = grid.get(0).getX();

        for (int i = 0; i < grid.size(); i++) {
            if (grid.get(i).getY() > initialRow) {
                initialRow++;
                sb.append("\n");
            } else {
                if (grid.get(i).getX() != firstX) sb.append(", ");
            }

            Point p = grid.get(i);
            sb.append(String.format("(%d,%d)", p.getX(), p.getY()));
        }

        return sb.toString();
    }


    // Use chess radius strats
    public ArrayList<Point> getPointsInRadius(Point center, int r, boolean print) {
        ArrayList<Point> radius = new ArrayList<>();
        if (center == null || r < 0) return radius;

        int cx = center.getX();
        int cy = center.getY();

        for (Point unit : grid) {
            int dx = Math.abs(unit.getX() - cx);
            int dy = Math.abs(unit.getY() - cy);
            if (dx <= r && dy <= r) radius.add(unit);
        }

        if (print) {
            StringBuilder sb = new StringBuilder("Radius points: ");
            for (int i = 0; i < radius.size(); i++) {
                Point rp = radius.get(i);
                sb.append(String.format("(%d,%d%s)", rp.getX(), rp.getY(), rp.getIsWall() ? " W" : ""));
                if (i < radius.size() - 1) sb.append(", ");
            }
            System.out.println(sb.toString());
        }

        return radius;
    }

    
    public Point getPointAt(int x, int y) {
        if (!inBounds(x, y)) return null;
        return grid.get(indexOf(x, y));
    }

    public ArrayList<Point> getPointsInRadius(Point center, int r) { return getPointsInRadius(center, r, false); }

    public List<Point> getGrid() { return Collections.unmodifiableList(grid); }

    public int getHeight() { return height; }

    public int getWidth() { return width; }

    public void setWall(int x, int y) {
        if (!inBounds(x, y)) return;
            Point w = new Point(x, y, true);
        w.setLabel('#');
        grid.set(indexOf(x, y), w);
    }

    public void setEmpty(int x, int y) {
        if (!inBounds(x, y)) return;
        grid.set(indexOf(x, y), new Point(x, y));
    }
}
