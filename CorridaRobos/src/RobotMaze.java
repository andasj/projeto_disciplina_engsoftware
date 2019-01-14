import java.util.ArrayList;
import java.util.List;

public class RobotMaze {
	double fieldSize = 5.0;
	int obstaclesAmount;
	double[] obstaclesPosition;
	private double xGoalPosition;
	private double yGoalPosition;
	List<Point> path = new ArrayList<Point>();
	
	
	
	public RobotMaze(double fieldSize, double x, double y) {
		super();
		this.fieldSize = fieldSize;
		this.xGoalPosition = x;
		this.yGoalPosition = y;
	}
	
	public double getFieldSize() {
		return fieldSize;
	}
	public double getxGoalPosition() {
		return xGoalPosition;
	}

	public void setxGoalPosition(double xGoalPosition) {
		this.xGoalPosition = xGoalPosition;
	}

	public double getyGoalPosition() {
		return yGoalPosition;
	}

	public void setyGoalPosition(double yGoalPosition) {
		this.yGoalPosition = yGoalPosition;
	}

	public void setFieldSize(double fieldSize) {
		this.fieldSize = fieldSize;
	}
	public int getObstaclesAmount() {
		return obstaclesAmount;
	}
	public void setObstaclesAmount(int obstaclesAmount) {
		this.obstaclesAmount = obstaclesAmount;
	}
	public double[] getObstaclesPosition() {
		return obstaclesPosition;
	}
	public void setObstaclesPosition(double[] obstaclesPosition) {
		this.obstaclesPosition = obstaclesPosition;
	}

	public void updateBestPath(double x, double y, double phi){
		Point currentPoint = new Point(x,y,phi);
		
		path.add(currentPoint);
	}
	
	public void resetBestPath(){
		path.clear();
	}

	public List<Point> getPath() {
		return path;
	}

	public void setPath(List<Point> path) {
		this.path = path;
	}
	
}
