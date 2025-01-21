public class DFAState {
    private String name;
    private boolean isAccepting;
    private int x, y; // For GUI positioning

    public DFAState(String name, boolean isAccepting) {
        this.name = name;
        this.isAccepting = isAccepting;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setAccepting(boolean isAccepting) {
        this.isAccepting = isAccepting;
    }

    public String getName() { return name; }
    public boolean isAccepting() { return isAccepting; }
    public int getX() { return x; }
    public int getY() { return y; }
}

