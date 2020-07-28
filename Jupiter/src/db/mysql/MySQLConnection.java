package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

/**
 * connection for MySQL server
 * 
 * @author stevefan
 *
 */
public class MySQLConnection implements DBConnection {

	private Connection conn;

	public MySQLConnection() {
		try {
			// load driver into JVM
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			// connect
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}

		try {
			String sql = "insert ignore into history (user_id, item_id) values (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}

		try {
			String sql = "delete from history where user_id = ? and item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}

		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);

		try {
			String sql = "select * from items where item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);

			for (String itemId : itemIds) {
				stmt.setString(1, itemId);

				ResultSet rs = stmt.executeQuery();

				ItemBuilder builder = new ItemBuilder();

				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));

					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}

		Set<String> favoriteItemIds = new HashSet<>();

		try {
			String sql = "select item_id from history where user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItemIds;

	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return new HashSet<String>();
		}

		Set<String> categories = new HashSet<>();
		try {
			String sql = "select category from categories where item_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				categories.add(rs.getString("category"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return categories;

	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;

	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			return;
		}

		try {
			// SQL injection (Problem)
			// Example:
			// SELECT * FROM users WHERE username = '<username>' AND password =
			// '<password>';
			//
			// sql = "SELECT * FROM users WHERE username = '" + username + "'
			// AND password = '" + password + "'";
			//
			// username: aoweifjoawefijwaoeifj
			// password: 123456' OR '1' = '1
			//
			// SELECT * FROM users WHERE username = 'aoweifjoawefijwaoeifj' AND password =
			// '123456' OR '1' = '1'
			// It is always true to have '1' = '1' in SQL, thus any password is correct ending
			// like this.

			// template using ?, using PreparedStatement
			String sql = "insert ignore into items values (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement stmt = conn.prepareStatement(sql);
			// begins from 1, not 0 --> this is to fill the ? for prepared statement
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());

			stmt.execute();
			
			// another table to save an item's categories
			sql = "insert ignore into categories values (?, ?)";
			stmt = conn.prepareStatement(sql);

			for (String category : item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "select first_name, last_name from users where user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			StringBuilder sb = new StringBuilder();
			if (rs.next()) {
				sb.append(rs.getString("first_name") + " " + rs.getString("last_name"));
				name = sb.toString();
				//name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;

	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "select user_id from users where user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			// If there is a result, then the combination of user name and password exists
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

}
