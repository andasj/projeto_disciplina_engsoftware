import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * This widget provides a graphical user interface to connect to Robotino.
 */
public class ConnectWidget extends JComponent
{
	protected final Robot robot;
	
	protected final JTextField textFieldAddress;
	protected final JButton buttonConnect;
	
	public ConnectWidget(Robot robot)
	{
		this.robot = robot;
		
		buttonConnect = new JButton("Connect");
		textFieldAddress = new JTextField("127.0.0.1:12080", 12);
		
		setLayout( new FlowLayout() );
		add(textFieldAddress);
		add(buttonConnect);
		
		setMinimumSize( new Dimension(100, 35) );
		setPreferredSize( new Dimension(100, 35) );
		setMaximumSize( new Dimension(Short.MAX_VALUE, 35) );
		
		buttonConnect.addActionListener( new ConnectButtonActionListener() );
		robot.addListener( new RobotListenerImpl() );
	}
	
	private class ConnectButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(!robot.isConnected())
			{
				System.out.println( "Connecting to " + textFieldAddress.getText() + " ...");
				robot.connect(textFieldAddress.getText(), false);
			}
			else
			{
				System.out.println( "Disconnecting...");
				robot.disconnect();
			}
			
			buttonConnect.setEnabled( false );
		}	
	}
	
	private class RobotListenerImpl implements RobotListener
	{
		double lastX=0, lastY=0,xRef=0,yRef=0;
		boolean firstReading = true;
		
		@Override
		public void onConnected()
		{
			EventQueue.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					buttonConnect.setText( "Disconnect" );
					buttonConnect.setEnabled( true );					
				}
			});
		}

		@Override
		public void onDisconnected()
		{
			EventQueue.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					buttonConnect.setText( "Connect" );
					buttonConnect.setEnabled( true );				
				}
			});
		}

		@Override
		public void onError(String error)
		{
			if(!robot.isConnected())
				onDisconnected();			
		}


		@Override
		public void onOdometryReceived(double x, double y, double phi) {
						
			if(firstReading){
				xRef = x;
				yRef = y;
				firstReading = false;
			}
			
			if((lastX != x) && (lastY != y)){
				System.out.println("odometry,"+(x-xRef)+","+(y-yRef)+","+phi);
			}
			lastX = x;
			lastY = y;
		}
	}
}
