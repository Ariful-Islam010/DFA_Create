import java.util.*;

public class DFA {
    private Set<DFAState> states;
    private Set<Character> alphabet;
    private DFAState startState;
    private Set<DFAState> acceptStates;
    private Map<DFAState, Map<Character, DFAState>> transitions;

    public DFA() {
        states = new HashSet<>();
        alphabet = new HashSet<>();
        acceptStates = new HashSet<>();
        transitions = new HashMap<>();
    }

    public static DFA fromFormalDescription(String description) {
        DFA dfa = new DFA();
        Map<String, DFAState> stateMap = new HashMap<>();

        String[] lines = description.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("States:")) {
                String[] stateNames = line.substring(7).trim().split(",\\s*");
                for (String stateName : stateNames) {
                    stateName = stateName.trim();
                    if (!stateName.isEmpty()) {
                        DFAState state = new DFAState(stateName, false);
                        dfa.addState(state);
                        stateMap.put(stateName.toLowerCase(), state);
                    }
                }
            } else if (line.startsWith("Alphabet:")) {
                String[] symbols = line.substring(9).trim().split(",\\s*");
                for (String symbol : symbols) {
                    if (!symbol.trim().isEmpty()) {
                        dfa.alphabet.add(symbol.trim().charAt(0));
                    }
                }
            } else if (line.startsWith("Start state:")) {
                String startStateName = line.substring(12).trim();
                DFAState startState = stateMap.get(startStateName.toLowerCase());
                if (startState != null) {
                    dfa.setStartState(startState);
                }
            } else if (line.startsWith("Accept states:")) {
                String[] acceptStateNames = line.substring(14).trim().split(",\\s*");
                for (String stateName : acceptStateNames) {
                    stateName = stateName.trim();
                    DFAState acceptState = stateMap.get(stateName.toLowerCase());
                    if (acceptState != null) {
                        dfa.addAcceptState(acceptState);
                    }
                }
            } else if (line.contains("->")) {
                String[] parts = line.split("->");
                if (parts.length == 2) {
                    String[] fromParts = parts[0].trim().split(",");
                    if (fromParts.length == 2) {
                        String fromState = fromParts[0].trim().toLowerCase();
                        String symbolStr = fromParts[1].trim();
                        String toState = parts[1].trim().toLowerCase();

                        DFAState from = stateMap.get(fromState);
                        DFAState to = stateMap.get(toState);

                        if (from != null && to != null && !symbolStr.isEmpty()) {
                            char symbol = symbolStr.charAt(0);
                            dfa.addTransition(from, to, symbol);
                        }
                    }
                }
            }
        }

        return dfa;
    }

    public void addState(DFAState state) {
        states.add(state);
        transitions.put(state, new HashMap<>());
    }

    public void setStartState(DFAState state) {
        if (states.contains(state)) {
            startState = state;
        }
    }

    public void addAcceptState(DFAState state) {
        if (states.contains(state)) {
            acceptStates.add(state);
            state.setAccepting(true);
        }
    }

    public void addTransition(DFAState from, DFAState to, char symbol) {
        if (states.contains(from) && states.contains(to)) {
            alphabet.add(symbol);
            transitions.get(from).put(symbol, to);
        }
    }

    public boolean accepts(String input) {
        if (startState == null) return false;

        DFAState current = startState;
        for (char c : input.toCharArray()) {
            if (!alphabet.contains(c)) return false;
            Map<Character, DFAState> stateTransitions = transitions.get(current);
            if (stateTransitions == null) return false;
            current = stateTransitions.get(c);
            if (current == null) return false;
        }
        return acceptStates.contains(current);
    }

    public Set<DFAState> getStates() { return states; }
    public Set<Character> getAlphabet() { return alphabet; }
    public DFAState getStartState() { return startState; }
    public Set<DFAState> getAcceptStates() { return acceptStates; }
    public Map<DFAState, Map<Character, DFAState>> getTransitions() { return transitions; }
}

