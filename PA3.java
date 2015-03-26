/*
 * Robert Conner McManus
 * PA 3
 * 11/5/14
 * 
 * PA3.java
 * 
 * This class is adapted from the PA3 code given for the assignment
 * it adds functionality for steroscopic vision which can be turned off and on
 * with the "s" key, in addition, the distance between the "eyes" can be changed with
 * the left or right arrow keys, the intensity of the red channel can be changed with the
 * "j" and "k" keys, and blue with "n" and "m" keys
 * Clicking the vivarium will now also create food for the water bugs to eat
 */

import javax.swing.*;

//import java.awt.*;
import java.awt.event.*; 

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;//for new version of gl
import com.jogamp.opengl.util.FPSAnimator;//for new version of gl


public class PA3 extends JFrame
  implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH =512;
	private final int DEFAULT_WINDOW_HEIGHT=512;
	

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;
	private GLU glu;
	@SuppressWarnings("unused")
	private GLUT glut;
	private Vivarium vivarium;
	private Quaternion viewing_quaternion; // world rotation controlled by mouse actions

  // State variables for the mouse actions
	int last_x, last_y;
	boolean rotate_world;
	int win_width, win_height;
	boolean stereo;
	private float EYE_DISTANCE = 0.5f;
	private float red_val = 1;
	private float blue_val = 1;

	public PA3(){

		stereo = false;
		
		capabilities = new GLCapabilities(null);
		capabilities.setDoubleBuffered(true);  // Enable Double buffering

		canvas  = new GLCanvas(capabilities);
		canvas.addGLEventListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		canvas.setAutoSwapBufferMode(true); // true by default. Just to be explicit
		getContentPane().add(canvas);

		animator = new FPSAnimator(canvas, 60); // drive the display loop @ 60 FPS

		glu  = new GLU();
		glut = new GLUT();

		setTitle("CS480/CS680 : Spinning Teapot");
		setSize( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		win_width = DEFAULT_WINDOW_WIDTH;
		win_height = DEFAULT_WINDOW_HEIGHT;
    
		last_x = last_y = 0;
		rotate_world = false;

		// Set initialization code for user created classes that involves OpenGL 
		// calls after here. After this line, the opengGl context will be
		// correctly initialized.
		vivarium = new Vivarium();
		viewing_quaternion = new Quaternion();
		assert(vivarium != null);
	}

	public void run(){
		animator.start();
	}

	public static void main( String[] args ){
		PA3 P = new PA3();
		P.run();
	}

  //***************************************************************************
  //GLEventListener Interfaces
  //***************************************************************************
  //
  // Place all OpenGL related initialization here. Including display list
  // initialization for user created classes
  //
	public void init( GLAutoDrawable drawable){
		GL2 gl = (GL2)drawable.getGL();

		/* set up for shaded display of the vivarium*/
		float light0_position[] = {1,1,1,0};
		float light0_ambient_color[] = {0.25f,0.25f,0.25f,1};
		float light0_diffuse_color[] = {1,1,1,1};

		gl.glPolygonMode(GL.GL_FRONT,GL2.GL_FILL);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL.GL_FRONT,GL2.GL_AMBIENT_AND_DIFFUSE);

		gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
		gl.glShadeModel(GL2.GL_SMOOTH);
    
		/* set up the light source */
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0_position, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0_ambient_color, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0_diffuse_color, 0);

		/* turn lighting and depth buffering on */
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_NORMALIZE);
    
		vivarium.init( gl );
	}

  // Redisplaying graphics
  public void display(GLAutoDrawable drawable)
  {
    GL2 gl = (GL2)drawable.getGL();
    
    // initially set the color mask so that all colors are drawn
    gl.glColorMask(true, true, true, true);
    
    // clear the accumulation buffer
    gl.glClearAccum(0, 0, 0, 0);

    // clear the display 
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL2.GL_ACCUM_BUFFER_BIT);
    
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();

    vivarium.update( gl ); // update the vivarium
    
    if (stereo){ // if stereoscopic vision is enabled, draw two version of the scene
	    gl.glPushMatrix();
	    
	    // for the first drawing, just draw the blue, green, and alpha values
	    gl.glColorMask(false, true, true, true);
	    
	    gl.glMatrixMode(GL2.GL_PROJECTION); // move the camera to the perspective of the right eye
	    gl.glLoadIdentity();
	    gl.glViewport(0, 0, win_width, win_height);
	    glu.gluPerspective(25,win_width/(float)win_height,0.1,100);
	    glu.gluLookAt(EYE_DISTANCE/2,0,12,0,0,-2,0,1,0);
	    gl.glMatrixMode(GL2.GL_MODELVIEW); // return to model view transforms
	    
	    // rotate the world and then call world display list object 
	    gl.glMultMatrixf( viewing_quaternion.to_matrix(), 0 );
	    
	    // draw the vivarium to the buffer
	    vivarium.draw( gl );
	    // load the buffer into the accumulation buffer
	    gl.glAccum(GL2.GL_LOAD, blue_val);
	    gl.glPopMatrix();
	    
	    // set the color mask back to all colors on
	    gl.glColorMask(true, true, true, true);
	    
	    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
	    
	    gl.glPushMatrix();
	    
	    // set the color mask so that only red is drawn
	    gl.glColorMask(true, false, false, false);
	    
	    gl.glMatrixMode(GL2.GL_PROJECTION); // move the camera to the perspective of the left eye
	    gl.glLoadIdentity();
	    gl.glViewport(0, 0, win_width, win_height);
	    glu.gluPerspective(25,win_width/(float)win_height,0.1,100);
	    glu.gluLookAt(-EYE_DISTANCE/2,0,12, 0,0,-2,0,1,0);
	    gl.glMatrixMode(GL2.GL_MODELVIEW); // return to model view transforms
	    
	    // rotate the world and then call world display list object 
	    gl.glMultMatrixf( viewing_quaternion.to_matrix(), 0 );
	    
	    // draw the vivarium to the buffer
	    vivarium.draw( gl );
	    // add the buffer to the accumulation buffer
	    gl.glAccum(GL2.GL_ACCUM, red_val);
	    gl.glPopMatrix();
	    
	    // return everything in the accumulation buffer to the drawing buffer
	    gl.glAccum(GL2.GL_RETURN, 1);
    } else { // if stereoscopic vision is not on, simply draw the scene as normal
    	
    	gl.glMatrixMode(GL2.GL_PROJECTION); // set the camera back to the default location
	    gl.glLoadIdentity();
	    gl.glViewport(0, 0, win_width, win_height);
	    glu.gluPerspective(25,win_width/(float)win_height,0.1,100);
	    glu.gluLookAt(0,0,12,0,0,0,0,1,0);
	    
	    // begin model transforms
	    gl.glMatrixMode(GL2.GL_MODELVIEW);
	    
	    // rotate the world and then call world display list object 
	    gl.glMultMatrixf( viewing_quaternion.to_matrix(), 0 );
	    
	    // draw the vivarium
	    vivarium.draw(gl);
    }
  }

  // Window size change
  public void reshape(GLAutoDrawable drawable, int x, int y, 
                            int width, int height)
  {
	  
	win_width = width;
	win_height = height;
    // Change viewport dimensions
    GL2 gl = (GL2)drawable.getGL();

    // Prevent a divide by zero, when window is too short (you cant make a
    // window of zero width).
    if(height == 0) height = 1;

    double ratio = 1.0f * width / height;

    // Reset the coordinate system before modifying 
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    
    // Set the viewport to be the entire window 
    gl.glViewport(0, 0, width, height);
    
    // Set the clipping volume 
    glu.gluPerspective(25,ratio,0.1,100);

    // Camera positioned at (0,0,6), look at point (0,0,0), Up Vector (0,1,0)
    glu.gluLookAt(0,0,12,0,0,0,0,1,0);

    gl.glMatrixMode(GL2.GL_MODELVIEW);
    
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
      boolean deviceChanged)
  {
  }


  //*********************************************** 
  //          KeyListener Interfaces
  //*********************************************** 
  public void keyTyped(KeyEvent key)
  {
      switch ( key.getKeyChar() ) {
        case 'Q' :
        case 'q' : new Thread() {
                     public void run()
                     { animator.stop(); }
                   }.start();
                   System.exit(0);
                   break;

        // set the viewing quaternion to 0 rotation 
        case 'R' :
        case 'r' :        
        	viewing_quaternion.reset();         
        	break;
        // turn on or off the stereoscopic vision
        case 'S':
        case 's':
        	stereo = !stereo;
        	break;
        // increment or decrement the intensity of the red side
        case 'j':
        case 'J':
      	  red_val -= 0.05;
      	  red_val = Math.max(red_val, 0);
      	  break;
        case 'k':
        case 'K':
      	  red_val += 0.05;
      	  red_val = Math.min(1, red_val);
      	  break;
      	// increment or decrement the intensity of the blue side
        case 'n':
        case 'N':
      	  blue_val += 0.05;
      	  blue_val = Math.min(1, blue_val);
      	  break;
        case 'm':
        case 'M':
      	  blue_val -= 0.05;
      	  blue_val = Math.max(0, blue_val);
      	  break;
        default :
          break;
    }
 }

  public void keyPressed(KeyEvent key)
  {
    switch (key.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        new Thread()
        {
          public void run()
          {
            animator.stop();
          }
        }.start();
        System.exit(0);
        break;
        
      // increment or decrement the distance between the two eye cameras
      case KeyEvent.VK_LEFT:
    	  EYE_DISTANCE -= 0.1f;
    	  break;
      case KeyEvent.VK_RIGHT:
    	  EYE_DISTANCE += 0.1f;
    	  break;
      default:
        break;
    }
  }

  public void keyReleased(KeyEvent key)
  {
  }

  //************************************************** 
  // MouseListener and MouseMotionListener Interfaces
  //************************************************** 
  public void mouseClicked(MouseEvent mouse){
	  
	  // get the position of the mouse and scale it to be within the vivarium
	  float x = mouse.getX() - win_width/2;
	  x /= win_width;
	  x *= 4;
	  float y = mouse.getY() - win_height/2;
	  y /= win_height;
	  y *= 4;
	  
	  // rotate the mouse coordinates to coincide with the vivarium location
	  Quaternion position = new Quaternion(0, x, y, 0);
	  Quaternion ret = viewing_quaternion.multiply(position);
	  Quaternion rotate = ret.multiply(viewing_quaternion.invert());
	  
	  // create the food for the Water Bugs
	  vivarium.createFood(rotate.v[0], rotate.v[1], rotate.v[2]);
  }

  public void mousePressed(MouseEvent mouse){
	  int button = mouse.getButton();
	  if ( button == MouseEvent.BUTTON1 ){
		  last_x = mouse.getX();
		  last_y = mouse.getY();
		  rotate_world = true;
	  }
  }

  public void mouseReleased(MouseEvent mouse)
  {
    int button = mouse.getButton();
    if ( button == MouseEvent.BUTTON1 )
    {
      rotate_world = false;
    }
  }

  public void mouseMoved( MouseEvent mouse)
  {
  }

  public void mouseDragged( MouseEvent mouse )
  {
    if (rotate_world)
    {
      // vector in the direction of mouse motion
      int x = mouse.getX();
      int y = mouse.getY();
      float dx = x - last_x;
      float dy = y - last_y;
     
      // spin around axis by small delta
      float mag = (float) Math.sqrt( dx*dx + dy*dy );
      if(mag < 0.0001)
    	  return;
     
      float[] axis = new float[3];
      axis[0] = dy/ mag;
      axis[1] = dx/ mag;
      axis[2] = 0.0f;

      // calculate appropriate quaternion
      float viewing_delta = 3.1415927f / 180.0f; // 1 degree
      float s = (float) Math.sin( 0.5f*viewing_delta );
      float c = (float) Math.cos( 0.5f*viewing_delta );

      Quaternion Q = new Quaternion( c, s*axis[0], s*axis[1], s*axis[2]);
      viewing_quaternion = Q.multiply( viewing_quaternion );

      // normalize to counteract acccumulating round-off error
      viewing_quaternion.normalize();

      // Save x, y as last x, y
      last_x = x;
      last_y = y;
    }
  }

  public void mouseEntered( MouseEvent mouse)
  {
  }

  public void mouseExited( MouseEvent mouse)
  {
  }

public void dispose(GLAutoDrawable drawable) {
	// TODO Auto-generated method stub
	
} 



}
