package dev.equo.swt.awt;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A heavyweight's native peer tells it when the pointer enters and leaves it; the embedding has no
 * peers, so moving between heavyweights has to raise those events itself. Applications act on them:
 * a 3D view only applies its tool's cursor once its canvas has seen the pointer enter.
 */
class HeavyweightCrossingTest {

    private final List<String> events = new ArrayList<>();
    private final JPanel root = new JPanel(null);
    private final Canvas left = canvas("left", 0, 0, 100, 100);
    private final Canvas right = canvas("right", 100, 0, 100, 100);
    private final Component[] under = {null};

    private Canvas canvas(String name, int x, int y, int w, int h) {
        Canvas c = new Canvas();
        c.setBounds(x, y, w, h);
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                events.add("enter " + name + " " + e.getX() + "," + e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                events.add("exit " + name + " " + e.getX() + "," + e.getY());
            }
        });
        root.add(c);
        return c;
    }

    private void moveTo(Component now, int x, int y) {
        AwtInput.crossHeavyweights(root, under, now, x, y, 0, 0);
    }

    @Test
    void movingOntoAHeavyweightEntersItInItsOwnCoordinates() {
        moveTo(right, 130, 20);

        assertEquals(List.of("enter right 30,20"), events);
    }

    @Test
    void movingFromOneHeavyweightToAnotherExitsTheFirstAndEntersTheSecond() {
        moveTo(left, 50, 50);
        moveTo(right, 110, 50);

        assertEquals(List.of("enter left 50,50", "exit left 110,50", "enter right 10,50"), events);
    }

    @Test
    void movingWithinOneHeavyweightRaisesNothingMore() {
        moveTo(left, 50, 50);
        moveTo(left, 60, 55);

        assertEquals(List.of("enter left 50,50"), events);
    }

    @Test
    void leavingTheCanvasExitsTheHeavyweightUnderThePointer() {
        moveTo(left, 50, 50);
        moveTo(null, -1, 50);

        assertEquals(List.of("enter left 50,50", "exit left -1,50"), events);
    }
}
