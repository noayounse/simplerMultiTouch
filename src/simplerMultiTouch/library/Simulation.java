package simplerMultiTouch.library;

import java.awt.Point;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class is the parent class of MouseToTUIO etc to allow easy formation of
 * TUIO messages in response to events.
 * 
 * 
 * @author Erik Paluka
 * @author Zach Cook
 * @version 2.0
 */
public class Simulation {

	/** Default host. (Implements TUIO: host "127.0.0.1") */
	public static String host = "127.0.0.1";

	/** OSC Port */
	private OSCPortOut oscPort;

	/** Current fame */
	private int currentFrame = 0;
	/** Last used session ID */
	private int sessionID = -1;
	/** Width of the PApplet */
	int windowWidth;
	/** Height of the PApplet */
	int windowHeight;

	/** Hash table of touch cursors with their session IDs as the key */
	protected static Hashtable<Integer, Finger> cursorList = new Hashtable<Integer, Finger>();

	private SimplerMultiTouch smtParent = null;

	/**
	 * Constructor, resets the simulator and create a table the size of the
	 * PApplet
	 * 
	 * @param width
	 *            int - width of the screen
	 * @param height
	 *            int - height of the screen
	 * @param port
	 *            int - port to connect to
	 */
	public Simulation(int width, int height, int port,
			SimplerMultiTouch smtParent) {
		super();

		this.windowWidth = width;
		this.windowHeight = height;
		this.smtParent = smtParent;

		try {
			oscPort = new OSCPortOut(java.net.InetAddress.getByName(host), port);
		} catch (SocketException exception) {
			oscPort = null;
			exception.printStackTrace();
		} catch (UnknownHostException exception) {
			oscPort = null;
			exception.printStackTrace();
		}

		reset();
	}

	/**
	 * Sends the given OSCPacket
	 * 
	 * @param packet
	 *            OSCPacket
	 */
	private void sendOSC(OSCPacket packet) {
		try {
			oscPort.send(packet);
		} catch (java.io.IOException exception) {
			exception.printStackTrace();
		}
	}

	private OSCMessage aliveMessage() {
		OSCMessage aliveMessage = new OSCMessage("/tuio/2Dcur");
		aliveMessage.addArgument("alive");
		Enumeration<Finger> cursorList = Simulation.cursorList.elements();
		while (cursorList.hasMoreElements()) {
			aliveMessage.addArgument(cursorList.nextElement().sessionID);
		}
		return aliveMessage;
	}

	private OSCMessage setMessage(Finger cursor) {
		Point point = cursor.getPosition();
		float xpos = (point.x + (cursor.isMouse ? smtParent.frameLocation.x : 0)) / (float) windowWidth;
		float ypos = (point.y + (cursor.isMouse ? smtParent.frameLocation.y : 0)) / (float) windowHeight;
		OSCMessage setMessage = new OSCMessage("/tuio/2Dcur");
		setMessage.addArgument("set");
		setMessage.addArgument(cursor.sessionID);
		setMessage.addArgument(xpos);
		setMessage.addArgument(ypos);
		setMessage.addArgument(cursor.xSpeed);
		setMessage.addArgument(cursor.ySpeed);
		setMessage.addArgument(cursor.mAccel);
		return setMessage;
	}

	private OSCMessage frameMessage(int currentFrame) {
		OSCMessage frameMessage = new OSCMessage("/tuio/2Dcur");
		frameMessage.addArgument("fseq");
		frameMessage.addArgument(currentFrame);
		return frameMessage;
	}

	/**
	 * Deletes a cursor by sending a list of alive cursors without it
	 */
	private void cursorDelete() {
		OSCBundle cursorBundle = new OSCBundle();
		cursorBundle.addPacket(aliveMessage());
		cursorBundle.addPacket(frameMessage(++currentFrame));

		sendOSC(cursorBundle);
	}

	/**
	 * Sends a single cursor message
	 */
	protected void cursorMessage(Finger cursor) {
		if (cursor == null)
			return;

		OSCBundle cursorBundle = new OSCBundle();
		cursorBundle.addPacket(aliveMessage());
		cursorBundle.addPacket(setMessage(cursor));
		cursorBundle.addPacket(frameMessage(++currentFrame));

		sendOSC(cursorBundle);
	}

	/**
	 * Sends a complete cursor message
	 */
	protected void allCursorMessage() {
		Enumeration<Finger> cursors = cursorList.elements();

		while (cursors.hasMoreElements()) {
			OSCBundle oscBundle = new OSCBundle();
			oscBundle.addPacket(aliveMessage());

			// send cursors info 10 at a time
			for (int j = 0; j < 10; j++) {
				if (!cursors.hasMoreElements()) {
					break;
				}
				oscBundle.addPacket(setMessage(cursors.nextElement()));
			}

			oscBundle.addPacket(frameMessage(-1));
			sendOSC(oscBundle);
		}
	}

	/**
	 * Resets the simulator
	 */
	protected void reset() {
		sessionID = -1;

		cursorList.clear();

		OSCBundle objBundle = new OSCBundle();
		OSCMessage aliveMessage = new OSCMessage("/tuio/2Dobj");
		aliveMessage.addArgument("alive");

		OSCMessage frameMessage = new OSCMessage("/tuio/2Dobj");
		frameMessage.addArgument("fseq");
		frameMessage.addArgument(-1);

		objBundle.addPacket(aliveMessage);
		objBundle.addPacket(frameMessage);
		sendOSC(objBundle);

		OSCBundle curBundle = new OSCBundle();
		curBundle.addPacket(aliveMessage());
		curBundle.addPacket(frameMessage(-1));
		sendOSC(curBundle);
	}

	/**
	 * Adds a touch cursor to the hash table
	 * 
	 * @param x
	 *            The x-coordinate of the touch cursor
	 * @param y
	 *            The y-coordinate of the touch cursor
	 * @param isMouse
	 *            Whether or not this is a mouse Finger
	 * @return cursor Touch - The created touch cursor
	 */
	protected Finger addCursor(int x, int y, boolean isMouse) {
		this.sessionID++;
		Finger cursor = new Finger(this.sessionID, x, y, isMouse);
		cursorList.put(this.sessionID, cursor);
		return cursor;
	}

	/**
	 * Updates a touch cursor
	 * 
	 * @param cursor
	 *            The touch cursor to update
	 * @param x
	 *            The x-coordinate of the touch cursor
	 * @param y
	 *            The y-coordinate of the touch cursor
	 */
	protected void updateCursor(Finger cursor, int x, int y) {
		cursor.update(x, y, windowWidth, windowHeight);
	}

	/**
	 * Returns the touch cursor with the given Session ID
	 * 
	 * @param sID
	 *            Session ID
	 * @return cursorList.get(sID) Touch
	 */
	protected Finger getCursor(int sID) {
		return cursorList.get(sID);
	}

	/**
	 * Removes the touch cursor
	 * 
	 * @param cursor
	 *            The touch cursor
	 */
	protected final void removeCursor(Finger cursor) {
		cursorList.remove(cursor.sessionID);
		cursorDelete();
	}

	public boolean contains(Point p) {
		if (p.x >= 0 && p.x <= windowWidth && p.y >= 0 && p.y <= windowHeight) {
			return true;
		}
		return false;
	}
}
