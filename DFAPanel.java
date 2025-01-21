import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class DFAPanel extends JPanel {
    private DFA dfa;
    private static final int STATE_RADIUS = 30;
    private static final int ARROW_SIZE = 10;
    private Map<Pair<DFAState, DFAState>, java.util.List<String>> transitionLabels;

    public DFAPanel() {
        setPreferredSize(new Dimension(800, 600));
        transitionLabels = new HashMap<>();
    }

    public void setDFA(DFA dfa) {
        this.dfa = dfa;
        updateTransitionLabels();
        positionStates();
        repaint();
    }

    private void updateTransitionLabels() {
        transitionLabels.clear();
        if (dfa == null) return;

        for (DFAState from : dfa.getStates()) {
            Map<Character, DFAState> stateTransitions = dfa.getTransitions().get(from);
            if (stateTransitions != null) {
                for (Map.Entry<Character, DFAState> transition : stateTransitions.entrySet()) {
                    DFAState to = transition.getValue();
                    Pair<DFAState, DFAState> transitionPair = new Pair<>(from, to);
                    transitionLabels.computeIfAbsent(transitionPair, k -> new ArrayList<>())
                            .add(String.valueOf(transition.getKey()));
                }
            }
        }
    }

    private void positionStates() {
        if (dfa == null || dfa.getStates().isEmpty()) return;

        java.util.List<DFAState> stateList = new ArrayList<>(dfa.getStates());
        int numStates = stateList.size();

        // Calculate center and radius
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - STATE_RADIUS - 50;

        // Position start state at the left
        DFAState startState = dfa.getStartState();
        if (startState != null) {
            startState.setPosition(centerX - radius, centerY);
            stateList.remove(startState);
        }

        // Position other states in a semi-circle
        int remainingStates = stateList.size();
        for (int i = 0; i < remainingStates; i++) {
            double angle = Math.PI * (i + 1) / (remainingStates + 1);
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY - (int)(radius * Math.sin(angle));
            stateList.get(i).setPosition(x, y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dfa == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw transitions first
        for (Map.Entry<Pair<DFAState, DFAState>, java.util.List<String>> entry : transitionLabels.entrySet()) {
            DFAState from = entry.getKey().first;
            DFAState to = entry.getKey().second;
            String label = String.join(",", entry.getValue());
            drawTransition(g2d, from, to, label);
        }

        // Draw states on top
        for (DFAState state : dfa.getStates()) {
            drawState(g2d, state, state == dfa.getStartState());
        }
    }

    private void drawState(Graphics2D g2d, DFAState state, boolean isStart) {
        int x = state.getX();
        int y = state.getY();

        // Draw outer circle
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - STATE_RADIUS, y - STATE_RADIUS, STATE_RADIUS * 2, STATE_RADIUS * 2);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x - STATE_RADIUS, y - STATE_RADIUS, STATE_RADIUS * 2, STATE_RADIUS * 2);

        // Draw accepting state (double circle)
        if (state.isAccepting()) {
            g2d.drawOval(x - STATE_RADIUS + 5, y - STATE_RADIUS + 5,
                    STATE_RADIUS * 2 - 10, STATE_RADIUS * 2 - 10);
        }

        // Draw start state indicator
        if (isStart) {
            int arrowX = x - STATE_RADIUS - 20;
            int arrowY = y;
            g2d.drawLine(arrowX, arrowY, x - STATE_RADIUS, y);
            g2d.fillPolygon(
                    new int[]{x - STATE_RADIUS, arrowX - 5, arrowX - 5},
                    new int[]{y, y - 5, y + 5},
                    3
            );
        }

        // Draw state name
        FontMetrics fm = g2d.getFontMetrics();
        String name = state.getName();
        int textX = x - fm.stringWidth(name) / 2;
        int textY = y + fm.getHeight() / 4;
        g2d.drawString(name, textX, textY);
    }

    private void drawTransition(Graphics2D g2d, DFAState from, DFAState to, String label) {
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();

        // Self-transition
        if (from == to) {
            drawSelfTransition(g2d, from, label);
            return;
        }

        // Calculate control point for curved line
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double normalX = -dy / distance;
        double normalY = dx / distance;

        // Curve the line more if there are transitions in both directions
        double curveFactor = 0.2;
        if (transitionLabels.containsKey(new Pair<>(to, from))) {
            curveFactor = 0.5;
        }

        int controlX = (int) ((x1 + x2) / 2 + normalX * distance * curveFactor);
        int controlY = (int) ((y1 + y2) / 2 + normalY * distance * curveFactor);

        // Calculate points where line meets circles
        double angle1 = Math.atan2(controlY - y1, controlX - x1);
        double angle2 = Math.atan2(y2 - controlY, x2 - controlX);

        int startX = (int) (x1 + STATE_RADIUS * Math.cos(angle1));
        int startY = (int) (y1 + STATE_RADIUS * Math.sin(angle1));
        int endX = (int) (x2 - STATE_RADIUS * Math.cos(angle2));
        int endY = (int) (y2 - STATE_RADIUS * Math.sin(angle2));

        // Draw curved arrow
        QuadCurve2D curve = new QuadCurve2D.Float(startX, startY, controlX, controlY, endX, endY);
        g2d.draw(curve);

        // Draw arrow head
        double arrowAngle = Math.atan2(endY - controlY, endX - controlX);
        drawArrowHead(g2d, endX, endY, arrowAngle);

        // Draw label with background
        Point labelPoint = getPointOnCurve(curve, 0.5);
        drawTransitionLabel(g2d, label, labelPoint.x, labelPoint.y);
    }

    private void drawSelfTransition(Graphics2D g2d, DFAState state, String label) {
        int x = state.getX();
        int y = state.getY();
        int radius = STATE_RADIUS + 20;

        // Draw loop above the state
        g2d.drawOval(x - radius/2, y - radius - STATE_RADIUS/2, radius, radius);

        // Draw arrow head
        double angle = -Math.PI/6;
        int arrowX = x + radius/4;
        int arrowY = y - radius - STATE_RADIUS/2;
        drawArrowHead(g2d, arrowX, arrowY, angle);

        // Draw label above the loop
        drawTransitionLabel(g2d, label, x, y - radius - STATE_RADIUS - 10);
    }

    private void drawArrowHead(Graphics2D g2d, int x, int y, double angle) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        xPoints[0] = x;
        yPoints[0] = y;
        xPoints[1] = (int) (x - ARROW_SIZE * Math.cos(angle - Math.PI/6));
        yPoints[1] = (int) (y - ARROW_SIZE * Math.sin(angle - Math.PI/6));
        xPoints[2] = (int) (x - ARROW_SIZE * Math.cos(angle + Math.PI/6));
        yPoints[2] = (int) (y - ARROW_SIZE * Math.sin(angle + Math.PI/6));
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawTransitionLabel(Graphics2D g2d, String label, int x, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(label);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - width/2 - 2, y - fm.getHeight()/2, width + 4, fm.getHeight());
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x - width/2, y + fm.getHeight()/3);
    }

    private Point getPointOnCurve(QuadCurve2D curve, double t) {
        double x = Math.pow(1-t, 2) * curve.getX1() +
                2 * (1-t) * t * curve.getCtrlX() +
                Math.pow(t, 2) * curve.getX2();
        double y = Math.pow(1-t, 2) * curve.getY1() +
                2 * (1-t) * t * curve.getCtrlY() +
                Math.pow(t, 2) * curve.getY2();
        return new Point((int)x, (int)y);
    }

    private static class Pair<T, U> {
        final T first;
        final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair<?, ?> p = (Pair<?, ?>) o;
            return Objects.equals(p.first, first) && Objects.equals(p.second, second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}

