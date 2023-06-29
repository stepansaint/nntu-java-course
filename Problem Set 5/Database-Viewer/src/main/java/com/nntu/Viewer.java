package com.nntu;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.sql.*;

class Viewer extends JFrame {

	private Connection connection;
	private JPanel mainPanel;
	private JButton add;
	private JButton find;
	private JTextField id;
	private JTextField idToFind;
	private JTextField month;
	private JTextField name;
	private JTextArea status;

	Viewer() {
		buildGUI();
		connectToDatabase();

		add.addActionListener((event) -> {
			String insertSql = "INSERT INTO birthdays (id, name, month) VALUES (?, ?, ?)";
			try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
				validateFieldsAreFilled(id, name, month);

				statement.setInt(1, Integer.parseInt(id.getText()));
				statement.setString(2, name.getText());
				statement.setInt(3, Integer.parseInt(month.getText()));

				statement.executeUpdate();
				status.setText("Successfully added a new entry.");
			} catch (NumberFormatException nfe) {
				status.setText("Please, use integers in `id` and `month` fields.");
			} catch (IllegalStateException ise) {
				status.setText("Please, specify all three fields (id, name, month).");
			} catch (SQLException sqle) {
				status.setText(sqle.getMessage());
				sqle.printStackTrace();
			}
		});

		find.addActionListener((event) -> {
			String selectSql = "SELECT * FROM birthdays WHERE id = ?";
			try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
				validateFieldsAreFilled(idToFind);

				statement.setInt(1, Integer.parseInt(idToFind.getText()));

				try (ResultSet result = statement.executeQuery()) {
					if (result.next()) {
						status.setText(
								"The birthday of " + result.getString("name")
										+ " is on " + result.getString("month") + " month."
						);
					} else status.setText("There's no entry with this id.");
				}
			} catch (NumberFormatException nfe) {
				status.setText("Please, use integer in `id` field.");
			} catch (IllegalStateException ise) {
				status.setText("Please, specify the field (id).");
			} catch (SQLException sqle) {
				status.setText(sqle.getMessage());
				sqle.printStackTrace();
			}
		});
	}

	/**
	 * Builds a graphical user interface and shows it to the user.
	 */
	private void buildGUI() {
		setContentPane(mainPanel);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(540,280);
		setTitle("DBViewer");
		setVisible(true);
	}

	/**
	 * Connects to the database.
	 */
	private void connectToDatabase() {
		Runtime.getRuntime().addShutdownHook(
				new Thread(() -> {
					if (connection != null) {
						try {
							connection.close();
						} catch (SQLException sqle) {
							sqle.printStackTrace();
						}
					}
				})
		);

		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/test_db",
					"postgres",
					""
			);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			status.setText(
					"Couldn't connect to the database. "
							+ "Check the connection settings in this application."
			);
		}
	}

	/**
	 * Checks components are not empty or blank.
	 * @param components components to be checked
	 */
	private void validateFieldsAreFilled(JTextComponent... components) {
		for (JTextComponent component : components) {
			if (component.getText().isBlank())
				throw new IllegalStateException("All fields should be filled.");
		}
	}
}
