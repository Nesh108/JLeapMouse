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
	
	//Just to control the speed, it can be changed accordingly to needs
	int SLOW = 10;
	
	//Screen resolution, it should match the current screen resolution for more precise movements
	int SCREEN_X = 1920;
	int SCREEN_Y = 1080;

	
	float cur_x = 0, cur_y = 0;
	boolean Lclicked = false;
	boolean Rclicked = false;
	boolean keystroke = false;
	boolean LHold = false;
	
    public void onInit(Controller controller) {
        System.out.println("Initialized");
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

        if (!frame.hands().empty()) {
            // Get the first hand
            Hand hand = frame.hands().get(0);

            // Check if the hand has any fingers
            FingerList fingers = hand.fingers();
            if (!fingers.empty()) {
                // Calculate the hand's average finger tip position
                Vector avgPos = Vector.zero();
                for (Finger finger : fingers) {
                    avgPos = avgPos.plus(finger.tipPosition());
                }
                avgPos = avgPos.divide(fingers.count());
  
                moveMouse(hand.palmPosition().getX()*15, SCREEN_X - hand.palmPosition().getY()*5);

                //Left Click
                if(fingers.count() == 1 && !Lclicked && avgPos.getZ()<=-100)
                {
                	clickMouse(0);
                	releaseMouse(0);
                	Lclicked = true;
                	
                	System.out.println("LClicked");
                }
                
                else if(fingers.count() != 1 || avgPos.getZ()>=-100)
                {

                	Lclicked = false;
                	slow();
                	
                }
                
                //Left Click hold
                if(fingers.count() == 2 && !LHold && avgPos.getZ()<=-100)
                {
                	clickMouse(0);
                	LHold = true;
                	
                	System.out.println("LHold");
                }
                
                else if(fingers.count() != 2 || avgPos.getZ()>=-100)
                {
                	if(LHold)
                		releaseMouse(0);
                	LHold = false;
                	slow();
                	
                }
                
                //Right Click hold
                if(fingers.count() == 3 && !Rclicked && avgPos.getZ()<=-100)
                {
                	clickMouse(1);
                	Rclicked = true;
                	
                	System.out.println("RClicked");

                }
                
                else if(fingers.count() != 3 ||  avgPos.getZ()>-100)
                {
                  	if(Rclicked)
                  		releaseMouse(1);
                	
                	Rclicked = false;
                	slow();
                	
                }

                
                
                Vector normal = hand.palmNormal();
               
                if(fingers.count() >= 7 && !keystroke && avgPos.getZ()<=-100 && (normal.roll() <5 || normal.roll() > -5))
                {
                	
                	
                	showHideDesktop();
                	System.out.println("Show/Hide Desktop");
                	keystroke = true;
                	
                	//To slow down the framerate, I found this would help avoid any sort of incorrect behaviour 
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

            
            
            slow();
        }
    }
    
    //Slows down the frame rate
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
    
    //0: Left
    //1: Right
    //2: Middle
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
 
    //0: Left
    //1: Right
    //2: Middle
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

    //Not implemented yet
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
}

class LeapMouse {
	

    public static void main(String[] args) throws IOException {
        
    	// Create a sample listener and controller
        LeapListener listener = new LeapListener();
        Controller controller = new Controller();

  

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
