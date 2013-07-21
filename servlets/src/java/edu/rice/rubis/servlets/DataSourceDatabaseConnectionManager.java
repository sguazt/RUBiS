/*
 * Copyright (C) 2013       Marco Guazzone (marco.guazzone@gmail.com)
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
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * Setup and manages a pool of database connections through a DataSource object.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class DataSourceDatabaseConnectionManager implements DatabaseConnectionManager
{
//	private String _driver;
//	private String _url;
//	private String _user;
//	private String _passwd;
	private DataSource _ds = null;


//	public DataSourceDatabaseConnectionManager(String driver,
//											   String url,
//											   String user,
//											   String password)
//	{
//		this._driver = driver;
//		this._url = url;
//		this._user = user;
//		this._passwd = password;
//	}

	public DataSourceDatabaseConnectionManager()
	{
	}

//	public String getDriver()
//	{
//		return this._driver;
//	}

//	public String getUrl()
//	{
//		return this._url;
//	}

//	public String getUsername()
//	{
//		return this._user;
//	}

//	public String getPassword()
//	{
//		return this._passwd;
//	}

	public void init() throws SQLException
	{
		try
		{
			Context initCtx = new InitialContext();
			if (initCtx == null)
			{
				throw new SQLException("Unable to create an initial context");
			}

			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			this._ds = (DataSource) envCtx.lookup("jdbc/RUBiS"); 
		}
		catch (NamingException ne)
		{
			throw new SQLException("Unable to find the wanted context: " + ne);
		}
	}

	public void destroy()
	{
		// Empty
	}

	public Connection getConnection() throws SQLException
	{
		return this._ds.getConnection();
	}

	public void releaseConnection(Connection conn) throws SQLException
	{  
		if (!conn.isClosed())
		{
			conn.close();
		}
	}
}
