/*
 * Robert Conner McManus
 * PA 3
 * 11/5/14
 * 
 * Lizard.java
 * 
 * This class creates display lists to draw a lizard as well as containing bounding box information and animation frames
 * for each lizard drawn. The lizard display lists are created once and used for each lizard drawn.
 */

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;

public class Lizard {
	
	private GLUT glut;
	private GLU glu;
	
	// static variables so new lists aren't created for each lizard
	private static boolean made = false;
	private static int bodylist;
	private static int[] lowerLegList;
	private static int[] midLegList;
	private static int[] upperLegList;
	private static int[] tailList;
	private static int[] headlist;
	
	// the number of frames in the animation
	private final int NUM_FRAMES = 60;
	private int current_frame;
	
	// bounding box information
	private final float box[] = {1, 0.75f, 1.5f};
	
	public Lizard(int start){
		
		// set the lizards current frame to the starting value
		current_frame = start;
		
		// create the display lists if they haven't been made yet
		if (!made){
		
			lowerLegList = new int[NUM_FRAMES];
			midLegList = new int[NUM_FRAMES];
			upperLegList = new int[NUM_FRAMES];
			tailList = new int[NUM_FRAMES];
			headlist = new int[NUM_FRAMES];
		}
		
		glut = new GLUT();
		glu = new GLU();
	}
	
	// return the bounding box information
	public float[] getBoundingBox(){
		return box.clone();
	}
	
	// initialize the elements needed for the lizard
	public void init(GL2 gl){
		
		// if the display lists have not been made, create them
		if (!made){
			// a new set of display lists for each frame of the animation
			bodylist = gl.glGenLists(NUM_FRAMES);
			for (int i = 0; i < NUM_FRAMES; i++){
				upperLegList[i] = gl.glGenLists(4);
				lowerLegList[i] = gl.glGenLists(4);
				midLegList[i] = gl.glGenLists(4);
			
				// create the legs
				for (int j = 0; j < 4; j++){
					createLowerLegList(gl, i, j);
					createMidLegList(gl, i, j);
					createLegList(gl, i, j);
				}
			
				// create the tail
				tailList[i] = gl.glGenLists(3);
			
				createLowerTailList(gl, i);
				createMidTailList(gl, i);
				createUpperTailList(gl, i);
			
				// create the head
				headlist[i] = gl.glGenLists(2);
			
				createHeadList(gl, i);
				createNeckList(gl, i);
			
				// create the body
				createBodyList(gl, i);
			}
			// update made so the lists are not recreated
			made = true;
		}
	}
	
	// creates the head display list
	private void createHeadList(GL2 gl, int frame){
		gl.glNewList(headlist[frame]+1, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.15f, 0.1f, 0.5, 20, 20);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// creates the neck display list
	private void createNeckList(GL2 gl, int frame){
		gl.glNewList(headlist[frame], GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		gl.glScalef(1, 0.8f, 1);
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.15f, 0.15f, 0.2, 20, 20);
		
		// defines the location of the sphere joint and rotation of the head
		gl.glTranslatef(0, 0, 0.2f);
		glut.glutSolidSphere(0.15, 20, 20);
		
		gl.glRotatef(30, 1, 0, 0);
		
		// the head turns across the different frames
		if (frame < NUM_FRAMES/2){ // defines the first half of the animation
			if (frame < NUM_FRAMES/4){ // each half is split into halves to move the head back and forth
				float rotate = 10.0f / (NUM_FRAMES/4);
				rotate *= frame;
				gl.glRotatef(rotate, 0, 1, 0);
			} else {
				float rotate = 10.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4);
				rotate += 10;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		} else { // defines the second half
			
			if (frame < (NUM_FRAMES/4 + NUM_FRAMES/2)){
				float rotate = 10.0f / (NUM_FRAMES/4);
				rotate *= (frame - NUM_FRAMES/2);
				gl.glRotatef(-rotate, 0, 1, 0);
			} else{
				float rotate = 10.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
				rotate += 10;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		}
		
		// create the head
		gl.glCallList(headlist[frame]+1);
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// create the tail display list
	private void createUpperTailList(GL2 gl, int frame){
		gl.glNewList(tailList[frame], GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		// scales the tail so it is flatter
		gl.glScalef(1, 0.625f, 1);
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.35f, 0.2f, 1, 20, 20);
		
		// creates the sphere joint
		gl.glTranslatef(0, 0, 1);
		glut.glutSolidSphere(0.2, 20, 20);
		
		// defines the rotation of the tail across the frames
		if (frame < NUM_FRAMES/2){// defines the first half of the animation
			if (frame < NUM_FRAMES/4){ // each half is split into halves to move the tail back and forth
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= frame;
				gl.glRotatef(-rotate, 0, 1, 0);
			} else {
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4);
				rotate += 20;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		} else { // defines the second half
			
			if (frame < (NUM_FRAMES/4 + NUM_FRAMES/2)){
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= (frame - NUM_FRAMES/2);
				gl.glRotatef(rotate, 0, 1, 0);
			} else{
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
				rotate += 20;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		}
		// creates the tail
		
		gl.glCallList(tailList[frame]+1);
		
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// creates the middle part of the tail
	private void createMidTailList(GL2 gl, int frame){
		gl.glNewList(tailList[frame]+1, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluCylinder(quad, 0.2f, 0.1f, 1, 20, 20);
		
		// defines the sphere joint
		gl.glTranslatef(0, 0, 1);
		glut.glutSolidSphere(0.1, 20, 20);
		
		// animates the middle tail piece
		if (frame < NUM_FRAMES/2){// defines the first half of the animation
			if (frame < NUM_FRAMES/4){// each half is split into halves to move the tail back and forth
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= frame;
				gl.glRotatef(-rotate, 0, 1, 0);
			} else {
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4);
				rotate += 20;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		} else { // defines the second half
			
			if (frame < (NUM_FRAMES/4 + NUM_FRAMES/2)){
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= (frame - NUM_FRAMES/2);
				gl.glRotatef(rotate, 0, 1, 0);
			} else{
				float rotate = 20.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
				rotate += 20;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		}
		
		// creates the lower tail
		gl.glCallList(tailList[frame]+2);
		
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// creates the display list for the lower tail
	private void createLowerTailList(GL2 gl, int frame){
		gl.glNewList(tailList[frame]+2, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		glut.glutSolidCone(0.1f, 1, 20, 20);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// creates the display list for the middle leg joint
	private void createMidLegList(GL2 gl, int frame, int val){
		gl.glNewList(midLegList[frame]+val, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		glut.glutSolidCylinder(0.075, 0.4, 20, 20);
		
		// creates the sphere joint
		gl.glTranslatef(0, 0, 0.4f);
		glut.glutSolidSphere(0.075, 20, 20);
		
		// each leg defined by val has a different animation
		if (val == 1){ // leg 1's animation
			gl.glRotatef(-75, 0, 1, 0);
			if (frame < NUM_FRAMES/2){ // split into half to make the leg move back and forth
				float rotate = 90.0f / (NUM_FRAMES/2);
				rotate *= frame;
				gl.glRotatef(rotate, 0, 1, 0);
			} else { // second half of the animation
				float rotate = 90.0f / (NUM_FRAMES/2);
				rotate *= -1 * (frame - NUM_FRAMES/2);
				rotate += 90;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		}
		else if (val == 3){ // leg 3's animation
			gl.glRotatef(-75, 0, -1, 0);
			if (frame < NUM_FRAMES/2){ // first half of animation
				float rotate = 90.0f / (NUM_FRAMES/2);
				rotate *= frame;
				gl.glRotatef(-rotate, 0, 1, 0);
			} else { // second half of animation
				float rotate = 90.0f / (NUM_FRAMES/2);
				rotate *= -1 * (frame - NUM_FRAMES/2);
				rotate += 90;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		}

		// creates the lower leg
		gl.glCallList(lowerLegList[frame]+val);
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// creates the display list for the lower leg
	private void createLowerLegList(GL2 gl, int frame, int val){
		gl.glNewList(lowerLegList[frame]+val, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		glut.glutSolidCone(0.075, 0.5, 20, 20);
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// creates the display list for the upper leg
	private void createLegList(GL2 gl, int frame, int val){
		gl.glNewList(upperLegList[frame]+val, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		glut.glutSolidCylinder(0.075, 0.4, 20, 20);
		gl.glTranslatef(0, 0, 0.4f);
		
		// defines the sphere joint
		glut.glutSolidSphere(0.075, 20, 20);
		
		if (val == 0){ // defines leg 0's rotation
			gl.glRotatef(50, 0, 1, 0);
			gl.glRotatef(-20, 1, 0, 0);
		}else if (val == 1){ // defines leg 1's rotation and animation
			gl.glRotatef(90, 0, 1, 0);
			if (frame < NUM_FRAMES/2){ // the first half of the animation
				float rotate = 60.0f / (NUM_FRAMES/2);
				rotate *= frame;
				gl.glRotatef(-rotate, 0, 1, 0);
			} else { // the second half
				float rotate = 60.0f / (NUM_FRAMES/2);
				rotate *= -1 * (frame - NUM_FRAMES/2);
				rotate += 60;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		}
		else if (val == 2){ // defines leg 2's rotation
			gl.glRotatef(-50, 0, 1, 0);
			gl.glRotatef(-20, 1, 0, 0);
		}else if (val == 3){ // defines leg 3's rotation and animation
			gl.glRotatef(90, 0, -1, 0);
			if (frame < NUM_FRAMES/2){ // the first half of the animation
				float rotate = 60.0f / (NUM_FRAMES/2);
				rotate *= frame;
				gl.glRotatef(rotate, 0, 1, 0);
			} else { // the second half
				float rotate = 60.0f / (NUM_FRAMES/2);
				rotate *= -1 * (frame - NUM_FRAMES/2);
				rotate += 60;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		}
		
		// creates the middle leg
		gl.glCallList(midLegList[frame]+val);
		
		gl.glPopMatrix();
		gl.glEndList();
	}

	// a function to create and place all the legs on the body
	private void buildLegs(GL2 gl, int frame){
		gl.glPushMatrix();
		
		// places and rotates the first leg
		gl.glTranslatef(0.2f, 0, 0.75f);
		gl.glRotatef(130, 0, 1, 0);
		gl.glRotatef(30, 1, 0, 0);
		
		gl.glCallList(upperLegList[frame]);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		// places and rotates the second leg
		gl.glTranslatef(0.2f, 0, -0.75f);
		gl.glRotatef(90, 0, 1, 0);
		gl.glRotatef(30, 1, 0, 0);
		if (frame < NUM_FRAMES/2){ // defines movement for the second leg
			float rotate = 50.0f / (NUM_FRAMES/2);
			rotate *= frame;
			gl.glRotatef(rotate, 0, 1, 0);
		} else { // and return movement so it goes back and forth
			float rotate = 50.0f / (NUM_FRAMES/2);
			rotate *= -1 * (frame - NUM_FRAMES/2);
			rotate += 50;
			gl.glRotatef(rotate, 0, 1, 0);
		}
		
		gl.glCallList(upperLegList[frame]+1);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		// places and rotates the third leg
		gl.glTranslatef(-0.2f, 0, 0.75f);
		gl.glRotatef(130, 0, -1, 0);
		gl.glRotatef(30, 1, 0, 0);
		
		gl.glCallList(upperLegList[frame]+2);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		// places and rotates the fourth leg
		gl.glTranslatef(-0.2f, 0, -0.75f);
		gl.glRotatef(90, 0, -1, 0);
		gl.glRotatef(30, 1, 0, 0);
		
		if (frame < NUM_FRAMES/2){ // defines movement for the fourth leg
			float rotate = 50.0f / (NUM_FRAMES/2);
			rotate *= frame;
			gl.glRotatef(-rotate, 0, 1, 0);
		} else { // and return movement so it goes back and forth
			float rotate = 50.0f / (NUM_FRAMES/2);
			rotate *= -1 * (frame - NUM_FRAMES/2);
			rotate += 50;
			gl.glRotatef(-rotate, 0, 1, 0);
		}
		
		gl.glCallList(upperLegList[frame]+3);
		
		gl.glPopMatrix();
	}
	
	// creates the display list for the body
	private void createBodyList(GL2 gl, int frame){
		gl.glNewList(bodylist+frame, GL2.GL_COMPILE);
		gl.glPushMatrix();
		
		// rotate the body each frame so it looks like it is swimming
		if (frame < NUM_FRAMES/2){// first half
			if (frame < NUM_FRAMES/4){ // subdivide animation so it is symetric
				float rotate = 5.0f / (NUM_FRAMES/4);
				rotate *= frame;
				gl.glRotatef(-rotate, 0, 1, 0);
			} else { 
				float rotate = 5.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4);
				rotate += 5;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		} else {// second half so it moves back and forth
			
			if (frame < (NUM_FRAMES/4 + NUM_FRAMES/2)){
				float rotate = 5.0f / (NUM_FRAMES/4);
				rotate *= (frame - NUM_FRAMES/2);
				gl.glRotatef(rotate, 0, 1, 0);
			} else{
				float rotate = 5.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
				rotate += 5;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		}
		
		// create the body
		gl.glPushMatrix();
		gl.glScalef(0.4f, 0.25f, 1);
		glut.glutSolidSphere(1, 20, 20);
		gl.glPopMatrix();
		
		// draw the legs
		buildLegs(gl, frame);
		
		gl.glPushMatrix();
		// define the position and scaling for the tail
		gl.glScalef(1, 1, -1);
		gl.glTranslatef(0, 0, 0.5f);
		if (frame < NUM_FRAMES/2){ // animate the tail to move back and forth
			if (frame < NUM_FRAMES/4){
				float rotate = 15.0f / (NUM_FRAMES/4);
				rotate *= frame;
				gl.glRotatef(rotate, 0, 1, 0);
			} else {
				float rotate = 15.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4);
				rotate += 15;
				gl.glRotatef(rotate, 0, 1, 0);
			}
		} else {
			
			if (frame < (NUM_FRAMES/4 + NUM_FRAMES/2)){
				float rotate = 15.0f / (NUM_FRAMES/4);
				rotate *= (frame - NUM_FRAMES/2);
				gl.glRotatef(-rotate, 0, 1, 0);
			} else{
				float rotate = 15.0f / (NUM_FRAMES/4);
				rotate *= -1 * (frame - NUM_FRAMES/4 - NUM_FRAMES/2);
				rotate += 15;
				gl.glRotatef(-rotate, 0, 1, 0);
			}
		}
		// draw the tail
		gl.glCallList(tailList[frame]);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		gl.glTranslatef(0, 0, 0.85f);
		gl.glRotatef(-30, 1, 0, 0);
		
		
		// draw the head
		gl.glCallList(headlist[frame]);
		
		gl.glPopMatrix();
		
		gl.glPopMatrix();
		gl.glEndList();
	}
	
	// each frame increment the lizard's current frame to animate it
	public void update(GL2 gl){
		current_frame++;
		current_frame %= NUM_FRAMES*2;
	}
	
	// draws the lizard
	public void draw(GL2 gl){
		gl.glPushMatrix();
		
		gl.glColor3f(0, 0.25f, 0);
		// change frames at half the actual speed
		gl.glCallList(bodylist+current_frame/2);
		gl.glPopMatrix();
	}
}
