public class DFATransition {
    private DFAState from;
    private DFAState to;
    private char symbol;

    public DFATransition(DFAState from, DFAState to, char symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    public DFAState getFrom() { return from; }
    public DFAState getTo() { return to; }
    public char getSymbol() { return symbol; }
}

