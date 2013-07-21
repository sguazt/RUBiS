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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Setup and manages a pool of database connections by means of the
 * DriverManager class.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class PooledDriverManagerDatabaseConnectionManager implements DatabaseConnectionManager
{
	private String _driver;
	private String _url;
	private String _user;
	private String _passwd;
	private int _size;
	private BlockingQueue<Connection> _pool = null;


	public PooledDriverManagerDatabaseConnectionManager(String driver,
														String url,
														String user,
														String password,
														int maxConnections)
	{
		this._driver = driver;
		this._url = url;
		this._user = user;
		this._passwd = password;
		this._size = maxConnections;
	}

	public String getDriver()
	{
		return this._driver;
	}

	public String getUrl()
	{
		return this._url;
	}

	public String getUsername()
	{
		return this._user;
	}

	public String getPassword()
	{
		return this._passwd;
	}

	public void init() throws SQLException
	{
		try
		{
			// Load the database driver
			Class.forName(this._driver);

			this._pool = new ArrayBlockingQueue<Connection>(this._size);
			for (int i = 0; i < this._size; ++i)
			{
				// Get connections to the database
				Connection conn = null;
				conn = DriverManager.getConnection(this._url,
												   this._user,
												   this._passwd);
				this._pool.add(conn);
			}
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new SQLException("Couldn't load database driver '" + this._driver + "': " + cnfe);
		}
	}

	public void destroy()
	{
		Connection conn = null;
		while ((conn = this._pool.poll()) != null)
		{
			try
			{
				if (!conn.isClosed())
				{
					conn.close();
				}
			}
			catch (Exception e)
			{
				// Ignore
			}
		}
	}

	public Connection getConnection() throws SQLException
	{
		try
		{
			Connection conn = this._pool.take();
			if (conn.isClosed())
			{
				conn = DriverManager.getConnection(this._url,
												   this._user,
												   this._passwd);
			}
			return conn;
		}
		catch (InterruptedException ie)
		{
			throw new SQLException("No available connection");
		}
	}

	public void releaseConnection(Connection conn) throws SQLException
	{  
		try
		{
			this._pool.put(conn);
		}
		catch (InterruptedException ie)
		{
			// Ignore
		}
	}
}
