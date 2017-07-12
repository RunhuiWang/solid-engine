

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Visualization extends JFrame{
	
	// map from ID to coordinate
	private HashMap<Integer, Coordinate> mMap;
	private ArrayList<Trajectory> mList;
	
	// panel
	private TPanel mPanel;
	private int minX;
	private int minY;
	private int maxX;
	private int maxY;
	
	private int width;
	private int height;
	
	private double scaleRatioX = 1;
	private double scaleRatioY = 1;
	
	private int preferredPanelWidth = 800;
	private int preferredPanelHeight = 600;
	
	/**
	 * Read road segments mid point file and map the id with mid point
	 */
	private void loadMap() {
		// create a new map
		mMap = new HashMap<>();
        // read road segment points file
        File file = new File("roadsegmentmidpoint");
        try {
            Scanner scLine = new Scanner(file);
            while (scLine.hasNextLine()) {
                // read each line
                String line = scLine.nextLine();
                String[] data = line.split(",");
                // get id, x, y
                int id = Integer.parseInt(data[0]);
                int x = Integer.parseInt(data[5]);
                int y = Integer.parseInt(data[6]);
                
                // create Coordinate instance
                int[] values = {x,y};
                Coordinate coordinate = new Coordinate(values);
                mMap.put(id, coordinate);
            }
            scLine.close();
            
        } catch (FileNotFoundException e) {
            System.err.println("Failed to open " + file+ " :" + e.getMessage());
            System.exit(1);
        }
        print(mMap.size()+"");
	}
	/**
	 * read trajectory file and load them into the list
	 */
	private void loadTrajectories() {
		
		// create a new list
		mList = new ArrayList<>();
        // read road segment points file
        File file = new File("trajectoryset");
        
        try {
            Scanner scLine = new Scanner(file);
            // discard the first line
            scLine.nextLine();
            
            while (scLine.hasNextLine()) {
                // read each line
                String line = scLine.nextLine();
                String[] data = line.split(",");
                
                // get id
                int id = Integer.parseInt(data[0]);
                // get trajectory length
                int length = Integer.parseInt(data[1]);
                
                // get location id list
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 1; i <= length; i ++) {
                	// some trajectories contain a "*"
                	if (data[2].equals("*")) {
                		list.add(Integer.parseInt(data[i+i+1]));
                	} else {
                		list.add(Integer.parseInt(data[i+i]));
                	}
                }
                
                // create a Trajectory instance
                Trajectory traj = new Trajectory(id, list);
                mList.add(traj);
                
            }
            scLine.close();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to open " + file);
            System.exit(1);
        }
        print(mList.size()+"");
	}
	
	/**
	 * Find the boundary of the map
	 */
	private void findMapBoundary() {
		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;
		
		for (Coordinate c : mMap.values()) {
			int x = c.getValue().get(0);
			int y = c.getValue().get(1);
			minX = x < minX ? x : minX;
			minY = y < minY ? y : minY;
			maxX = x > maxX ? x : maxX;
			maxY = y > maxY ? y : maxY;
		}
		
		width = maxX - minX;
		height = maxY - minY;
		
		// set scale ratio
		scaleRatioX = width / preferredPanelWidth;
		scaleRatioY = height / preferredPanelHeight;
		
		print("("+minX+","+minY+") ("+maxX+","+maxY+") Rect : " 
				+ width + " x " + height);
		print("scale ratio : "+ scaleRatioX + " "+ scaleRatioY);
	}
	
	private void findTrajectoriesBoundary() {
		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;
		
		for (Trajectory traj : mList) {
			// traverse each coordinate in the trajectory
			for (Integer id : traj.getList()) {
				Coordinate c = mMap.get(id);
				//System.out.println(c.toString());
				int x = c.getValue().get(0);
				int y = c.getValue().get(1);
				minX = x < minX ? x : minX;
				minY = y < minY ? y : minY;
				maxX = x > maxX ? x : maxX;
				maxY = y > maxY ? y : maxY;
			}
		}
		
		width = maxX - minX;
		height = maxY - minY;
		
		// set scale ratio
		scaleRatioX = (double)width / (double)preferredPanelWidth;
		scaleRatioY = (double)height / (double)preferredPanelHeight;
		
		scaleRatioX = scaleRatioX > 0 ? scaleRatioX : 1;
		scaleRatioY = scaleRatioY > 0 ? scaleRatioY : 1;
				
		print("("+minX+","+minY+") ("+maxX+","+maxY+") Rect : " 
						+ width + " x " + height);
		print("scale ratio : "+ scaleRatioX + " "+ scaleRatioY);
	}
	/**
	 * create an panel for drawing
	 */
	private void initComponents() {
		mPanel = new TPanel();
		mPanel.setPreferredSize(
				new Dimension(preferredPanelWidth, preferredPanelHeight));
		
		this.setContentPane(mPanel);
		// *** frame size includes the task bar, so use panel to set the size
		//this.setPreferredSize(new Dimension(1400,400));
		this.pack();
		this.setVisible(true);
	}
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		Visualization vis = new Visualization();
		
		vis.loadMap();
		vis.loadTrajectories();
		vis.findTrajectoriesBoundary();
		vis.initComponents();
	}
	
	/**
	 * The coordinate of a certain location
	 * @author richie
	 *
	 */
	private class Coordinate {
		private ArrayList<Integer> value;
		/**
		 * Constructor with two parameters
		 * @param dimension
		 * @param values
		 */
		public Coordinate(int[] values) {
			setValue(new ArrayList<>());
			for (int id : values) {
				value.add(id);
			}
		}
		/**
		 * @return the value
		 */
		public ArrayList<Integer> getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(ArrayList<Integer> value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value.get(0)+" "+value.get(1);
		}
	}
	
	/**
	 * A trajectory
	 * @author richie
	 *
	 */
	private class Trajectory {
		private int id;
		private LinkedList<Integer> list;
		/**
		 * Constructor with two parameters
		 * @param id
		 * @param list
		 */
		public Trajectory(int id, LinkedList<Integer> list) {
			this.id = id;
			this.setList(list);
		}
		/**
		 * @return the list
		 */
		public LinkedList<Integer> getList() {
			return list;
		}
		/**
		 * @param list the list to set
		 */
		public void setList(LinkedList<Integer> list) {
			this.list = list;
		}
		
		
	}
	/**
	 * Utility function to output information
	 * @param s the string to be printed
	 */
	private void print(String s) {
        System.out.println(s);
    }
	
	/**
	 * Use this panel to draw lines
	 * @author richie
	 *
	 */
	private class TPanel extends JPanel {
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Coordinate start = null;
			Coordinate end = null;
			//g.drawLine(0, 0, 200, 200);
			
			// traverse all trajectories
			for (Trajectory traj : mList) {
				// traverse each coordinate in the trajectory
				for (Integer id : traj.getList()) {
					Coordinate c = mMap.get(id);
					//System.out.println(c.toString());
					if (start != null) {
						end = start;
						start = c;
						int x1 = (int) ((start.getValue().get(0) - minX) / scaleRatioX);
						int y1 = (int) ((start.getValue().get(1) - minY) / scaleRatioY);
						int x2 = (int) ((end.getValue().get(0) - minX) / scaleRatioX);
						int y2 = (int) ((end.getValue().get(1) - minY) / scaleRatioY);
						g.drawLine(x1, y1, x2, y2);
						//System.out.println("("+x1+","+y1+") ("+x2+","+y2 +")");
					}
					else {
						start = c;
					}
				}
			}
		}
	}
}
