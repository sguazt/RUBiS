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
 * This servlets displays the full description of a given item
 * and allows the user to bid on this item.
 * It must be called this way :
 * <pre>
 * http://..../ViewItem?itemId=xx where xx is the id of the item
 * /<pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class ViewItem extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		sp = new ServletPrinter(response, "ViewItem");
		ResultSet rs = null;

		String value = request.getParameter("itemId");
		if ((value == null) || (value.equals("")))
		{
			this.printError("No item identifier received. Cannot process the request", sp);
			return;
		}

		// get the item
		int itemId = Integer.parseInt(value);
		try
		{
			conn = this.getConnection();
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
				sp.printHTML("<h2>This item does not exist!</h2>");
				this.closeConnection(stmt, conn);
				return;
			}
			String itemName, endDate, startDate, description, sellerName;
			float maxBid, initialPrice, buyNow, reservePrice;
			int quantity, sellerId, nbOfBids = 0;
			itemName = rs.getString("name");
			description = rs.getString("description");
			endDate = rs.getString("end_date");
			startDate = rs.getString("start_date");
			initialPrice = rs.getFloat("initial_price");
			reservePrice = rs.getFloat("reserve_price");
			buyNow = rs.getFloat("buy_now");
			quantity = rs.getInt("quantity");
			sellerId = rs.getInt("seller");

			maxBid = rs.getFloat("max_bid");
			nbOfBids = rs.getInt("nb_of_bids");
			if (maxBid < initialPrice)
			{
				maxBid = initialPrice;
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
					return;
				}
				sellerStmt.close();
			}
			catch (SQLException e)
			{
				this.printError("Failed to execute Query for seller: " + e, sp);
				sellerStmt.close();
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
									-1,
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
		return Config.ViewItemPoolSize;
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

	/**
	 * Display an error message.
	 * @param errorMsg the error message value
	 */
	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("View Item", errorMsg, sp);
	}
}
