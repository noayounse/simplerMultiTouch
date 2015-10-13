package simplerMultiTouch.library;

import java.util.Hashtable;

import processing.core.PApplet;
import TUIO.*;

public class TuioComponent implements TuioListener {

	private Hashtable<Long, TuioObject> objectList = new Hashtable<Long, TuioObject>();
	private Hashtable<Long, TuioCursor> cursorList = new Hashtable<Long, TuioCursor>();
	private Hashtable<Long, TuioBlob> blobList = new Hashtable<Long, TuioBlob>();

	public static final int finger_size = 15;
	public static final int object_size = 60;
	public static final int table_size = 760;

	public static int width, height;
	private float scale = 1.0f;
	/** whether or not to say things */
	public boolean verbose = false;

	public TuioComponent() {
	} // end constructor

	public void addTuioObject(TuioObject tobj) {
		TuioObject obj = new TuioObject(tobj);
		objectList.put(tobj.getSessionID(), obj);

		if (verbose)
			System.out.println("add obj " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY() + " " + tobj.getAngle());
	} // end addTuioObject

	public void updateTuioObject(TuioObject tobj) {
		TuioObject obj = (TuioObject) objectList.get(tobj.getSessionID());
		obj.update(tobj);

		if (verbose)
			System.out.println("set obj " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY() + " " + tobj.getAngle() + " " + tobj.getMotionSpeed() + " "
					+ tobj.getRotationSpeed() + " " + tobj.getMotionAccel() + " " + tobj.getRotationAccel());
	} // end updateTuioObject

	public void removeTuioObject(TuioObject tobj) {
		objectList.remove(tobj.getSessionID());

		if (verbose)
			System.out.println("del obj " + tobj.getSymbolID() + " (" + tobj.getSessionID() + ")");
	} // end removeTuioObject

	public Hashtable<Long, TuioObject> getTuioObjectHashtable() {
		return objectList;
	} // end getTuioObjectHashtable

	public void addTuioCursor(TuioCursor tcur) {
		if (!cursorList.containsKey(tcur.getSessionID())) {
			cursorList.put(tcur.getSessionID(), tcur);
		}

		if (verbose)
			System.out.println("add cur " + tcur.getCursorID() + " (" + tcur.getSessionID() + ") " + tcur.getX() + " " + tcur.getY());
	} // end addTuioCursor

	public void updateTuioCursor(TuioCursor tcur) {
		if (verbose)
			System.out.println("set cur " + tcur.getCursorID() + " (" + tcur.getSessionID() + ") " + tcur.getX() + " " + tcur.getY() + " " + tcur.getMotionSpeed() + " " + tcur.getMotionAccel());
	} // end updateTuioCursor

	public void removeTuioCursor(TuioCursor tcur) {
		cursorList.remove(tcur.getSessionID());
		if (verbose)
			System.out.println("del cur " + tcur.getCursorID() + " (" + tcur.getSessionID() + ")");
	} // end removeTuioCursor

	public Hashtable<Long, TuioCursor> getTuioCursorHashtable() {
		return cursorList;
	} // end getTuioCursorHashtable

	public void addTuioBlob(TuioBlob tblb) {
		TuioBlob blob = new TuioBlob(tblb);
		blobList.put(tblb.getSessionID(), blob);

		if (verbose)
			System.out.println("add blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ") " + tblb.getX() + " " + tblb.getY() + " " + tblb.getAngle());
	} // end addTuioBlob

	public void updateTuioBlob(TuioBlob tblb) {
		TuioBlob blob = (TuioBlob) blobList.get(tblb.getSessionID());
		blob.update(tblb);

		if (verbose)
			System.out.println("set blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ") " + tblb.getX() + " " + tblb.getY() + " " + tblb.getAngle() + " " + tblb.getMotionSpeed() + " "
					+ tblb.getRotationSpeed() + " " + tblb.getMotionAccel() + " " + tblb.getRotationAccel());
	} // end updateTuioBlob

	public void removeTuioBlob(TuioBlob tblb) {
		blobList.remove(tblb.getSessionID());

		if (verbose)
			System.out.println("del blb " + tblb.getBlobID() + " (" + tblb.getSessionID() + ")");
	} // end removeTuioBlob

	public void refresh(TuioTime frameTime) {

	} // end refresh

} // end TuioComponent