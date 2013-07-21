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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet records a bid in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreBid?itemId=aa&userId=bb&minBid=cc&maxQty=dd&bid=ee&maxBid=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable bid for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user bid
 *          ff is the maximum bid the user wants
 *          gg is the quantity asked by the user
 * </pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */

public class StoreBid extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		int userId; // item id
		int itemId; // user id
		float minBid; // minimum acceptable bid for this item
		float bid; // user bid
		float maxBid; // maximum bid the user wants
		int maxQty; // maximum quantity available for this item
		int qty; // quantity asked by the user
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		sp = new ServletPrinter(response, "StoreBid");

		/* Get and check all parameters */

		String value = request.getParameter("userId");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a user identifier!", sp);
			return;
		}
		userId = Integer.parseInt(value);

		value = request.getParameter("itemId");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide an item identifier!", sp);
			return;
		}
		itemId = Integer.parseInt(value);

		value = request.getParameter("minBid");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a minimum bid!", sp);
			return;
		}
		minBid = Float.parseFloat(value);

		value = request.getParameter("bid");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a bid!", sp);
			return;
		}
		bid = Float.parseFloat(value);

		value = request.getParameter("maxBid");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a maximum bid!", sp);
			return;
		}
		maxBid = Float.parseFloat(value);

		value = request.getParameter("maxQty");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a maximum quantity!", sp);
			return;
		}
		maxQty = Integer.parseInt(value);

		value = request.getParameter("qty");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a quantity!", sp);
			return;
		}
		qty = Integer.parseInt(value);

		/* Check for invalid values */

		if (maxQty <= 0 || qty > maxQty)
		{
			this.printError("You cannot request " + qty + " items because only " + maxQty + " are proposed!", sp);
			return;
		}
		if (bid < minBid)
		{
			this.printError("Your bid of $" + bid + " is not acceptable because it is below the $" + minBid + " minimum bid!", sp);
			return;
		}
		if (maxBid < minBid)
		{
			this.printError("Your maximum bid of $" + maxBid + " is not acceptable because it is below the $" + minBid + " minimum bid!", sp);
			return;
		}
		if (maxBid < bid)
		{
			this.printError("Your maximum bid of $" + maxBid + " is not acceptable because it is below your current bid of $" + bid + "!", sp);
			return;
		}
		try
		{
			conn = this.getConnection();
			conn.setAutoCommit(false);
			String now = TimeManagement.currentDateToString();
			stmt = conn.prepareStatement( "INSERT INTO bids VALUES (NULL,?,?,?,?,?,?)");
			stmt.setInt(1, userId);
			stmt.setInt(2, itemId);
			stmt.setInt(3, qty);
			stmt.setFloat(4, bid);
			stmt.setFloat(5, maxBid);
			stmt.setString(6, now);
			stmt.executeUpdate();
			stmt.close();
			// update the number of bids and the max bid for the item
			PreparedStatement update = null;
			try
			{
				stmt = conn.prepareStatement("SELECT nb_of_bids, max_bid FROM items WHERE id=?");
				stmt.setInt(1, itemId);
				ResultSet rs = stmt.executeQuery();
				if (rs.first())
				{
					int nbOfBids = rs.getInt("nb_of_bids");
					nbOfBids++;
					float oldMaxBid = rs.getFloat("max_bid");
					if (bid > oldMaxBid)
					{
						oldMaxBid = bid;
						update =
						conn.prepareStatement("UPDATE items SET max_bid=?, nb_of_bids=? WHERE id=?");
						update.setFloat(1, maxBid);
						update.setInt(2, nbOfBids);
						update.setInt(3, itemId);
						update.executeUpdate();
						update.close();
					}
					else
					{
						update = conn.prepareStatement("UPDATE items SET nb_of_bids=? WHERE id=?");
						update.setInt(1, nbOfBids);
						update.setInt(2, itemId);
						update.executeUpdate();
						update.close();
					}
				}
				else
				{
					conn.rollback();
					this.printError("Couldn't find the item.", sp);
					this.closeConnection(stmt, conn);
					return;
				}
			}
			catch (Exception ex)
			{
				conn.rollback();
				this.printError("Failed to update nb of bids and max bid: " + ex, sp);
				if (update != null) 
				{
					update.close();
				}
				this.closeConnection(stmt, conn);
				return;
			}
			conn.commit();

			sp.printHTMLheader("RUBiS: Bidding result");
			sp.printHTML("<center><h2>Your bid has been successfully processed.</h2></center>\n");
			sp.printHTMLfooter();
		}
		catch (Exception e)
		{
			this.printError("Error while storing the bid: " + e, sp);
			try
			{
				conn.rollback();
			}
			catch (Exception se)
			{
				this.printError("Transaction rollback failed: " + e, sp);
			}
		}
		this.closeConnection(stmt, conn);
	}

	@Override
	protected int getPoolSize()
	{
		return Config.StoreBidPoolSize;
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
		this.printError("Store Bid", errorMsg, sp);
	}
}
