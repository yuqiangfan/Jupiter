package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is to clean up the database, thus only a main() method
 * @author stevefan
 *
 */
public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			// This is java.sql.Connection. Not com.mysql.jdbc.Connection.
			Connection conn = null;

			// Step 1 Connect to MySQL.
			try {
				System.out.println("Connecting to " + MySQLDBUtil.URL);
				// new instance for MySQL
				Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
				// connect
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (conn == null) {
				System.out.print("Failed to connect!");
				return;
			}
			
			// Drop table if exists
			Statement stmt = conn.createStatement();
			
			String sql = "drop table if exists categories";
			stmt.executeUpdate(sql);
			
			sql = "drop table if exists history";
			stmt.executeUpdate(sql);
			
			sql = "drop table if exists items";
			stmt.executeUpdate(sql);
			
			sql = "drop table if exists users";
			stmt.executeUpdate(sql);
			
			// Create tables
			sql = "create table items ("
					+ "item_id varchar(255) not null,"
					+ "name varchar(255),"
					+ "rating float,"
					+ "address varchar(255),"
					+ "image_url varchar(255),"
					+ "url varchar(255),"
					+ "distance float,"
					+ "primary key (item_id)"
					+ ")";
			stmt.executeUpdate(sql);
			
			sql = "create table categories ("
					+ "item_id varchar(255) not null,"
					+ "category varchar(255) not null,"
					+ "primary key (item_id, category),"
					+ "foreign key (item_id) references items(item_id)"
					+ ")";
			stmt.executeUpdate(sql);

			sql = "create table users ("
					+ "user_id varchar(255) not null,"
					+ "password varchar(255) not null,"
					+ "first_name varchar(255),"
					+ "last_name varchar(255),"
					+ "primary key (user_id)"
					+ ")";
			stmt.executeUpdate(sql);
			
			sql = "create table history ("
					+ "user_id varchar(255) not null,"
					+ "item_id varchar(255) not null,"
					+ "last_favor_time timestamp not null default current_timestamp,"
					+ "primary key (user_id, item_id),"
					+ "foreign key (item_id) references items(item_id),"
					+ "foreign key (user_id) references users(user_id)"
					+ ")";
			stmt.executeUpdate(sql);
			
			System.out.println("Database refreshed successfully.");
			
			// Test
			// create fake user
			sql = "insert into users values ("
					+ "'1111', '3229c1097c00d497a0fd282d586be050', 'Steve', 'F'"
					+ ")";
			
			System.out.println("Executing query: " + sql);
			stmt.executeUpdate(sql);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
