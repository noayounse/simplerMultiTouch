/**
 * This is a simpler version of the SimpleMultTouch library.  Does without the 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package simplerMultiTouch.library;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import processing.core.*;
import processing.event.MouseEvent;
import TUIO.*;

/**
 * This is a modification of the SimpleMultiTouch library
 * 
 */

public class SimplerMultiTouch {
	public PApplet parent;
	public String os_string = "";
	public boolean os_windows = true;
	public File tempdir = null;
	public int port = 3333; // default

	public MouseToTUIO mtt;

	public TuioComponent tuioComponent;
	public TuioClient tuioClient;
	TouchSourceThread wintouch_thread;

	// private ArrayList<TouchPoint> touchPointsCursors = new ArrayList();
	private ArrayList<TouchPoint> touchPointsObjects = new ArrayList();
	private HashMap<Long, TouchPoint> touchPointsCursors = new HashMap();
	private HashMap<Long, TouchPoint> touchPointsCursorsOLD = new HashMap();
	private ArrayList<TuioObject> tuioObjects = new ArrayList();
	private ArrayList<TuioCursor> tuioCursors = new ArrayList();

	public PVector frameLocation = new PVector();
	private PVector menuOffset = new PVector(); // to account for the menu
												// height
	private PVector manualFrameLocationOffset = new PVector();

	private boolean recordPaths = true; // to record paths for the touchPonts

	// focus stuff - to make it so it automatically disconnects/shutsdown when
	// parent frame isnt in focus
	private boolean useFocusAbility = false;
	private boolean isInFocus = true;
	private int focusDelay = 30; // frames to delay focus frame
	private int lastFocusChange = 0;

	private InteractionManager interactionManager;

	private int lastGetCursorRequest = -1; // keep track of when this was called

	private long startupDelay = 500l; // amt of time before the thing starts
										// listening?
	private long startupTime = 0l;

	/** whether or not to say things */
	public boolean verbose = false;

	private boolean addedShutdownHook = false; // so it only adds once

	/**
	 * 
	 */
	public SimplerMultiTouch() {
		// empty
	} // end constructor

	/**
	 * Default constructor with default port 3333
	 * 
	 * @param parent
	 *            The parent PApplet
	 */
	public void begin(PApplet parent) {
		begin(parent, port); // default
	} // end constructor

	/**
	 * 
	 * @param parent
	 *            The parent PApplet
	 * @param port
	 *            The int port that it will listen on for the touches
	 */
	public void begin(PApplet parent, int port) {
		this.parent = parent;
		this.port = port;
		System.out.println("started up Simpler");

		this.parent.registerMethod("pre", this);
		this.parent.registerMethod("draw", this);
		this.parent.registerMethod("mouseEvent", this);

		init();
	} // end begin

	/**
	 * For when restarting the listeners and stuff
	 */
	public void reBegin() {
		init();
	} // end reBegin

	/*
	 * 
	 */
	private void init() {
		if (verbose) {
			System.out.println("__");
			System.out.println("__");
			System.out.println(parent.frameCount + " in init().  Starting up SimplerMultiTouch ");
		}

		this.interactionManager = new InteractionManager(this.parent);

		os_string = System.getProperty("os.name");
		os_windows = os_string.startsWith("Windows");

		System.out.println("os_string: " + os_string);
		System.out.println("os_windows: " + os_windows);

		// start the mouse listener
		connectMouse();

		// start the WinTouchTuioServer
		if (os_windows)
			connectWindowsTouch();

		// start the TUIO stuff
		tuioClient = new TuioClient(this.port);
		tuioComponent = new TuioComponent();
		if (tuioClient != null) {
			tuioClient.addTuioListener(tuioComponent);
			tuioClient.connect();
			if (verbose)
				System.out.println("connected to tuio on port " + this.port);
		}

		// add the shutdown thing.. but only once [hence the addedShutdownHook]
		// prettymuch the same as the SimpleMultiTouch methodology
		if (!addedShutdownHook) {
			addJVMShutdownHook(this);
			addedShutdownHook = true;
		}

		if (verbose)
			System.out.println(parent.frameCount + " done with init");

		startupTime = parent.millis();
	} // end init

	/*
	 * 
	 * Whether or not be verbose. False by default
	 */
	public void setVerbose(boolean verbose) {
		tuioComponent.verbose = verbose;
		interactionManager.verbose = verbose;
		this.verbose = verbose;
	} // end setVerbose

	/**
	 * Setting the focus
	 * 
	 * @param useFocusAbility
	 *            The boolean
	 */
	public void setUseFocusAbility(boolean useFocusAbility) {
		this.useFocusAbility = useFocusAbility;
	} // end setUseFocusAbility

	/*
	 * Whether or not to record the paths
	 */
	public void setRecordPaths(boolean recordPaths) {
		this.recordPaths = recordPaths;
	} // end setRecordPaths

	/*
	 * @return boolean recordPaths
	 */
	public boolean shouldRecoredPaths() {
		return this.recordPaths;
	} // end shouldRecordPaths

	/**
	 * This is the function that runs before the draw loop
	 */
	public void pre() {
		// anything that happens pre-draw
		Point frameLoc = parent.frame.getLocation();
		this.frameLocation.set((float) frameLoc.getX(), (float) frameLoc.getY());
		this.frameLocation.add(manualFrameLocationOffset);
		this.menuOffset.set(parent.frame.getInsets().left, parent.frame.getInsets().top);
		this.frameLocation.add(menuOffset);
	} // end pre in draw

	/**
	 * 
	 */
	public void draw() {
		getTouchPoints();
		if (interactionManager != null)
			interactionManager.update(touchPointsCursors);

		// look at the focus if its set to do that
		if (useFocusAbility) {
			if (parent.focused && !isInFocus && parent.frameCount - lastFocusChange > focusDelay) {
				isInFocus = parent.focused;
				lastFocusChange = parent.frameCount;
				// reBegin
				if (verbose)
					System.out.println("REFOCUSING. GOING TO RE-BEGIN");
				reBegin();
			} else if (!parent.focused && isInFocus && parent.frameCount - lastFocusChange > focusDelay) {
				isInFocus = parent.focused;
				lastFocusChange = parent.frameCount;
				if (verbose)
					System.out.println("FOCUS OUT. GOING TO STOP STUFF");
				dispose();
			}
		}
	} // end draw

	/**
	 * 
	 */
	private void connectMouse() {
		mtt = new MouseToTUIO(parent.width, parent.height, this.port, this);
	} // end connectMouse

	/**
 * 
 */
	private void connectWindowsTouch() {
		System.out.println("trying to connect_windows");
		boolean system_is32 = !System.getProperty("os.arch").equals("x86");
		runWinTouchTuioServer(system_is32, "127.0.0.1", port);
	} // end connectWindowsTouch

	/**
	 * Runs a server that sends TUIO events using Windows 7 Touch events
	 * 
	 * @param is64Bit
	 *            Whether to use the 64-bit version of the exe
	 */
	@SuppressWarnings("unused")
	private void runWinTouchTuioServer(boolean is64Bit, final String address, final int port) {
		System.out.println("in runWinTouchTuioServer at frame " + parent.frameCount);
		try {
			File temp = tempDir();
			File touchhook = loadFile(temp, is64Bit ? "TouchHook_x64.dll" : "TouchHook.dll");
			// File touchhook = new File(temp + "\\"
			// + (is64Bit ? "TouchHook_x64.dll" : "TouchHook.dll"));
			File touch2tuio = loadFile(temp, is64Bit ? "Touch2Tuio_x64.exe" : "Touch2Tuio.exe");
			// File touch2tuio = new File(temp + "\\"
			// + (is64Bit ? "Touch2Tuio_x64.exe" : "Touch2Tuio.exe"));

			// File touch2tuio = new File(parent.sketchPath("") + "\\data\\"
			// + (is64Bit ? "Touch2Tuio_x64.exe" : "Touch2Tuio.exe"));

			String touch2tuio_path = touch2tuio.getAbsolutePath();

			String window_title = parent.frame.getTitle();

			System.out.println("parent frameLocation at: " + parent.frame.getLocation().getX() + ", " + parent.frame.getLocation().getY());

			// does not seem to work......
			/*
			 * String exec = String.format("%s %s %s %s %d %d %d %d",
			 * touch2tuio_path, window_title, address, port, (int)parent.frame
			 * .getLocation().getX(), (int)parent.frame.getLocation() .getY(),
			 * (int)parent.width, (int)parent.height);
			 */
			String exec = String.format("%s %s %s %s %d %d %d %d", touch2tuio_path, window_title, address, port, (int) 0, (int) 0, (int) parent.width, (int) parent.height);

			String error_message = "WM_TOUCH Process died early, make sure Visual C++ Redistributable for Visual Studio 2012 is installed (http://www.microsoft.com/en-us/download/details.aspx?id=30679), otherwise try restarting your computer.";

			// TouchSourceThread wintouch_thread = new
			// TouchSourceThread("WM_TOUCH", exec, error_message);
			wintouch_thread = new TouchSourceThread("WM_TOUCH", exec, error_message);
			wintouch_thread.start();

			System.out.println("started the wintouch_thread");

		}
		// catch( IOException exception) {
		catch (Exception exception) {
			System.out.println("problem running wintouchserver");
			exception.printStackTrace();
		}
	} // end runWinTouchTuioServer

	/**
	 * @return A temp file directory
	 * @throws IOException
	 */
	private File tempDir() {
		if (tempdir == null) {
			tempdir = new File(System.getProperty("java.io.tmpdir") + "/MamaPajama");
			tempdir.mkdir();
			tempdir.deleteOnExit();
		}
		return tempdir;
	} // end tempDir

	/**
	 * @param dir
	 *            The directory to load the resource into
	 * @param resource
	 *            A string containing the name a program in SMT's resources
	 *            folder
	 * @return A file written into dir
	 * @throws IOException
	 */
	private File loadFile(File dir, String resource) throws IOException {
		// BufferedInputStream src = new BufferedInputStream(
		// PApplet.class.getResourceAsStream("/resources/" + resource));
		System.out.println("dir.getAbsolutePath()\\data\\: " + dir.getAbsolutePath() + "\\" + resource);

		// BufferedInputStream src = new
		// BufferedInputStream(PApplet.class.getResourceAsStream("/data/" +
		// resource));
		BufferedInputStream src = new BufferedInputStream(PApplet.class.getResourceAsStream("/data/" + resource));
		final File exeTempFile = new File(dir.getAbsolutePath() + "\\" + resource);

		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(exeTempFile));

		byte[] tempexe = new byte[4 * 1024];
		int rc;
		while ((rc = src.read(tempexe)) > 0) {
			out.write(tempexe, 0, rc);
		}
		src.close();
		out.close();
		exeTempFile.deleteOnExit();
		return exeTempFile;
	} // end loadFile

	/*
	 * This will set a manual frame location offset.. eg if you run a screen app
	 * that doesnt fill the screen
	 */
	void setManualFrameLocationOffset(PVector manualFrameLocationOffset) {
		this.manualFrameLocationOffset = manualFrameLocationOffset;
	} // end manualFrameLocationOffset

	/**
	 * 
	 * @return
	 */
	// public ArrayList<TouchPoint> getTuioObjectList() {
	public Hashtable<Long, TuioObject> getTuioObjectHashtable() {
		// touchPointsObjects.clear();
		// tuioObjects = tuioClient.getTuioObjectList();
		// return touchPointsObjects;

		return tuioComponent.getTuioObjectHashtable();
	} // end getTuioObjectList

	/**
	 * 
	 * @return the HashTable of TuioCursor objects via the tuioComponent
	 */
	// public ArrayList<TouchPoint> getTuioCursorList() {
	public Hashtable<Long, TuioCursor> getTuioCursorHashtable() {
		// touchPointsCursors.clear();
		// tuioCursors = tuioClient.getTuioCursorList();
		return tuioComponent.getTuioCursorHashtable();
		// return touchPointsCursors;
	} // end getTuioCursorList

	/**
	 * This is the main function used to play with the TouchPoint objects
	 * 
	 * @return The HashMap of current TouchPoint objects
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Long, TouchPoint> getTouchPoints() {
		// in the event that it's not listening because it wasn't initted or was
		// already disposed
		if (parent == null || tuioComponent == null) {
			return touchPointsCursors;
		}

		// do the frame check. if it already ran this frame just return the hm
		if (parent.frameCount == lastGetCursorRequest)
			return touchPointsCursors;
		else
			lastGetCursorRequest = parent.frameCount;

		if (true) {
			// System.out.print(parent.frameCount + " ");
			// return null;
		}

		touchPointsCursors.clear();

		Hashtable<Long, TuioCursor> ht = (Hashtable<Long, TuioCursor>) tuioComponent.getTuioCursorHashtable().clone();
		Set<Long> keys = ht.keySet();
		for (Long key : keys) {
			if (touchPointsCursorsOLD.containsKey(key)) {
				TouchPoint existing = (TouchPoint) touchPointsCursorsOLD.get(key);
				existing.updateCursor((TuioCursor) ht.get(key));
				touchPointsCursors.put(key, existing);
			} else {
				touchPointsCursors.put(key, new TouchPoint(parent, this, (TuioCursor) ht.get(key), parent.millis()));
			}
		}
		touchPointsCursorsOLD = (HashMap<Long, TouchPoint>) touchPointsCursors.clone();

		return touchPointsCursors;
	} // end getTouchPoints

	/**
	 * 
	 * @param event
	 *            The MouseEvent
	 * 
	 */
	public void mouseEvent(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		int button = event.getButton();

		switch (event.getAction()) {
		case MouseEvent.PRESS:
			mtt.mousePressed(x, y, button);
			break;
		case MouseEvent.RELEASE:
			mtt.mouseReleased(x, y, button);
			break;
		case MouseEvent.CLICK:
			break;
		case MouseEvent.DRAG:
			mtt.mouseDragged(x, y, button);
			break;
		case MouseEvent.MOVE:
			break;
		}
	} // end mouseEvent

	/**
	 * Simply point to the dispose method
	 */
	private static void addJVMShutdownHook(final SimplerMultiTouch smtIn) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				smtIn.dispose();
			}
		}));
	} // end addJVMShutdownHook

	/**
	 * Called when closed or when pausing
	 */
	public void dispose() {
		if (tuioComponent != null) {
			tuioComponent = null;
		}
		if (tuioClient.isConnected()) {
			tuioClient.disconnect();
			System.out.println("disposing of tuioClient");
			tuioClient = null;
		}

		for (BufferedWriter writer : wintouch_thread.tuioServerInList) {
			try {
				writer.newLine();
				writer.flush();
				System.out.println("stoppping bufferedWriter");
				writer = null;
			} catch (IOException exception) {
			}
		}
		wintouch_thread.tuioServerInList.clear();

		for (BufferedReader reader : wintouch_thread.tuioServerOutList) {
			try {
				reader.close();
				reader = null;
				System.out.println("stoppping bufferedReader");
			} catch (Exception ee) {
			}
		}
		wintouch_thread.tuioServerOutList.clear();

		try {
			Thread.sleep(500);
		} catch (InterruptedException s) {

		}
		for (Process p : wintouch_thread.tuioServerList) {
			if (p != null) {
				System.out.println("destroying process");
				p.destroy();
				p = null;
			}
		}
		wintouch_thread.tuioServerList.clear();
		try {
			wintouch_thread.interrupt();
		} catch (Exception e) {
			System.err.println("error closing wintouch_thread");
		}
		wintouch_thread = null;

		// this.parent.unregisterMethod("pre", this);
		// this.parent.unregisterMethod("draw", this);
		// this.parent.unregisterMethod("mouseEvent", this);

		this.interactionManager = null;
		// this.parent = null;

		// reset the cursor list
		this.touchPointsCursors.clear();
		this.touchPointsCursorsOLD.clear();
		System.gc();
	} // end dispose

	/**
	 * 
	 * @param type
	 *            A TouchFunctions enum. See TouchFunctions.java for types
	 * @param functionName
	 *            The String of the function name to run
	 */
	public void setFunction(TouchFunctions type, String functionName) {
		interactionManager.setFunction(type, functionName);
	} // end setFunction

	/**
	 * The frameCount used to determine a swipe
	 * 
	 * @param swipeThresh
	 */
	public void setSwipeThresh(int swipeThresh) {
		interactionManager.setSwipeThresh(swipeThresh);
	} // end setSwipeThresh

	/**
	 * Just a test function
	 */
	public String whosYourCat() {
		return "Your cat is Richard";
	} // end whosYourCat
} // end class SimplerMultiTouch

