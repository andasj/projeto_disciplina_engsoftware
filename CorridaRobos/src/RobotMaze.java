
public class RobotMaze {
	double fieldSize = 10;
	int obstaclesAmount;
	double[] obstaclesPosition;
	double[] goalPosition;
	double[] bestPath;
	
	public RobotMaze(double fieldSize, double[] goalPosition) {
		super();
		this.fieldSize = fieldSize;
		this.goalPosition = goalPosition;
	}
	
	public double getFieldSize() {
		return fieldSize;
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
	public double[] getGoalPosition() {
		return goalPosition;
	}
	public void setGoalPosition(double[] goalPosition) {
		this.goalPosition = goalPosition;
	}
	public double[] getBestPath() {
		return bestPath;
	}
	public void setBestPath(double[] bestPath) {
		this.bestPath = bestPath;
	}
	
}
