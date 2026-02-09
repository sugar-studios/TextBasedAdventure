public class Point {
    private int x;
    private int y;
    private final boolean isWall;
    private Entity occupant;
    private char label;

    public Point(int x, int y) {
        this(x, y, false, null);
    }

    public Point(int x, int y, Entity o) {
        this(x, y, false, o);
    }

    public Point(int x, int y, boolean isWall) {
        this(x, y, isWall, null);
    }

    private Point(int x, int y, boolean isWall, Entity o) {
        this.x = x;
        this.y = y;
        this.isWall = isWall;
        preventWallEntityOverlap(o);
        this.label = '.';
    }

    private void preventWallEntityOverlap(Entity o) {
        if (isWall) {
            this.occupant = null;
        } else {
            this.occupant = o;
        }
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char c) {
        this.label = c;
    }

    public boolean getIsWall() {
        if (isWall) {
            this.label = '#';
        }
        return isWall;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Entity getOccupant() {
        return occupant;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setOccupant(Entity occupant) {
        preventWallEntityOverlap(occupant);
    }
}
