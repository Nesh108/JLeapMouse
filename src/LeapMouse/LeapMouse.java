package LeapMouse;

/******************************************************************************\
* Author: Alberto Vaccari
* LeapMouse.java
* 
* This app simulates a mouse, based on the Sample.java for LeapMotion
*           
*           
\******************************************************************************/

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.leapmotion.leap.*;

class LeapListener extends Listener {
	
	//True for Debugging
	boolean DEBUG = false;
	
	//0 = Key Tap 
	//1 = Finger Tap
	int CLICK_TYPE = 0;
	
	//Just to control the speed, it can be changed accordingly to needs
	int SLOW = 10;
	
	//Screen resolution, it should match the current screen resolution for more precise movements
	int SCREEN_X = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	int SCREEN_Y = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;

	
	float cur_x = 0, cur_y = 0;
	
	int fingers_count = 0;
	int prev_fingers_count = 0;
	
	boolean Lclicked = false;
	boolean Rclicked = false;
	boolean keystroke = false;
	boolean LHold = false;
	
	boolean Swype = false;
	boolean Circle = false;
	
    public void onInit(Controller controller) {
        System.out.println("Initialized");
        System.out.println("Current screen resolution: " + SCREEN_X +"x" + SCREEN_Y);
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }

    public void onDisconnect(Controller controller) {
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
        System.exit(0);
    }

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

        int numGestures = frame.gestures().count();
        
        	for (int i=0; i < numGestures; i++) {
        	    if(frame.gestures().get(i).type() == Gesture.Type.TYPE_KEY_TAP && !Lclicked) {
                	
        	    	//If Key Tap Mode enabled 
        	    	//Left Click
        	    	if(CLICK_TYPE == 0)
        	    	{	
        	    		clickMouse(0);
                		releaseMouse(0);
                		Lclicked = true;
	                    if(DEBUG)
	                    {
	                    	System.out.println("LClicked - Key Tap");
	                    }
        	    	}
        	    	
                	if(DEBUG)
                		System.out.println("key tap");
        	    	slow();
        	    } else if (frame.gestures().get(i).type() == Gesture.Type.TYPE_SWIPE && !Swype) {
        	    	
        	    	switchApplication();
        	    	Swype = true;
        	    	if(DEBUG)
        	    		System.out.println("swype");
        	    	
        	    	slow();
        	    } else if (frame.gestures().get(i).type() == Gesture.Type.TYPE_CIRCLE && !Circle) {
      	    		if(DEBUG)
    	    		System.out.println("Circle");
        	    	
        	    	Circle = true; 	
        	    	CircleGesture circle = new CircleGesture(frame.gestures().get(i));
        	        float progress = circle.progress();
        	        if (progress >= 1.0f) {
        	        	
        	        	copy();
          	    		if(DEBUG)
        	    		System.out.println("Circle - Copy");
        	        }
        	        else
        	        {
        	        	paste();
              	    	if(DEBUG)
            	    		System.out.println("Circle - Paste");
        	        }
        	    	
        	    	
        	    	slow();
        	    }        	    
        	    else
        	    {
        	    	Circle = false; 	
        	    	Swype = false;
        	    }
        	    
        	  
        	  }
        
        if (!frame.fingers().empty()) {
          
            // Get fingers
            FingerList fingers = frame.fingers();
            fingers_count = frame.fingers().count();
            
            if(DEBUG && fingers_count != prev_fingers_count)
            {
            	System.out.println("Currently " + fingers_count + " fingers visible.\n");
            	prev_fingers_count = fingers_count;
            }
            
            
            if (!fingers.empty()) {
                // Calculate the hand's average finger tip position
                Vector avgPos = Vector.zero();
                for (Finger finger : fingers) {
                    avgPos = avgPos.plus(finger.tipPosition());
                }
                avgPos = avgPos.divide(fingers.count());
              
                
                //New Pointing System using first calibrated screen. Thanks to wooster @ freenode IRC
                ScreenList screens = controller.calibratedScreens();
                
                if (screens.empty()) return;
                Screen s = screens.get(0);
                PointableList pointables = frame.hands().get(0).pointables();
                
                if(pointables.empty()) return;
                Pointable firstPointable = pointables.get(0);
                Vector intersection = s.intersect(
                        firstPointable,
                        true, // normalize
                        1.0f // clampRatio
                        );

		        // if the user is not pointing at the screen all components of
		        // the returned vector will be Not A Number (NaN)
		        // isValid() returns true only if all components are finite
		        if (!intersection.isValid()) return;
		
		        float x = s.widthPixels() * intersection.getX();
		        // flip y coordinate to standard top-left origin
		        float y = s.heightPixels() * (1.0f - intersection.getY());
		                
		
		        moveMouse(x, y);
	        
               //If Finger Tap Mode enabled 
               if(CLICK_TYPE == 1){ 
	               // Left Click
	               if(fingers.count() == 1 && !Lclicked && avgPos.getZ()<=-70)
	                {
	                	clickMouse(0);
	                	releaseMouse(0);
	                	Lclicked = true;
	                	
	                    if(DEBUG)
	                    {
	                    	System.out.println("LClicked  - Finger Tap");
	                    }
	                	
	                }
	                
	                else if(fingers.count() != 1 || avgPos.getZ()>0)
	                {
	
	                	Lclicked = false;
	                	slow();
	                	
	                }
               }
                // Left Click hold
                if(fingers.count() == 2 && !LHold && avgPos.getZ()<=-70)
                {
                	clickMouse(0);
                	LHold = true;
                	
                    if(DEBUG)
                    {
                    	System.out.println("LHold");
                    }
                	
                }
                
                else if(fingers.count() != 2 || avgPos.getZ()>0)
                {
                	if(LHold)
                		releaseMouse(0);
                	LHold = false;
                	slow();
                	
                }
                
                
                
                // Right Click
                if(fingers.count() == 3 && !Rclicked && avgPos.getZ()<=-70)
                {
                	clickMouse(1);
                	releaseMouse(1);
                	
                	Rclicked = true;
                	
                    if(DEBUG)
                    {
                    	System.out.println("RClicked");
                    }

                }
                
                else if(fingers.count() != 3 ||  avgPos.getZ()>0)
                {
                	Rclicked = false;
                	slow();
                	
                }

                
                // Place both hands on device
                if(frame.hands().count()>1){
                
                	Hand hand1 = frame.hands().get(0);
                	Vector normal1 = hand1.palmNormal();
                	Hand hand2 = frame.hands().get(1);
                	Vector normal2 = hand2.palmNormal();
	               
	                if(hand1.fingers().count() >= 5 && !keystroke && avgPos.getZ()<=-70 && (normal1.roll() <5 || normal1.roll() > -5) && (normal2.roll() <5 || normal2.roll() > -5))
	                {
	                	
	                	showHideDesktop();
	                	
	                    if(DEBUG)
	                    {
	                    	System.out.println("Show/Hide Desktop");
	                    }
	                	
	                	keystroke = true;
	                	
	                	// To slow down the framerate, I found this would help avoid any sort of incorrect behaviour 
	                	try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	                }
	                else
	                {
	                	keystroke = false;
	                	
	                }
	                
	                
	                
	                
	            }

            }
            
            slow();
        }
    }
    
    
    // Slows down the frame rate
    private void slow(){
    	try {
			Thread.sleep(SLOW);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public void moveMouse(float x, float y)
    {
    	 Robot mouseHandler;
    	 
    	 if(cur_x != x || cur_y != y){
	    	
    		 cur_x = x;
	    	 cur_y = y;
	    	 
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mouseMove((int)x, (int)y);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 }
    	 
    	 
    }
    
    // 0: Left
    // 1: Right
    // 2: Middle  -not implemented yet-
    public void clickMouse(int value)
    {
    	int input;
    	
    	switch(value){
    		case 0: input = InputEvent.BUTTON1_MASK; break;
    		case 1: input = InputEvent.BUTTON3_MASK; break;
    		case 2: input = InputEvent.BUTTON2_MASK; break;
    		default: input = 0;
    	}
    	
    	 Robot mouseHandler;
    	 
    	
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mousePress(input);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }
 
    // 0: Left
    // 1: Right
    // 2: Middle  -not implemented yet-
    public void releaseMouse(int value)
    {
    	int input;
    	
    	switch(value){
    		case 0: input = InputEvent.BUTTON1_MASK; break;
    		case 1: input = InputEvent.BUTTON3_MASK; break;
    		case 2: input = InputEvent.BUTTON2_MASK; break;
    		default: input = 0;
    	}
    	
    	 Robot mouseHandler;
    	 
    	
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mouseRelease(input);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }   
    
    
    public void showHideDesktop()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_WINDOWS);
					keyHandler.keyPress(KeyEvent.VK_D);
					keyHandler.keyRelease(KeyEvent.VK_WINDOWS);
					keyHandler.keyRelease(KeyEvent.VK_D);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }

    public void copy()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_CONTROL);
					keyHandler.keyPress(KeyEvent.VK_C);
					keyHandler.keyRelease(KeyEvent.VK_CONTROL);
					keyHandler.keyRelease(KeyEvent.VK_V);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }

    public void paste()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_CONTROL);
					keyHandler.keyPress(KeyEvent.VK_V);
					keyHandler.keyRelease(KeyEvent.VK_CONTROL);
					keyHandler.keyRelease(KeyEvent.VK_V);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }
    public void switchApplication()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_ALT);
					keyHandler.keyPress(KeyEvent.VK_TAB);
					keyHandler.keyRelease(KeyEvent.VK_ALT);
					keyHandler.keyRelease(KeyEvent.VK_TAB);
					
				} catch (AWTException e) {
					e.printStackTrace();
				}
	    	 
    	 
    	 
    }
    
    public void setDebug(boolean d){
    	DEBUG = d;
    }
    
    public void setClickType(int i){
    	CLICK_TYPE = i;
    }
    
}

class LeapMouse {
	
    public static void main(String[] args) throws IOException {
        
    	// Create a sample listener and controller
        LeapListener listener = new LeapListener();
        Controller controller = new Controller();
        controller.enableGesture( Gesture.Type.TYPE_KEY_TAP );
      //  controller.enableGesture( Gesture.Type.TYPE_SWIPE );
      //  controller.enableGesture( Gesture.Type.TYPE_CIRCLE );
        
        System.out.println("Do you want to enable Debug Mode? Y/N (Default: Disabled)");
    	
        try{
        	
    	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    	    
    	    if(bufferRead.readLine().equalsIgnoreCase("Y"))
    	    {
    	    	listener.setDebug(true);
    	    	System.out.println("Debug Mode Enabled.");
    	    }
    	    else
    	    	System.out.println("Default: Debug Mode Disabled.");
    	    
    	    
    	    System.out.println("Do you want to use Key Tap (0) or Finger Tap (1)? 0/1 (Default: Key Tap)");
    	    
    	    if(bufferRead.readLine().equals("1")){
    	    	listener.setClickType(1);
    	    	System.out.println("Finger Tap Disabled.");
    	    }
    	    else
    	    	System.out.println("Default: Key Tap Enabled.");
    	    	
    	    
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    	
        

    }
}
