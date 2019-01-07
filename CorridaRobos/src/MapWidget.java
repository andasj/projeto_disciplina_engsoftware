import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Shape;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;

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



//TODO: use phi to tell direction it is moving and to plot correctly
//TODO: use a shape that indicates the direction, different color
//TODO: covering the same area, will erase the path
//TODO: if it is outside the area, pop up it has failed
//TODO: store the path
//TODO: enable learning tour, and knowledge tour options

public class MapWidget extends JComponent {
	protected JFreeChart map;
	public XYSeries pathPoints, goalPoints, bestPathPoints;
	public XYZDataset bestPathData;
	public XYSeriesCollection dataset;
	Shape goal = ShapeUtilities.createDiagonalCross(3, 1);
	final double sizeField = 5.0, sizeMap = 10.0;
	double xRef=0, yRef = 0;
	
	public MapWidget() {
		dataset = new XYSeriesCollection();

		pathPoints = new XYSeries("Line Plot");
		dataset.addSeries(pathPoints);

//		// scatter plot
		goalPoints = new XYSeries("");
		goalPoints.add(-sizeField, sizeField);
		dataset.addSeries(goalPoints);

		map = ChartFactory.createXYLineChart("Robot path", "x (m)", "y (m)", dataset);
		
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
					double signal = Math.signum(Double.parseDouble(coordinates[3]));
					
					// if it is new information
					if ((x != xNow) && (y != yNow)) {
						try{
							pathPoints.add(-signal*yNow, signal*xNow);
						}
						catch(Exception e){
							System.err.println(e.getMessage());
							
							//path = new XYSeries("Path");
							
						}
					}

					x = xNow;
					y = yNow;
					toUpdate = true;

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
					
				}
			});
		}
	}

}
