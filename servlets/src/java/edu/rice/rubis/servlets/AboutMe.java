/*
 * Copyright (C) 2002-2009  OW2 Consortium
 *
 * This file is part of dcsj-rubis (below referred to as "this program").
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Author: Marco Guazzone (marco.guazzone@gmail.com)
 */

package edu.rice.rubis.servlets;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This servlets displays general information about the user loged in
 * and about his current bids or items to sell.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class AboutMe extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = new ServletPrinter(response, "About me");

		String username = request.getParameter("nickname");
		String password = request.getParameter("password");
		if ((username == null || username.isEmpty())
			|| (password == null || password.isEmpty()))
		{
			this.printError(" You must provide valid username and password.", sp);
			return;
		}

		Connection conn = this.getConnection();

		// Authenticate the user
		int userId = -1;
		Auth auth = new Auth(conn, sp);
		userId = auth.authenticate(username, password);
		if (userId == -1)
		{
			this.printError("You (" + username + "," + password + ") don't have an account on RUBiS! You have to register first", sp);
			this.releaseConnection(conn);
			return;
		}

		// Try to find the user corresponding to the userId
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try
		{
			stmt = conn.prepareStatement("SELECT * FROM users WHERE id=?");
			stmt.setInt(1, userId);
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute Query for user: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			if (!rs.first())
			{
				sp.printHTML("<h2>This user does not exist!</h2>");
				this.closeConnection(stmt, conn);
				sp.printHTMLfooter();
				return;
			}

			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String nickname = rs.getString("nickname");
			String email = rs.getString("email");
			String date = rs.getString("creation_date");
			int rating = rs.getInt("rating");
			stmt.close();

			StringBuilder result = new StringBuilder();
			result.append("<h2>Information about " + nickname + "<br></h2>");
			result.append("Real life name : " + firstname + " " + lastname + "<br>");
			result.append("Email address  : " + email + "<br>");
			result.append("User since     : " + date + "<br>");
			result.append("Current rating : <b>" + rating + "</b><br>");
			sp.printHTMLheader("RUBiS: About " + nickname);
			sp.printHTML(result.toString());

		}
		catch (SQLException e)
		{
			this.printError("Failed to get general information about the user: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}

		boolean connAlive = false;
		connAlive = this.listBids(userId, username, password, stmt, conn, sp);
		if (connAlive)
		{
			connAlive = this.listItem(userId, conn, sp);
		}
		if (connAlive)
		{
			connAlive = this.listWonItems(userId, stmt, conn, sp);
		}
		if (connAlive)
		{
			connAlive = this.listBoughtItems(userId, stmt, conn, sp);
		}
		if (connAlive)
		{
			connAlive = this.listComment(userId, stmt, conn, sp);
		}
		sp.printHTMLfooter();
		if (connAlive)
		{
			this.closeConnection(stmt, conn);
		}
	}

	@Override
	protected int getPoolSize()
	{
		return Config.AboutMePoolSize;
	}

	/**
	 * Close both statement and connection.
	 */
	private void closeConnection(PreparedStatement stmt, Connection conn)
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
			if (conn != null)
			{
				conn.setAutoCommit(true);
				this.releaseConnection(conn);
			}
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	/**
	 * Display an error message.
	 * @param errorMsg the error message value
	 */
	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("About Me", errorMsg, sp);
	}

	/**
	 * List items the user is currently selling and sold in yhe past 30 days
	 */
	private boolean listItem(int userId, Connection conn, ServletPrinter sp)
	{
		// current sellings
		PreparedStatement currentStmt = null;
		ResultSet currentSellings = null;
		try
		{
			currentStmt = conn.prepareStatement("SELECT * FROM items WHERE items.seller=? AND items.end_date>=NOW()");
			currentStmt.setInt(1, userId);
			currentSellings = currentStmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Exception getting current sellings list: " + e, sp);
			this.closeConnection(currentStmt, conn);
			return false;
		}
		try
		{
			if (!currentSellings.first())
			{
				sp.printHTML("<br>");
				sp.printHTMLHighlighted("<h3>You are currently selling no item.</h3>");
				currentStmt.close();
			}
			else
			{
				// display current sellings
				sp.printHTML("<br>");
				sp.printSellHeader("Items you are currently selling.");
				do
				{
					int itemId;
					String itemName;
					String endDate;
					String startDate;
					float initialPrice;
					float reservePrice;
					float buyNow;
					int quantity;
					float currentPrice;

					// Get the item detail
					try
					{
						itemId = currentSellings.getInt("id");
						itemName = currentSellings.getString("name");
						endDate = currentSellings.getString("end_date");
						startDate = currentSellings.getString("start_date");
						initialPrice = currentSellings.getFloat("initial_price");
						reservePrice = currentSellings.getFloat("reserve_price");
						buyNow = currentSellings.getFloat("buy_now");
						quantity = currentSellings.getInt("quantity");
						currentPrice = currentSellings.getFloat("max_bid");
						if (currentPrice < initialPrice)
						{
							currentPrice = initialPrice;
						}
					}
					catch (Exception e)
					{
						this.printError("Exception getting item: " + e, sp);
						this.closeConnection(currentStmt, conn);
						return false;
					}

					// display information about the item
					sp.printSell(itemId,
								 itemName,
								 initialPrice,
								 reservePrice,
								 quantity,
								 buyNow,
								 startDate,
								 endDate,
								 currentPrice);
				}
				while (currentSellings.next());
				currentStmt.close();
				sp.printItemFooter();
			}
		}
		catch (Exception e)
		{
			this.printError("Exception getting current items in sell: " + e, sp);
			this.closeConnection(currentStmt, conn);
			return false;
		}

		// Past sellings
		PreparedStatement pastStmt = null;
		ResultSet pastSellings = null;
		try
		{
			pastStmt = conn.prepareStatement("SELECT * FROM items WHERE items.seller=? AND TO_DAYS(NOW()) - TO_DAYS(items.end_date) < 30");
			pastStmt.setInt(1, userId);
			pastSellings = pastStmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Exception getting past sellings list: " + e, sp);
			this.closeConnection(pastStmt, conn);
			return false;
		}
		try
		{
			if (!pastSellings.first())
			{
				sp.printHTML("<br>");
				sp.printHTMLHighlighted("<h3>You didn't sell any item.</h3>");
				pastStmt.close();
				return true;
			}
			// display past sellings
			sp.printHTML("<br>");
			sp.printSellHeader("Items you sold in the last 30 days.");
			do
			{
				int itemId;
				String itemName;
				String endDate;
				String startDate;
				float initialPrice;
				float reservePrice;
				float buyNow;
				int quantity;
				float currentPrice;

				// Get the item detail
				try
				{
					itemId = pastSellings.getInt("id");
					itemName = pastSellings.getString("name");
					endDate = pastSellings.getString("end_date");
					startDate = pastSellings.getString("start_date");
					initialPrice = pastSellings.getFloat("initial_price");
					reservePrice = pastSellings.getFloat("reserve_price");
					buyNow = pastSellings.getFloat("buy_now");
					quantity = pastSellings.getInt("quantity");
					currentPrice = pastSellings.getFloat("max_bid");
					if (currentPrice < initialPrice)
					{
						currentPrice = initialPrice;        
					}
				}
				catch (Exception e)
				{
					this.printError("Exception getting sold item: " + e, sp);
					this.closeConnection(pastStmt, conn);
					return false;
				}

				// display information about the item
				sp.printSell(itemId,
							 itemName,
							 initialPrice,
							 reservePrice,
							 quantity,
							 buyNow,
							 startDate,
							 endDate,
							 currentPrice);
			}
			while (pastSellings.next());
			pastStmt.close();
		}
		catch (Exception e)
		{
			this.printError("Exception getting sold items: " + e, sp);
			this.closeConnection(pastStmt, conn);
			return false;
		}

		sp.printItemFooter();

		return true;
	}

	/**
	 * List items the user bought in the last 30 days
	 */ 
	private boolean listBoughtItems(int userId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		ResultSet buy = null;
		String itemName;
		String sellerName;
		int quantity;
		int sellerId;
		int itemId;
		float buyNow;

		// Get the list of items the user bought
		try
		{
			stmt = conn.prepareStatement("SELECT * FROM buy_now WHERE buy_now.buyer_id=? AND TO_DAYS(NOW()) - TO_DAYS(buy_now.date)<=30");
			stmt.setInt(1, userId);
			buy = stmt.executeQuery();
			if (!buy.first())
			{
				sp.printHTML("<br>");
				sp.printHTMLHighlighted("<h3>You didn't buy any item in the last 30 days.</h3>");
				sp.printHTML("<br>");
				stmt.close();
				return true;
			}
		}
		catch (Exception e)
		{
			this.printError("Exception getting bought items list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printUserBoughtItemHeader();

		try
		{
			do
			{
				itemId = buy.getInt("item_id");
				quantity = buy.getInt("qty");
				// Get the name of the items
				try
				{
					PreparedStatement itemStmt = null;
					try
					{
						ResultSet itemRS = null;
						itemStmt = conn.prepareStatement("SELECT * FROM items WHERE id=?");
						itemStmt.setInt(1, itemId);
						itemRS = itemStmt.executeQuery();
						if (!itemRS.first())
						{
							sp.printHTML("Couldn't find bought item.<br>");
							itemStmt.close();
							return true;
						}
						itemName = itemRS.getString("name");
						sellerId = itemRS.getInt("seller");
						buyNow = itemRS.getFloat("buy_now");
						itemStmt.close();
					}
					catch (SQLException e)
					{
						this.printError("Failed to execute Query for item (buy now): " + e, sp);
						if (itemStmt != null) 
						{
							itemStmt.close();
						}
						this.closeConnection(stmt, conn);
						return false;
					}
					PreparedStatement sellerStmt = null;
					try
					{
						sellerStmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
						sellerStmt.setInt(1, sellerId);
						ResultSet sellerResult = sellerStmt.executeQuery();
						// Get the seller's name		 
						if (sellerResult.first())
						{
							sellerName = sellerResult.getString("nickname");
						}
						else
						{
							sp.printHTML("Unknown seller");
							sellerStmt.close();
							this.closeConnection(stmt, conn);
							return false;
						}
						sellerStmt.close();
					}
					catch (SQLException e)
					{
						this.printError("Failed to execute Query for seller (buy now): " + e, sp);
						if (sellerStmt != null) 
						{
							sellerStmt.close();
						}
						this.closeConnection(stmt, conn);
						return false;
					}
				}
				catch (Exception e)
				{
					this.printError("Exception getting buyNow: " + e, sp);
					this.closeConnection(stmt, conn);
					return false;
				}
				// display information about the item
				sp.printUserBoughtItem(itemId,
									   itemName,
									   buyNow,
									   quantity,
									   sellerId,
									   sellerName);
			}
			while (buy.next());
			stmt.close();
		}
		catch (Exception e)
		{
			this.printError("Exception getting bought items: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}
		sp.printItemFooter();
		return true;
	}

	/**
	 * List items the user won in the last 30 days
	 */
	private boolean listWonItems(int userId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		ResultSet won = null;

		// Get the list of the user's won items
		try
		{
			stmt = conn.prepareStatement("SELECT item_id FROM bids, items WHERE bids.user_id=? AND bids.item_id=items.id AND TO_DAYS(NOW()) - TO_DAYS(items.end_date) < 30 GROUP BY item_id");
			stmt.setInt(1, userId);
			won = stmt.executeQuery();
			if (!won.first())
			{
				sp.printHTML("<br>");
				sp.printHTMLHighlighted("<h3>You didn't win any item in the last 30 days.</h3>");
				sp.printHTML("<br>");
				stmt.close();
				return true;
			}
		}
		catch (Exception e)
		{
			this.printError("Exception getting won items list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printUserWonItemHeader();
		try
		{
			do
			{
				int sellerId;
				int itemId;
				float currentPrice;
				float initialPrice;
				String itemName;
				String sellerName;

				itemId = won.getInt("item_id");

				// Get the item detail
				try
				{
					PreparedStatement itemStmt = null;
					try
					{
						ResultSet itemRS = null;
						itemStmt = conn.prepareStatement("SELECT * FROM items WHERE id=?");
						itemStmt.setInt(1, itemId);
						itemRS = itemStmt.executeQuery();
						if (!itemRS.first())
						{
							sp.printHTML("Couldn't find won item.<br>");
							itemStmt.close();          
							return true;
						}
						itemName = itemRS.getString("name");
						sellerId = itemRS.getInt("seller");
						initialPrice = itemRS.getFloat("initial_price");
						currentPrice = itemRS.getFloat("max_bid");
						if (currentPrice < initialPrice)
						{
							currentPrice = initialPrice;
						}
						itemStmt.close();
					}
					catch (SQLException e)
					{
						this.printError("Failed to execute Query for item (won items): " + e, sp);
						if (itemStmt != null) 
						{
							itemStmt.close();
						}
						this.closeConnection(stmt, conn);
						return false;
					}
					PreparedStatement sellerStmt = null;
					try
					{
						sellerStmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
						sellerStmt.setInt(1, sellerId);
						ResultSet sellerResult = sellerStmt.executeQuery();
						// Get the seller's name		 
						if (sellerResult.first())
						{
							sellerName = sellerResult.getString("nickname");
						}
						else
						{
							sp.printHTML("Unknown seller");
							sellerStmt.close();
							this.closeConnection(stmt, conn);
							return false;
						}
						sellerStmt.close();
					}
					catch (SQLException e)
					{
						this.printError("Failed to execute Query for seller (won items): " + e, sp);
						if (sellerStmt != null) 
						{
							sellerStmt.close();
						}
						this.closeConnection(stmt, conn);
						sellerStmt = null;
						return false;
					}
					//PreparedStatement currentPriceStmt = null;
					//try 
					//{
					//	currentPriceStmt = conn.prepareStatement("SELECT MAX(bid) AS bid FROM bids WHERE item_id=?");
					//	currentPriceStmt.setInt(1, itemId);
					//	ResultSet currentPriceResult = currentPriceStmt.executeQuery();
					//	// Get the current price (max bid)		 
					//	if (currentPriceResult.first()) 
					//	{
					//		currentPrice = currentPriceResult.getFloat("bid");
					//	}
					//	else
					//	{
					//		currentPrice = initialPrice;
					//	}
					//}
					//catch (SQLException e)
					//{
					//	sp.printHTML("Failed to executeQuery for current price: " +e);
					//	this.closeConnection();
					//	if (currentPriceStmt!=null)
					//	{
					//		currentPriceStmt.close();
					//	}
					//	return false;
					//}
				}
				catch (Exception e)
				{
					this.printError("Exception getting item: " + e, sp);
					this.closeConnection(stmt, conn);
					return false;
				}

				// display information about the item
				sp.printUserWonItem(itemId,
									itemName,
									currentPrice,
									sellerId,
									sellerName);
			}
			while (won.next());
			stmt.close();
		}
		catch (Exception e)
		{
			this.printError("Exception getting won items: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printItemFooter();

		return true;
	}

	/**
	 * List comments about the user
	 */
	private boolean listComment(int userId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		ResultSet rs = null;
		try
		{
			conn.setAutoCommit(false); // faster if made inside a Tx

			// Try to find the comment corresponding to the user
			try
			{
				stmt = conn.prepareStatement("SELECT * FROM comments WHERE to_user_id=?");
				stmt.setInt(1, userId);
				rs = stmt.executeQuery();
			}
			catch (Exception e)
			{
				this.printError("Failed to execute Query for list of comments: " + e, sp);
				conn.rollback();
				this.closeConnection(stmt, conn);
				return false;
			}
			if (!rs.first())
			{
				sp.printHTML("<br>");
				sp.printHTMLHighlighted("<h3>There is no comment yet for this user.</h3>");
				sp.printHTML("<br>");
				conn.commit();
				conn.setAutoCommit(true);
				stmt.close();
				return true;
			}
			else
			{
				sp.printHTML("<br><hr><br><h3>Comments for this user</h3><br>");
			}

			sp.printCommentHeader();
			// Display each comment and the name of its author
			do
			{
				String comment = rs.getString("comment");
				String date = rs.getString("date");
				int authorId = rs.getInt("from_user_id");

				String authorName = "none";
				ResultSet authorRS = null;
				PreparedStatement authorStmt = null;
				try
				{
					authorStmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
					authorStmt.setInt(1, authorId);
					authorRS = authorStmt.executeQuery();
					if (authorRS.first())
					{
						authorName = authorRS.getString("nickname");
					}
					authorStmt.close();
				}
				catch (Exception e)
				{
					this.printError("Failed to execute Query for the comment author: " + e, sp);
					conn.rollback();
					if (authorStmt != null)
					{
						authorStmt.close();
					}
					this.closeConnection(stmt, conn);
					return false;
				}
				sp.printComment(authorName, authorId, date, comment);
			}
			while (rs.next());      

			sp.printCommentFooter();

			conn.commit();
			conn.setAutoCommit(true);
			stmt.close();
		}
		catch (Exception e)
		{
			this.printError("Exception getting comment list: " + e, sp);
			try
			{
				conn.rollback();
				this.closeConnection(stmt, conn);
			}
			catch (Exception se)
			{
				this.printError("Transaction rollback failed: " + e, sp);
				this.closeConnection(stmt, conn);
			}
			return false;
		}

		return true;
	}

	/**
	 * List items the user put a bid on in the last 30 days
	 */
	private boolean listBids(int userId, String username, String password, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		ResultSet bid = null;

		// Get the list of the user's last bids
		try
		{
			stmt = conn.prepareStatement("SELECT item_id, bids.max_bid FROM bids, items WHERE bids.user_id=? AND bids.item_id=items.id AND items.end_date>=NOW() GROUP BY item_id");
			stmt.setInt(1, userId);
			bid = stmt.executeQuery();
			if (!bid.first())
			{
				sp.printHTMLHighlighted("<h3>You didn't put any bid.</h3>");
				sp.printHTML("<br>");
				stmt.close();
				return true;
			}
		}
		catch (Exception e)
		{
			this.printError("Exception getting bids list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printUserBidsHeader();
		ResultSet rs = null;
		PreparedStatement itemStmt = null;
		try
		{
			do
			{
				float currentPrice;
				float initialPrice;
				float maxBid;
				String itemName;
				String sellerName;
				String startDate;
				String endDate;
				int sellerId;
				int quantity;
				int itemId;

				itemId = bid.getInt("item_id");
				maxBid = bid.getFloat("max_bid");
				try
				{
					itemStmt = conn.prepareStatement("SELECT * FROM items WHERE id=?");
					itemStmt.setInt(1, itemId);
					rs = itemStmt.executeQuery();
				}
				catch (Exception e)
				{
					this.printError("Failed to execute Query for item the user has bid on: " + e, sp);
					if (itemStmt != null)
					{
						itemStmt.close();
					}
					this.closeConnection(stmt, conn);
					return false;
				}

				// Get the name of the items
				try
				{
					if (!rs.first())
					{
						sp.printHTML("<h3>Failed to get items.</h3><br>");
						itemStmt.close();
						this.closeConnection(stmt, conn);
						return false;
					}
					itemName = rs.getString("name");
					initialPrice = rs.getFloat("initial_price");
					quantity = rs.getInt("quantity");
					startDate = rs.getString("start_date");
					endDate = rs.getString("end_date");
					sellerId = rs.getInt("seller");
					currentPrice = rs.getFloat("max_bid");
					if (currentPrice < initialPrice)
					{
						currentPrice = initialPrice;
					}
					itemStmt.close();

					PreparedStatement sellerStmt = null;
					try
					{
						sellerStmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
						sellerStmt.setInt(1, sellerId);
						ResultSet sellerResult = sellerStmt.executeQuery();
						// Get the seller's name		 
						if (sellerResult.first())
						{
							sellerName = sellerResult.getString("nickname");
						}
						else
						{
							sp.printHTML("Unknown seller");
							if (sellerStmt != null)
							{
								sellerStmt.close();
							}
							this.closeConnection(stmt, conn);
							return false;
						}
						sellerStmt.close();
					}
					catch (Exception e)
					{
						this.printError("Failed to execute Query for seller (bids): " + e, sp);
						if (itemStmt != null)
						{
							itemStmt.close();
						}
						if (sellerStmt != null)
						{
							sellerStmt.close();
						}
						this.closeConnection(stmt, conn);
						return false;
					}
				}
				catch (Exception e)
				{
					this.printError("Exception getting item: " + e, sp);
					this.closeConnection(stmt, conn);
					return false;
				}

				//  display information about user's bids
				sp.printItemUserHasBidOn(itemId,
										 itemName,
										 initialPrice,
										 quantity,
										 startDate,
										 endDate,
										 sellerId,
										 sellerName,
										 currentPrice,
										 maxBid,
										 username,
										 password);
			}
			while (bid.next());
			stmt.close();
		}
		catch (Exception e)
		{
			this.printError("Exception getting items the user has bid on: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printItemFooter();

		return true;
	}
}
