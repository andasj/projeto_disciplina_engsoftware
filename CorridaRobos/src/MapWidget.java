import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Shape;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.util.ShapeUtilities;



//TODO: reset map, will erase the path
//TODO: if it is outside the area, pop up it has failed


public class MapWidget extends JComponent {
	protected JFreeChart map;
	public XYSeries pathPoints, goalPoints, bestPathPoints;
	public XYZDataset bestPathData;
	public XYSeriesCollection dataset;
	Shape goal = ShapeUtilities.createDiagonalCross(3, 1);
	double sizeField = 5.0, sizeMap = 5.0;
	double xRef=0, yRef = 0;
	
	private RobotMaze mazeInfo;
	
	public MapWidget(RobotMaze mazeInfo) {
		this.mazeInfo = mazeInfo;
		this.sizeMap = mazeInfo.getFieldSize();
		
		dataset = new XYSeriesCollection();

		pathPoints = new XYSeries("Line Plot");
		dataset.addSeries(pathPoints);

//		// scatter plot
		goalPoints = new XYSeries("");
		goalPoints.add(-mazeInfo.getyGoalPosition(), mazeInfo.getxGoalPosition());
		dataset.addSeries(goalPoints);

		map = ChartFactory.createXYLineChart("Robot path", "y (m)", "x (m)", dataset);
		
		XYPlot plot = (XYPlot) map.getPlot();
		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(-sizeMap, sizeMap);
        domain.setTickUnit(new NumberTickUnit(sizeMap/10));
        
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(-sizeMap, sizeMap);
        range.setTickUnit(new NumberTickUnit(sizeMap/5));
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		// "0" is the line plot
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(0, true);

		// "1" is the scatter plot
		renderer.setSeriesLinesVisible(1, false);
		renderer.setSeriesShapesVisible(1, true);
		renderer.setSeriesShape(1, goal);
		

		plot.setRenderer(renderer);

		setLayout(new BorderLayout());
		add(new ChartPanel(map), BorderLayout.CENTER);

		setMinimumSize(new Dimension(100, 30));
		setPreferredSize(new Dimension(800, 200));
		setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		PrintStream stdOut = new PrintStream(new JFreeChartOutputStream());
		System.setOut(stdOut);
		//System.setErr(stdOut);

	}

	private class JFreeChartOutputStream extends OutputStream {
		private final StringBuffer buffer = new StringBuffer();
		private double x = 0, y = 0;
		Boolean toUpdate = new Boolean(false);
		Boolean toSave = new Boolean(false);
		Boolean toReset = new Boolean(false);

		XYSeries path = new XYSeries("Path");

		@Override
		public void write(int b) throws IOException {
			buffer.append((char) b);
			flush();
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
			flush();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (b == null)
				throw new NullPointerException();
			else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
				throw new IndexOutOfBoundsException();
			else if (len == 0)
				return;

			for (int i = 0; i < len; i++)
				buffer.append((char) b[off + i]);

			flush();
		}

		@Override
		public void flush() throws IOException {
			final String str;

			synchronized (buffer) {
				str = buffer.toString();
				buffer.delete(0, buffer.length());

				String coordinates[] = str.split(",");
				if (coordinates[0].equals("odometry")) {
					double xNow = (double) Double.parseDouble(coordinates[1]);
					double yNow = (double) Double.parseDouble(coordinates[2]);
					double phi = (double) Double.parseDouble(coordinates[3]);
					double signal = Math.signum(phi);
					
					// if it is new information
					if ((x != xNow) && (y != yNow)) {
						try{
							pathPoints.add(-signal*yNow, signal*xNow);
							mazeInfo.updateBestPath(yNow, xNow, phi);
						}
						catch(Exception e){
							System.err.println(e.getClass().getCanonicalName());
														
						}
					}

					x = xNow;
					y = yNow;
					toUpdate = true;

				} else if(coordinates[0].equals("stop")){
					toSave = true;
				}else if(coordinates[0].equals("reset")){
					toReset = true;
				}

			}

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (toUpdate) {
						// update chart
						dataset.removeAllSeries();
						dataset.addSeries(pathPoints);
//						series.add(2, 2);
						dataset.addSeries(goalPoints);

						toUpdate = false;
						// textArea.append(str);
					}
					if(toSave){
						PrintWriter writer = null;
						 Date date= new Date();
						 long time = date.getTime();
						 Timestamp ts = new Timestamp(time);
						 String formattedDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
						try {
							writer = new PrintWriter("robot_log_"+formattedDate+".txt", "UTF-8");
							writer.println("robot_log");
						} catch (FileNotFoundException | UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						List<Point> localPath = mazeInfo.getPath();
						
						for(Point p : localPath){
							writer.println(p.x+","+p.y+","+p.phi);
						};
						writer.close();
						
						toSave = false;
						
					}
					if(toReset){
						dataset.removeAllSeries();
						pathPoints.clear();
						dataset.addSeries(pathPoints);
						dataset.addSeries(goalPoints);
						toReset = false;

					}
					
				}
			});
		}
	}

}
