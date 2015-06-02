/*
 * 
 * This is an adaptation of the fingerManager function made in the CyberCrimes project
 * 
 * 
 */

package simplerMultiTouch.library;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import processing.core.*;

public class InteractionManager {

	/** whether or not to say things */
	public boolean verbose = false;

	ArrayList<TouchPoint> currentTouches = new ArrayList(); // a list of all of
															// the
	// current SimpleTouch
	// objects
	HashMap<Long, TouchPoint> newTouches = new HashMap(); // a list of the
															// touches that
	// didnt exist last frame
	HashMap<Long, TouchPoint> lastTouches = new HashMap(); // a list of the
															// touches from
	// the last frame

	// TouchPoint[] allTouches = null; // the Touch[] brought in from the SMT
	// lib

	int firstTouch = 0; // frame of the first touch
	int lastFirstTouch = -100; // frame of the last first touch
	int touchUp = 0; // after letting go of a touch
	int touchTracker = 0; // is frameCount when there is at least one touch
	int nonTouchTracker = 0; // is frameCount when there are no touches
	int doubleClickThresh = 20; // two first clicks within this time period will
								// register a double click

	int lastTouchCount = 0;

	// swipe vars
	boolean swipeEligible = true; // false when a swipe occurs, back to true
									// when there is a change in touchcount
	float swipeCounter = 0; // use this to count up to a certain thresh
	float swipeThresh = 8; // when the abs(swipeCounter is above this then the
							// thing will trigger
	float swipeFriction = (float) .9; // when the swipe thing does not happen
										// then the
	// counter decreases by this

	// singleFinger vars
	boolean onlyOnePress = true;
	TouchPoint onlyOnePressTP = null; // save the first touch
	boolean didRunDoublePress = false; // set to true when is run
	int doublePressFrame = -1; // keep track of the frame where a doublePress happened to try to avoid single press function

	PApplet parent = null;

	// function strings
	public HashMap<TouchFunctions, String> functionNames = new HashMap();;

	/**
	 * 
	 */
	public InteractionManager(PApplet parent) {
		this.parent = parent;
	} // end constructor
	
	/**
	 * sort of determines how many frames a swipe must take place before it triggers
	 * @param swipeThresh the int value for frames before swipe
	 */
	public void setSwipeThresh(int swipeThresh) {
		this.swipeThresh = (float)swipeThresh;
	} // end setSwipeThresh

	//
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void update(HashMap<Long, TouchPoint> tpsIn) {
		// find which touches were added, which remain,
		// and which were left behind
		newTouches.clear();
		for (Map.Entry me : tpsIn.entrySet()) {
			TouchPoint tp = (TouchPoint) me.getValue();
			if (lastTouches.containsKey(tp.id))
				lastTouches.remove(tp.id);
			else {
				newTouches.put(tp.id, tp);
			}
		}

		if (verbose && newTouches.size() > 0)
			System.out.println(parent.frameCount + " .. new touchPoints: " + newTouches.size());

		// FUNCTION STUFF
		// START AND END FUNCTIONS per touch
		// run any new touch or last touch calls
		if (newTouches.size() > 0 && functionNames.containsKey(TouchFunctions.NEW_TOUCH)) {
			for (Map.Entry me : newTouches.entrySet()) {
				ArrayList<TouchPoint> list = new ArrayList();
				list.add((TouchPoint) me.getValue());
				runFunction(list, (String) functionNames.get(TouchFunctions.NEW_TOUCH));
			}
		}
		if (lastTouches.size() > 0 && functionNames.containsKey(TouchFunctions.END_TOUCH)) {
			for (Map.Entry me : lastTouches.entrySet()) {
				ArrayList<TouchPoint> list = new ArrayList();
				list.add((TouchPoint) me.getValue());
				runFunction(list, (String) functionNames.get(TouchFunctions.END_TOUCH));
			}
		}

		// save these as the lastTouches
		lastTouches.clear();
		currentTouches.clear();

		for (Map.Entry me : tpsIn.entrySet()) {
			TouchPoint tp = (TouchPoint) me.getValue();
			lastTouches.put(tp.id, tp);
			currentTouches.add(tp);
		}

		// MORE SPECIFIC FUNCTION STUFF.. SWIPES AND ALL THAT
		// check for a general change
		if (lastTouchCount != currentTouches.size()) {
			lastTouchCount = currentTouches.size();
			swipeEligible = true;
			swipeCounter = 0;
		}
		// actually run through and check for swipes and such
		if (tpsIn.size() != 0) {
			if (currentTouches.size() > 1) {
				onlyOnePress = false;
				onlyOnePressTP = null;
			}
			for (int i = 0; i < currentTouches.size(); i++) {
				currentTouches.get(i).setOrder(i);
			}

			// note that these functions actually mess with currentTouches... it
			// will copy over the values as groups and all that
			currentTouches = makePairings(currentTouches); // when there are 4
															// they will get
															// buddies
			currentTouches = makeGroupCenter(currentTouches); // make the group
																// center when
																// there are 2
																// or more
			currentTouches = makeAverageAngle(currentTouches); // make the
																// average angle
																// when there
																// are 3 or more
			checkFourFingerCommands(currentTouches); // for open and close
														// swipes
			checkTwoFingerCommands(currentTouches); // for general two finger
													// swipes

			touchTracker = parent.frameCount;
			if (parent.frameCount - nonTouchTracker == 1) {
				ArrayList<TouchPoint> list = (ArrayList<TouchPoint>) currentTouches.clone();
				lastFirstTouch = firstTouch;
				firstTouch = parent.frameCount;
				if (currentTouches.size() == 1)
					onlyOnePressTP = currentTouches.get(0);
				// singleFingerPress();
				if (parent.frameCount - lastFirstTouch < doubleClickThresh) {
					// doubleClick();
					// TO DO:
					// TO DO:
					// TO DO:
					// TO DO: check for distance so separate singles dont act as
					// a double?
					// TO DO:
					// TO DO:
					// TO DO:
					if (functionNames.containsKey(TouchFunctions.DOUBLE_PRESS)) {
						runFunction(list, (String) functionNames.get(TouchFunctions.DOUBLE_PRESS));
						didRunDoublePress = true;
						doublePressFrame = parent.frameCount;
					}

				}

				if (functionNames.containsKey(TouchFunctions.SINGLE_PRESS) && !didRunDoublePress) {
					runFunction(list, (String) functionNames.get(TouchFunctions.SINGLE_PRESS));
				}
			}

		} else {
			if (parent.frameCount - touchTracker == 1) {
				touchUp = parent.frameCount;
				currentTouches.clear();
				if (onlyOnePress) {
					ArrayList<TouchPoint> list = (ArrayList<TouchPoint>) currentTouches.clone();
					if (onlyOnePressTP != null) {
						list.add(onlyOnePressTP);
					}
					// singleFingerReleased();
					if (functionNames.containsKey(TouchFunctions.SINGLE_RELEASE) && !didRunDoublePress) {
						runFunction(list, (String) functionNames.get(TouchFunctions.SINGLE_RELEASE));
					}
				}
				onlyOnePress = true;
			}
			nonTouchTracker = parent.frameCount;
		}

		// reset the double if necessary
		// give it a little wiggle room too
		if (didRunDoublePress && parent.frameCount - doublePressFrame >= 11) {
			didRunDoublePress = false;
		}
	} // end update

	/**
	 * This will try to carry out a function
	 * 
	 * @param ptsIn
	 *            The list of Touch points to act on
	 * @param functionName
	 *            The String name of the function to try to run
	 */
	private void runFunction(ArrayList<TouchPoint> ptsIn, String functionName) {
		if (verbose)
			System.out.println(parent.frameCount + "  in runFunction for function: " + functionName + " with list insize of " + ptsIn.size());
		try {
			Class appletClass = parent.getClass();
			if (verbose)
				System.out.println("parent class as: " + appletClass);
			Class[] argTypes = new Class[] { ArrayList.class };
			if (verbose)
				System.out.println("made arg types");
			Method main = appletClass.getDeclaredMethod(functionName, argTypes);
			ArrayList<TouchPoint> mainArgs = (ArrayList<TouchPoint>) ptsIn.clone();
			if (verbose)
				System.out.println("made mainArgs.  size as: " + mainArgs.size());
			// String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
			// System.out.format("invoking %s.main()%n", appletClass.getName());
			main.invoke(null, (ArrayList<TouchPoint>) mainArgs);

		} catch (Exception e) {
			System.err.println("Problem in SimplerMultiTouch >> InteractionManager -- trying to run function: _" + functionName
					+ "_\n  was the function declared static?\n  does it accept an ArrayList<TouchPoint>?");
		}
	} // end runCommands

	// SET FUNCTIONS HERE
	/**
	 * This is the actual function that sets the motions.  Use a TouchFunctions enum and a String of the static function name within your sketch
	 * @param type A TouchFunctions enum value
	 * @param functionName The String of the static function within the sketch
	 */
	public void setFunction(TouchFunctions type, String functionName) {
		this.functionNames.put(type, functionName);
	} // end setFunctionNameNewTouch

	// groupings and pairings and all that
	// assume there are 4 touches
	private ArrayList<TouchPoint> makePairings(ArrayList<TouchPoint> touchesIn) {
		if (touchesIn.size() != 4)
			for (TouchPoint st : touchesIn)
				st.setPairingBuddy(null);
		else {
			if (touchesIn.get(0).pairingBuddy != null)
				return touchesIn; // if it already has a buddy stick with it
			// will simply pair the first point with whichever is closest, then
			// the remaining two will be paired
			// not the smartest, but eh
			TouchPoint a = touchesIn.get(0);
			TouchPoint b = touchesIn.get(1);
			TouchPoint c = touchesIn.get(2);
			TouchPoint d = touchesIn.get(3);
			float ab = a.pos.dist(b.pos);
			float ac = a.pos.dist(c.pos);
			float ad = a.pos.dist(d.pos);
			if (ab < ac && ab < ad) {
				a.setPairingBuddy(b);
				b.setPairingBuddy(a);
				c.setPairingBuddy(d);
				d.setPairingBuddy(c);
			} else if (ac < ab && ac < ad) {
				a.setPairingBuddy(c);
				b.setPairingBuddy(d);
				c.setPairingBuddy(a);
				d.setPairingBuddy(b);
			} else {
				a.setPairingBuddy(d);
				b.setPairingBuddy(c);
				c.setPairingBuddy(b);
				d.setPairingBuddy(a);
			}
		}
		return touchesIn;
	} // end makePairings

	/**
	 * This will simply find the center for a group of TouchPoints. Will also
	 * assign it to each TouchPoint
	 * 
	 * @param touchesIn
	 * @return
	 */
	private ArrayList<TouchPoint> makeGroupCenter(ArrayList<TouchPoint> touchesIn) {
		PVector center = new PVector();
		for (TouchPoint st : touchesIn)
			center.add(st.pos);
		center.div(touchesIn.size());
		for (TouchPoint st : touchesIn)
			st.setGroupCenterPos(center, touchesIn.size());
		return touchesIn;
	} // end makeGroupCenter

	/**
	 * This will make the average angle for a group of TouchPoints. Will also
	 * assign it to each TouchPoint
	 * 
	 * @param touchesIn
	 * @return
	 */
	private ArrayList<TouchPoint> makeAverageAngle(ArrayList<TouchPoint> touchesIn) {
		if (touchesIn.size() <= 2) {
			for (TouchPoint st : touchesIn)
				st.setGroupAngle(0f);
		} else {
			PVector center = new PVector();
			for (TouchPoint st : touchesIn)
				center.add(st.pos);
			center.div(touchesIn.size());
			PVector avgPt = new PVector();
			for (TouchPoint st : touchesIn) {
				float angle = (float) Math.atan((st.pos.y - center.y) / (st.pos.x - center.x));
				if (st.pos.x < center.x)
					angle += (float) Math.PI;
				if (st.pos.x > center.x && st.pos.y < center.y)
					angle += (float) Math.PI * 2f;
				PVector direction = new PVector((float) Math.cos(angle), (float) Math.sin(angle));
				direction.add(center);
				avgPt.add(direction);
			}
			avgPt.div(touchesIn.size());
			float thisAngle = (float) Math.atan((avgPt.y - center.y) / (avgPt.x - center.x));
			if (avgPt.x < center.x)
				thisAngle += (float) Math.PI;
			if (avgPt.x > center.x && avgPt.y < center.y)
				thisAngle += (float) Math.PI * 2;
			for (TouchPoint st : touchesIn)
				st.setGroupAngle(thisAngle);
		}
		return touchesIn;
	} // end makeAverageAngle

	// when there are four simple touches then this will return two new
	// SimpleTouch objects located at the center point of their pairing
	private ArrayList<TouchPoint> getSTPairs() {
		ArrayList<TouchPoint> newPairs = new ArrayList<TouchPoint>();
		if (currentTouches.size() != 4)
			return newPairs;
		TouchPoint p1, p2;
		p1 = currentTouches.get(0).get();
		p2 = null;
		for (int i = 1; i < currentTouches.size(); i++) {
			if (currentTouches.get(i).id != p1.id && currentTouches.get(i).pairingBuddy.id != p1.id) {
				p2 = currentTouches.get(i).get();
				break;
			}
		}
		PVector p1Mid = PVector.add(p1.pos, p1.pairingBuddy.pos);
		p1Mid.div(2);
		p1.pos = p1Mid;
		PVector p1AvgSpeed = PVector.add(p1.speed, p1.pairingBuddy.speed);
		p1AvgSpeed.div(2);
		p1.speed = p1AvgSpeed;
		PVector p2Mid = PVector.add(p2.pos, p2.pairingBuddy.pos);
		p2Mid.div(2);
		p2.pos = p2Mid;
		PVector p2AvgSpeed = PVector.add(p2.speed, p2.pairingBuddy.speed);
		p2AvgSpeed.div(2);
		p2.speed = p2AvgSpeed;
		newPairs.add(p1);
		newPairs.add(p2);
		return newPairs;
	} // end getSTPairs

	/**
	 * Look for the two finger commands
	 * 
	 * @param touchesIn
	 */
	private void checkTwoFingerCommands(ArrayList<TouchPoint> touchesIn) {
		if (touchesIn.size() != 2 || !swipeEligible) {
			swipeCounter *= swipeFriction;
			return;
		}
		// split into two pairs going in opposite directions based on the
		// primary direction of the first touch
		boolean horizontal = true;
		if ((float) Math.abs(touchesIn.get(0).pos.x - touchesIn.get(0).inceptionPos.x) < (float) Math.abs(touchesIn.get(0).pos.y - touchesIn.get(0).inceptionPos.y))
			horizontal = false;
		ArrayList<ArrayList<TouchPoint>> groups = splitIntoDirectionalGroups(touchesIn, horizontal);
		ArrayList<TouchPoint> a = groups.get(0);
		ArrayList<TouchPoint> b = groups.get(1);
		// bust out if the groups arent equal
		if (a.size() == 1 || b.size() == 1)
			return;

		int result = 0;
		if (a.size() > 0) {
			int aResult = twoFingerDistanceCovered(a, horizontal);
			if (aResult == 0) {
				swipeCounter *= swipeFriction;
				return;
			}
			result += aResult;
		}
		if (b.size() > 0) {
			int bResult = twoFingerDistanceCovered(b, horizontal);
			if (bResult == 0) {
				swipeCounter *= swipeFriction;
				return;
			}
			result += bResult;
		}

		swipeCounter += result;

		// System.out.println(parent.frameCount +
		// " in checkTwoFingerCommands.  total touchesIn: " + touchesIn.size() +
		// " result: " + result + " a.size(): " + a.size() + " b.size(): " +
		// b.size() + " result: " + result + " swipeCounter: " + swipeCounter);

		if ((float) Math.abs(swipeCounter) > swipeThresh) {
			ArrayList<TouchPoint> list = (ArrayList<TouchPoint>) touchesIn.clone();
			if (swipeCounter < 0 && horizontal) {
				// System.out.println("TWO FINGER LEFT HORIZONTAL");
				// twoFingerHorizontalLeft();
				if (functionNames.containsKey(TouchFunctions.TWO_SWIPE_LEFT)) {
					runFunction(list, (String) functionNames.get(TouchFunctions.TWO_SWIPE_LEFT));
				}
			} else if (swipeCounter > 0 && horizontal) {
				// System.out.println("TWO FINGER RIGHT HORIZONTAL");
				if (functionNames.containsKey(TouchFunctions.TWO_SWIPE_RIGHT)) {
					// twoFingerHorizontalRight();
					runFunction(list, (String) functionNames.get(TouchFunctions.TWO_SWIPE_RIGHT));
				}
			} else if (swipeCounter < 0 && !horizontal) {
				if (functionNames.containsKey(TouchFunctions.TWO_SWIPE_UP)) {
					// twoFingerVerticalUp();
					runFunction(list, (String) functionNames.get(TouchFunctions.TWO_SWIPE_UP));
				}
			} else {
				if (functionNames.containsKey(TouchFunctions.TWO_SWIPE_DOWN)) {
					// twoFingerVerticalDown();
					runFunction(list, (String) functionNames.get(TouchFunctions.TWO_SWIPE_DOWN));
				}
			}
			// make it so that other swipes won't occur until the finger count
			// changes
			swipeEligible = false;
		}
	} // end checkTwoFingerCommands

	/**
	 * Look for the four finger commands
	 * 
	 * @param touchesIn
	 */
	private void checkFourFingerCommands(ArrayList<TouchPoint> touchesIn) {
		if (touchesIn.size() != 4 || !swipeEligible) {
			swipeCounter *= swipeFriction;
			return;
		}
		// split into two pairs going in opposite directions based on the
		// primary direction of the first touch
		boolean horizontal = true;
		if ((float) Math.abs(touchesIn.get(0).pos.x - touchesIn.get(0).inceptionPos.x) < (float) Math.abs(touchesIn.get(0).pos.y - touchesIn.get(0).inceptionPos.y))
			horizontal = false;
		ArrayList<ArrayList<TouchPoint>> groups = splitIntoDirectionalGroups(touchesIn, horizontal);
		ArrayList<TouchPoint> a = groups.get(0);
		ArrayList<TouchPoint> b = groups.get(1);
		// bust out if the groups arent equal
		if (a.size() != 2 || b.size() != 2)
			return;
		// otherwise do stuff
		int aResult = twoFingerDistanceCovered(a, horizontal);
		if (aResult == 0) {
			swipeCounter *= swipeFriction;
			return;
		}

		int bResult = twoFingerDistanceCovered(b, horizontal);
		if (bResult == 0) {
			swipeCounter *= swipeFriction;
			return;
		}

		swipeCounter += bResult;

		if ((float) Math.abs(swipeCounter) > swipeThresh) {
			PVector aAvg = PVector.add(a.get(0).pos, a.get(1).pos);
			aAvg.div(2);
			PVector bAvg = PVector.add(b.get(0).pos, b.get(1).pos);
			bAvg.div(2);

			ArrayList<TouchPoint> list = (ArrayList<TouchPoint>) touchesIn.clone();

			if (horizontal) {
				if (bAvg.x < aAvg.x) {
					// openHorizontal();
					if (functionNames.containsKey(TouchFunctions.OPEN_HORIZONTAL)) {
						runFunction(list, (String) functionNames.get(TouchFunctions.OPEN_HORIZONTAL));
					}
				} else {
					// closeHorizontal();
					if (functionNames.containsKey(TouchFunctions.CLOSE_HORIZONTAL)) {
						runFunction(list, (String) functionNames.get(TouchFunctions.CLOSE_HORIZONTAL));
					}
				}
			} else {
				if (bAvg.y < aAvg.y) {
					// openVertical();
					if (functionNames.containsKey(TouchFunctions.OPEN_VERTICAL)) {
						runFunction(list, (String) functionNames.get(TouchFunctions.OPEN_VERTICAL));
					}
				} else {
					// closeVertical();
					if (functionNames.containsKey(TouchFunctions.CLOSE_VERTICAL)) {
						runFunction(list, (String) functionNames.get(TouchFunctions.CLOSE_VERTICAL));
					}
				}
			}
			// make it so that other swipes won't occur until the finger count
			// changes
			swipeEligible = false;
		}
	} // end checkFourFingerCommands

	/**
	 *
	 * will return -1 for a good negative result, and 1 for a good positive
	 * result. 0 for a non-result
	 * 
	 * @param touchesIn
	 * @param horizontal
	 * @return
	 */

	private int twoFingerDistanceCovered(ArrayList<TouchPoint> touchesIn, boolean horizontal) {
		float triggerVelocity = 5f / 5f; // pixels per frame
		float minDistanceBetweenTouches = 200f;

		float groupVel = 0f;
		if (horizontal)
			groupVel = ((touchesIn.get(0).pos.x - touchesIn.get(0).oldPos.x) + (touchesIn.get(1).pos.x - touchesIn.get(1).oldPos.x)) / 2;
		else
			groupVel = ((touchesIn.get(0).pos.y - touchesIn.get(0).oldPos.y) + (touchesIn.get(1).pos.y - touchesIn.get(1).oldPos.y)) / 2;

		// cut out if distance too far
		if (touchesIn.get(0).pos.dist(touchesIn.get(1).pos) > minDistanceBetweenTouches)
			return 0;

		float groupDistTraveled = 0;
		if (horizontal)
			groupDistTraveled = (touchesIn.get(0).pos.x - touchesIn.get(0).inceptionPos.x + touchesIn.get(1).pos.x - touchesIn.get(1).inceptionPos.x) / 2;
		else
			groupDistTraveled = (touchesIn.get(0).pos.y - touchesIn.get(0).inceptionPos.y + touchesIn.get(1).pos.y - touchesIn.get(1).inceptionPos.y) / 2;

		// use the velocity to determine whether or not this passes the thresh
		if ((float) Math.abs(groupVel) > triggerVelocity) {
			if (groupDistTraveled < 0)
				return -1;
			else
				return 1;
		}
		return 0;
	} // end twoFingerDistanceCovered

	/**
	 * this will split the touches into directional groups either by horizontal
	 * or vertical
	 * 
	 * @param touchesIn
	 * @param horizontal
	 * @return
	 */
	ArrayList<ArrayList<TouchPoint>> splitIntoDirectionalGroups(ArrayList<TouchPoint> touchesIn, boolean horizontal) {
		ArrayList<ArrayList<TouchPoint>> groups = new ArrayList<ArrayList<TouchPoint>>();
		ArrayList<TouchPoint> a = new ArrayList<TouchPoint>(); // the positive
																// group
		ArrayList<TouchPoint> b = new ArrayList<TouchPoint>(); // the negative
																// group
		if (horizontal) {
			for (TouchPoint st : touchesIn) {
				if ((float) Math.abs(st.pos.x - st.inceptionPos.x) > (float) Math.abs(st.pos.y - st.inceptionPos.y)) {
					if (st.pos.x > st.inceptionPos.x)
						a.add(st);
					else
						b.add(st);
				}
			}
		} else {
			for (TouchPoint st : touchesIn) {
				if ((float) Math.abs(st.pos.y - st.inceptionPos.y) > (float) Math.abs(st.pos.x - st.inceptionPos.x)) {
					if (st.pos.y > st.inceptionPos.y)
						a.add(st);
					else
						b.add(st);
				}
			}
		}
		groups.add(a);
		groups.add(b);
		return groups;
	} // end splitIntoDirectionalGroups

} // end class InteractionManager
