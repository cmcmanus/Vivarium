/*
 * Robert Conner McManus
 * PA 3
 * 11/5/14
 * 
 * WaterBug.java
 * 
 * This class creates a WaterBug and begins playing its animation at the given starting animation count
 */

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.jogamp.opengl.util.gl2.GLUT;

public class WaterBug {

	private GLUT glut;
	private GLU glu;
	
	// static variables so new lists aren't created for each bug
	private static boolean made = false;
	private static int bodylist;
	private static int[] upperleglist;
	private static int[] lowerleglist;
	private static int[] upperantennalist;
	private static int[] midantennalist;
	private static int[] lowerantennalist;
	
	// the number of frames in the animation
	private final int NUM_FRAMES = 60;
	private int current_frame;
	
	// defines the bounding box
	private final float box[] = {1f, 0.25f, 0.875f};
	
	public WaterBug(int start){
		glut = new GLUT();
		glu = new GLU();
		
		// only create new lists if the bug hasn't yet been made
		if (!made){
			upperleglist = new int[NUM_FRAMES];
			lowerleglist = new int[NUM_FRAMES];
			upperantennalist = new int[NUM_FRAMES];
			midantennalist = new int[NUM_FRAMES];
			lowerantennalist = new int[NUM_FRAMES];
		}
		
		// set the current frame to the start value
		current_frame = start;
	}
	
	// returns the bounding box information
	public float[] getBoundingBox(){
		return box.clone();
	}
	
	// creates all the information needed for the Water Bug and builds the display lists
	public void init(GL2 gl){
		// only create new lists if the lists haven't already been created
		if (!made){
			bodylist = gl.glGenLists(NUM_FRAMES);
			
			for (int i = 0; i < NUM_FRAMES; i++){
				// create four legs in each frame
				lowerleglist[i] = gl.glGenLists(4);
				upperleglist[i] = gl.glGenLists(4);
				
				for (int j = 0; j < 4; j++){
					createLowerLegList(gl, i, j);
					createUpperLegList(gl, i, j);
				}
				
				// create two antenna
				lowerantennalist[i] = gl.glGenLists(2);
				midantennalist[i] = gl.glGenLists(2);
				upperantennalist[i] = gl.glGenLists(2);
				
				for (int j = 0; j < 2; j++){
					createLowerAntennaList(gl, i, j);
					createMidAntennaList(gl, i, j);
					createUpperAntennaList(gl, i, j);
				}
				
				// create the actual body
				createBodyList(gl, i);
			}
			// update made so no more lists are created
			made = true;
		}
	}

	// create the display list for the upper Antenna
	private void createUpperAntennaList(GL2 gl, int frame, int antenna){
		gl.glNewList(upperantennalist[frame]+antenna, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.04f, 0.02f, 0.4, 20, 20);
		
		// defines a sphere joint
		gl.glTranslatef(0, 0, 0.4f);
		glut.glutSolidSphere(0.02, 20, 20);
		
		// defines the placement of each antenna
		if (antenna == 0)
			gl.glRotatef(80, 0, 1, 0);
		else
			gl.glRotatef(-80, 0, 1, 0);
		
		gl.glCallList(midantennalist[frame]+antenna);
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// create the display list for the middle antenna
	private void createMidAntennaList(GL2 gl, int frame, int antenna){
		gl.glNewList(midantennalist[frame]+antenna, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.02f, 0.02f, 0.2, 20, 20);
		
		// defines the sphere joint
		gl.glTranslatef(0, 0, 0.2f);
		glut.glutSolidSphere(0.02, 20, 20);
		
		// defines the rotation for each antenna
		if (antenna == 0)
			gl.glRotatef(-80, 0, 1, 0);
		else
			gl.glRotatef(80, 0, 1, 0);
		
		gl.glCallList(lowerantennalist[frame]+antenna);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// create the display list for the lower antenna
	private void createLowerAntennaList(GL2 gl, int frame, int antenna){
		gl.glNewList(lowerantennalist[frame]+antenna, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.02f, 0.01f, 0.2, 20, 20);
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// create the display list for the upper leg
	private void createUpperLegList(GL2 gl, int frame, int leg){
		gl.glNewList(upperleglist[frame]+leg, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.05f, 0.05f, 1.5, 20, 20);
		
		// defines the sphere joint
		gl.glTranslatef(0, 0, 1.5f);
		glut.glutSolidSphere(0.05, 20, 20);
		
		// defines the rotation for the lower leg and calls it
		gl.glRotatef(15, 1, 0, 0);
		gl.glCallList(lowerleglist[frame]+leg);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// creates the display list for the lower leg
	private void createLowerLegList(GL2 gl, int frame, int leg){
		gl.glNewList(lowerleglist[frame]+leg, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.05f, 0.01f, 1.5, 20, 20);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// function which draws the legs in the proper location on the body
	private void buildLegs(GL2 gl, int frame){
		
		gl.glPushMatrix();
		
		// sets the position and rotation for the first leg
		gl.glTranslatef(0.2f, 0, 0.1f);
		gl.glRotatef(60, 0, 1, 0);
		
		if (frame < NUM_FRAMES/4){ // defines the first part of the rotation
			float rotate = 80.0f / (NUM_FRAMES/4);
			rotate *= frame;
			gl.glRotatef(rotate, 0, 1, 0);
		} else if (frame > NUM_FRAMES/2 + NUM_FRAMES/4) { // defines the return to start
			float rotate = 80.0f / (NUM_FRAMES/4);
			rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
			rotate += 80;
			gl.glRotatef(rotate, 0, 1, 0);
		} else{ // holds here to appear like gliding
			gl.glRotatef(80, 0, 1, 0);
		}
		
		gl.glCallList(upperleglist[frame]);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		// sets the position and rotation for the second leg
		gl.glTranslatef(-0.2f, 0, 0.1f);
		gl.glRotatef(-60, 0, 1, 0);
		
		if (frame < NUM_FRAMES/4){ // defines the first part of the animation
			float rotate = 80.0f / (NUM_FRAMES/4);
			rotate *= frame;
			gl.glRotatef(-rotate, 0, 1, 0);
		} else if (frame > NUM_FRAMES/2 + NUM_FRAMES/4) { // defines the return to the start position
			float rotate = 80.0f / (NUM_FRAMES/4);
			rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
			rotate += 80;
			gl.glRotatef(-rotate, 0, 1, 0);
		} else{ // holds here to appear like gliding
			gl.glRotatef(-80, 0, 1, 0);
		}
		
		gl.glCallList(upperleglist[frame]+1);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		// defines the position and location of the third leg
		gl.glTranslatef(0.2f, 0, -0.1f);
		gl.glRotatef(150, 0, 1, 0);
		
		if (frame < NUM_FRAMES/4){ // defines the first part of the animation
			float rotate = 30.0f / (NUM_FRAMES/4);
			rotate *= frame;
			gl.glRotatef(rotate, 0, 1, 0);
		} else if (frame > NUM_FRAMES/2 + NUM_FRAMES/4) { // defines the return to start
			float rotate = 30.0f / (NUM_FRAMES/4);
			rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
			rotate += 30;
			gl.glRotatef(rotate, 0, 1, 0);
		} else{ // hold here to appear like gliding
			gl.glRotatef(30, 0, 1, 0);
		}
		
		gl.glCallList(upperleglist[frame]+2);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		// defines the position and rotation of the fourth leg
		gl.glTranslatef(-0.2f, 0, -0.1f);
		gl.glRotatef(-150, 0, 1, 0);
		
		if (frame < NUM_FRAMES/4){ // defines the first part of the animation
			float rotate = 30.0f / (NUM_FRAMES/4);
			rotate *= frame;
			gl.glRotatef(-rotate, 0, 1, 0);
		} else if (frame > NUM_FRAMES/2 + NUM_FRAMES/4) { // defines the return to start
			float rotate = 30.0f / (NUM_FRAMES/4);
			rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
			rotate += 30;
			gl.glRotatef(-rotate, 0, 1, 0);
		} else{ // hold here to appear like gliding
			gl.glRotatef(-30, 0, 1, 0);
		}
		
		gl.glCallList(upperleglist[frame]+3);
		
		gl.glPopMatrix();
	}

	// creates the display list for the body
	private void createBodyList(GL2 gl, int frame){
		gl.glNewList(bodylist+frame, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		// creates the body as a scaled sphere
		gl.glPushMatrix();
		gl.glScalef(0.41666f, 0.3333f, 1f);
		glut.glutSolidSphere(0.6, 20, 20);
		gl.glPopMatrix();
		
		// draws the legs
		buildLegs(gl, frame);
		
		gl.glPushMatrix();
		// places the first antenna
		gl.glTranslatef(0.05f, 0, 0.45f);
		gl.glRotatef(15, 0, 1, 0);
		
		if (frame < NUM_FRAMES/2){ // defines the first part of the antenna's animation
			float rotate = 10.0f / (NUM_FRAMES/2);
			rotate *= frame;
			gl.glRotatef(rotate, 0, 1, 0);
		} else { // defines its return to start
			float rotate = 10.0f / (NUM_FRAMES/2);
			rotate *= -1 * (frame - NUM_FRAMES/2);
			rotate += 10;
			gl.glRotatef(rotate, 0, 1, 0);
		}
		
		// draws the first antenna
		gl.glCallList(upperantennalist[frame]);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		// places the second antenna
		gl.glTranslatef(-0.05f, 0, 0.45f);
		gl.glRotatef(-15, 0, 1, 0);
		
		if (frame < NUM_FRAMES/2){ // defines the first part of the antenna's animation
			float rotate = 5.0f / (NUM_FRAMES/2);
			rotate *= frame;
			gl.glRotatef(rotate, 0, 1, 0);
		} else { // defines its return to start
			float rotate = 5.0f / (NUM_FRAMES/2);
			rotate *= -1 * (frame - NUM_FRAMES/2);
			rotate += 5;
			gl.glRotatef(rotate, 0, 1, 0);
		}
		
		// draws the second antenna
		gl.glCallList(upperantennalist[frame]+1);
		gl.glPopMatrix();
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// updates the current frame each time the creature is drawn
	public void update(GL2 gl){
		current_frame++;
		current_frame %= NUM_FRAMES;
	}
	
	// draws the proper display list for the current frame
	public void draw(GL2 gl){
		gl.glPushMatrix();
		gl.glColor3f(0.25f, 0.25f, 0.25f);
		gl.glCallList(bodylist+current_frame);
		gl.glPopMatrix();
	}
}
