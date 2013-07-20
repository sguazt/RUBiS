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
 * This servlet records a BuyNow in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreBuyNow?itemId=aa&userId=bb&minBuyNow=cc&maxQty=dd&BuyNow=ee&maxBuyNow=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable BuyNow for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user BuyNow
 *          ff is the maximum BuyNow the user wants
 *          gg is the quantity asked by the user
 * </pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class StoreBuyNow extends RubisHttpServlet
{
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		int userId; // item id
		int itemId; // user id
		//     float   minBuyNow; // minimum acceptable BuyNow for this item
		//     float   BuyNow;    // user BuyNow
		//     float   maxBuyNow; // maximum BuyNow the user wants
		int maxQty; // maximum quantity available for this item
		int qty; // quantity asked by the user
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		sp = new ServletPrinter(response, "StoreBuyNow");

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
		String now = TimeManagement.currentDateToString();
		// Try to find the Item corresponding to the Item ID
		try
		{
			int quantity;
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement("SELECT quantity, end_date FROM items WHERE id=?");
			stmt.setInt(1, itemId);
			ResultSet irs = stmt.executeQuery();
			if (!irs.first())
			{
				conn.rollback();
				this.printError("This item does not exist in the database.", sp);
				this.closeConnection(stmt, conn);
				return;
			}
			quantity = irs.getInt("quantity");
			quantity = quantity - qty;
			stmt.close();
			if (quantity == 0)
			{
				stmt = conn.prepareStatement("UPDATE items SET end_date=?, quantity=? WHERE id=?");
				stmt.setString(1, now);
				stmt.setInt(2, quantity);
				stmt.setInt(3, itemId);
				stmt.executeUpdate();
				stmt.close();
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE items SET quantity=? WHERE id=?");
				stmt.setInt(1, quantity);
				stmt.setInt(2, itemId);
				stmt.executeUpdate();
				stmt.close();
			}
		}
		catch (SQLException e)
		{
			this.printError("Failed to execute Query for the item: " + e, sp);
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
			return;
		}
		try
		{
			stmt = conn.prepareStatement("INSERT INTO buy_now VALUES (NULL,?,?,?,?)");
			stmt.setInt(1, userId);
			stmt.setInt(2, itemId);
			stmt.setInt(3, qty);
			stmt.setString(4, now);
			stmt.executeUpdate();
			conn.commit();

			sp.printHTMLheader("RUBiS: BuyNow result");
			if (qty == 1)
			{
				sp.printHTML("<center><h2>Your have successfully bought this item.</h2></center>\n");
			}
			else
			{
				sp.printHTML("<center><h2>Your have successfully bought these items.</h2></center>\n");
			}
			sp.printHTMLfooter();
		}
		catch (Exception e)
		{
			this.printError("Error while storing the BuyNow: " + e, sp);
			try
			{
				conn.rollback();
			}
			catch (Exception se)
			{
				this.printError("Transaction rollback failed: " + e, sp);
			}
			return;
		}
		this.closeConnection(stmt, conn);
	}

	@Override
	protected int getPoolSize()
	{
		return Config.StoreBuyNowPoolSize;
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
			conn.setAutoCommit(true);
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
		this.printError("Store Buy Now", errorMsg, sp);
	}
}
