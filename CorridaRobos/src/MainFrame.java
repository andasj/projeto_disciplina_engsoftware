import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This is the main frame of this example.
 * It provides an user interface to enter the IP and connect to the robot,
 * a basic interface to drive Robotino, it shows the camera image and
 * displays the standard text output.
 */
public class MainFrame extends JFrame
{
	protected final ConnectWidget connectComponent;
	protected final DriveWidget driveComponent;
	protected final MapWidget consoleComponent;
	
	protected final JPanel centerPanel;

	public MainFrame(Robot robot, RobotMaze mazeInfo)
	{	
		connectComponent = new ConnectWidget(robot);
		driveComponent = new DriveWidget(robot, mazeInfo);
		consoleComponent = new MapWidget(mazeInfo);
		
		centerPanel = new JPanel();
		centerPanel.setLayout( new BoxLayout(centerPanel, BoxLayout.X_AXIS) );
		centerPanel.add( driveComponent );

		Container content = getContentPane();
		content.setLayout( new BoxLayout(content, BoxLayout.Y_AXIS) );
		content.add( connectComponent );
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add( centerPanel );
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add( consoleComponent);

		//Place this component in the middle of the screen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dGuiSize = new Dimension( 640, 480 );
		setSize( dGuiSize );
		setLocation( (d.width - dGuiSize.width) / 2, (d.height - dGuiSize.height) / 2 );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		try {
            URL resource = getClass().getResource("icons/headrobot.png");
            BufferedImage image = ImageIO.read(resource);
            setIconImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		setVisible( true );
	}
}