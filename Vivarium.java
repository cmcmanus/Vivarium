/*
 * Robert Conner McManus
 * PA3
 * 11/5/14
 * 
 * Vivarium.java
 * This class creates the vivarium and all its inhabitants
 * it handles functionality to move the inhabitants around,
 * detect collisions between inhabitants, determine where each
 * inhabitant should move, and create food for the inhabitants to
 * eat
 */

import javax.media.opengl.*;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.util.*;

public class Vivarium
{
	private Tank tank;
	
	// variables to hold the information needed for the lizards
	private float[][] lizardloc;
	private float[][] lizardface;
	private Lizard lizards[];
	
	// variables to hold information for the bugs
	private ArrayList<Float[]> bugloc;
	private ArrayList<Float[]> bugface;
	private ArrayList<WaterBug> waterbugs;
	
	// variables to hold information for the food
	private Food food;
	private ArrayList<Float[]> foodloc;
	
	// defines how much of each item to create
	private final int NUM_LIZARDS = 0;
	private final int NUM_BUGS = 7;
	private final int NUM_FOOD_CREATE = 10;
	
	// defines the dimensions of the tank
	private final float tankx = 4.0f;
	private final float tanky = 4.0f;
	private final float tankz = 4.0f;
	
	// defines the variables needed to place and move the bug around
	private final float bugscale = 0.125f;
	private final float bugspeed = 0.005f;
	private final double bugpredatorweight = -3000;
	private final float bugfoodweight = 10000;
	private final float bugrandomweight = 0.000001f;
	
	// defines variables for flocking
	private final float flockdistance = 0.5f;
	private final float flockdistanceweight = 1;
	private final float flockpointweight = 1;
	private final float flockfaceweight = 0.25f;
	
	// weight to keep inhabitants out of the corner
	private final float cornerweight = -1000;
	
	// a goal point for the creatures if no food exists
	private float goalpoint[];
	
	// defines variables needed for placement and movement of the lizards
	private final float lizardscale = 0.25f;
	private final float lizardspeed = 0.0055f;
	private final double lizardpreyweight = 10000;
	
	// speed the food falls to the floor
	private final float foodspeed = 0.0025f;
	
	private GLUT glut;

	public Vivarium(){
		
		glut = new GLUT();
		
		// creates the tank the animals inhabit
		tank = new Tank( tankx, tanky, tankz);
		
		// creates a new food object which will serve as the image of all the food
		food = new Food();
		// an array to hold the location of each thing of food
		foodloc = new ArrayList<Float[]>();
		
		// array to hold random goal point
		goalpoint = new float[3];
		
		// creates the lizard location and facing arrays
		lizards = new Lizard[NUM_LIZARDS];
		lizardloc = new float[NUM_LIZARDS][3];
		lizardface = new float[NUM_LIZARDS][2];
		for (int i = 0; i < NUM_LIZARDS; i++){ // each lizard gets a random location and facing as well as start frame
			lizardloc[i][0] = (float) (Math.random() * tankx) - tankx/2;
			lizardloc[i][1] = (float) (Math.random() * tanky) - tanky/2;
			lizardloc[i][2] = (float) (Math.random() * tankz) - tankz/2;
			lizardface[i][0] = (float) Math.random() * 360;
			lizardface[i][1] = (float) Math.random() * 90 - 45;
			lizards[i] = new Lizard((int) (Math.random()*300));
		}
    
		// creates the bug location and facing array lists
		bugloc = new ArrayList<Float[]>();
		bugface = new ArrayList<Float[]>();
		waterbugs = new ArrayList<WaterBug>();
		for (int i = 0; i < NUM_BUGS; i++){ // each bug has a random location and facing
			Float loc[] = new Float[3];
			loc[0] = (float) (Math.random() * tankx) - tankx/2;
			loc[1] = (float) (Math.random() * tanky) - tanky/2;
			loc[2] = (float) (Math.random() * tankz) - tankz/2;
			bugloc.add(loc);
			Float face[] = new Float[2];
			face[0] = (float) Math.random() * 360;
			face[1] = (float) Math.random() * 90 - 45;
			bugface.add(face);
			waterbugs.add(new WaterBug((int) (Math.random()*300)));
		}
	}

	// creates food around the specified x and z
	public void createFood(float x, float y, float z){
		for (int i = 0; i < NUM_FOOD_CREATE; i++){ // creates a number of food equal to the constant
			Float[] pos = new Float[3];
			pos[0] = (float) (Math.random() * 1 - 0.5 + x); // each food gets a random location around the point
			pos[1] = (float) (Math.random() * 1 - 0.5 + tanky/2);
			pos[2] = (float) (Math.random() * 1 - 0.5 + z);
			foodloc.add(pos);
		}
	}

	// initializes each of the inhabitants
	public void init(GL2 gl){
		tank.init( gl );
		food.init(gl);
		for (WaterBug w : waterbugs){
			w.init(gl);
		}
		for (Lizard l : lizards){
			l.init(gl);
		}
	}

	// function to decide which direction each bug should travel
	private void preyMoveUpdate(int bug){
		
		// keeps an updated set of points representing which direction is the best to travel in
		double maxscore = Double.NEGATIVE_INFINITY;
		float newyaw = bugface.get(bug)[0];
		float newpitch = bugface.get(bug)[1];
		float newx = bugloc.get(bug)[0];
		float newy = bugloc.get(bug)[1];
		float newz = bugloc.get(bug)[2];
		
		// iterates through partial sphere in front of the creature 120 degrees from side to side and 90 degrees up and down
		for (int i = 0; i <= 120; i+= 1){
			for (int j = 0; j <= 90; j+= 1){
				// sets the pitch and yaw for the new position
				float yaw = i - 60 + bugface.get(bug)[0];
				float pitch = j - 45 + bugface.get(bug)[1];
				pitch %= 360;
				
				// gets the coordinates the creature will be at at the new location
				float xpos = (float) (bugspeed * Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)));
				xpos += bugloc.get(bug)[0];
				float ypos = (float) (bugspeed * Math.sin(Math.toRadians(pitch)));
				ypos += bugloc.get(bug)[1];
				float zpos = (float) (bugspeed * Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)));
				zpos += bugloc.get(bug)[2];
				
				// if the new position would be outside the tank, reject it
				if (xpos >= tankx/2 || xpos <= -tankx/2)
					continue;
				else if (ypos >= tanky/2 || ypos <= -tanky/2)
					continue;
				else if (zpos >= tankz/2 || zpos <= -tankz/2)
					continue;
				
				double score = 0;
				
				// reduces the score based on the distance to each lizard, closer to the lizard produces a greater reduction
				for (int p = 0; p < NUM_LIZARDS; p++){
					float xdis = xpos - lizardloc[p][0];
					float ydis = ypos - lizardloc[p][1];
					float zdis = zpos - lizardloc[p][2];
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					score += bugpredatorweight / distance;
				}
				
				// increases the score based on the distance to food, closer to food increases the score
				for (Float[] f : foodloc){
					float xdis = xpos - f[0];
					float ydis = ypos - f[1];
					float zdis = zpos - f[2];
					
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					score += bugfoodweight / distance;
				}
				
				// if there is no longer any food, create an arbitrary goal point to swim to
				if (foodloc.size() == 0){
					
					float xdis = xpos - goalpoint[0];
					float ydis = ypos - goalpoint[1];
					float zdis = zpos - goalpoint[2];
					
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					// if the bug is already close to the goal point, create a new goal
					if (distance <= 0.05){
						goalpoint[0] = (float) (Math.random() * tankx - tankx/2);
						goalpoint[1] = (float) (Math.random() * tanky - tanky/2);
						goalpoint[2] = (float) (Math.random() * tankz - tankz/2);
						
						xdis = xpos - goalpoint[0];
						ydis = ypos - goalpoint[1];
						zdis = zpos - goalpoint[2];
						
						distance = xdis*xdis + ydis*ydis + zdis*zdis;
						distance = Math.sqrt(distance);
					}
					
					// treat the goal as food for the purposes of increasing the score
					score += bugfoodweight / distance;
				}
				
				// variables set up for flocking
				
				float distsum = 0;
				float curdistsum = 0;
				float xsum = 0;
				float ysum = 0;
				float zsum = 0;
				
				float pitchsum = 0;
				float yawsum = 0;
				
				int num_bugs = 0;
				
				// iterate through each of the other waterbugs
				for (int k = 0; k < waterbugs.size(); k++){
					if (k == bug) // if the bug is the current bug, do not consider it
						continue;
					
					// get the distance to that bug
					float xdis = xpos - bugloc.get(k)[0];
					float ydis = ypos - bugloc.get(k)[1];
					float zdis = zpos - bugloc.get(k)[2];
					
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					if (distance <= flockdistance){ // if the bug is within flocking distance, try to flock with it
						distsum += distance;
						
						xdis = bugloc.get(bug)[0] - bugloc.get(k)[0];
						ydis = bugloc.get(bug)[1] - bugloc.get(k)[1];
						zdis = bugloc.get(bug)[2] - bugloc.get(k)[2];
						distance = xdis*xdis + ydis*ydis + zdis*zdis;
						// add the distance to a running total to get average distance
						distance = Math.sqrt(distance);
						curdistsum += distance;
						
						// add the location of each of the bugs to running total for average point
						xsum += bugloc.get(k)[0];
						ysum += bugloc.get(k)[1];
						zsum += bugloc.get(k)[2];
						
						// add facing of each bug to running total for average facing
						pitchsum += bugface.get(k)[0];
						yawsum += bugface.get(k)[1];
						
						// increment the number of bugs
						num_bugs++;
					}
				}
				
				// don't consider flocking if there are no bugs within flocking distance
				if (num_bugs != 0){
					
					// find the average distance, point, and facing
					distsum /= num_bugs;
					xsum /= num_bugs;
					ysum /= num_bugs;
					zsum /= num_bugs;
					pitchsum /= num_bugs;
					yawsum /= num_bugs;
					
					// find the average distance to each flocking bug at the current location
					float distancesum = 0;
					
					for (int k = 0; k < waterbugs.size(); k++){
						float xdis = xpos - bugloc.get(k)[0];
						float ydis = ypos - bugloc.get(k)[1];
						float zdis = zpos - bugloc.get(k)[2];
						
						double distance = xdis*xdis + ydis*ydis + zdis*zdis;
						distance = Math.sqrt(distance);
						if (distance <= flockdistance){
							distancesum += Math.abs(distance - distsum);
						}
					}
					
					distancesum /= num_bugs;
					
					// add to score the distance weight altered by how the distance between the bugs will change, less change produces a higher score
					score += flockdistanceweight / Math.abs(distancesum - curdistsum);
					
					// find the distance from the point to the average flock point
					float xdis = xpos - xsum;
					float ydis = ypos - ysum;
					float zdis = zpos - zsum;
					
					float distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = (float) Math.sqrt(distance);
					
					// add it to the score by weight, closer to the average point produces higher scores
					score += flockpointweight / distance;
					
					// determine the distance between the posited facing and the average facing
					float facemag = yaw*yaw + pitch*pitch;
					facemag = (float) Math.sqrt(facemag);
					float checkyaw = yaw / facemag;
					float checkpitch = pitch / facemag;
					facemag = yawsum * yawsum + pitchsum * pitchsum;
					facemag = (float) Math.sqrt(facemag);
					pitchsum /= facemag;
					yawsum /= facemag;
					
					xdis = yawsum - checkyaw;
					ydis = pitchsum - checkpitch;
					distance = xdis * xdis + ydis * ydis;
					distance = (float) Math.sqrt(distance);
					
					// closer to average facing will yield a higher score
					score += flockfaceweight / distance;
				}
				
				// reduce the score based on the distance to each corner, the closer the bug is, the more it is reduced
				for (int p = -1; p <= 1; p+=2){
					for (int q = -1; q <= 1; q += 2){
						for (int r = -1; r <= 1; r += 2){
							float xdis = xpos - p*tankx/2;
							float ydis = ypos - q*tanky/2;
							float zdis = zpos - r*tankz/2;
							float distance = xdis*xdis + ydis*ydis + zdis*zdis;
							distance = (float) Math.sqrt(distance);
							score += cornerweight / distance;
						}
					}
				}
				
				// add some random weight to the score
				score += Math.random() * bugrandomweight;
				
				// if the score is greater than the current high score, update which position to choose
				if (score > maxscore){
					maxscore = score;
					newx = xpos;
					newy = ypos;
					newz = zpos;
					newpitch = pitch;
					newyaw = yaw;
				}
			}
		}
		
		// update position and facing to move to the point of highest score
		bugloc.get(bug)[0] = newx;
		bugloc.get(bug)[1] = newy;
		bugloc.get(bug)[2] = newz;
		bugface.get(bug)[0] = newyaw;
		bugface.get(bug)[1] = newpitch;
	}

	// function to decide which direction each lizard should travel
	private void predatorMoveUpdate(int lizard){
		
		// variables to hold the current best point to move to
		double maxscore = Double.NEGATIVE_INFINITY;
		float newyaw = 0;
		float newpitch = 0;
		float newx = 0;
		float newy = 0;
		float newz = 0;
		
		// iterates through partial sphere in front of the creature 120 degrees from side to side and 90 degrees up and down
		for (int i = 0; i < 120; i+= 1){
			for (int j = 0; j < 90; j+= 1){
				
				// get the new facing of the lizard
				float yaw = i - 60 + lizardface[lizard][0];
				float pitch = j - 45 + lizardface[lizard][1];
				pitch %= 360;
				
				// get the new location of the lizard
				float xpos = (float) (lizardspeed * Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)));
				xpos += lizardloc[lizard][0];
				float ypos = (float) (lizardspeed * Math.sin(Math.toRadians(pitch)));
				ypos += lizardloc[lizard][1];
				float zpos = (float) (lizardspeed * Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)));
				zpos += lizardloc[lizard][2];
				double score = 0;
				
				// reject the location if it is outside the tank
				if (xpos >= tankx/2 || xpos <= -tankx/2)
					continue;
				else if (ypos >= tanky/2 || ypos <= -tanky/2)
					continue;
				else if (zpos >= tankz/2 || zpos <= -tankz/2)
					continue;
				
				// increase the score based on the distance to each waterbug, the closer it is to a bug, the higher the score
				for (int p = 0; p < waterbugs.size(); p++){
					float xdis = xpos - bugloc.get(p)[0];
					float ydis = ypos - bugloc.get(p)[1];
					float zdis = zpos - bugloc.get(p)[2];
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					score += lizardpreyweight / distance;
				}
				
				// if there are no water bugs, create a goal point for the lizard to move to
				if (waterbugs.size() == 0){
					
					// get the distance to the goal point
					float xdis = xpos - goalpoint[0];
					float ydis = ypos - goalpoint[1];
					float zdis = zpos - goalpoint[2];
					
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					
					// if the lizard is already close to the goal point, create a new one
					if (distance <= 0.1){
						goalpoint[0] = (float) (Math.random() * tankx - tankx/2);
						goalpoint[1] = (float) (Math.random() * tanky - tanky/2);
						goalpoint[2] = (float) (Math.random() * tankz - tankz/2);
						
						xdis = xpos - goalpoint[0];
						ydis = ypos - goalpoint[1];
						zdis = zpos - goalpoint[2];
						
						distance = xdis*xdis + ydis*ydis + zdis*zdis;
						distance = Math.sqrt(distance);
					}
					
					// treat the goal point as prey for the lizard when calculating score
					score += lizardpreyweight / distance;
				}
				
				// set up flocking variables
				float distsum = 0;
				float curdistsum = 0;
				float xsum = 0;
				float ysum = 0;
				float zsum = 0;
				
				float pitchsum = 0;
				float yawsum = 0;
				
				int num_lizards = 0;
				
				// iterate through each of the other lizards
				for (int k = 0; k < lizards.length; k++){
					if (k == lizard) // if the lizard is the current lizard, do not consider it
						continue;
					
					// find the average distance, point, and facing
					float xdis = xpos - lizardloc[k][0];
					float ydis = ypos - lizardloc[k][1];
					float zdis = zpos - lizardloc[k][2];
					
					double distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = Math.sqrt(distance);
					if (distance <= flockdistance){ // if the lizard is within flocking distance, try to flock with it
						distsum += distance;
						
						xdis = lizardloc[lizard][0] - lizardloc[k][0];
						ydis = lizardloc[lizard][1] - lizardloc[k][1];
						zdis = lizardloc[lizard][2] - lizardloc[k][2];
						
						// add the distance to a running total to get average distance
						distance = xdis*xdis + ydis*ydis + zdis*zdis;
						distance = Math.sqrt(distance);
						curdistsum += distance;
						
						// add the location of each of the lizards to running total for average point
						xsum += lizardloc[k][0];
						ysum += lizardloc[k][1];
						zsum += lizardloc[k][2];
						
						// add facing of each lizard to running total for average facing
						pitchsum += lizardface[k][0];
						yawsum += lizardface[k][1];
						
						// increment the number of bugs
						num_lizards++;
					}
				}
				
				// don't consider flocking if there are no lizards within flocking distance
				if (num_lizards != 0){
					
					// find the average distance, point, and facing
					distsum /= num_lizards;
					xsum /= num_lizards;
					ysum /= num_lizards;
					zsum /= num_lizards;
					pitchsum /= num_lizards;
					yawsum /= num_lizards;
					
					float distancesum = 0;
					
					// find the average distance to each flocking bug at the current location
					for (int k = 0; k < lizards.length; k++){
						float xdis = xpos - lizardloc[k][0];
						float ydis = ypos - lizardloc[k][1];
						float zdis = zpos - lizardloc[k][2];
						
						double distance = xdis*xdis + ydis*ydis + zdis*zdis;
						distance = Math.sqrt(distance);
						if (distance <= flockdistance){
							distancesum += Math.abs(distance - distsum);
						}
					}
					
					distancesum /= num_lizards;
					
					// add to score the distance weight altered by how the distance between the lizards will change, less change produces a higher score
					score += flockdistanceweight / Math.abs(distancesum - curdistsum);
					
					// find the distance from the point to the average flock point
					float xdis = xpos - xsum;
					float ydis = ypos - ysum;
					float zdis = zpos - zsum;
					
					float distance = xdis*xdis + ydis*ydis + zdis*zdis;
					distance = (float) Math.sqrt(distance);
					
					// add it to the score by weight, closer to the average point produces higher scores
					score += flockpointweight / distance;
					
					// determine the distance between the posited facing and the average facing
					float facemag = yaw*yaw + pitch*pitch;
					facemag = (float) Math.sqrt(facemag);
					float checkyaw = yaw / facemag;
					float checkpitch = pitch / facemag;
					facemag = yawsum * yawsum + pitchsum * pitchsum;
					facemag = (float) Math.sqrt(facemag);
					pitchsum /= facemag;
					yawsum /= facemag;
					
					xdis = yawsum - checkyaw;
					ydis = pitchsum - checkpitch;
					distance = xdis * xdis + ydis * ydis;
					distance = (float) Math.sqrt(distance);
					
					// closer to average facing will yield a higher score
					score += flockfaceweight / distance;
				}
				
				// reduce the score based on the distance to each corner, the closer the lizard is, the more it is reduced
				for (int p = -1; p <= 1; p+=2){
					for (int q = -1; q <= 1; q += 2){
						for (int r = -1; r <= 1; r += 2){
							float xdis = xpos - p*tankx/2;
							float ydis = ypos - q*tanky/2;
							float zdis = zpos - r*tankz/2;
							float distance = xdis*xdis + ydis*ydis + zdis*zdis;
							distance = (float) Math.sqrt(distance);
							score += cornerweight / distance;
						}
					}
				}
				
				// if the score is greater than the current high score, update which position to choose
				if (score > maxscore){
					maxscore = score;
					newx = xpos;
					newy = ypos;
					newz = zpos;
					newpitch = pitch;
					newyaw = yaw;
				}
			}
		}
		
		// update position and facing to move to the point of highest score
		lizardloc[lizard][0] = newx;
		lizardloc[lizard][1] = newy;
		lizardloc[lizard][2] = newz;
		lizardface[lizard][0] = newyaw;
		lizardface[lizard][1] = newpitch;
	}

	// detects if the prey has collided with some object
	private void preyCollisionDetect(){
		// iterates through each bug to determine if a collision has occurred
		for (int i = 0; i < waterbugs.size(); i++){
			// get the bounding box
			float box[] = waterbugs.get(i).getBoundingBox();
			float temp[] = new float[3];
			float coords[] = new float[3];
			
			// rotate the box so that it is in world coordinates
			temp[0] = (float) (box[0] * Math.cos(Math.toRadians(bugface.get(i)[0])) + box[2] * Math.sin(Math.toRadians(bugface.get(i)[0])));
			temp[1] = box[1];
			temp[2] = (float) (box[2] * Math.cos(Math.toRadians(bugface.get(i)[0])) - box[2] * Math.sin(Math.toRadians(bugface.get(i)[0])));
			coords[0] = temp[0] * bugscale;
			coords[1] = (float) (temp[1] * Math.cos(Math.toRadians(-bugface.get(i)[1])) - temp[2] * Math.sin(Math.toRadians(-bugface.get(i)[1]))) * bugscale;
			coords[2] = (float) (temp[2] * Math.cos(Math.toRadians(-bugface.get(i)[1])) + temp[1] * Math.sin(Math.toRadians(-bugface.get(i)[1]))) * bugscale;
			
			// make each edge positive
			coords[0] = Math.abs(coords[0]);
			coords[1] = Math.abs(coords[1]);
			coords[2] = Math.abs(coords[2]);
			
			int j = 0;
			
			// iterate through each of the food to determine a collision
			while (j < foodloc.size()){
				Float loc[] = foodloc.get(j++);
				if (loc[0]+0.025 > coords[0]+bugloc.get(i)[0] && loc[0]-0.025 > coords[0]+bugloc.get(i)[0]) // above x
					continue;
				if (loc[0]+0.025 < bugloc.get(i)[0]-coords[0] && loc[0]-0.025 < bugloc.get(i)[0]-coords[0]) // below x
					continue;
				if (loc[1]+0.025 > coords[1]+bugloc.get(i)[1] && loc[1]-0.025 > bugloc.get(i)[1]+coords[1]) // above y
					continue;
				if (loc[1]+0.025 < bugloc.get(i)[1]-coords[1] && loc[1]-0.025 < bugloc.get(i)[1]-coords[1]) // below y
					continue;
				if (loc[2] > coords[2]+bugloc.get(i)[2] || loc[2] < bugloc.get(i)[2]-coords[2]) // above or below z
					continue;
				// if it makes it here, there was a collision
				foodloc.remove(--j); // remove the food the bug hit
			}
			
			// iterate through each of the waterbugs to determine if there was a collision
			for (int k = 0; k < waterbugs.size(); k++){
				if (k == i) // ignore the bug if it is the current bug
					continue;
				
				// retrieve the bounding box for the bug
				float collidebox[] = waterbugs.get(k).getBoundingBox();
				float collidecoords[] = new float[3];
				
				// rotate the box to world coordinates
				temp[0] = (float) (collidebox[0] * Math.cos(Math.toRadians(bugface.get(k)[0])) + collidebox[2] * Math.sin(Math.toRadians(bugface.get(k)[0])));
				temp[1] = collidebox[1];
				temp[2] = (float) (collidebox[2] * Math.cos(Math.toRadians(bugface.get(k)[0])) - collidebox[2] * Math.sin(Math.toRadians(bugface.get(k)[0])));
				collidecoords[0] = temp[0] * bugscale;
				collidecoords[1] = (float) (temp[1] * Math.cos(Math.toRadians(-bugface.get(k)[1])) - temp[2] * Math.sin(Math.toRadians(-bugface.get(k)[1]))) * bugscale;
				collidecoords[2] = (float) (temp[2] * Math.cos(Math.toRadians(-bugface.get(k)[1])) + temp[1] * Math.sin(Math.toRadians(-bugface.get(k)[1]))) * bugscale;
				
				// make all coordinates positive
				collidecoords[0] = Math.abs(collidecoords[0]);
				collidecoords[1] = Math.abs(collidecoords[1]);
				collidecoords[2] = Math.abs(collidecoords[2]);
				
				Float loc[] = bugloc.get(k);
				if (loc[0]+collidecoords[0] > coords[0]+bugloc.get(i)[0] && loc[0]-collidecoords[0] > coords[0]+bugloc.get(i)[0])// above x
					continue;
				if (loc[0]+collidecoords[0] < bugloc.get(i)[0]-coords[0] && loc[0]-collidecoords[0] < bugloc.get(i)[0]-coords[0]) // below x
					continue;
				if (loc[1]+collidecoords[1] > coords[1]+bugloc.get(i)[1] && loc[1]-collidecoords[1] > bugloc.get(i)[1]+coords[1]) // above y
					continue;
				if (loc[1]+collidecoords[1] < bugloc.get(i)[1]-coords[1] && loc[1]-collidecoords[1] < bugloc.get(i)[1]-coords[1]) // below y
					continue;
				if (loc[2]+collidecoords[2] > coords[2]+bugloc.get(i)[2] && loc[2]-collidecoords[2] > bugloc.get(i)[2]+coords[2]) // above z
					continue;
				if (loc[2]+collidecoords[2] < bugloc.get(i)[2]-coords[2] && loc[2]-collidecoords[2] < bugloc.get(i)[2]-coords[2]) // below z
					continue;
				
				// collision if it reaches here
				float distance = Math.abs(loc[2] - bugloc.get(i)[2]);
				float dif = collidecoords[2] + coords[2] - distance;
				
				bugloc.get(k)[2] += dif; // move the bug hit outside the collision zone
			}
			// if the current bug hit the edge of the tank, turn it around
			if (bugloc.get(i)[0] >= tankx/2-0.01 || bugloc.get(i)[0] <= -tankx/2+0.01){
				bugface.get(i)[0] = -bugface.get(i)[0];
				bugface.get(i)[1] = -bugface.get(i)[1];
			} else if (bugloc.get(i)[1] >= tanky/2-0.01 || bugloc.get(i)[1] <= -tanky/2+0.01){
				bugface.get(i)[0] = -bugface.get(i)[0];
				bugface.get(i)[1] = -bugface.get(i)[1];
			} else if (bugloc.get(i)[2] >= tankz/2-0.01 || bugloc.get(i)[2] <= -tankz/2+0.01){
				bugface.get(i)[0] = -bugface.get(i)[0];
				bugface.get(i)[1] = -bugface.get(i)[1];
			}
		}
	}
	
	// detects if the predator has collided with some object
	private void predatorCollisionDetect(){
		// iterate through each lizard
		for (int i = 0; i < lizards.length; i++){
			// get the bounding box for the lizard
			float box[] = lizards[i].getBoundingBox();
			float temp[] = new float[3];
			float coords[] = new float[3];
			
			// transform the box into world coordinates
			temp[0] = (float) (box[0] * Math.cos(Math.toRadians(lizardface[i][0])) + box[2] * Math.sin(Math.toRadians(lizardface[i][0])));
			temp[1] = box[1];
			temp[2] = (float) (box[2] * Math.cos(Math.toRadians(lizardface[i][0])) - box[2] * Math.sin(Math.toRadians(lizardface[i][0])));
			coords[0] = temp[0] * lizardscale;
			coords[1] = (float) (temp[1] * Math.cos(Math.toRadians(-lizardface[i][1])) - temp[2] * Math.sin(Math.toRadians(-lizardface[i][1]))) * lizardscale;
			coords[2] = (float) (temp[2] * Math.cos(Math.toRadians(-lizardface[i][1])) + temp[1] * Math.sin(Math.toRadians(-lizardface[i][1]))) * lizardscale;
			
			// make each edge positive
			coords[0] = Math.abs(coords[0]);
			coords[1] = Math.abs(coords[1]);
			coords[2] = Math.abs(coords[2]);
			
			int k = 0;
			
			// iterate through each of the waterbugs to determine if there is a collision
			while (k < waterbugs.size()){
				
				// get the bounding box of the bug
				float collidebox[] = waterbugs.get(k).getBoundingBox();
				float collidecoords[] = new float[3];
				
				// transform the box into world coordinates
				temp[0] = (float) (collidebox[0] * Math.cos(Math.toRadians(bugface.get(k)[0])) + collidebox[2] * Math.sin(Math.toRadians(bugface.get(k)[0])));
				temp[1] = collidebox[1];
				temp[2] = (float) (collidebox[2] * Math.cos(Math.toRadians(bugface.get(k)[0])) - collidebox[2] * Math.sin(Math.toRadians(bugface.get(k)[0])));
				collidecoords[0] = temp[0] * bugscale;
				collidecoords[1] = (float) (temp[1] * Math.cos(Math.toRadians(-bugface.get(k)[1])) - temp[2] * Math.sin(Math.toRadians(-bugface.get(k)[1]))) * bugscale;
				collidecoords[2] = (float) (temp[2] * Math.cos(Math.toRadians(-bugface.get(k)[1])) + temp[1] * Math.sin(Math.toRadians(-bugface.get(k)[1]))) * bugscale;
				
				collidecoords[0] = Math.abs(collidecoords[0]);
				collidecoords[1] = Math.abs(collidecoords[1]);
				collidecoords[2] = Math.abs(collidecoords[2]);
				
				Float loc[] = bugloc.get(k);
				k++;
				// check the collision
				if (loc[0]+collidecoords[0] > coords[0]+lizardloc[i][0] && loc[0]-collidecoords[0] > coords[0]+lizardloc[i][0]) // above x
					continue;
				if (loc[0]+collidecoords[0] < lizardloc[i][0]-coords[0] && loc[0]-collidecoords[0] < lizardloc[i][0]-coords[0]) // below x
					continue;
				if (loc[1]+collidecoords[1] > coords[1]+lizardloc[i][1] && loc[1]-collidecoords[1] > lizardloc[i][1]+coords[1]) // above y
					continue;
				if (loc[1]+collidecoords[1] < lizardloc[i][1]-coords[1] && loc[1]-collidecoords[1] < lizardloc[i][1]-coords[1]) // below y
					continue;
				if (loc[2]+collidecoords[2] > coords[2]+lizardloc[i][2] && loc[2]-collidecoords[2] > lizardloc[i][2]+coords[2]) // above z
					continue;
				if (loc[2]+collidecoords[2] < lizardloc[i][2]-coords[2] && loc[2]-collidecoords[2] < lizardloc[i][2]-coords[2]) // below z
					continue;
				
				// there is a collision if it gets here, the bug will be consumed and removed from the drawing
				waterbugs.remove(--k);
				bugface.remove(k);
				bugloc.remove(k);
			}
			
			// iterate through each of the lizards to determine if there is a collision
			for (int j = 0; j < lizards.length; j++){
				if (j == i) // ignore the lizard if it is the current one
					continue;
				// get the bounding box for the lizard
				float collidebox[] = lizards[j].getBoundingBox();
				float collidecoords[] = new float[3];
				
				// transform the bounding box into world coordinates
				temp[0] = (float) (collidebox[0] * Math.cos(Math.toRadians(lizardface[j][0])) + collidebox[2] * Math.sin(Math.toRadians(lizardface[j][0])));
				temp[1] = collidebox[1];
				temp[2] = (float) (collidebox[2] * Math.cos(Math.toRadians(lizardface[j][0])) - collidebox[2] * Math.sin(Math.toRadians(lizardface[j][0])));
				collidecoords[0] = temp[0] * lizardscale;
				collidecoords[1] = (float) (temp[1] * Math.cos(Math.toRadians(-lizardface[j][1])) - temp[2] * Math.sin(Math.toRadians(-lizardface[j][1]))) * lizardscale;
				collidecoords[2] = (float) (temp[2] * Math.cos(Math.toRadians(-lizardface[j][1])) + temp[1] * Math.sin(Math.toRadians(-lizardface[j][1]))) * lizardscale;
				
				//make each edge positive
				collidecoords[0] = Math.abs(collidecoords[0]);
				collidecoords[1] = Math.abs(collidecoords[1]);
				collidecoords[2] = Math.abs(collidecoords[2]);
				
				// detect any collisions
				float loc[] = lizardloc[j];
				if (loc[0]+collidecoords[0] > coords[0]+lizardloc[i][0] && loc[0]-collidecoords[0] > coords[0]+lizardloc[i][0]) // above x
					continue;
				if (loc[0]+collidecoords[0] < lizardloc[i][0]-coords[0] && loc[0]-collidecoords[0] < lizardloc[i][0]-coords[0]) // below x
					continue;
				if (loc[1]+collidecoords[1] > coords[1]+lizardloc[i][1] && loc[1]-collidecoords[1] > lizardloc[i][1]+coords[1]) // above y
					continue;
				if (loc[1]+collidecoords[1] < lizardloc[i][1]-coords[1] && loc[1]-collidecoords[1] < lizardloc[i][1]-coords[1]) // below y
					continue;
				if (loc[2]+collidecoords[2] > coords[2]+lizardloc[i][2] && loc[2]-collidecoords[2] > lizardloc[i][2]+coords[2]) // above z
					continue;
				if (loc[2]+collidecoords[2] < lizardloc[i][2]-coords[2] && loc[2]-collidecoords[2] < lizardloc[i][2]-coords[2]) // below z
					continue;
				
				// there is a collision if it reaches this point
				// collision if it reaches here
				float distance = Math.abs(loc[2] - lizardloc[i][2]);
				float dif = collidecoords[2] + coords[2] - distance;
				
				lizardloc[j][2] += dif; // move the lizard hit outside the collision zone
			}
			
			// if the lizard hits the tank wall, turn it around
			if (lizardloc[i][0] >= tankx/2-0.01 || lizardloc[i][0] <= -tankx/2+0.01){
				lizardface[i][0] = -lizardface[i][0];
				lizardface[i][1] = -lizardface[i][1];
			} else if (lizardloc[i][1] >= tanky/2-0.01 || lizardloc[i][1] <= -tanky/2+0.01){
				lizardface[i][0] = -lizardface[i][0];
				lizardface[i][1] = -lizardface[i][1];
			} else if (lizardloc[i][2] >= tankz/2-0.01 || lizardloc[i][2] <= -tankz/2+0.01){
				lizardface[i][0] = -lizardface[i][0];
				lizardface[i][1] = -lizardface[i][1];
			}
		}
	}
	
	// updates the current scene
	public void update(GL2 gl){
		tank.update( gl );
		
		// causes each food particle to fall
		for (Float[] f : foodloc){
			f[1] -= foodspeed;
		}
		int j = 0;
		// removes food if it falls out of the tank
		while (j < foodloc.size()){
			if (foodloc.get(j)[1] < -tanky/2)
				foodloc.remove(j);
			else
				j++;
		}
		
		// moves each of the bugs and updates its current frame
		for (int i = 0; i < waterbugs.size(); i++){
			preyMoveUpdate(i);
			waterbugs.get(i).update(gl);
		}
		
		// moves each of the lizards and updates its current frame
		for (int i = 0; i < lizards.length; i++){
			predatorMoveUpdate(i);
			lizards[i].update(gl);
		}
		
		// detects any collisions that may have occurred
		predatorCollisionDetect();
		preyCollisionDetect();
	}

	// draws the bugs and lizards to the scene
	private void drawCreatures(GL2 gl){
	  
		// iterates through each water bug
		for (int i = 0; i < waterbugs.size(); i++){
			gl.glPushMatrix();
			// places the bug at its proper location
			gl.glTranslatef(bugloc.get(i)[0], bugloc.get(i)[1], bugloc.get(i)[2]);
			
			// rotates the and scales the bug
			gl.glRotatef(bugface.get(i)[0], 0, 1, 0);
			gl.glRotatef(-bugface.get(i)[1], 1, 0, 0);
			gl.glScalef(bugscale, bugscale, bugscale);
		  
			// draws the bug
			waterbugs.get(i).draw(gl);
			gl.glPopMatrix();
		}
	  
		// iterates through each lizard
		for (int i = 0; i < lizards.length; i++){
			gl.glPushMatrix();
			// places the lizard at the proper location
			gl.glTranslatef(lizardloc[i][0], lizardloc[i][1], lizardloc[i][2]);
			
			// rotates and scales each lizard
			gl.glRotatef(lizardface[i][0], 0, 1, 0);
			gl.glRotatef(-lizardface[i][1], 1, 0, 0);
			gl.glScalef(lizardscale, lizardscale, lizardscale);
		  
			// draws the lizard
			lizards[i].draw(gl);
			gl.glPopMatrix();
		}
	}

	// draws the food
	private void drawFood(GL2 gl){
		
		// iterates through all the food
		for (Float[] f : foodloc){
			gl.glPushMatrix();
			
			// place the food at the proper location
			gl.glTranslatef(f[0], f[1], f[2]);
			
			// draw the food
			food.draw(gl);
			
			gl.glPopMatrix();
		}
		
	}

	// draws the vivarium
	public void draw(GL2 gl){
		tank.draw(gl);
		drawFood(gl);
		drawCreatures(gl);
	}
}
