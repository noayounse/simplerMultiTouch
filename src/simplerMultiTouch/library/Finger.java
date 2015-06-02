package simplerMultiTouch.library;

import java.awt.Point;
import java.util.Vector;

/**
 * This class holds touch event information associated with the same session ID
 * (e.g.all the information associated with someone placing their finger on the
 * screen, dragging it, then removing it.)
 * 
 * @author Erik Paluka
 * @version 1.0
 */
public class Finger {
	/** Touch Cursor's Session ID */
	protected int sessionID;
	/** Touch Cursor's speed in the x-direction */
	protected float xSpeed;
	/** Touch Cursor's speed in the y-direction */
	protected float ySpeed;
	/** Touch Cursor's speed */
	protected float mSpeed;
	/** Touch Cursor's acceleration */
	protected float mAccel;
	/** Last update time */
	protected long lastTime;
	/** Path of the touch cursor */
	private Vector<Point> path;
	/** ID used by a touch for track keeping in Android */
	protected int touchID = -1;

	public boolean isMouse = false; // whether or not it is a mouse

	/**
	 * Touch Constructor, creates a touch cursor with the given session ID, and
	 * coordinates.
	 * 
	 * @param sID
	 *            Session ID
	 * @param xPos
	 *            x-coordinate
	 * @param yPos
	 *            y-coordinate
	 */
	public Finger(int sID, int xPos, int yPos, boolean isMouse) {
		this.sessionID = sID;
		path = new Vector<Point>();
		path.addElement(new Point(xPos, yPos));
		this.xSpeed = 0.0f;
		this.ySpeed = 0.0f;
		this.mAccel = 0.0f;
		lastTime = System.currentTimeMillis();
		this.isMouse = isMouse;
	}

	/**
	 * Updates the touch cursor with the new coordinates
	 * 
	 * @param xPos
	 *            New x-coordinate
	 * @param yPos
	 *            New y-coordinate
	 * @param windowWidth
	 *            Window width
	 * @param windowHeight
	 *            Window height
	 */
	public final void update(int xpos, int ypos, int windowWidth,
			int windowHeight) {
		Point lastPoint = getPosition();
		path.addElement(new Point(xpos, ypos));

		// time difference in seconds
		long currentTime = System.currentTimeMillis();
		float dt = (currentTime - lastTime) / 1000.0f;

		if (dt > 0) {
			float dx = (xpos - lastPoint.x) / (float) windowWidth;
			float dy = (ypos - lastPoint.y) / (float) windowHeight;
			float dist = (float) Math.sqrt(dx * dx + dy * dy);
			float newSpeed = dist / dt;
			this.xSpeed = dx / dt;
			this.ySpeed = dy / dt;
			this.mAccel = (newSpeed - mSpeed) / dt;
			this.mSpeed = newSpeed;
		}
		lastTime = currentTime;
	}

	/**
	 * Resets the touch cursors speed, acceleration and time of last update
	 */
	public final void stop() {
		lastTime = System.currentTimeMillis();
		this.xSpeed = 0.0f;
		this.ySpeed = 0.0f;
		this.mAccel = 0.0f;
		this.mSpeed = 0.0f;
	}

	/**
	 * Returns the current position of the touch cursor
	 * 
	 * @return path.lastElement() Point
	 */
	public final Point getPosition() {
		return path.lastElement();
	}

	/**
	 * Returns the path of the touch cursor
	 * 
	 * @return path Vector<Point>
	 */
	public final Vector<Point> getPath() {
		return path;
	}

	/**
	 * Sets the touch ID for Android touches
	 * 
	 * @param tID
	 *            ID of the Android touch
	 */
	public final void setTouchId(int tID) {
		touchID = tID;
	}

	/**
	 * Returns the touch ID of the associated Android touch
	 * 
	 * @return touchID
	 */
	public final int getTouchId() {
		return touchID;
	}
}
