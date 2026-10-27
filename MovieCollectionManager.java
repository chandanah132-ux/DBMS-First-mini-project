/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package moviecollectionmanager;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MovieCollectionManager extends JFrame {

    private static final String URL =
            "jdbc:mysql://localhost:3306/";

    private static final String USER = "root";

    // Replace with your actual MySQL password
    private static final String PASSWORD = "Chandana_gowda!?2008";

    private JTextField titleField;
    private JTextField genreField;
    private JTextField yearField;
    private JTextField directorField;
    private JTextField ratingField;
    private JTextField searchField;

    private JComboBox<String> statusBox;

    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public MovieCollectionManager() {

        setTitle("Movie Collection Manager");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "MOVIE COLLECTION MANAGER",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        // Movie details form
        JPanel formPanel = new JPanel(
                new GridLayout(6, 2, 8, 8)
        );

        formPanel.setBorder(
                BorderFactory.createTitledBorder("Movie Details")
        );

        titleField = new JTextField();
        genreField = new JTextField();
        yearField = new JTextField();
        directorField = new JTextField();
        ratingField = new JTextField();

        statusBox = new JComboBox<>(
                new String[]{"Watched", "Not Watched"}
        );

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);

        formPanel.add(new JLabel("Genre:"));
        formPanel.add(genreField);

        formPanel.add(new JLabel("Release Year:"));
        formPanel.add(yearField);

        formPanel.add(new JLabel("Director:"));
        formPanel.add(directorField);

        formPanel.add(new JLabel("Rating (0-10):"));
        formPanel.add(ratingField);

        formPanel.add(new JLabel("Watch Status:"));
        formPanel.add(statusBox);

        // Buttons
        JButton addButton = new JButton("Add Movie");
        JButton viewButton = new JButton("View Movies");
        JButton updateButton = new JButton("Update Movie");
        JButton deleteButton = new JButton("Delete Movie");
        JButton clearButton = new JButton("Clear Fields");

        JPanel buttonPanel = new JPanel(
                new GridLayout(5, 1, 5, 5)
        );

        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Search panel
        searchField = new JTextField(15);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        JPanel searchPanel = new JPanel();

        searchPanel.add(new JLabel("Search Title:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        // Movie table
        String[] columns = {
                "Title", "Genre", "Year",
                "Director", "Rating", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        movieTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(movieTable);

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        totalLabel = new JLabel("Total Movies: 0");
        tablePanel.add(totalLabel, BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));

        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);

        // Button actions
        addButton.addActionListener(e -> addMovie());
        viewButton.addActionListener(e -> viewMovies());
        updateButton.addActionListener(e -> updateMovie());
        deleteButton.addActionListener(e -> deleteMovie());
        clearButton.addActionListener(e -> clearFields());
        searchButton.addActionListener(e -> searchMovie());
        showAllButton.addActionListener(e -> viewMovies());

        movieTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFieldsFromTable();
            }
        });

        viewMovies();
    }

    // Connect to MySQL and create the database/table if needed
    private static Connection getConnection() throws SQLException {

        try (Connection con = DriverManager.getConnection(
                URL, USER, PASSWORD);
             Statement st = con.createStatement()) {

            st.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS movie_collection_db"
            );
        }

        Connection con = DriverManager.getConnection(
                URL + "movie_collection_db", USER, PASSWORD
        );

        try (Statement st = con.createStatement()) {

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS movies (
                        title VARCHAR(100) PRIMARY KEY,
                        genre VARCHAR(50) NOT NULL,
                        release_year INT NOT NULL,
                        director VARCHAR(100) NOT NULL,
                        rating DECIMAL(3,1),
                        watch_status VARCHAR(20)
                            DEFAULT 'Not Watched'
                    )
                    """);
        }

        return con;
    }

    // Validate movie details
    private Object[] getMovieDetails() {

        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String yearText = yearField.getText().trim();
        String director = directorField.getText().trim();
        String ratingText = ratingField.getText().trim();
        String status = (String) statusBox.getSelectedItem();

        if (title.isEmpty() || genre.isEmpty()
                || yearText.isEmpty() || director.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please fill in all required fields."
            );

            return null;
        }

        if (title.length() > 100
                || genre.length() > 50
                || director.length() > 100) {

            JOptionPane.showMessageDialog(
                    this, "One or more text fields are too long."
            );

            return null;
        }

        try {

            int year = Integer.parseInt(yearText);

            if (year < 1888 || year > 2100) {
                throw new IllegalArgumentException(
                        "Enter a valid release year."
                );
            }

            Double rating = null;

            if (!ratingText.isEmpty()) {

                rating = Double.parseDouble(ratingText);

                if (!Double.isFinite(rating)
                        || rating < 0 || rating > 10) {

                    throw new IllegalArgumentException(
                            "Rating must be between 0 and 10."
                    );
                }
            }

            return new Object[]{
                    title, genre, year, director, rating, status
            };

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this, "Year must be a whole number and rating must be numeric."
            );

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage());
        }

        return null;
    }

    // INSERT
    private void addMovie() {

        Object[] d = getMovieDetails();

        if (d == null) return;

        String sql = """
                INSERT INTO movies
                (title, genre, release_year, director, rating, watch_status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setParameters(ps, d);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this, "Movie added successfully!"
            );

            clearFields();
            viewMovies();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // SELECT
    private void viewMovies() {

        tableModel.setRowCount(0);

        String sql = """
                SELECT title, genre, release_year,
                       director, rating, watch_status
                FROM movies
                ORDER BY title
                """;

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                addRow(rs);
            }

            totalLabel.setText(
                    "Total Movies: " + tableModel.getRowCount()
            );

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void addRow(ResultSet rs) throws SQLException {

        tableModel.addRow(new Object[]{
                rs.getString("title"),
                rs.getString("genre"),
                rs.getInt("release_year"),
                rs.getString("director"),
                rs.getObject("rating"),
                rs.getString("watch_status")
        });
    }

    // SEARCH
    private void searchMovie() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter a movie title to search."
            );
            return;
        }

        tableModel.setRowCount(0);

        String sql = """
                SELECT title, genre, release_year,
                       director, rating, watch_status
                FROM movies
                WHERE title LIKE ?
                ORDER BY title
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    addRow(rs);
                }
            }

            totalLabel.setText(
                    "Search Results: " + tableModel.getRowCount()
            );

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // Fill the form when a table row is selected
    private void fillFieldsFromTable() {

        int row = movieTable.getSelectedRow();

        if (row == -1) return;

        titleField.setText(tableModel.getValueAt(row, 0).toString());
        genreField.setText(tableModel.getValueAt(row, 1).toString());
        yearField.setText(tableModel.getValueAt(row, 2).toString());
        directorField.setText(tableModel.getValueAt(row, 3).toString());

        Object rating = tableModel.getValueAt(row, 4);

        ratingField.setText(
                rating == null ? "" : rating.toString()
        );

        statusBox.setSelectedItem(
                tableModel.getValueAt(row, 5).toString()
        );
    }

    // UPDATE
    private void updateMovie() {

        int row = movieTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Select a movie from the table first."
            );
            return;
        }

        Object[] d = getMovieDetails();

        if (d == null) return;

        String originalTitle =
                tableModel.getValueAt(row, 0).toString();

        String sql = """
                UPDATE movies
                SET title=?, genre=?, release_year=?,
                    director=?, rating=?, watch_status=?
                WHERE title=?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setParameters(ps, d);
            ps.setString(7, originalTitle);

            int count = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    count > 0 ? "Movie updated successfully!"
                              : "Movie not found."
            );

            clearFields();
            viewMovies();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // DELETE
    private void deleteMovie() {

        int row = movieTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Select a movie from the table first."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this movie?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String title = tableModel.getValueAt(row, 0).toString();

        String sql = "DELETE FROM movies WHERE title=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);

            int count = ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    count > 0 ? "Movie deleted successfully!"
                              : "Movie not found."
            );

            clearFields();
            viewMovies();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    // Set parameters for INSERT and UPDATE
    private void setParameters(
            PreparedStatement ps, Object[] d
    ) throws SQLException {

        ps.setString(1, (String) d[0]);
        ps.setString(2, (String) d[1]);
        ps.setInt(3, (Integer) d[2]);
        ps.setString(4, (String) d[3]);

        if (d[4] == null) {
            ps.setNull(5, Types.DECIMAL);
        } else {
            ps.setDouble(5, (Double) d[4]);
        }

        ps.setString(6, (String) d[5]);
    }

    // CLEAR
    private void clearFields() {

        titleField.setText("");
        genreField.setText("");
        yearField.setText("");
        directorField.setText("");
        ratingField.setText("");

        statusBox.setSelectedItem("Not Watched");
        movieTable.clearSelection();
    }

    private void showDatabaseError(SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Database error:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                Class.forName("com.mysql.cj.jdbc.Driver");

                MovieCollectionManager app =
                        new MovieCollectionManager();

                app.setVisible(true);

            } catch (ClassNotFoundException ex) {

                JOptionPane.showMessageDialog(
                        null,
                        "MySQL JDBC driver not found. "
                        + "Add MySQL Connector/J to your project."
                );
            }
        });
    }
}

