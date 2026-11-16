package dev.equo.swt.awt;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.e4.ui.workbench.renderers.swt.SashLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An island's AWT frame sits where the island is on screen, as native {@code SWT_AWT}'s embedded
 * frame does: an application that maps a Swing component's screen position back into SWT gets the
 * component's place in the island, not one short by the island's own screen position.
 */
@ExtendWith(Mocks.class)
class SwingIslandFrameLocationTest {

    /** Away from the origin on both axes, so a frame left at the origin cannot pass. */
    private static final int SCREEN_X = 1155;
    private static final int SCREEN_Y = 114;

    /** The bounds a part-stack holding an island, and its view, were measured at. */
    private static final int STACK_X = 768;
    private static final int STACK_W = 377;
    private static final int VIEW_W = 371;

    private final Shell shell = mock(Shell.class);
    private final Composite island = mock(Composite.class);
    private final Display display = Mocks.display();
    private final int[] at = {SCREEN_X, SCREEN_Y};

    SwingIslandFrameLocationTest() {
        when(island.isDisposed()).thenReturn(false);
        when(island.getParent()).thenReturn(shell);
        when(island.getDisplay()).thenReturn(display);
        when(island.getSize()).thenReturn(new Point(371, 1055));
        when(island.toDisplay(anyInt(), anyInt()))
                .thenAnswer(call -> new Point(at[0] + (int) call.getArgument(0), at[1] + (int) call.getArgument(1)));
    }

    private static Frame frameOf(int width) {
        Frame frame = mock(Frame.class);
        when(frame.getWidth()).thenReturn(width);
        return frame;
    }

    private static void drainEdt() throws Exception {
        java.awt.EventQueue.invokeAndWait(() -> { });
    }

    /** Runs what AWT posts to the display, in place, as the SWT thread would. */
    private void displayDelivers() {
        doAnswer(call -> {
            call.<Runnable>getArgument(0).run();
            return null;
        }).when(display).asyncExec(any());
    }

    /** The one listener {@code followIsland} put on {@code control} for {@code type}. */
    private static Listener listenerOn(Composite control, int type) {
        ArgumentCaptor<Listener> listener = ArgumentCaptor.forClass(Listener.class);
        verify(control).addListener(eq(type), listener.capture());
        return listener.getValue();
    }

    private static ComponentListener listenerOn(Frame frame) {
        ArgumentCaptor<ComponentListener> listener = ArgumentCaptor.forClass(ComponentListener.class);
        verify(frame).addComponentListener(listener.capture());
        return listener.getValue();
    }

    @Test
    void theFrameIsPutWhereTheIslandIs() throws Exception {
        Frame frame = frameOf(371);

        SwingIslandHost.followIsland(island, frame);
        drainEdt();

        verify(frame).setLocation(SCREEN_X, SCREEN_Y);
    }

    @Test
    void anAncestorMovingMovesTheFrame() throws Exception {
        // A sash moving the view moves the island on screen without a Move on the island itself.
        Frame frame = frameOf(371);
        SwingIslandHost.followIsland(island, frame);
        ArgumentCaptor<Listener> moved = ArgumentCaptor.forClass(Listener.class);
        verify(shell).addListener(eq(SWT.Move), moved.capture());

        at[0] = SCREEN_X - 200;
        moved.getValue().handleEvent(new Event());
        drainEdt();

        verify(frame).setLocation(SCREEN_X - 200, SCREEN_Y);
    }

    @Test
    void thePositionIsInSwingPixels() throws Exception {
        // The mirror gave the frame two Swing pixels per SWT unit: its position scales the same way.
        Frame frame = frameOf(742);

        SwingIslandHost.placeFrame(island, frame);
        drainEdt();

        verify(frame).setLocation(2 * SCREEN_X, 2 * SCREEN_Y);
    }

    @Test
    void aDisposedIslandIsLeftAlone() throws Exception {
        Frame frame = frameOf(371);
        when(island.isDisposed()).thenReturn(true);

        SwingIslandHost.placeFrame(island, frame);
        drainEdt();

        verify(frame, never()).setLocation(anyInt(), anyInt());
    }

    @Test
    void javaResizingTheFramePlacesItAgain() throws Exception {
        // The mirror's first layout gives the frame its size on AWT's thread, which is also when
        // the Swing pixels per SWT unit the position scales by stop being 1.
        Frame frame = frameOf(371);
        SwingIslandHost.followIsland(island, frame);
        drainEdt();
        displayDelivers();

        at[0] = SCREEN_X + 60;
        listenerOn(frame).componentResized(new ComponentEvent(frame, ComponentEvent.COMPONENT_RESIZED));
        drainEdt();

        verify(frame).setLocation(SCREEN_X + 60, SCREEN_Y);
    }

    @Test
    void aDisposedIslandLeavesNoListenerBehind() {
        // The parent composite keeps the frame under SWT_AWT.embeddedFrame for as long as it lives,
        // so a listener left on the frame keeps the disposed island and its tree reachable.
        Frame frame = frameOf(371);
        SwingIslandHost.followIsland(island, frame);
        Listener follower = listenerOn(island, SWT.Move);
        ComponentListener onFrame = listenerOn(frame);

        listenerOn(island, SWT.Dispose).handleEvent(new Event());

        verify(frame).removeComponentListener(onFrame);
        verify(island).removeListener(SWT.Move, follower);
        verify(island).removeListener(SWT.Resize, follower);
        verify(shell).removeListener(SWT.Move, follower);
    }

    @Test
    void anIslandGivenAnotherParentIsFollowedFromItsNewChain() throws Exception {
        Frame frame = frameOf(371);
        SwingIslandHost.followIsland(island, frame);
        Listener follower = listenerOn(island, SWT.Move);

        Composite adopted = mock(Composite.class);
        when(island.getParent()).thenReturn(adopted);
        // Control.setParent sends nothing of its own; the layout of the new parent that follows it
        // resizes the island, and the chain is walked again there.
        listenerOn(island, SWT.Resize).handleEvent(new Event());

        verify(shell).removeListener(SWT.Move, follower);
        verify(adopted).addListener(SWT.Move, follower);

        at[1] = SCREEN_Y + 40;
        follower.handleEvent(new Event());
        drainEdt();
        verify(frame).setLocation(SCREEN_X, SCREEN_Y + 40);
    }

    /**
     * An island in a part-stack of the workbench's main sash area, which the client paints as a
     * panel: inset by a gap and its content scaled into what is left
     * ({@link org.eclipse.swt.widgets.ControlToDisplayPanelInsetTest}). The island is then neither
     * at its view composite's unscaled position nor a fixed distance from its siblings, so the
     * walk, not an offset read off one control, is what the frame has to follow.
     */
    @Nested
    class InAPaintedPanel extends SerializeTestBase {

        private Composite view;
        private Composite islandInPanel;

        private void layOut() {
            Shell shell = Mocks.shell();
            Composite main = new Composite(shell, SWT.NONE);
            main.setLayout(new SashLayout());
            main.setBounds(0, 0, 1918, 1090);
            CTabFolder stack = new CTabFolder(main, SWT.NONE);
            stack.setBounds(STACK_X, 0, STACK_W, 1090);
            view = new Composite(stack, SWT.NONE);
            view.setBounds(3, 32, VIEW_W, 1055);
            islandInPanel = new Composite(view, SWT.NONE);
            islandInPanel.setBounds(0, 0, VIEW_W, 1055);
        }

        @Test
        void theFrameGoesWhereThePanelPaintsTheIsland() throws Exception {
            layOut();
            Frame frame = frameOf(VIEW_W);

            SwingIslandHost.placeFrame(islandInPanel, frame);
            drainEdt();

            Point painted = islandInPanel.toDisplay(0, 0);
            verify(frame).setLocation(painted.x, painted.y);
            assertThat(painted)
                    .as("a gap inside the stack's panel, and the view's own 3,32 scaled into it")
                    .isEqualTo(new Point(776, 37));
        }

        @Test
        void aSiblingIsNotAFixedNumberOfPixelsAway() {
            layOut();
            Composite sibling = new Composite(view, SWT.NONE);
            sibling.setBounds(336, 0, 25, 989);

            assertThat(sibling.toDisplay(0, 0).x - islandInPanel.toDisplay(0, 0).x)
                    .as("336 units of the view are painted scaled into the stack's panel, so a "
                            + "frame at the island's painted position is not at the sibling's less 336")
                    .isEqualTo(327);
        }
    }
}
