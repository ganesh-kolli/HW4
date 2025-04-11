module teamproject45 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	requires java.base;
	requires org.junit.jupiter.api;
	requires javafx.swing;
	requires javafx.base;
	
	opens application to javafx.graphics, javafx.fxml;
}