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
 * This servlets displays general information about a user. It must be called
 * this way :
 * 
 * <pre>
 * 
 *  http://..../ViewUserInfo?userId=xx where xx is the id of the user
 *  
 * </pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class ViewUserInfo extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String value = request.getParameter("userId");
		int userId;
		ResultSet rs = null;
		ServletPrinter sp = null;
		PreparedStatement stmt = null;
		Connection conn = null;

		sp = new ServletPrinter(response, "ViewUserInfo");

		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a user identifier!", sp);
			return;
		}
		userId = Integer.parseInt(value);

		sp.printHTMLheader("RUBiS: View user information");

		// Try to find the user corresponding to the userId
		try
		{
			conn = this.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM users WHERE id=?");
			stmt.setInt(1, userId);
			rs = stmt.executeQuery();
		}
		catch (Exception e)
		{
			this.printError("Failed to execute Query for user: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			if (!rs.first())
			{
				sp.printHTML("<h2>This user does not exist!</h2>");
				sp.printHTMLfooter();
				this.closeConnection(stmt, conn);
				return;
			}
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String nickname = rs.getString("nickname");
			String email = rs.getString("email");
			String date = rs.getString("creation_date");
			int rating = rs.getInt("rating");
			stmt.close();

			String result = new String();

			result = result + "<h2>Information about " + nickname + "<br></h2>";
			result = result + "Real life name : " + firstname + " " + lastname + "<br>";
			result = result + "Email address  : " + email + "<br>";
			result = result + "User since     : " + date + "<br>";
			result = result + "Current rating : <b>" + rating + "</b><br>";
			sp.printHTML(result);

		}
		catch (SQLException s)
		{
			this.printError("Failed to get general information about the user: " + s, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		boolean connAlive = this.commentList(userId, stmt, conn, sp);
		sp.printHTMLfooter();
		// connAlive means we must close it. Otherwise we must NOT do a
		// double free
		if (connAlive)
		{
			this.closeConnection(stmt, conn);
		}
	}

	@Override
	protected int getPoolSize()
	{
		return Config.ViewUserInfoPoolSize;
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
		catch (SQLException e)
		{
		}
	}

	private boolean commentList(int userId, PreparedStatement stmt, Connection conn, ServletPrinter sp)
	{
		ResultSet rs = null;
		String date, comment;
		int authorId;

		try
		{
			conn.setAutoCommit(false); // faster if made inside a Tx

			// Try to find the comment corresponding to the user
			try
			{
				stmt = conn.prepareStatement("SELECT * FROM comments WHERE to_user_id=?");
				stmt.setInt(1, userId);
				rs = stmt.executeQuery();
			}
			catch (Exception e)
			{
				this.printError("Failed to execute Query for list of comments: " + e, sp);
				conn.rollback();
				this.closeConnection(stmt, conn);
				return false;
			}
			if (!rs.first())
			{
				sp.printHTML("<h3>There is no comment yet for this user.</h3><br>");
				conn.commit();
				this.closeConnection(stmt, conn);
				return false;
			}
			sp.printHTML("<br><hr><br><h3>Comments for this user</h3><br>");

			sp.printCommentHeader();
			// Display each comment and the name of its author
			do
			{
				comment = rs.getString("comment");
				date = rs.getString("date");
				authorId = rs.getInt("from_user_id");

				String authorName = "none";
				ResultSet authorRS = null;
				PreparedStatement authorStmt = null;
				try
				{
					authorStmt = conn.prepareStatement("SELECT nickname FROM users WHERE id=?");
					authorStmt.setInt(1, authorId);
					authorRS = authorStmt.executeQuery();
					if (authorRS.first())
					{
						authorName = authorRS.getString("nickname");
					}
					authorStmt.close();
				}
				catch (Exception e)
				{
					this.printError("Failed to execute Query for the comment author: " + e, sp);
					conn.rollback();
					if (authorStmt != null)
					{
						authorStmt.close();
					}
					this.closeConnection(stmt, conn);
					return false;
				}
				sp.printComment(authorName, authorId, date, comment);
			}
			while (rs.next());
			sp.printCommentFooter();
			conn.commit();
		}
		catch (Exception e)
		{
			this.printError("Exception getting comment list: " + e, sp);
			try
			{
				conn.rollback();
				this.closeConnection(stmt, conn);
				return false;
			}
			catch (Exception se)
			{
				this.printError("Transaction rollback failed: " + e, sp);
				this.closeConnection(stmt, conn);
				return false;
			}
		}
		return true;
	}

	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("View User Info", errorMsg, sp);
	}
}
