import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * This widget provides basic controls to drive Robotino.
 */
public class DriveWidget extends JComponent
{
	protected static final float speed = 0.2f;
	protected static final float rotSpeed = 0.5f;
	
	boolean isTimeToLearn = false, isTimeToKnow = false;

	protected enum TourAction{
		LEARNING,
		KNOWLEDGE,
		RESET;
	};
	protected final Robot robot;
	private RobotMaze mazeInfo;
	Timer _timer;
	private float vx;
	private float vy;
	private float omega;
	
	JFileChooser fc;
	
	public DriveWidget(Robot robot, RobotMaze mazeInfo)
	{
		this.robot = robot;
		this.mazeInfo = mazeInfo;
		setLayout(new GridLayout(3, 5));
		add(new JLabel());
		
		JButton buttonLearningTour = new JButton("Learning Tour");
		buttonLearningTour.addActionListener( new ButtonListener(TourAction.LEARNING, this) );
		add(buttonLearningTour);
		JLabel label = new JLabel();
		add(label);
		
		JLabel lblNewLabel_1 = new JLabel("");
		add(lblNewLabel_1);

		fc = new JFileChooser();
		
		setMinimumSize( new Dimension(60, 30) );
		setPreferredSize( new Dimension(200, 120) );
		setMaximumSize( new Dimension(Short.MAX_VALUE, Short.MAX_VALUE) );
		
		JButton btnResetMap = new JButton("Reset Map");
		btnResetMap.addActionListener( new ButtonListener(TourAction.RESET, this) );
		
				add(btnResetMap);
		
		JLabel label_1 = new JLabel();
		add(label_1);
		
		JLabel lblNewLabel = new JLabel("");
		add(lblNewLabel);

	}
	
	
	public void setVelocity(float vx, float vy, float omega)
	{
		this.vx = vx;
		this.vy = vy;
		this.omega = omega;		
		robot.setVelocity( vx, vy, omega );
	}
	
	public void setVelocity_i()
	{	
		robot.setVelocity( this.vx, this.vy, this.omega );
	}
	
//	class OnTimeOut extends TimerTask
//	{
//		public void run()
//		{
//			setVelocity_i();
//		}
//	}
	
	private class ButtonListener implements ActionListener
	{

		private final TourAction typeTour;
		Boolean alreadyKnownTour = new Boolean(false);
		MyRunnable myRunnable = new MyRunnable(robot);
        Thread t = new Thread(myRunnable);
        Thread knownTour = new Thread(new KnowledgeTourRunnable(robot));
        
		private final DriveWidget driveWidget;
		
		public ButtonListener(TourAction type, DriveWidget p){
			this.typeTour = type;
			this.driveWidget = p;
			
		}

			
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(this.typeTour == TourAction.RESET){
				mazeInfo.resetBestPath();
				robot.reset();
				System.out.println("reset");
			}
			else if(this.typeTour == TourAction.LEARNING){
				if(!t.isAlive()){
					t.start();
				}
				isTimeToLearn = true;
			}else if(this.typeTour == TourAction.KNOWLEDGE){
				
				if(!alreadyKnownTour){
				mazeInfo.resetBestPath();
				int returnVal = fc.showOpenDialog(DriveWidget.this);
				 
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                boolean isCorrectFile = false;
	                
	                try {
						Scanner scanner = new Scanner(file);
						
						while(scanner.hasNextLine()){
							String coordinates[] = scanner.nextLine().split(",");
							if(isCorrectFile){
								mazeInfo.updateBestPath((double) Double.parseDouble(coordinates[0]),
										(double) Double.parseDouble(coordinates[1]),
										(double) Double.parseDouble(coordinates[2]));
							}else if(coordinates[0].equals("robot_log")){
								isCorrectFile = true;
							}else{
								JOptionPane.showMessageDialog(DriveWidget.this, "First line of file must be `robot_log`");
							}
						}
						scanner.close();
						if(!knownTour.isAlive()){
							knownTour.start();
						}
						isTimeToKnow = true;
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            } else {
	            	System.err.println("Open command cancelled by user.");
	            }
				}
			}
//			this.driveWidget.setVelocity( vx, vy, omega );
		}
	}
	
	public class MyRunnable implements Runnable{
		private Robot robot;
		
		public MyRunnable(Robot robot){
			this.robot = robot;
		}
		
		public void run(){
			while(true){
				
					try {
						if(isTimeToLearn){
							robot.drive(mazeInfo.getxGoalPosition(), mazeInfo.getyGoalPosition());
							isTimeToLearn = false;
						}else{
							Thread.sleep(100);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
			
		}
		
	}
	
	public class KnowledgeTourRunnable implements Runnable{
		private Robot robot;
		
		public KnowledgeTourRunnable(Robot robot){
			this.robot = robot;
		}
		
		public void run(){
			try{
				if(isTimeToKnow){
					robot.driveLastPath(mazeInfo);
					isTimeToKnow = false;
				}else{
					Thread.sleep(100);
				}
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}