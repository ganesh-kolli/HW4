package databasePart1;

import application.Message;
import application.Review;
import application.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The {@code DatabaseHelper} class is responsible for managing the connection to the database
 * and performing various operations such as user registration, login validation, invitation code
 * handling, Q&A management, reviews, messaging, and trusted reviewers functionality.
 */
public class DatabaseHelper {

    /** JDBC driver name. */
    static final String JDBC_DRIVER = "org.h2.Driver";
    /** Database URL. */
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";
    /** Database user name. */
    static final String USER = "sa";
    /** Database password. */
    static final String PASS = "";

    /** The database connection object. */
    private static Connection connection = null;
    /** Statement object used for executing SQL queries. */
    private Statement statement = null;

    /** Holds the current session role (e.g., student, reviewer, etc.). */
    private static String sessionRole = null;
    /** Holds the username of the currently logged in user. */
    private static String currentUsername = null;

    /**
     * Sets the session role.
     *
     * @param role the role to be set for the current session
     */
    public static void setSessionRole(String role) {
        sessionRole = role;
    }

    /**
     * Returns the current session role.
     *
     * @return the session role; may be {@code null} if not set
     */
    public static String getSessionRole() {
        return (sessionRole != null) ? sessionRole : null;
    }

    /**
     * Returns the username of the currently logged in user.
     *
     * @return the current username; if not set, returns "Unknown"
     */
    public static String getCurrentUsername() {
        return (currentUsername != null) ? currentUsername : "Unknown";
    }

    /**
     * Establishes a connection to the database and creates the required tables if they do not exist.
     *
     * @throws SQLException if a database access error occurs or the connection fails
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
            // Uncomment the following lines to clear/reset tables for testing:
            // statement.execute("DELETE FROM answers");
            // statement.execute("DELETE FROM questions");
            // statement.execute("ALTER TABLE questions ALTER COLUMN id RESTART WITH 1");
            // statement.execute("ALTER TABLE answers ALTER COLUMN id RESTART WITH 1");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates all required tables for the application if they do not already exist.
     *
     * @throws SQLException if a database access error occurs
     */
    private void createTables() throws SQLException {
        // Create cse360users table
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20))";
        statement.execute(userTable);

        // Drop and recreate InvitationCodes table
        String dropTable = "DROP TABLE IF EXISTS InvitationCodes";
        statement.execute(dropTable);
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "deadline TIMESTAMP, "
                + "role VARCHAR(20))";
        statement.execute(invitationCodesTable);

        // Drop and recreate user_otp table
        String dropOtpTable = "DROP TABLE IF EXISTS user_otp";
        statement.execute(dropOtpTable);
        String otpTable = "CREATE TABLE IF NOT EXISTS user_otp ("
                + "userName VARCHAR(255), "
                + "otp VARCHAR(10), "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(otpTable);

        // Create questions table
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(1000) NOT NULL, "
                + "createdBy VARCHAR(255) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(questionsTable);

        // Create answers table with foreign key to questions table
        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "question_id INT, "
                + "answeredBy VARCHAR(255) NOT NULL, "
                + "text VARCHAR(1000) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE)";
        statement.execute(answersTable);

        // Drop dependent tables Messages and Reviews if they exist
        statement.execute("DROP TABLE IF EXISTS Messages");
        statement.execute("DROP TABLE IF EXISTS Reviews");

        // Create Reviews table
        String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "reviewer_id VARCHAR(255), "
                + "target_type VARCHAR(20), "
                + "target_id INT, "
                + "content VARCHAR(2000), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "weightage INT DEFAULT 0)";
        statement.execute(reviewsTable);

        // Create Messages table with foreign key to Reviews table
        String messagesTable = "CREATE TABLE IF NOT EXISTS Messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "review_id INT, "
                + "sender VARCHAR(255), "
                + "receiver VARCHAR(255), "
                + "content VARCHAR(2000), "
                + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (review_id) REFERENCES Reviews(id) ON DELETE CASCADE)";
        statement.execute(messagesTable);

        // Create ReviewerRequests table
        String reviewerRequestsTable = "CREATE TABLE IF NOT EXISTS ReviewerRequests ("
                + "senderUsername VARCHAR(255), "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(reviewerRequestsTable);

        // Drop and recreate TrustedReviewers table
        statement.execute("DROP TABLE IF EXISTS TrustedReviewers");
        String trustedReviewersTable = """
            CREATE TABLE IF NOT EXISTS TrustedReviewers (
                studentUsername VARCHAR(255),
                reviewerUsername VARCHAR(255),
                CONSTRAINT PK_TrustedReviewers PRIMARY KEY (studentUsername, reviewerUsername)
            )
        """;
        statement.execute(trustedReviewersTable);
    }

    // -----------------------------------------
    // USER / AUTHENTICATION-RELATED METHODS
    // -----------------------------------------

    /**
     * Checks whether the user table is empty.
     *
     * @return {@code true} if the cse360users table is empty, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user in the database.
     *
     * @param user the {@code User} object containing the registration details
     * @throws SQLException if a database access error occurs
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
        }
    }

    /**
     * Validates a user's login credentials.
     *
     * @param user the {@code User} object containing the login credentials
     * @return {@code true} if the credentials are valid; {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentUsername = user.getUserName();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param userName the username to check
     * @return {@code true} if the user exists; {@code false} otherwise
     */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the role of a given user.
     *
     * @param userName the username whose role is to be retrieved
     * @return the role of the user, or {@code null} if not found
     */
    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -----------------------------------------
    // INVITATION CODES & OTP
    // -----------------------------------------

    /**
     * Generates an invitation code for a given role.
     *
     * @param role the role for which the invitation code is generated
     * @return the generated invitation code
     */
    public String generateInvitationCode(String role) {
        String code = UUID.randomUUID().toString().substring(0, 4);
        long deadlineMillis = System.currentTimeMillis() + 2 * 60 * 1000; // 2 minutes
        Timestamp deadline = new Timestamp(deadlineMillis);

        String query = "INSERT INTO InvitationCodes (code, deadline, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setTimestamp(2, deadline);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * Retrieves the role associated with a given invitation code.
     *
     * @param code the invitation code
     * @return the role associated with the code, or {@code null} if not found
     */
    public String getRoleFromInvitationCode(String code) {
        String query = "SELECT role FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Validates an invitation code.
     * <p>
     * The code is valid if it exists, is not marked as used, and its deadline has not passed.
     * If valid, the code is marked as used.
     * </p>
     *
     * @param code the invitation code to validate
     * @return {@code true} if the code is valid; {@code false} otherwise
     */
    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE AND deadline > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marks a given invitation code as used.
     *
     * @param code the invitation code to mark as used
     */
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a 6-digit OTP for a user.
     * <p>
     * Before generating a new OTP, unused OTPs for the user are cleared.
     * </p>
     *
     * @param userName the username for which the OTP is generated
     * @return the generated OTP
     */
    public String generateOTP(String userName) {
        String cleanupQuery = "DELETE FROM user_otp WHERE userName = ? AND isUsed = FALSE";
        try (PreparedStatement cleanupStmt = connection.prepareStatement(cleanupQuery)) {
            cleanupStmt.setString(1, userName);
            cleanupStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        String query = "INSERT INTO user_otp (userName, otp, isUsed) VALUES (?, ?, FALSE)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Generated OTP for user " + userName + ": " + otp);
        return otp;
    }

    /**
     * Validates a given OTP for a user.
     * <p>
     * If the OTP is found and not used, it is marked as used.
     * </p>
     *
     * @param userName the username for which the OTP is validated
     * @param otp the OTP to validate
     * @return {@code true} if the OTP is valid; {@code false} otherwise
     */
    public boolean validateOTP(String userName, String otp) {
        String query = "SELECT otp, isUsed FROM user_otp WHERE userName = ? AND otp = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isUsed = rs.getBoolean("isUsed");
                if (!isUsed) {
                    System.out.println("Valid OTP found. Marking as used...");
                    markOtpAsUsed(userName, otp);
                    return true;
                } else {
                    System.out.println("OTP has already been used.");
                }
            } else {
                System.out.println("No matching OTP found for user: " + userName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marks a given OTP as used for a user.
     *
     * @param userName the username associated with the OTP
     * @param otp the OTP to mark as used
     */
    private void markOtpAsUsed(String userName, String otp) {
        String query = "UPDATE user_otp SET isUsed = TRUE WHERE userName = ? AND otp = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, otp);
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Rows updated after marking OTP as used: " + rowsUpdated);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all OTP records for a given user.
     *
     * @param userName the username for which OTPs are cleared
     */
    public void clearOTP(String userName) {
        String query = "DELETE FROM user_otp WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the password for a given user.
     *
     * @param userName the username whose password is to be updated
     * @param newPassword the new password
     */
    public void updatePassword(String userName, String newPassword) {
        String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user has any OTP records.
     *
     * @param userName the username to check for OTPs
     * @return {@code true} if at least one OTP record exists; {@code false} otherwise
     */
    public boolean hasOTP(String userName) {
        String query = "SELECT COUNT(*) FROM user_otp WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -----------------------------------------
    // USER MANAGEMENT
    // -----------------------------------------

    /**
     * Retrieves all usernames and roles from the database.
     *
     * @return a list of string arrays where each array contains a username and its role
     */
    public List<String[]> getAllUsernamesAndRoles() {
        List<String[]> userList = new ArrayList<>();
        String query = "SELECT userName, role FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                userList.add(new String[]{rs.getString("userName"), rs.getString("role")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Deletes a user from the database.
     *
     * @param username the username of the user to delete
     * @return {@code true} if the deletion was successful; {@code false} otherwise
     */
    public boolean deleteUser(String username) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the role of a given user.
     *
     * @param userName the username of the user
     * @param newRole the new role to assign
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    public boolean updateUserRole(String userName, String newRole) {
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, userName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Counts the number of admin users in the database.
     *
     * @return the count of admin users
     */
    public int countAdmins() {
        String query = "SELECT COUNT(*) FROM cse360users WHERE role = 'admin'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // -----------------------------------------
    // QUESTIONS & ANSWERS
    // -----------------------------------------

    /**
     * Inserts a new question into the database.
     *
     * @param text the question text
     * @param createdBy the username of the creator
     * @return the generated question ID
     * @throws SQLException if an error occurs during insertion
     */
    public int insertQuestion(String text, String createdBy) throws SQLException {
        String query = "INSERT INTO questions (text, createdBy) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, text);
            pstmt.setString(2, createdBy);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert question, no ID obtained.");
    }

    /**
     * Inserts a new answer for a question into the database.
     *
     * @param questionId the ID of the question being answered
     * @param answeredBy the username of the answerer
     * @param text the answer text
     * @throws SQLException if an error occurs during insertion
     */
    public void insertAnswer(int questionId, String answeredBy, String text) throws SQLException {
        String query = "INSERT INTO answers (question_id, answeredBy, text) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, answeredBy);
            pstmt.setString(3, text);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all questions from the database.
     *
     * @return a list of string arrays, each containing the question ID and text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getAllQuestions() throws SQLException {
        List<String[]> questions = new ArrayList<>();
        String query = "SELECT id, text FROM questions";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                questions.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return questions;
    }

    /**
     * Retrieves all answers from the database.
     *
     * @return a list of string arrays, each containing the answer ID and text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getAllAnswers() throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = "SELECT id, text FROM answers";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                answers.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return answers;
    }

    /**
     * Retrieves the question ID associated with a given answer ID.
     *
     * @param answerId the answer ID
     * @return the corresponding question ID, or -1 if not found
     * @throws SQLException if a database access error occurs
     */
    public int getQuestionIdByAnswerId(int answerId) throws SQLException {
        String query = "SELECT question_id FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("question_id");
                }
            }
        }
        return -1;
    }

    /**
     * Updates the text of a question.
     *
     * @param id the question ID
     * @param newText the new text for the question
     * @throws SQLException if a database access error occurs
     */
    public void updateQuestion(int id, String newText) throws SQLException {
        String query = "UPDATE questions SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the text of an answer.
     *
     * @param id the answer ID
     * @param newText the new text for the answer
     * @throws SQLException if a database access error occurs
     */
    public void updateAnswer(int id, String newText) throws SQLException {
        String query = "UPDATE answers SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all questions created by a specific user.
     *
     * @param studentUsername the username of the user
     * @return a list of string arrays, each containing the question ID and text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getQuestionsByUser(String studentUsername) throws SQLException {
        List<String[]> questions = new ArrayList<>();
        String query = "SELECT id, text FROM questions WHERE createdBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return questions;
    }

    /**
     * Retrieves all answers provided by a specific user.
     *
     * @param studentUsername the username of the user
     * @return a list of string arrays, each containing the answer ID and text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getAnswersByUser(String studentUsername) throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = "SELECT id, text FROM answers WHERE answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("text")});
            }
        }
        return answers;
    }

    /**
     * Retrieves all answers for a specific question.
     *
     * @param questionId the question ID
     * @return a list of string arrays, each containing the answerer and answer text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getAnswersByQuestion(int questionId) throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = "SELECT answeredBy, text FROM answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String answeredBy = rs.getString("answeredBy");
                    String answerText = rs.getString("text");
                    answers.add(new String[]{answeredBy, answerText});
                }
            }
        }
        return answers;
    }

    /**
     * Retrieves the text of a question by its ID.
     *
     * @param questionId the question ID
     * @return the text of the question, or "(not found)" if not present
     * @throws SQLException if a database access error occurs
     */
    public String getQuestionTextById(int questionId) throws SQLException {
        String query = "SELECT text FROM questions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("text");
        }
        return "(not found)";
    }

    /**
     * Retrieves the text of an answer by its ID.
     *
     * @param answerId the answer ID
     * @return the text of the answer, or "(not found)" if not present
     * @throws SQLException if a database access error occurs
     */
    public String getAnswerTextById(int answerId) throws SQLException {
        String query = "SELECT text FROM answers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, answerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("text");
        }
        return "(not found)";
    }

    /**
     * Retrieves all answers for a given question ID.
     *
     * @param questionId the question ID
     * @return a list of string arrays, each containing the answer ID and text
     * @throws SQLException if a database access error occurs
     */
    public List<String[]> getAnswersByQuestionId(int questionId) throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String sql = "SELECT id, text FROM answers WHERE question_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                answers.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("text")
                });
            }
        }
        return answers;
    }

    // -----------------------------------------
    // REVIEWS & MESSAGES
    // -----------------------------------------

    /**
     * Inserts a new review into the database.
     *
     * @param review the {@code Review} object containing review details
     * @return the generated review ID, or -1 if insertion fails
     * @throws SQLException if a database access error occurs
     */
    public int insertReview(Review review) throws SQLException {
        String sql = "INSERT INTO Reviews (reviewer_id, target_type, target_id, content, weightage, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, review.getReviewerId());
            stmt.setString(2, review.getTargetType());
            stmt.setInt(3, review.getTargetId());
            stmt.setString(4, review.getContent());
            stmt.setInt(5, review.getWeightage());
            stmt.setTimestamp(6, Timestamp.valueOf(review.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(review.getUpdatedAt()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    /**
     * Updates the content of a review.
     *
     * @param review the {@code Review} object with updated content and timestamp
     * @throws SQLException if a database access error occurs
     */
    public void updateReview(Review review) throws SQLException {
        String sql = "UPDATE Reviews SET content = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, review.getContent());
            stmt.setTimestamp(2, Timestamp.valueOf(review.getUpdatedAt()));
            stmt.setInt(3, review.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a review from the database.
     *
     * @param reviewId the ID of the review to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM Reviews WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves all reviews provided by a specific reviewer.
     *
     * @param reviewerId the reviewer's username
     * @return a list of {@code Review} objects
     * @throws SQLException if a database access error occurs
     */
    public List<Review> getReviewsByReviewer(String reviewerId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE reviewer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reviewerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Review review = new Review(
                    rs.getInt("id"),
                    rs.getString("reviewer_id"),
                    rs.getString("target_type"),
                    rs.getInt("target_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
                review.setWeightage(rs.getInt("weightage"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    /**
     * Retrieves reviews related to a student.
     * <p>
     * For questions, it retrieves reviews for questions created by the student.
     * For answers, it retrieves all reviews related to answers provided by the student.
     * </p>
     *
     * @param studentUsername the student's username
     * @return a list of {@code Review} objects
     * @throws SQLException if a database access error occurs
     */
    public List<Review> getReviewsAboutStudent(String studentUsername) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = """
            SELECT * FROM Reviews 
            WHERE (target_type = 'question' AND target_id IN (
                SELECT id FROM questions WHERE createdBy = ?
            ))
            OR (target_type = 'answer')
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review(
                    rs.getInt("id"),
                    rs.getString("reviewer_id"),
                    rs.getString("target_type"),
                    rs.getInt("target_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
                );
                review.setWeightage(rs.getInt("weightage"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    /**
     * Updates the weightage of a review.
     *
     * @param reviewId the ID of the review
     * @param weightage the new weightage value
     * @throws SQLException if a database access error occurs
     */
    public void updateReviewWeightage(int reviewId, int weightage) throws SQLException {
        String sql = "UPDATE Reviews SET weightage = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, weightage);
            stmt.setInt(2, reviewId);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a new message into the database.
     *
     * @param message the {@code Message} object containing message details
     * @throws SQLException if a database access error occurs
     */
    public void insertMessage(Message message) throws SQLException {
        String query = "INSERT INTO Messages (review_id, sender, receiver, content, sent_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, message.getReviewId());
            pstmt.setString(2, message.getSender());
            pstmt.setString(3, message.getReceiver());
            pstmt.setString(4, message.getContent());
            pstmt.setTimestamp(5, Timestamp.valueOf(message.getSentAt()));
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all messages for a given review, ordered by the time sent.
     *
     * @param reviewId the ID of the review
     * @return a list of {@code Message} objects
     * @throws SQLException if a database access error occurs
     */
    public List<Message> getMessagesForReview(int reviewId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM Messages WHERE review_id = ? ORDER BY sent_at ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message(
                    rs.getInt("id"),
                    rs.getInt("review_id"),
                    rs.getString("sender"),
                    rs.getString("receiver"),
                    rs.getString("content"),
                    rs.getTimestamp("sent_at").toLocalDateTime()
                );
                messages.add(msg);
            }
        }
        return messages;
    }

    /**
     * Retrieves the reviewer ID associated with a review.
     *
     * @param reviewId the review ID
     * @return the reviewer's username, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public String getReviewerIdForReview(int reviewId) throws SQLException {
        String query = "SELECT reviewer_id FROM Reviews WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("reviewer_id");
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the student ID (username) associated with a review.
     * <p>
     * For reviews of questions, it returns the creator of the question.
     * For reviews of answers, it returns the answerer.
     * </p>
     *
     * @param reviewId the review ID
     * @return the student's username, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public String getStudentIdForReview(int reviewId) throws SQLException {
        String query = """
            SELECT q.createdBy FROM questions q
            JOIN reviews r ON r.target_type = 'question' AND r.target_id = q.id
            WHERE r.id = ?
            UNION
            SELECT a.answeredBy FROM answers a
            JOIN reviews r ON r.target_type = 'answer' AND r.target_id = a.id
            WHERE r.id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            stmt.setInt(2, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    // -----------------------------------------
    // REVIEWER REQUESTS (Brian’s additions)
    // -----------------------------------------

    /**
     * Inserts a reviewer request into the database.
     *
     * @param senderUsername the username of the student requesting to become a reviewer
     */
    public void sendReviewerRequest(String senderUsername) {
        String sql = "INSERT INTO ReviewerRequests (senderUsername) VALUES (?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, senderUsername);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all reviewer requests from the database.
     *
     * @return a list of usernames who have requested to become reviewers
     */
    public List<String> getReviewerRequests() {
        List<String> requests = new ArrayList<>();
        String sql = "SELECT senderUsername FROM ReviewerRequests";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(rs.getString("senderUsername"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Promotes a student to a reviewer by updating their role in the database.
     * Also removes the corresponding reviewer request.
     *
     * @param username the username of the student to be promoted
     */
    public void promoteToReviewer(String username) {
        String sql = "UPDATE cse360users SET role = 'Reviewer' WHERE userName = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            removeReviewerRequest(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a reviewer request from the database.
     *
     * @param username the username whose reviewer request is to be removed
     */
    public void removeReviewerRequest(String username) {
        String sql = "DELETE FROM ReviewerRequests WHERE senderUsername = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------
    // TRUSTED REVIEWERS (Task 5)
    // -----------------------------------------

    /**
     * Inserts a trusted reviewer pair into the TrustedReviewers table.
     *
     * @param student the student's username
     * @param reviewer the reviewer's username
     * @throws SQLException if a database access error occurs
     */
    public void addTrustedReviewer(String student, String reviewer) throws SQLException {
        String sql = "INSERT INTO TrustedReviewers (studentUsername, reviewerUsername) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student);
            pstmt.setString(2, reviewer);
            pstmt.executeUpdate();
        }
    }

    /**
     * Removes a trusted reviewer pair from the TrustedReviewers table.
     *
     * @param student the student's username
     * @param reviewer the reviewer's username to remove
     * @throws SQLException if a database access error occurs
     */
    public void removeTrustedReviewer(String student, String reviewer) throws SQLException {
        String sql = "DELETE FROM TrustedReviewers WHERE studentUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student);
            pstmt.setString(2, reviewer);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a list of reviewer usernames that a student has marked as trusted.
     *
     * @param student the student's username
     * @return a list of trusted reviewer usernames
     * @throws SQLException if a database access error occurs
     */
    public List<String> getTrustedReviewers(String student) throws SQLException {
        List<String> results = new ArrayList<>();
        String sql = "SELECT reviewerUsername FROM TrustedReviewers WHERE studentUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("reviewerUsername"));
            }
        }
        return results;
    }

    /**
     * Searches for reviews from trusted reviewers based on a keyword.
     * <p>
     * Only reviews from reviewers that the student has marked as trusted are considered.
     * The search is case-insensitive and the results are ordered by weightage (descending) and creation time.
     * </p>
     *
     * @param student the student's username requesting the search
     * @param keyword the keyword to search for in the review content
     * @return a list of {@code Review} objects matching the search criteria
     * @throws SQLException if a database access error occurs
     */
    public List<Review> searchReviewsByTrustedReviewers(String student, String keyword) throws SQLException {
        // 1) Get student's trusted reviewers
        List<String> trusted = getTrustedReviewers(student);
        if (trusted.isEmpty()) {
            // no trusted reviewers => return empty
            return new ArrayList<>();
        }

        // 2) Build an IN clause: ("?","?","?")
        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < trusted.size(); i++) {
            inClause.append("?");
            if (i < trusted.size() - 1) {
                inClause.append(",");
            }
        }
        inClause.append(")");

        // 3) Create the parameterized SQL
        String sql = "SELECT * FROM Reviews " +
                     "WHERE reviewer_id IN " + inClause +
                     "  AND LOWER(content) LIKE ? " +
                     "ORDER BY weightage DESC, created_at DESC";

        // 4) Execute and build the result list.
        List<Review> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int idx = 1;
            // Bind each trusted reviewer.
            for (String reviewer : trusted) {
                stmt.setString(idx++, reviewer);
            }
            // Bind the wildcard keyword.
            stmt.setString(idx, "%" + keyword.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review(
                            rs.getInt("id"),
                            rs.getString("reviewer_id"),
                            rs.getString("target_type"),
                            rs.getInt("target_id"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                    review.setWeightage(rs.getInt("weightage"));
                    results.add(review);
                }
            }
        }
        return results;
    }

    // -----------------------------------------
    // CLOSE CONNECTION
    // -----------------------------------------

    /**
     * Closes the database connection and associated statement.
     */
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
    
 // === STAFF METHODS START ===

    /**
     * Retrieves all questions and associated answers.
     * @return List of formatted Q&A strings.
     */
    public List<String> getAllQuestionsAndAnswers() {
        List<String> results = new ArrayList<>();
        String sql = "SELECT q.text as question_text, a.text as answer_text FROM questions q LEFT JOIN answers a ON q.id = a.question_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String q = rs.getString("question_text");
                String a = rs.getString("answer_text");
                results.add("Q: " + q + "\nA: " + (a != null ? a : "(No answer)"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Retrieves all private feedback messages from the database.
     * @return List of feedback message strings.
     */
    public List<String> getAllPrivateFeedback() {
        List<String> feedbacks = new ArrayList<>();
        String sql = "SELECT content FROM Messages";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                feedbacks.add("Feedback: " + rs.getString("content"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbacks;
    }

    /**
     * Inserts a new instructor note into the database.
     * @param note The note to add.
     */
    public void addInstructorNote(String note) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS instructor_notes (id INT AUTO_INCREMENT PRIMARY KEY, note TEXT)";
            statement.execute(sql);

            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO instructor_notes (note) VALUES (?)");
            pstmt.setString(1, note);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches for a keyword in questions and answers.
     * @param keyword The search keyword.
     * @return List of matched results.
     */
    public List<String> searchContentByKeyword(String keyword) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT text FROM questions WHERE text LIKE ? UNION SELECT text FROM answers WHERE text LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

// === STAFF METHODS END ===


}
