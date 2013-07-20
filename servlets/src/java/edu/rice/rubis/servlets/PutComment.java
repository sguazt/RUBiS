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

/** This servlets display the page allowing a user to put a comment
 * on an item.
 * It must be called this way :
 * <pre>
 * http://..../PutComment?to=ww&itemId=xx&nickname=yy&password=zz
 *    where ww is the id of the user that will receive the comment
 *          xx is the item id
 *          yy is the nick name of the user
 *          zz is the user password
 * /<pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */

public class PutComment extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;

		String toStr = request.getParameter("to");
		String itemStr = request.getParameter("itemId");
		String name = request.getParameter("nickname");
		String pass = request.getParameter("password");
		sp = new ServletPrinter(response, "PubComment");

		if (toStr == null
			|| toStr.equals("")
			|| itemStr == null
			|| itemStr.equals("")
			|| name == null
			|| name.equals("")
			|| pass == null
			|| pass.equals(""))
		{
			this.printError("User id, name and password are required. Cannot process the request", sp);
			return;
		}

		PreparedStatement stmt = null;
		Connection conn = null;
		// Authenticate the user who want to comment
		conn = this.getConnection();
		Auth auth = new Auth(conn, sp);
		int userId = auth.authenticate(name, pass);
		if (userId == -1)
		{
			this.printError("You (" + name + "," + pass + ") don't have an account on RUBiS! You have to register first", sp);
			this.closeConnection(stmt, conn);
			return;
		}

		// Try to find the user corresponding to the 'to' ID

		try
		{
			Integer toId = new Integer(toStr);
			Integer itemId = new Integer(itemStr);
			ResultSet urs, irs;
			String toName = null, itemName = null;
			try
			{
				stmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
				stmt.setInt(1, toId.intValue());
				urs = stmt.executeQuery();
				if (urs.first())
				{
					toName = urs.getString("nickname");
				}
				stmt.close();
			}
			catch (Exception e)
			{
				this.printError("Failed to execute Query for user: " + e, sp);
				this.closeConnection(stmt, conn);
				return;
			}
			try
			{
				stmt = conn.prepareStatement("SELECT name FROM items WHERE id=?");
				stmt.setInt(1, itemId.intValue());
				irs = stmt.executeQuery();
				if (irs.first())
				{
					itemName = irs.getString("name");
				}
				stmt.close();
			}
			catch (Exception e)
			{
				this.printError("Failed to execute Query for item: " + e, sp);
				this.closeConnection(stmt, conn);
				return;
			}

			// Display the form for comment
			sp.printHTMLheader("RUBiS: Comment service");
			sp.printHTML("<center><h2>Give feedback about your experience with " + toName + "</h2><br>");
			sp.printHTML("<form action=\"/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreComment\" method=POST>"
						+ "<input type=hidden name=to value="
						+ toStr
						+ ">"
						+ "<input type=hidden name=from value="
						+ userId
						+ ">"
						+ "<input type=hidden name=itemId value="
						+ itemId
						+ ">"
						+ "<center><table>"
						+ "<tr><td><b>From</b><td>"
						+ name
						+ "<tr><td><b>To</b><td>"
						+ toName
						+ "<tr><td><b>About item</b><td>"
						+ itemName
						+ "<tr><td><b>Rating</b>"
						+ "<td><SELECT name=rating>"
						+ "<OPTION value=\"5\">Excellent</OPTION>"
						+ "<OPTION value=\"3\">Average</OPTION>"
						+ "<OPTION selected value=\"0\">Neutral</OPTION>"
						+ "<OPTION value=\"-3\">Below average</OPTION>"
						+ "<OPTION value=\"-5\">Bad</OPTION>"
						+ "</SELECT></table><p><br>"
						+ "<TEXTAREA rows=\"20\" cols=\"80\" name=\"comment\">Write your comment here</TEXTAREA><br><p>"
						+ "<input type=submit value=\"Post this comment now!\"></center><p>");
			sp.printHTMLfooter();
		}
		catch (Exception e)
		{
			this.printError("The item does not exist: " + e, sp);
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
		return Config.PutCommentPoolSize;
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
		this.printError("Put Comment", errorMsg, sp);
	}
}
