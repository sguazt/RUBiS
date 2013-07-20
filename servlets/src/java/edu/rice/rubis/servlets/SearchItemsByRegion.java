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
 * Build the html page with the list of all items for given category and region.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class SearchItemsByRegion extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		int categoryId, regionId;
		int page;
		int nbOfItems;

		ServletPrinter sp = null;
		sp = new ServletPrinter(response, "SearchItemsByRegion");

		String value = request.getParameter("category");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a category!", sp);
			return;
		}
		categoryId = Integer.parseInt(value);

		value = request.getParameter("region");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a region!", sp);
			return;
		}
		regionId = Integer.parseInt(value);

		value = request.getParameter("page");
		if ((value == null) || (value.equals("")))
		{
			page = 0;
		}
		else
		{
			page = Integer.parseInt(value);
		}

		value = request.getParameter("nbOfItems");
		if ((value == null) || (value.equals("")))
		{
			nbOfItems = 25;
		}
		else
		{
			nbOfItems = Integer.parseInt(value);
		}

		sp.printHTMLheader("RUBiS: Search items by region");
		this.itemList(categoryId, regionId, page, nbOfItems, sp);
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
		return Config.SearchItemsByRegionPoolSize;
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
		this.printError("Search Items by Region", errorMsg, sp);
	}

	/** List items in the given category for the given region */
	private void itemList(int categoryId, int regionId, int page, int nbOfItems, ServletPrinter sp)
	{
		String itemName, endDate;
		int itemId, nbOfBids = 0;
		float maxBid;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		// get the list of items
		try
		{
			conn = this.getConnection();
			stmt = conn.prepareStatement("SELECT items.name, items.id, items.end_date, items.max_bid, items.nb_of_bids, items.initial_price FROM items,users WHERE items.category=? AND items.seller=users.id AND users.region=? AND end_date>=NOW() ORDER BY items.end_date ASC LIMIT ?,?");
			stmt.setInt(1, categoryId);
			stmt.setInt(2, regionId);
			stmt.setInt(3, page * nbOfItems);
			stmt.setInt(4, nbOfItems);
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute Query for items in region: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			if (!rs.first())
			{
				if (page == 0)
				{
					sp.printHTML("<h3>Sorry, but there is no items in this category for this region.</h3><br>");
				}
				else
				{
					sp.printHTML("<h3>Sorry, but there is no more items in this category for this region.</h3><br>");
					sp.printItemHeader();
					sp.printItemFooter("<a href=\"/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByRegion?category="
									+ categoryId
									+ "&region="
									+ regionId
									+ "&page="
									+ (page - 1)
									+ "&nbOfItems="
									+ nbOfItems
									+ "\">Previous page</a>",
									"");
				}
				this.closeConnection(stmt, conn);
				return;
			}

			sp.printItemHeader();
			do
			{
				itemName = rs.getString("name");
				itemId = rs.getInt("id");
				endDate = rs.getString("end_date");
				maxBid = rs.getFloat("max_bid");
				nbOfBids = rs.getInt("nb_of_bids");
				float initialPrice = rs.getFloat("initial_price");
				if (maxBid < initialPrice)
				{
					maxBid = initialPrice;
				}
				sp.printItem(itemName, itemId, maxBid, nbOfBids, endDate);
			}
			while (rs.next());
			if (page == 0)
			{
				sp.printItemFooter("",
								"<a href=\"/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByRegion?category="
								+ categoryId
								+ "&region="
								+ regionId
								+ "&page="
								+ (page + 1)
								+ "&nbOfItems="
								+ nbOfItems
								+ "\">Next page</a>");
			}
			else
			{
				sp.printItemFooter("<a href=\"/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByRegion?category="
								+ categoryId
								+ "&region="
								+ regionId
								+ "&page="
								+ (page - 1)
								+ "&nbOfItems="
								+ nbOfItems
								+ "\">Previous page</a>",
								"<a href=\"/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByRegion?category="
								+ categoryId
								+ "&region="
								+ regionId
								+ "&page="
								+ (page + 1)
								+ "&nbOfItems="
								+ nbOfItems
								+ "\">Next page</a>");
			}
		}
		catch (Exception e)
		{
			this.printError("Exception getting item list: " + e, sp);
		}
		this.closeConnection(stmt, conn);
	}
}
