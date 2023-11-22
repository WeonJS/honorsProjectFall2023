package application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;

public class SampleController {
	
	private class ShowerPoint {
		public double x;
		public double y;
		public ShowerLogEntry entry;
		
		public ShowerPoint(double x, double y, ShowerLogEntry entry) {
			this.x = x;
			this.y = y;
			this.entry = entry;
		}
	}
	
	private GraphicsContext gc;
	
	// duration of current shower 
	private int showerDurationSeconds = 1;
	private boolean timerRunning = false;
	private Timer showerTimer = new Timer();
	private final int POINT_RADIUS = 8;
	private ShowerPoint selectedPoint;
	
	// singleton
	private static SampleController instance;
	
	private ArrayList<ShowerPoint> showerPoints = new ArrayList<>();
	
	@FXML
	private Button viewHistoryButton;
	
	@FXML 
	private Text timerText;
	
	@FXML
	private Pane showerHistoryPane;
	
	@FXML
	private Pane mainMenuPane;
	
	@FXML 
	private Button backButton;
	
	@FXML
	private Canvas canvas;
	
	@FXML
	private TextArea showerDataView;
	
	@FXML
	private Button startStopBtn;
	
	
	private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
        	if (timerRunning) {
        		updateTimerTextInSeconds(showerDurationSeconds++);
        	}
        }
    };
    
    public void initialize() {
    	if (instance == null) {
    		instance = this;
    	} else
    		return;
    	
    	showerTimer.schedule(timerTask, 0, 1000);
    	
    	showerHistoryPane.setVisible(false);
    	mainMenuPane.setVisible(true);
    	gc = canvas.getGraphicsContext2D();
    	drawGraph();
    }
	
	// =======================
	// start of event handlers
	// =======================
	
    
    @FXML
    private void onBackButtonClicked(MouseEvent event) {
    	showerHistoryPane.setVisible(false);
    	mainMenuPane.setVisible(true);
    }
    
    @FXML
    private void onViewHistoryButtonClicked(MouseEvent event) {
    	showerHistoryPane.setVisible(true);
    	mainMenuPane.setVisible(false);
    	selectedPoint = null;
    	drawGraph();
    }
	
	
	@FXML
	private void onStartStopShowerButtonClicked(MouseEvent event) throws InterruptedException {
		String currentText = startStopBtn.getText();
		startStopBtn.setText(currentText.equals("Start") ? "Stop" : "Start");
		
		
		ShowerLogger instance = ShowerLogger.getInstance();
		LocalDateTime start = instance.getStartTime();
		if (start == null) {
			instance.startShower();
			timerRunning = true;
		} else {
			instance.endShower();
			timerRunning = false;
			showerDurationSeconds = 1;
			updateTimerTextInSeconds(0);
		}
	}
	
	private void updateTimerTextInSeconds(int s) {
		int hours = s / 3600;
		int minutes = (s % 3600) / 60;
		int seconds = s % 60;

		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        
		timerText.setText(timeString);
	}
	
	@FXML
	private void onCanvasClick(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		for (int i = 0; i < showerPoints.size(); i++) {
			ShowerPoint p = showerPoints.get(i);
			double dist = distanceBetween(x, y, p.x, p.y);
			if (dist < POINT_RADIUS) {
				selectPoint(p);
			}
		}
	}
	
	private void selectPoint(ShowerPoint p) {
		selectedPoint = p;
		
		// update textbox
		showerDataView.setText("");
		
		LocalDateTime start = p.entry.getStartTime();
		String dayOfWeek = start.getDayOfWeek().toString();
		String month = start.getMonth().toString();
		String dayOfMonth = ""+start.getDayOfMonth();
		String timeOfDay = String.format("%d:%d %s", start.getHour() >= 12 ? start.getHour() - 12 : start.getHour(), start.getMinute(), start.getHour() >= 12 ? "PM" : "AM");
		showerDataView.appendText(String.format("* Start date: %s, %s %s, %s; %s\n", dayOfWeek, month, dayOfMonth, start.getYear(), timeOfDay));
		showerDataView.appendText("* Duration: "+p.entry.getDuration()+"\n");
		showerDataView.appendText(String.format("* Gallons used: %.2f\n", p.entry.getGallonsUsed()));
		
		drawGraph();
	}
	
	private double distanceBetween(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}
	
	
	private void drawGraph() {
		// draw background
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		gc.fillRect(0, 0, width, height);
		float graphMargin = 20;
		
		// draw x axis
		gc.strokeLine(graphMargin, height - graphMargin, width - graphMargin, height - graphMargin);
		
		// y axis
		gc.strokeLine(graphMargin, graphMargin, graphMargin, height - graphMargin);
		
		ArrayList<ShowerLogEntry> showers = ShowerLogger.getInstance().getShowers();
		
		// find max shower duration
		int max = -1;
		for (ShowerLogEntry s : showers) {
			int dur = s.getDuration();
			if (dur > max) {
				max = dur;
			}
		}
		
		gc.setFill(Color.RED);
		double xAxisWidth = width - 2 * graphMargin;
		double yAxisHeight = height - 2 * graphMargin;
		showerPoints.clear();
		int showerCount = showers.size();
		for (int i = 0; i < showers.size(); i++) {
			ShowerLogEntry shower = showers.get(i);
			double pointX = ((float)i / (float)showerCount) * xAxisWidth + graphMargin;
			double pointY = (height - graphMargin) - shower.getGallonsUsed() / max * yAxisHeight;
			showerPoints.add(new ShowerPoint(pointX, pointY, shower));
			gc.beginPath();
			gc.fillArc(pointX, pointY, POINT_RADIUS, POINT_RADIUS, 0, 360, ArcType.ROUND);
			gc.closePath();
		}
		
		if (selectedPoint != null) {
			gc.setStroke(Color.BLUE);
			gc.setLineWidth(2);
			gc.beginPath();
			gc.strokeArc(selectedPoint.x, selectedPoint.y, POINT_RADIUS, POINT_RADIUS, 0, 360, ArcType.OPEN);
			gc.stroke();
		}
	}
	
	public static SampleController getInstance() {
		return instance;
	}
	
	public void destroyThread() {
		showerTimer.cancel();
	}
}
