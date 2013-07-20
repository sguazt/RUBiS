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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Authenticate the current user.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class Auth
{

	//private Context servletContext;
	private Connection _conn = null;
	private ServletPrinter _sp = null;

	public Auth(Connection connect, ServletPrinter printer)
	{
		this._conn = connect;
		this._sp = printer;
	}

	public int authenticate(String name, String password)
	{
		int userId = -1;
		ResultSet rs = null;
		PreparedStatement stmt = null;

		// Lookup the user
		try
		{
			stmt = this._conn.prepareStatement("SELECT users.id FROM users WHERE nickname=? AND password=?");
			stmt.setString(1, name);
			stmt.setString(2, password);
			rs = stmt.executeQuery();
			if (!rs.first())
			{
				this._sp.printHTML(" User " + name + " does not exist in the database!<br><br>");
				return userId;
			}
			userId = rs.getInt("id");
		}
		catch (SQLException e)
		{
			// Ignore: return -1 as userId
		}
		finally
		{
			try
			{
				if (stmt != null)
				{
					stmt.close(); // close statement
				}
			}
			catch (Exception ignore)
			{
			}
		}
		return userId;
	}
}
