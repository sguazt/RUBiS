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
 * This servlets display the page allowing a user to buy an item
 * It must be called this way:
 * <pre>
 * http://..../BuyNow?itemId=xx&nickname=yy&password=zz
 *    where xx is the id of the item
 *          yy is the nick name of the user
 *          zz is the user password
 * </pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */

public class BuyNow extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;

		String itemStr = request.getParameter("itemId");
		String name = request.getParameter("nickname");
		String pass = request.getParameter("password");
		sp = new ServletPrinter(response, "BuyNow");

		if (itemStr == null
			|| itemStr.equals("")
			|| name == null
			|| name.equals("")
			|| pass == null
			|| pass.equals(""))
		{
			this.printError("Item id, name and password are required. Cannot process the request.", sp);
			return;
		}

		PreparedStatement stmt = null;
		Connection conn = null;
		conn = this.getConnection();
		// Authenticate the user who want to bid
		Auth auth = new Auth(conn, sp);
		int userId = auth.authenticate(name, pass);
		if (userId == -1)
		{
			this.printError(" You (" + name + "," + pass + ") don't have an account on RUBiS! You have to register first.", sp);
			this.closeConnection(stmt, conn);
			return;
		}
		// Try to find the Item corresponding to the Item ID
		int itemId = Integer.parseInt(itemStr);
		try
		{
			stmt = conn.prepareStatement("SELECT * FROM items WHERE id=?");
			stmt.setInt(1, itemId);
			ResultSet irs = stmt.executeQuery();
			if (!irs.first())
			{
				this.printError("This item does not exist in the database.", sp);
				this.closeConnection(stmt, conn);
				return;
			}

			String itemName = irs.getString("name");
			String description = irs.getString("description");
			String startDate = irs.getString("start_date");
			String endDate = irs.getString("end_date");
			float buyNow = irs.getFloat("buy_now");
			int quantity = irs.getInt("quantity");
			int sellerId = irs.getInt("seller");
			stmt.close();
			String sellerName = null;
			try
			{
				stmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
				stmt.setInt(1, sellerId);
				ResultSet srs = stmt.executeQuery();
				if (!srs.first())
				{
					this.printError("This user does not exist in the database.", sp);
					this.closeConnection(stmt, conn);
					return;
				}
				sellerName = srs.getString("nickname");
			}
			catch (SQLException s)
			{
				this.printError("Failed to execute Query for seller: " + s, sp);
				this.closeConnection(stmt, conn);
				return;
			}
			// Display the form for buying the item
			sp.printItemDescriptionToBuyNow(itemId,
											itemName,
											description,
											buyNow,
											quantity,
											sellerId,
											sellerName,
											startDate,
											endDate,
											userId);

			sp.printHTMLfooter();
		}
		catch (SQLException e)
		{
			this.printError("Failed to execute Query for item: " + e, sp);
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
		return Config.BuyNowPoolSize;
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
		this.printError("Buy Now", errorMsg, sp);
	}
}
