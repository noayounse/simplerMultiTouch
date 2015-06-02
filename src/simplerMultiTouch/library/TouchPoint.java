package simplerMultiTouch.library;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioPoint;

public class TouchPoint {
	private PApplet parent = null;
	private SimplerMultiTouch smtParent = null;
	public Long id;
	public float xSpeed = 0f;
	public float ySpeed = 0f;
	public PVector speed = new PVector(); // simply the xSpeed, ySpeed
	public PVector oldSpeed = new PVector();
	public float angle = 0f;
	public float motionAccel = 0f;
	public float motionSpeed = 0f;
	public float oldMotionSpeed = 0f;
	public PVector pos = new PVector();
	public PVector oldPos = new PVector();
	public boolean isMoving = false;

	PVector inceptionPos = null; // where the touch first originated

	private TuioPoint pt;
	public ArrayList<PVector> path = new ArrayList();
	public long inceptionTime = 0l; // millis when it was created

	// for the InteractionManager stuff and figuring out things that are goin on
	int order = -1; // the numerical order of each touch from the oldest to the
					// newest
	int life = 0; // frame count since touch started
	// pairing
	TouchPoint pairingBuddy = null; // if there is a pairing - when there are
									// exactly 4 touches total - then this will
									// be its buddy

	// grouping center
	PVector groupCenterPos = new PVector(); // the average position for all
											// touches - is the same for each
											// currentTouch
	PVector oldGroupCenterPos = new PVector(); // group center last frame
	int totalGroupCount = 0; // total number of SimpleTouches that make up this
								// Grouping
	int oldTotalGroupCount = 0; // group count last frame

	// group angle
	float groupAngle = 0f; // with 3 or more SimpleTouches this will be the
							// average angle from the center position. Same for
							// all currentTouches
	float oldGroupAngle = 0f; // group angle from last frame

	//
	TouchPoint(PApplet parent, SimplerMultiTouch smtParent, TuioCursor cursorIn, long inceptionTime) {
		this.parent = parent;
		this.smtParent = smtParent;
		this.id = cursorIn.getSessionID();
		this.inceptionTime = inceptionTime;
		if (cursorIn != null)
			updateCursor(cursorIn);
	} // end cursor constructor

	// TBD
	TouchPoint(TuioObject objectIn) {

	} // end object constructor

	/**
	 * 
	 * @return A copy of this touchpoint
	 */
	public TouchPoint get() {
		TouchPoint cp = new TouchPoint(parent, smtParent, null, inceptionTime);
		cp.speed = speed.get();
		cp.oldSpeed = oldSpeed.get();
		cp.oldMotionSpeed = oldMotionSpeed;
		cp.pos = pos.get();
		cp.oldPos = oldPos.get();
		cp.pairingBuddy = pairingBuddy;
		cp.order = order;
		cp.inceptionPos = inceptionPos.get();
		cp.groupCenterPos = groupCenterPos.get();
		cp.totalGroupCount = totalGroupCount;
		cp.oldTotalGroupCount = oldTotalGroupCount;
		cp.groupAngle = groupAngle;
		cp.oldGroupAngle = oldGroupAngle;
		return cp;
	} // end get

	@SuppressWarnings("static-access")
	public void updateCursor(TuioCursor tuio) {
		life++;
		this.oldPos.set(this.pos.x, this.pos.y);
		this.oldSpeed.set(this.speed.x, this.speed.y);
		this.oldMotionSpeed = motionSpeed;
		pt = tuio.getPosition();
		// this.pos.set(pt.getX(), pt.getY()); // returns between 0 and 1
		// System.out.println(parent.frameCount + " -- " + this.pos.x + ", " +
		// this.pos.y);
		this.pos.set(pt.getScreenX(parent.width), pt.getScreenY(parent.height));
		// account for the offset of the frame and menu
		this.pos.x -= smtParent.frameLocation.x;
		this.pos.y -= smtParent.frameLocation.y;
		// save the initial position
		if (inceptionPos == null)
			inceptionPos = new PVector(this.pos.x, this.pos.y);

		if (smtParent.shouldRecoredPaths())
			path.add(this.pos.get());
		this.xSpeed = tuio.getXSpeed();
		this.ySpeed = tuio.getYSpeed();
		this.isMoving = tuio.isMoving();
		this.speed.set(xSpeed, ySpeed);

		this.motionSpeed = tuio.getMotionSpeed();
		this.motionAccel = tuio.getMotionAccel();
		if (path.size() > 1) {
			this.angle = parent.atan2(this.pos.y - path.get(path.size() - 2).y, this.pos.x - path.get(path.size() - 2).x);
		}
	} // end updateCursor

	public void updateObject(TuioObject tuio) {

	} // end updateObject

	// touch functions
	/**
	 * InteractionManager function
	 * @param buddyIn
	 */
	public void setPairingBuddy(TouchPoint buddyIn) {
		pairingBuddy = buddyIn;
	} // end setPairingBuddy

	/**
	 * InteractionManager function
	 * @param orderIn
	 */
	public void setOrder(int orderIn) {
		order = orderIn;
	} // end setOrder

	/**
	 * InteractionManager function
	 * @param posIn
	 * @param groupCountIn
	 */
	public void setGroupCenterPos(PVector posIn, int groupCountIn) {
		oldGroupCenterPos = groupCenterPos.get();
		groupCenterPos = posIn;
		oldTotalGroupCount = totalGroupCount;
		totalGroupCount = groupCountIn;
	} // end setGroupCenterPos

	public void setGroupAngle(float angleIn) {
		oldGroupAngle = groupAngle;
		groupAngle = angleIn;
	} // end setGroupAngle

	/**
	 * Returns the String version of this TouchPoint
	 */
	public String toString() {
		String builder = "TouchPoint id: " + id + " pos: " + (int) pos.x + ", " + (int) pos.y + " isMoving? " + isMoving + " x/ySpeed: " + xSpeed + ", " + ySpeed;
		return builder;
	} // end toString
} // end class TouchPoint
