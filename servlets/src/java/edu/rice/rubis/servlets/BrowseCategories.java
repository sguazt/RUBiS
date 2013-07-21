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
 * Builds the html page with the list of all categories and provides links to
 * browse all items in a category or items in a category for a given region
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class BrowseCategories extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		int regionId = -1, userId = -1;
		String username = null, password = null;

		sp = new ServletPrinter(response, "BrowseCategories");
		sp.printHTMLheader("RUBiS available categories");

		username = request.getParameter("nickname");
		password = request.getParameter("password");

		conn = this.getConnection();

		// Authenticate the user who want to sell items
		if ((username != null && username != "") || (password != null && password != ""))
		{
			Auth auth = new Auth(conn, sp);
			userId = auth.authenticate(username, password);
			if (userId == -1)
			{
				this.printError("You (" + username + "," + password + ") don't have an account on RUBiS!<br>You have to register first", sp);
				this.closeConnection(stmt, conn);
				return;
			}
		}

		String value = request.getParameter("region");
		if ((value != null) && (!value.equals("")))
		{
			// get the region ID
			try
			{
				stmt = conn.prepareStatement("SELECT id FROM regions WHERE name=?");
				stmt.setString(1, value);
				ResultSet rs = stmt.executeQuery();
				if (!rs.first())
				{
					sp.printHTML(" Region " + value + " does not exist in the database!<br>");
					this.closeConnection(stmt, conn);
					return;
				}
				regionId = rs.getInt("id");
				stmt.close();
			}
			catch (SQLException e)
			{
				this.printError("Failed to execute Query for region: " + e, sp);
				this.closeConnection(stmt, conn);
				return;
			}
		}

		boolean connAlive = this.categoryList(regionId, userId, stmt, conn, sp);
		if (connAlive)
		{
			this.closeConnection(stmt, conn);
		}
		sp.printHTMLfooter();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doGet(request, response);
	}

	@Override
	protected int getPoolSize()
	{
		return Config.BrowseCategoriesPoolSize;
	}

	/**
	* Close the connection and statement.
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
			// Ignore
		}
	}

	/** List all the categories in the database */
	private boolean categoryList(int regionId, int userId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		String categoryName;
		int categoryId;
		ResultSet rs = null;

		// get the list of categories
		try
		{
			stmt = conn.prepareStatement("SELECT name, id FROM categories");
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute query for categories list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		try
		{
			if (!rs.first())
			{
				sp.printHTML("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>");
				this.closeConnection(stmt, conn);
				return false;
			}
			else
			{
				sp.printHTML("<h2>Currently available categories</h2><br>");
			}

			do
			{
				categoryName = rs.getString("name");
				categoryId = rs.getInt("id");

				if (regionId != -1)
				{
					sp.printCategoryByRegion(categoryName, categoryId, regionId);
				}
				else
				{
					if (userId != -1)
					{
						sp.printCategoryToSellItem(categoryName, categoryId, userId);
					}
					else
					{
						sp.printCategory(categoryName, categoryId);
					}
				}
			}
			while (rs.next());
		}
		catch (Exception e)
		{
			this.printError("Exception getting categories list: " + e, sp);
			this.closeConnection(stmt, conn);
			return false;
		}

		return true;
	}

	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("Browse Categories", errorMsg, sp);
	}
}
