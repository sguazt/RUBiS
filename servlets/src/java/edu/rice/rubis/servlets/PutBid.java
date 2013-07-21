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
 * This servlets display the page allowing a user to put a bid
 * on an item.
 * It must be called this way :
 * <pre>
 * http://..../PutBid?itemId=xx&nickname=yy&password=zz
 *    where xx is the id of the item
 *          yy is the nick name of the user
 *          zz is the user password
 * /<pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */

public class PutBid extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;

		String itemStr = request.getParameter("itemId");
		String name = request.getParameter("nickname");
		String pass = request.getParameter("password");
		sp = new ServletPrinter(response, "PubBid");

		if (itemStr == null
			|| itemStr.equals("")
			|| name == null
			|| name.equals("")
			|| pass == null
			|| pass.equals(""))
		{
			this.printError("Item id, name and password are required. Cannot process the request", sp);
			return;
		}
		int itemId = Integer.parseInt(itemStr);

		PreparedStatement stmt = null;
		Connection conn = null;
		conn = this.getConnection();

		// Authenticate the user who want to bid
		Auth auth = new Auth(conn, sp);
		int userId = auth.authenticate(name, pass);
		if (userId == -1)
		{
			this.printError("You (" + name + "," + pass + ") don't have an account on RUBiS!. You have to register first", sp);
			this.closeConnection(stmt, conn);
			return;
		}

		// Try to find the Item corresponding to the Item ID
		String itemName, endDate, startDate, description, sellerName;
		float maxBid, initialPrice, buyNow, reservePrice;
		int quantity, sellerId, nbOfBids = 0;
		ResultSet rs = null;
		try
		{
			stmt = conn.prepareStatement("SELECT * FROM items WHERE id=?");
			stmt.setInt(1, itemId);
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute Query for item: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			if (!rs.first())
			{
				this.printError("This item does not exist!", sp);
				this.closeConnection(stmt, conn);
				return;
			}
			itemName = rs.getString("name");
			description = rs.getString("description");
			endDate = rs.getString("end_date");
			startDate = rs.getString("start_date");
			initialPrice = rs.getFloat("initial_price");
			reservePrice = rs.getFloat("reserve_price");
			buyNow = rs.getFloat("buy_now");
			quantity = rs.getInt("quantity");
			sellerId = rs.getInt("seller");

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
					this.printError("Unknown seller", sp);
					if (sellerStmt != null)
					{
						sellerStmt.close();
					}
					this.closeConnection(stmt, conn);
					return;
				}
				sellerStmt.close();
			}
			catch (SQLException e)
			{
				this.printError("Failed to executeQuery for seller: " + e, sp);
				if (sellerStmt != null)
				{
					sellerStmt.close();
				}
				this.closeConnection(stmt, conn);
				return;
			}
			PreparedStatement maxBidStmt = null;
			try
			{
				maxBidStmt = conn.prepareStatement("SELECT MAX(bid) AS bid FROM bids WHERE item_id=?");
				maxBidStmt.setInt(1, itemId);
				ResultSet maxBidResult = maxBidStmt.executeQuery();
				// Get the current price (max bid)		 
				if (maxBidResult.first())
				{
					maxBid = maxBidResult.getFloat("bid");
				}
				else
				{
					maxBid = initialPrice;
				}
				maxBidStmt.close();
			}
			catch (SQLException e)
			{
				this.printError("Failed to executeQuery for max bid: " + e, sp);
				if (maxBidStmt != null)
				{
					maxBidStmt.close();
				}
				this.closeConnection(stmt, conn);
				return;
			}
			PreparedStatement nbStmt = null;
			try
			{
				nbStmt = conn.prepareStatement("SELECT COUNT(*) AS bid FROM bids WHERE item_id=?");
				nbStmt.setInt(1, itemId);
				ResultSet nbResult = nbStmt.executeQuery();
				// Get the number of bids for this item
				if (nbResult.first())
				{
					nbOfBids = nbResult.getInt("bid");
				}
				nbStmt.close();
			}
			catch (SQLException e)
			{
				this.printError("Failed to executeQuery for number of bids: " + e, sp);
				if (nbStmt != null)
				{
					nbStmt.close();
				}
				this.closeConnection(stmt, conn);
				return;
			}
			sp.printItemDescription(itemId,
									itemName,
									description,
									initialPrice,
									reservePrice,
									buyNow,
									quantity,
									maxBid,
									nbOfBids,
									sellerName,
									sellerId,
									startDate,
									endDate,
									userId,
									conn);
			sp.printHTMLfooter();
		}
		catch (Exception e)
		{
			this.printError("Exception getting item list: " + e, sp);
		}
		this.closeConnection(stmt, conn);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doGet(request, response);
	}

	@Override
	protected int getPoolSize()
	{
		return Config.PutBidPoolSize;
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
				stmt.close(); // close statement
			}
			if (conn != null)
			{
				conn.setAutoCommit(true);
				this.releaseConnection(conn);
			}
		}
		catch (Exception ignore)
		{
		}
	}

	/**
	 * Display an error message.
	 * @param errorMsg the error message value
	 */
	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("Put Bid", errorMsg, sp);
	}
}
