import javax.swing.UIManager;

public class Main
{	
    public static void main(String[] args)
    {    
        //Try to use the Look & Feel of the native Operating System
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) { }
    
    	MainFrame frame = new MainFrame(new Robot(), new RobotMaze(5,4.1,1.5));
    	frame.setTitle( "Robotino Maze Race" );
        
        System.out.println("Welcome to the GUI example.");
    }	
}