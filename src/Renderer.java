class Renderer {
    private final BattleGrid grid;

    public Renderer(BattleGrid grid) {
        this.grid = grid;
    }

    public String drawGrid() {
        return drawGrid(false);
    }

    public String drawGrid(boolean printTrue) {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Point p = grid.getPointAt(x, y);

                char c = (p == null) ? '?' : (p.getIsWall() ? '#' : p.getLabel());

                //sb.append('[').append(c).append(']');
                sb.append(' ').append(c).append(' ');
            }
            if (y < grid.getHeight() - 1) sb.append('\n');
        }

        String result = sb.toString();

        if (printTrue) System.out.println(result);
        return result;
    }
}
