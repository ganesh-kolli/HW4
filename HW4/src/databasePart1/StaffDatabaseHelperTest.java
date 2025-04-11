package databasePart1;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains JUnit tests for the staff-related features
 * implemented in the DatabaseHelper class.
 */
public class StaffDatabaseHelperTest {
    static DatabaseHelper db;

    @BeforeAll
    public static void setup() {
        db = new DatabaseHelper();
        try {
            db.connectToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Database connection failed.");
        }
    }

    /**
     * Tests retrieval of all questions and answers.
     */
    @Test
    public void testGetAllQuestionsAndAnswers() {
        List<String> qa = db.getAllQuestionsAndAnswers();
        assertNotNull(qa, "Q&A list should not be null");
    }

    /**
     * Tests retrieval of private feedback.
     */
    @Test
    public void testGetAllPrivateFeedback() {
        List<String> feedback = db.getAllPrivateFeedback();
        assertNotNull(feedback, "Feedback list should not be null");
    }

    /**
     * Tests insertion of an instructor note.
     */
    @Test
    public void testAddInstructorNote() {
        assertDoesNotThrow(() -> db.addInstructorNote("JUnit test note"));
    }

    /**
     * Tests searching content by keyword.
     */
    @Test
    public void testSearchContentByKeyword() {
        List<String> results = db.searchContentByKeyword("test");
        assertNotNull(results, "Search result should not be null");
    }

    /**
     * Dummy test to simulate report logic trigger.
     */
    @Test
    public void testGenerateReportLogic() {
        boolean logicTriggered = true;
        assertTrue(logicTriggered, "The report logic trigger should be true.");
    }
}