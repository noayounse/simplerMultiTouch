package simplerMultiTouch.library;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import processing.core.PConstants;
import processing.event.MouseEvent;

/**
 * This class responds to mouse events by sending a corresponding TUIO message.
 * modified version
 * 
 * @author Erik Paluka
 * @author Zach Cook
 * @version 2.0
 */
public class MouseToTUIO {

	private Simulation sim;
	private SimplerMultiTouch smtParent = null;

	/** Currently selected cursor */
	Finger selectedCursor = null;

	/** Vector of the sticky cursors */
	Vector<Integer> stickyCursors = new Vector<Integer>();
	/**
	 * Vector of the joint cursors. <br/>
	 * These cursors are meant to move as a group.
	 */
	Vector<Integer> jointCursors = new Vector<Integer>();

	private int width;

	private int height;

	/**
	 * Sets up everything to get ready to begin converting mouse events to TUIO
	 * messages. Sets the screen to be full-screen if the boolean/flag is set to
	 * true.
	 * 
	 * @param width
	 *            width of the screen
	 * @param height
	 *            height of the screen
	 * @param port
	 *            port to connect to
	 */
	public MouseToTUIO(int width, int height, int port,
			SimplerMultiTouch smtParent) {
		super();
		sim = new Simulation(width, height, port, smtParent);
		this.width = width;
		this.height = height;
		System.out.println("connected mouse");
		this.smtParent = smtParent;
	} // end constructor

	/**
	 * Updates the selected cursor
	 * 
	 * @param me
	 *            MouseEvent - The mouse dragged event
	 */
	// public void mouseDragged( MouseEvent me){
	public void mouseDragged(int x, int y, int button) {

		// Point pt = new Point(me.getX(), me.getY());
		Point pt = new Point(x, y);

		if (selectedCursor != null) {
			if (sim.contains(pt)) {
				if (jointCursors.contains(selectedCursor.sessionID)) {
					Point selPoint = selectedCursor.getPosition();
					int dx = pt.x - selPoint.x;
					int dy = pt.y - selPoint.y;

					for (Iterator<Integer> jointIter = jointCursors.iterator(); jointIter
							.hasNext();) {
						int jointId = jointIter.next();
						if (jointId == selectedCursor.sessionID)
							continue;
						Finger joint_cursor = sim.getCursor(jointId);
						Point joint_point = joint_cursor.getPosition();
						if ((joint_point.x + dx) < 0
								|| (joint_point.x + dx) > (width - 1)
								|| (joint_point.y + dy) < 0
								|| (joint_point.y + dy) > (height - 1)) {
							// remove the joint cursor if it leaves the window
							joint_cursor.stop();
							sim.cursorMessage(joint_cursor);
							if (stickyCursors.contains(jointId))
								stickyCursors.removeElement(jointId);
							if (jointCursors.contains(jointId))
								jointIter.remove();
							sim.removeCursor(joint_cursor);
						} else {
							sim.updateCursor(joint_cursor, joint_point.x + dx,
									joint_point.y + dy);
						}
					}
					sim.updateCursor(selectedCursor, pt.x, pt.y);
					sim.allCursorMessage();
				} else {
					sim.updateCursor(selectedCursor, pt.x, pt.y);
					sim.cursorMessage(selectedCursor);
				}
			} else {
				selectedCursor.stop();
				sim.cursorMessage(selectedCursor);
				if (stickyCursors.contains(selectedCursor.sessionID))
					stickyCursors.removeElement(selectedCursor.sessionID);
				if (jointCursors.contains(selectedCursor.sessionID))
					jointCursors.removeElement(selectedCursor.sessionID);
				sim.removeCursor(selectedCursor);
				selectedCursor = null;
			}
		} else {
			if (sim.contains(pt)) {
				boolean isMouse = true;
				selectedCursor = sim.addCursor(pt.x, pt.y, isMouse);
				sim.cursorMessage(selectedCursor);
				// if (me.isShiftDown() || me.getButton() == PConstants.RIGHT)
				if (button == PConstants.RIGHT)
					stickyCursors.addElement(selectedCursor.sessionID);
			}
		}

	} // end mouseDragged

	/**
	 * Adds a cursor to the table state
	 * 
	 * @param me
	 *            MouseEvent - The mouse pressed event
	 */
	// public void mousePressed(MouseEvent me){
	public void mousePressed(int x, int y, int button) {
		// int x = me.getX();
		// int y = me.getY();

		Enumeration<Finger> cursorList = Simulation.cursorList.elements();
		while (cursorList.hasMoreElements()) {
			Finger cursor = cursorList.nextElement();
			Point point = cursor.getPosition();

			float touchRadius = 10f;

			if (point.distance(x, y) < touchRadius) {
				int selCur = -1;
				if (selectedCursor != null)
					selCur = selectedCursor.sessionID;

				// if ((me.isShiftDown() || me.getButton() == PConstants.RIGHT)
				if ((button == PConstants.RIGHT) && selCur != cursor.sessionID) {
					stickyCursors.removeElement(cursor.sessionID);
					if (jointCursors.contains(cursor.sessionID))
						jointCursors.removeElement(cursor.sessionID);
					sim.removeCursor(cursor);
					selectedCursor = null;
					return;
				}
				// else if (me.isControlDown() || me.getButton() ==
				// PConstants.CENTER){
				else if (button == PConstants.CENTER) {
					if (jointCursors.contains(cursor.sessionID))
						jointCursors.removeElement(cursor.sessionID);
					else
						jointCursors.addElement(cursor.sessionID);
					return;
				} else {
					selectedCursor = cursor;
					sim.updateCursor(selectedCursor, x, y);
					sim.cursorMessage(selectedCursor);
					return;
				}
			}
		}

		// if (me.isControlDown() || me.getButton() == PConstants.CENTER){
		if (button == PConstants.CENTER) {
			return;
		}

		if (sim.contains(new Point(x, y))) {
			boolean isMouse = true;
			selectedCursor = sim.addCursor(x, y, isMouse);
			sim.cursorMessage(selectedCursor);
			// if (me.isShiftDown() || me.getButton() == PConstants.RIGHT){
			if (button == PConstants.RIGHT) {
				stickyCursors.addElement(selectedCursor.sessionID);
			}
			return;
		}

		selectedCursor = null;
	} // end mousePressed

	/**
	 * Removes the cursor or makes it stationary if it is a sticky cursor
	 * 
	 * @param me
	 *            MouseEvent - The mouse released event
	 */
	// public void mouseReleased( MouseEvent me) {
	public void mouseReleased(int x, int y, int button) {
		if ((selectedCursor != null)) {
			if (!stickyCursors.contains(selectedCursor.sessionID)) {
				selectedCursor.stop();
				sim.cursorMessage(selectedCursor);
				if (jointCursors.contains(selectedCursor.sessionID))
					jointCursors.removeElement(selectedCursor.sessionID);
				sim.removeCursor(selectedCursor);
			} else {
				selectedCursor.stop();
				sim.cursorMessage(selectedCursor);
			}

			selectedCursor = null;
		}
	}

	/**
	 * Currently not used
	 * 
	 * @param me
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent me) {
	}

	/**
	 * Currently not used
	 * 
	 * @param me
	 *            MouseEvent
	 */
	public void mouseClicked(MouseEvent me) {
	}

	/**
	 * Currently not used
	 * 
	 * @param me
	 *            MouseEvent
	 */
	public void mouseEntered(MouseEvent me) {
	}

	/**
	 * Will release the Touch when the mouse exits the window
	 * 
	 * @param me
	 *            MouseEvent
	 */
	private void mouseExited(MouseEvent me) {
		// prevent touches getting stuck down by making them release on exit
		// this.mouseReleased(me);
		this.mouseReleased(me.getX(), me.getY(), me.getButton());
	}

	public void mouseEvent(MouseEvent event) {
		switch (event.getAction()) {
		case MouseEvent.PRESS:
			// mousePressed( event);
			mousePressed(event.getX(), event.getY(), event.getButton());
			break;
		case MouseEvent.RELEASE:
			// mouseReleased( event);
			mouseReleased(event.getX(), event.getY(), event.getButton());
			break;
		case MouseEvent.CLICK:
			mouseClicked(event);
			break;
		case MouseEvent.DRAG:
			// mouseDragged( event);
			mouseDragged(event.getX(), event.getY(), event.getButton());
			break;
		case MouseEvent.MOVE:
			mouseMoved(event);
			break;
		case MouseEvent.ENTER:
			mouseEntered(event);
			break;
		case MouseEvent.EXIT:
			mouseExited(event);
			break;
		}
	}

	/**
	 * Resets MouseToTUIO state, just making the already available functionality
	 * visible
	 */
	public void reset() {
		sim.reset();
		stickyCursors.clear();
		jointCursors.clear();
	}

	public Integer[] getJointCursors() {
		return jointCursors.toArray(new Integer[jointCursors.size()]);
	}
} // end class MouseToTUIO
