import simplerMultiTouch.library.*;
import java.util.Map;

SimplerMultiTouch smt = new SimplerMultiTouch();

public static PApplet applet = null;
public static Object aa =null;
//
void setup() {
  size(1200, 800);

  applet = this;
  aa = this;
  background(0);
  smt.begin(this);
  smt.setVerbose(false);
  
  
  
  //smt.setFunction(TouchFunctions.NEW_TOUCH, "newTouchFunction");
  //smt.setFunction(TouchFunctions.END_TOUCH, "endTouchFunction");
  //smt.setFunction(TouchFunctions.SINGLE_PRESS, "singlePressFunction");
  smt.setFunction(TouchFunctions.SINGLE_RELEASE, "singleReleaseFunction");
  smt.setFunction(TouchFunctions.DOUBLE_PRESS, "doubleClickFunction");

  smt.setFunction(TouchFunctions.TWO_SWIPE_LEFT, "twoSwipeLeftFunction");
  smt.setFunction(TouchFunctions.TWO_SWIPE_RIGHT, "twoSwipeRightFunction");
  smt.setFunction(TouchFunctions.TWO_SWIPE_UP, "twoSwipeUpFunction");
  smt.setFunction(TouchFunctions.TWO_SWIPE_DOWN, "twoSwipeDownFunction");

  smt.setFunction(TouchFunctions.OPEN_HORIZONTAL, "openH");
  smt.setFunction(TouchFunctions.CLOSE_HORIZONTAL, "closeH");
  smt.setFunction(TouchFunctions.OPEN_VERTICAL, "openV");
  smt.setFunction(TouchFunctions.CLOSE_VERTICAL, "closeV");
} // end setup


//
void draw() {
  //background(0);
  HashMap<Long, TouchPoint> tps = smt.getTouchPoints();
  for (Map.Entry me : tps.entrySet ()) {
    TouchPoint tp = (TouchPoint)me.getValue();
    pushMatrix();
    translate(tp.pos.x, tp.pos.y);
    fill(155, 155, 0, 5);
    stroke(255, 255, 0, 10);
    ellipse(0, 0, 100, 100);
    popMatrix();
  }
} // end draw

//
public static void newTouchFunction(ArrayList<TouchPoint> tpsIn) {
  println(applet.frameCount + " in newTouchFunction.  took in: " + tpsIn.size() + " objects in array");
  for (TouchPoint tp : tpsIn) println("added touchPoint: " + tp);
} // end newTouchFunction


//
static void endTouchFunction(ArrayList<TouchPoint> tpsIn) {
  println("in endTouchFunction.  took in: " + tpsIn.size() + " objects in array");
  for (TouchPoint tp : tpsIn) println("removed touchPoint: " + tp);
} // end endTouchFunction

//
static void singlePressFunction(ArrayList<TouchPoint> tpsIn) {
  println("in singlePressFunction.  took in: " + tpsIn.size() + " objects in array");
  for (TouchPoint tp : tpsIn) println("single press of touchPoint: " + tp);
} // end singlePressFunction

//
static void singleReleaseFunction(ArrayList<TouchPoint> tpsIn) {
  println(applet.frameCount + " in singleReleaseFunction.  took in: " + tpsIn.size() + " objects in array");
  for (TouchPoint tp : tpsIn) println("single release of touchPoint: " + tp);
  try {
    mark("singleRelease at: " + (int)tpsIn.get(0).pos.x + ", "+ (int)tpsIn.get(0).pos.y);
  }
  catch(Exception e) {
  }
} // end singleReleaseFunction

//
static void twoSwipeLeftFunction(ArrayList<TouchPoint> tpsIn) {
  println("in twoSwipeLeftFunction.  took in: " + tpsIn.size() + " objects in array");
  mark("twoSwipeLeftFunction");
} // end twoSwipeLeftFunction

//
static void twoSwipeRightFunction(ArrayList<TouchPoint> tpsIn) {
  println("in twoSwipeRightFunction.  took in: " + tpsIn.size() + " objects in array");
  mark("twoSwipeRightFunction");
} // end twoSwipeRightFunction



//
static void doubleClickFunction(ArrayList<TouchPoint> tpsIn) {
  println(applet.frameCount + " in doubleClickFunction.  took in: " + tpsIn.size() + " objects in array");
  mark("doubleClickFunction");
} // end doubleClickFunction

//
static void twoSwipeUpFunction(ArrayList<TouchPoint> tpsIn) {
  println("in twoSwipeUpFunction.  took in: " + tpsIn.size() + " objects in array");
  mark("twoSwipeUpFunction");
} // end twoSwipeUpFunction

//
static void twoSwipeDownFunction(ArrayList<TouchPoint> tpsIn) {
  println("in twoSwipeDownFunction.  took in: " + tpsIn.size() + " objects in array");
  mark("twoSwipeDownFunction");
} // end twoSwipeDownFunction

static void openH(ArrayList<TouchPoint> tpsIn) {
  println("in openH.  took in: " + tpsIn.size() + " objects in array");
  mark("openHorizontal");
} // end openH
static void closeH(ArrayList<TouchPoint> tpsIn) {
  println("in closeH.  took in: " + tpsIn.size() + " objects in array");
  mark("closeHorizontal");
} // end closeH
static void openV(ArrayList<TouchPoint> tpsIn) {
  println("in openV.  took in: " + tpsIn.size() + " objects in array");
  mark("openVertical");
} // end openV
static void closeV(ArrayList<TouchPoint> tpsIn) {
  println("in closeV.  took in: " + tpsIn.size() + " objects in array");
  mark("closeVertical");
} // end closeV


//
public static int y = 10;
public static void mark(String m) {
  // all calls to Processing functions should be done through an instance of the PApplet
  //applet.background(0);
  applet.fill(255);
  applet.textAlign(CENTER, CENTER);
  applet.text(m, applet.width/2, y);
  y += 12;
  if (y > applet.height - 12) {
    applet.fill(0, 150);
    applet.rect(0, 0, applet.width, applet.height);
    y = 10;
  }
} // end mark



//
//
//
//
//
//

