/*
 * Robert Conner McManus
 * PA 3
 * 11/5/14
 * 
 * Food.java
 * 
 * This class defines the simple food that the Water Bugs eat
 */

import javax.media.opengl.*;

public class Food {

	// all that is needed is the display list for the food
	private int foodlist;
	
	public Food(){
		
	}
	
	public void init(GL2 gl){
		foodlist = gl.glGenLists(1);
		
		gl.glNewList(foodlist, GL2.GL_COMPILE);
		gl.glPushMatrix();
		// colors the food brown
		gl.glColor3f(0.647f, 0.1647f, 0.1647f);
		
		// creates the food simply as a triangle
		gl.glBegin(GL2.GL_TRIANGLES);
			
		gl.glVertex3f(0, 0.025f, 0);
		gl.glVertex3f(0.025f, -0.025f, 0);
		gl.glVertex3f(-0.025f, -0.025f, 0);
		
		gl.glEnd();
		
		gl.glPopMatrix();
		
		gl.glEndList();
	}

	// draws the food to the screen
	public void draw(GL2 gl){
		gl.glPushMatrix();
		gl.glCallList(foodlist);
		gl.glPopMatrix();
	}
}
