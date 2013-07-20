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
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Add a new item in the database 
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class RegisterItem extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String name = null, description = null;
		float initialPrice, buyNow, reservePrice;
		int quantity, duration;
		int categoryId, userId, stringToInt;
		String startDate, endDate;
		int itemId;

		ServletPrinter sp = null;
		sp = new ServletPrinter(response, "RegisterItem");

		String value = request.getParameter("name");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a name!", sp);
			return;
		}
		name = value;

		value = request.getParameter("description");
		if ((value == null) || (value.equals("")))
		{
			description = "No description.";
		}
		else
		{
			description = value;
		}

		value = request.getParameter("initialPrice");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide an initial price!", sp); return;
		}
		initialPrice = Float.parseFloat(value);

		value = request.getParameter("reservePrice");
		if ((value == null) || (value.equals("")))
		{
			reservePrice = 0;
		}
		else
		{
			reservePrice = Float.parseFloat(value);
		}

		value = request.getParameter("buyNow");
		if ((value == null) || (value.equals("")))
		{
			buyNow = 0;
		}
		else
		{
			buyNow = Float.parseFloat(value);
		}

		value = request.getParameter("duration");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a duration!", sp);
			return;
		}
		duration = Integer.parseInt(value);
		GregorianCalendar now, later;
		now = new GregorianCalendar();
		later = TimeManagement.addDays(now, duration);
		startDate = TimeManagement.dateToString(now);
		endDate = TimeManagement.dateToString(later);

		value = request.getParameter("quantity");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a quantity!", sp);
			return;
		}
		quantity = Integer.parseInt(value);

		userId = Integer.parseInt(request.getParameter("userId"));
		categoryId = Integer.parseInt(request.getParameter("categoryId"));

		PreparedStatement stmt = null;
		Connection conn = null;
		try
		{
			conn = this.getConnection();
			conn.setAutoCommit(false);

			// Try to create a new item
			try
			{
				stmt = conn.prepareStatement("INSERT INTO items VALUES (NULL,?,?,?,?,?,?,0,0,?,?,?,?)");
				stmt.setString(1, name);
				stmt.setString(2, description);
				stmt.setFloat(3, initialPrice);
				stmt.setInt(4, quantity);
				stmt.setFloat(5, reservePrice);
				stmt.setFloat(6, buyNow);
				stmt.setString(7, startDate);
				stmt.setString(8, endDate);
				stmt.setInt(9, userId);
				stmt.setInt(10, categoryId);
				stmt.executeUpdate();
				stmt.close();
			}
			catch (SQLException e)
			{
				conn.rollback();
				this.printError("Item registration failed: " + e, sp);
				this.closeConnection(stmt, conn);
				return;
			}
			// To test if the item was correctly added in the database
			try
			{
				stmt = conn.prepareStatement("SELECT id FROM items WHERE name=?");
				stmt.setString(1, name);
				ResultSet irs = stmt.executeQuery();
				if (!irs.first())
				{
					conn.rollback();
					this.printError("This item does not exist in the database.", sp);
					this.closeConnection(stmt, conn);
					return;
				}
				itemId = irs.getInt("id");
			}
			catch (SQLException e)
			{
				conn.rollback();
				this.printError("Failed to execute Query for the new item: " + e, sp);
				this.closeConnection(stmt, conn);
				return;
			}

			conn.commit();

			sp.printHTMLheader("RUBiS: Item to sell " + name);
			sp.printHTML("<h2>Your Item has been successfully registered.</h2><br>");
			sp.printHTML("RUBiS has stored the following information about your item:<br>");
			sp.printHTML("Name         : " + name + "<br>");
			sp.printHTML("Description  : " + description + "<br>");
			sp.printHTML("Initial price: " + initialPrice + "<br>");
			sp.printHTML("ReservePrice : " + reservePrice + "<br>");
			sp.printHTML("Buy Now      : " + buyNow + "<br>");
			sp.printHTML("Quantity     : " + quantity + "<br>");
			sp.printHTML("User id      :" + userId + "<br>");
			sp.printHTML("Category id  :" + categoryId + "<br>");
			sp.printHTML("Duration     : " + duration + "<br>");
			sp.printHTML("<br>The following information has been automatically generated by RUBiS:<br>");
			sp.printHTML("Start date   :" + startDate + "<br>");
			sp.printHTML("End date     :" + endDate + "<br>");
			sp.printHTML("item id      :" + itemId + "<br>");
			sp.printHTMLfooter();
		}
		catch (Exception e)
		{
			this.printError("Exception getting comment list: " + e, sp);
			try
			{
				conn.rollback();
			}
			catch (Exception se)
			{
				this.printError("Transaction rollback failed: " + se, sp);
			}
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
		return Config.RegisterItemPoolSize;
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
		this.printError("Register Item", errorMsg, sp);
	}
}
