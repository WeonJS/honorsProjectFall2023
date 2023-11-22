package application;
	
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.nio.file.Path;

import javax.swing.filechooser.FileSystemView;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;


public class ShowerLogger extends Application {
	private ArrayList<ShowerLogEntry> showers = new ArrayList<ShowerLogEntry>();
	private LocalDateTime startTime;
	private JSONDataManager jsonManager = new JSONDataManager();
	
	// singleton
	private static ShowerLogger instance;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = (Pane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
			Scene scene = new Scene(root,root.prefWidthProperty().floatValue(),root.prefHeightProperty().floatValue());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					jsonManager.storeShowersDataList();
					SampleController.getInstance().destroyThread();
	          	}
			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ShowerLogger() {
		if (instance == null)
			instance = this;
		showers = jsonManager.retrieveShowersDataList();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	// =================
	// start of methods
	// =================
	public static ShowerLogger getInstance() {
		return instance;
	}
	
	public void startShower() {
		startTime = LocalDateTime.now();
	}
	
	public void endShower() {
		ShowerLogEntry entry = new ShowerLogEntry(startTime, LocalDateTime.now());
		showers.add(entry);
		startTime = null;
		System.out.println("Shower duration: " + entry.getDuration());
		System.out.println("Shower gals: " + entry.getGallonsUsed());
	}
	
	public void addShowerEntry(ShowerLogEntry entry) throws IOException {
		// update runtime record of showers
		showers.add(entry);
	}
	
	// ==================
	// accessors/mutators
	// ==================
	public LocalDateTime getStartTime() {
		return startTime;
	}
	
	public void setStartTime(LocalDateTime start) {
		startTime = start;
	}
	
	public ArrayList<ShowerLogEntry> getShowers() {
		return showers;
	}
	
	public JSONDataManager getJSONManager() {
		return jsonManager;
	}
}
