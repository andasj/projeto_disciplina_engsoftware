import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Timer;

import rec.robotino.api2.Bumper;
import rec.robotino.api2.Com;
import rec.robotino.api2.DistanceSensor;
import rec.robotino.api2.Motor;
import rec.robotino.api2.OmniDrive;
import rec.robotino.api2.Camera;
import rec.robotino.api2.Odometry;


/**
 * The class Robot demonstrates the usage of the most common robot component classes.
 * Furthermore it shows how to handle events and receive incoming camera images.
 */
public class Robot
{
	protected final float SLOW_VELOCITY = 0.08f;
	protected final float MEDIUM_VELOCITY = 0.16f;
	protected final float VELOCITY = 0.24f;
	protected final float FAST_VELOCITY = 0.32f;
	protected final float ANGULARVELOCITY = 0.02f;
	
	protected final Com _com;
	protected final OmniDrive _omniDrive;
	protected final Bumper _bumper;
	protected final List<DistanceSensor> _distanceSensors;
	protected final Odometry _odometry;
	
	protected final Collection<RobotListener> _listeners;

	public double lastx = 0;
	public double lasty = 0;
	public boolean stop = false; 
	
	float[] esquerda = new float[] { 0.0f, -0.3f };
	float[] desquerda = new float[] { 0.3f, -0.3f };
	float[] direita = new float[] { 0.0f, 0.3f };
	float[] ddireita = new float[] { 0.3f, 0.3f };
	
	float[] tras = new float[] { -1.0f, 0.0f };    	
	float[] frente = new float[] { 1.0f, 0.0f };
	
	
	public Robot()
	{
		_com = new MyCom();
		_omniDrive = new OmniDrive();
		_bumper = new Bumper();
		_distanceSensors = new ArrayList<DistanceSensor>();
		_odometry = new MyOdometry();
		
		_listeners = new CopyOnWriteArrayList<RobotListener>();
		
		init();

		for(int i=0; i<9; ++i)
		{
			DistanceSensor s = new DistanceSensor();
			s.setSensorNumber(i);
			s.setComId(_com.id());
			_distanceSensors.add(s);
		}
		_odometry.set(0, 0, 0, true);
	}

	public void reset(){
		
		lastx = 0;
		lasty = 0;
		stop = false;
		
		for(int i=0; i<9; ++i)
		{
			DistanceSensor s = new DistanceSensor();
			s.setSensorNumber(i);
			s.setComId(_com.id());
			_distanceSensors.add(s);
		}
		_odometry.set(0, 0, 0, true);
	}
	protected void init()
	{
		_omniDrive.setComId( _com.id() );

		_bumper.setComId( _com.id() );
		
		_odometry.setComId( _com.id() );
		
		
	}
	
	public void addListener(RobotListener listener)
	{
		_listeners.add( listener );
	}
	
	public void removeListener(RobotListener listener)
	{
		_listeners.remove( listener );
	}
	
	public boolean isConnected()
	{
		return _com.isConnected();
	}

	public void connect(String hostname, boolean block)
	{
		_com.setAddress( hostname );
		_com.connectToServer(block);
	}

	public void disconnect()
	{
		_com.disconnectFromServer();
	}
	
	public void setVelocity(float vx, float vy, float omega)
	{
		_omniDrive.setVelocity( vx, vy, omega );
	}
	
    public void rotate(float[] inArray, float[] outArray, float deg)
    {
        float rad = 2 * (float)Math.PI / 360.0f * deg;
        outArray[0] = (float)Math.cos(rad) * inArray[0] - (float)Math.sin(rad) * inArray[1];
        outArray[1] = (float)Math.sin(rad) * inArray[0] + (float)Math.cos(rad) * inArray[1];
    }
    
    void addScaledVector(float[] srcDest, float[] uv, float scale)
    {
        srcDest[0] += uv[0] * scale;
        srcDest[1] += uv[1] * scale;
    }
    
    void normalizeVector(float[] v)
    {
        float len = (float)Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        v[0] /= len;
        v[1] /= len;
    }
    
    public void driveLastPath(RobotMaze mazeInfo){
		
    	List<Point> pathCopy = new CopyOnWriteArrayList<Point>();
    	
    	pathCopy.addAll(mazeInfo.getPath());
    	
    	for(Point p : pathCopy){
    		try {
    			stop = false;
    			getToPoint(-p.y, p.x);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
    private void getToPoint(double xGoal, double yGoal) throws InterruptedException{
    	float[] ev = new float[] { 1.0f, 0.0f };
    	float[] esquerda = new float[] { 0.0f, -0.5f };
    	float[] desquerda = new float[] { 0.5f, -0.5f };
    	float[] direita = new float[] { 0.0f, 0.5f };
    	float[] ddireita = new float[] { 0.5f, 0.5f };
    	
    	
    	float[] frente = new float[] { 1.0f, 0.0f };

        float[] dir = new float[] { 1.0f, 0.0f };
        float velocity = VELOCITY;
        float rotVelocity = 0f;
        long time = 0;
        
        while (_com.isConnected() && _bumper.value() == false && !stop )
        {
        	
        	velocity = VELOCITY;
            int numEscape = 0;
            int minIndex = 0;
            float minDistance = 0.40f;
            int sensor = 0;
        
        if(lastx >xGoal || lastx < -4.0)
			if(lasty > yGoal )
				dir = esquerda;
			else if(lasty < yGoal - 0.2)
				dir =direita;
			else
			{
				stop = true;
			}
		else
			dir = frente;
        _omniDrive.setVelocity(velocity * (float)dir[0], velocity * (float)dir[1], rotVelocity);
        Thread.sleep(5);
        }
    	
    }
    public void drive(double xGoal, double yGoal) throws InterruptedException
    {
    	
    	System.out.println("Driving...");
    	
    	float[] ev = new float[] { 1.0f, 0.0f };
    	// Cria Vetores na direção dos Sensores
        float[][] escapeVector = new float[][]
        {
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f},
            new float[]{0.0f, 0.0f}
        };

        for (int i = 0; i < 9; i++)
        {
            rotate(ev, escapeVector[i], 40.0f * i);
        }
        // final da criação de vetores
        
        final float ESCAPE_DISTANCE = 0.20f;
        float[] escape = new float[] { 0.0f, 0.0f };
        float[] dir = new float[] { 1.0f, 0.0f };
        float velocity = VELOCITY;
        float rotVelocity = 0f;
        boolean wall =false;
        long time = 0;

        while (_com.isConnected() && _bumper.value() == false && !stop )
        {
        	
        	velocity = VELOCITY;
            int numEscape = 0;
            int minIndex = 0;
            float minDistance = 0.40f;
            int sensor = 0;            
               
            //getting sensors values 
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < _distanceSensors.size(); ++i)
            {
                float v = (float)_distanceSensors.get(i).distance();
                values.append(v + " ");
                if (v < minDistance)
                {
                    minDistance = v;
                    minIndex = i;
                }
                if (v < ESCAPE_DISTANCE)
                {
                	sensor = i;
                    ++numEscape;
                    addScaledVector(escape, escapeVector[i], v); // acha a perpendicular
                }
            }
            
           
            if(numEscape == 1)
            {
            	switch(sensor)
            	{
            		case 0:
            			dir = esquerda;
            			wall = true;
            			break;
            		case 1:
            			dir = esquerda;
            			wall = true;
            			break;
            		case 2:
            			dir = desquerda;
            			wall = true;
            			break;
            		case 3:
            			dir = esquerda;
            			wall = true;
            			break;
            		case 4:
            			dir = frente;
                		wall = true;
                		break;
            		case 5:
            			dir = frente;
            			wall = true;
            			break;
            		case 6:
           				dir = direita;
            			wall = true;
            			break;
            		case 7:
            			dir = direita;
            			wall = true;
            			break;
            		case 8:
            			dir = ddireita;
            			wall = true;
            			break;
            		default:
            			dir = frente;
            			break;
            	}
        		
            }
            else if(numEscape == 0)
            {
            	if(wall==true)
            	{
                	while(time != 5) {
                		time++;
                	}
                	wall=false;
                	time=0;
            	}
            	else
            	{
            		System.err.println("x:"+lastx+",y:"+lasty);
            		//se atingir o x maximo, tento corrigir o y       		
            		if(lastx >xGoal){
            			if(lasty > yGoal+0.3)
            				dir = esquerda;
            			else if(lasty < yGoal-0.3)
            				dir =direita;
            			else
            			{
            				System.out.print("stop");
            				stop = true;
            			}
            		}
            		//tento atingir o x
            		else{
            			//verifico o limite maximo do y
            			if(lasty > yGoal)
            				dir = esquerda;
            			else if(lasty < -0.3)
            				dir = direita;
            			else
            				dir = frente;
            		}
            	}
            }
           	_omniDrive.setVelocity(velocity * (float)dir[0], velocity * (float)dir[1], rotVelocity);
            
           Thread.sleep(5);            
        }
    }
	
	/**
	 * The class MyCom derives from rec.robotino.com.Com and implements some of the virtual event handling methods.
	 * This is the standard approach for handling these Events.
	 */
	class MyCom extends Com
	{
		Timer _timer;
		
		public MyCom()
		{
			_timer = new Timer();
			_timer.scheduleAtFixedRate(new OnTimeOut(), 0, 20);
		}
		
		class OnTimeOut extends TimerTask
		{
			public void run()
			{
				processEvents();
			}
		}

		@Override
		public void connectedEvent()
		{
			System.out.println( "Connected" );
			for(RobotListener listener : _listeners)
				listener.onConnected();
		}

		@Override
		public void errorEvent(String errorStr)
		{
			System.err.println( "Error: " + errorStr );
			for( RobotListener listener : _listeners)
				listener.onError(errorStr);
		}

		@Override
		public void connectionClosedEvent()
		{
			System.out.println( "Disconnected" );
			for(RobotListener listener : _listeners)
				listener.onDisconnected();
		}
	}
	
	class MyOdometry extends Odometry
	{
		
		public double xref = 0;
		public double yref = 0;
		public boolean fr = true; 
		private double phi = 0;
		
		public MyOdometry()
		{
			super();
		}
		
		
		@Override
		public void readingsEvent(double x, double y, double phi, float vx, float vy, float omega, long sequence)
		{
			this.phi = phi;
			// Ponto final na diagonal X = 4.4  Y = 0
			if(fr)
			{
				xref = x;
				yref = y;
				fr=false;
			}
			else
			{
				lastx = x - xref;
				lasty = y - yref;
				
				if(!stop) {
					for(RobotListener listener : _listeners)
						listener.onOdometryReceived(x, y, phi);
					//System.out.println("odometry,"+(lastx)+","+(lasty)+","+phi);
//				System.out.print("X: ");
//				System.out.println(lastx);
//				System.out.print("Y: "); 
//				System.out.println(lasty);	
				}
			}	
			
//			for(RobotListener listener : _listeners)
//				listener.onOdometryReceived(x, y, phi);
		}
	}
}
