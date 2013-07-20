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
 * This servlets displays the list of bids regarding an item.
 * It must be called this way :
 * <pre>
 * http://..../ViewUserInfo?itemId=xx where xx is the id of the item
 * /<pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class ViewBidHistory extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String value = request.getParameter("itemId");
		int itemId;
		String itemName;
		ResultSet rs = null;
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		sp = new ServletPrinter(response, "ViewBidHistory");

		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide an item identifier", sp);
			return;
		}
		itemId = Integer.parseInt(value);
		if (itemId == -1)
		{
			sp.printHTML("ItemId is -1: this item does not exist.<br>");
		}

		sp.printHTMLheader("RUBiS: Bid history");

		// get the item
		try
		{
			conn = this.getConnection();
			stmt = conn.prepareStatement("SELECT name FROM items WHERE id=?");
			stmt.setInt(1, itemId);
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute Query for item in table items: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			if (!rs.first())
			{
				sp.printHTML("<h2>This item does not exist!</h2>");
				this.closeConnection(stmt, conn);
				return;
			}
			itemName = rs.getString("name");
			sp.printHTML("<center><h3>Bid History for " + itemName + "<br></h3></center>");
		}
		catch (Exception e)
		{
			this.printError("This item does not exist: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}

		boolean connAlive = this.listBids(itemId, stmt, conn, sp);
		// connAlive means we must close it. Otherwise we must NOT do a
		// double free
		if (connAlive)
		{
			this.closeConnection(stmt, conn);
		}
		sp.printHTMLfooter();
	}

	@Override
	protected int getPoolSize()
	{
	return Config.ViewBidHistoryPoolSize;
	}

	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("View Bid History", errorMsg, sp);
	}

	/**
	 * Close both statement and connection to the database.
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

	/** List the bids corresponding to an item */
	private boolean listBids(int itemId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		float bid;
		int userId;
		String bidderName, date;
		ResultSet rs = null;

		// Get the list of the user's last bids
		try
		{
			stmt = conn.prepareStatement("SELECT * FROM bids WHERE item_id=? ORDER BY date DESC");
			stmt.setInt(1, itemId);
			rs = stmt.executeQuery();
			if (!rs.first())
			{
				sp.printHTML("<h3>There is no bid corresponding to this item.</h3><br>");
				this.closeConnection(stmt, conn);
				return false;
			}
		}
		catch (SQLException e)
		{
			this.printError("Exception getting bids list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		sp.printBidHistoryHeader();
		try
		{
			do
			{
				// Get the bids
				date = rs.getString("date");
				bid = rs.getFloat("bid");
				userId = rs.getInt("user_id");

				ResultSet urs = null;
				try
				{
					stmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
					stmt.setInt(1, userId);
					urs = stmt.executeQuery();
					if (!urs.first())
					{
						sp.printHTML("This user does not exist in the database.<br>");
						this.closeConnection(stmt, conn);
						return false;
					}
					bidderName = urs.getString("nickname");
				}
				catch (SQLException e)
				{
					this.printError("Couldn't get bidder name: " + e, sp);
					this.closeConnection(stmt, conn);
					return false;
				}
				sp.printBidHistory(userId, bidderName, bid, date);
			}
			while (rs.next());
		}
		catch (SQLException e)
		{
			this.printError("Exception getting bid: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}
		sp.printBidHistoryFooter();
		return true;
	}
}
