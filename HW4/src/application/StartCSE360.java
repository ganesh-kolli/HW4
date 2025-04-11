package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.application.Application;
import javafx.stage.Stage;


public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	public static void main( String[] args )
	{
		 launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
	    try {
	        databaseHelper.connectToDatabase();
	        new StaffHomePage(primaryStage).show(); // 🚀 Direct to Staff page
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	

}
