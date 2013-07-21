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
 * Add a new user in the database 
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class RegisterUser extends RubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		PreparedStatement stmt = null;
		Connection conn = null;

		String  firstname = null,
				lastname = null,
				nickname = null,
				email = null,
				password = null;
		int regionId;
		int userId;
		String creationDate, region;

		ServletPrinter sp = null;
		sp = new ServletPrinter(response, "RegisterUser");

		String value = request.getParameter("firstname");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a first name!", sp);
			return;
		}
		firstname = value;

		value = request.getParameter("lastname");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a last name!", sp);
			return;
		}
		lastname = value;

		value = request.getParameter("nickname");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a nick name!", sp);
			return;
		}
		nickname = value;

		value = request.getParameter("email");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide an email address!", sp);
			return;
		}
		email = value;

		value = request.getParameter("password");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a password!", sp);
			return;
		}
		password = value;

		value = request.getParameter("region");
		if ((value == null) || (value.equals("")))
		{
			this.printError("You must provide a valid region!", sp);
			return;
		}
		region = value;

		// Create the region ID
		try
		{
			conn = this.getConnection();
			stmt = conn.prepareStatement("SELECT id FROM regions WHERE name=?");
			stmt.setString(1, region);
			ResultSet rs = stmt.executeQuery();
			if (!rs.first())
			{
				this.printError(" Region " + value + " does not exist in the database!", sp);
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

		// Try to create a new user
		try
		{
			stmt = conn.prepareStatement("SELECT nickname FROM users WHERE nickname=?");
			stmt.setString(1, nickname);
			ResultSet rs = stmt.executeQuery();
			if (rs.first())
			{
				this.printError("The nickname you have choosen is already taken by someone else. Please choose a new nickname.", sp);
				this.closeConnection(stmt, conn);
				return;
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			this.printError("Failed to execute Query to check the nickname: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			String now = TimeManagement.currentDateToString();
			stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL,?,?,?,?,?,0,0,?,?)");
			stmt.setString(1, firstname);
			stmt.setString(2, lastname);
			stmt.setString(3, nickname);
			stmt.setString(4, password);
			stmt.setString(5, email);
			stmt.setString(6, now);
			stmt.setInt(7, regionId);
			stmt.executeUpdate();
			stmt.close();
		}
		catch (SQLException e)
		{
			this.printError("User registration failed: " + e, sp);
			this.closeConnection(stmt, conn);
			return;
		}
		try
		{
			stmt = conn.prepareStatement("SELECT id, creation_date FROM users WHERE nickname=?");
			stmt.setString(1, nickname);
			ResultSet urs = stmt.executeQuery();
			if (!urs.first())
			{
				this.printError("This user does not exist in the database.", sp);
				this.closeConnection(stmt, conn);
				return;
			}
			userId = urs.getInt("id");
			creationDate = urs.getString("creation_date");

			sp.printHTMLheader("RUBiS: Welcome to " + nickname);
			sp.printHTML("<h2>Your registration has been processed successfully</h2><br>");
			sp.printHTML("<h3>Welcome " + nickname + "</h3>");
			sp.printHTML("RUBiS has stored the following information about you:<br>");
			sp.printHTML("First Name : " + firstname + "<br>");
			sp.printHTML("Last Name  : " + lastname + "<br>");
			sp.printHTML("Nick Name  : " + nickname + "<br>");
			sp.printHTML("Email      : " + email + "<br>");
			sp.printHTML("Password   : " + password + "<br>");
			sp.printHTML("Region     : " + region + "<br>");
			sp.printHTML("<br>The following information has been automatically generated by RUBiS:<br>");
			sp.printHTML("User id       :" + userId + "<br>");
			sp.printHTML("Creation date :" + creationDate + "<br>");
			sp.printHTMLfooter();
		}
		catch (SQLException e)
		{
			this.printError("Failed to execute Query for user: " + e, sp);
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
		return Config.RegisterUserPoolSize;
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
		this.printError("Register User", errorMsg, sp);
	}
}
