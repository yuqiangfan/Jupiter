package db;

import db.mysql.MySQLConnection;
/**
 * create different database instance
 * @author stevefan
 *
 */
public class DBConnectionFactory {
	private static final String DEFAULT_DB = "mysql";
	
	/**
	 * depending on input, create different database instance
	 * @param db
	 * @return
	 */
	public static DBConnection getConnection(String db) {
		switch (db) {
		case "mysql":
			return new MySQLConnection();
		case "mongodb":
			return null;
		default:
			throw new IllegalArgumentException("Invalid DB: " + db);
		}

	}
	
	/**
	 * create MySQL database instance if no argument
	 * @return
	 */
	public static DBConnection getConnection() {
		return getConnection(DEFAULT_DB);
	}

}
