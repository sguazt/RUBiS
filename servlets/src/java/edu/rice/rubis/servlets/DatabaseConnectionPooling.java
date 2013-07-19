/*
 * Copyright (C) 2002-2004  French National Institute For Research In Computer
 *                          Science And Control (INRIA).
 *                          [Contact: jmob@objectweb.org]
 * Copyright (C) 2005-2009  OW2 Consortium
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;


/**
 * Setup and manages a pool of database connections.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class DatabaseConnectionPooling
{
	private String _driver;
	private String _url;
	private String _user;
	private String _passwd;
	private int _size;
	/** Stack of available connections (pool) */
	private Deque<Connection> _freeConnections = null;


	public static Connection makeNewConnection(String driver,
											   String url,
											   String user,
											   String password) throws SQLException
	{
		// Load the database driver (if not already loaded)
		try
		{
			Class.forName(driver);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new SQLException("Cannot load driver '" + driver + "': " + cnfe);
		}

		// Create the connection
		return DriverManager.getConnection(url,
										   user,
										   password);
	}

	/**
	 * Closes a <code>Connection</code>.
	 * 
	 * @param connection to close
	 */
	public static void closeConnection(Connection conn)
	{
		try
		{
			if (!conn.isClosed())
			{
				conn.close();
			}
		}
		catch (SQLException e)
		{
			// ignore
		}
	}


	public DatabaseConnectionPooling(String driver,
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

	/**
	 * Initialize the pool of connections to the database.
	 *
	 * @exception SQLException if an error occurs
	 */
	public synchronized void initializeConnections() throws SQLException
	{
		try
		{
			// Load the database driver
			Class.forName(this._driver);

			this._freeConnections = new ArrayDeque<Connection>();
			for (int i = 0; i < this._size; ++i)
			{
				// Get connections to the database
				Connection conn = null;
				conn = DriverManager.getConnection(this._url,
												   this._user,
												   this._passwd);
				this._freeConnections.addFirst(conn);
			}
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new SQLException("Couldn't load database driver '" + this._driver + "': " + cnfe);
		}
	}

	/**
	 * Release all the connections to the database.
	 * 
	 * @exception SQLException if an error occurs
	 */
	public synchronized void finalizeConnections() throws SQLException
	{
		while (!this._freeConnections.isEmpty())
		{
			Connection conn = this._freeConnections.removeFirst();
			if (!conn.isClosed())
			{
				conn.close();
			}
		}
	}

	/**
	 * Gets a connection from the pool (round-robin)
	 * 
	 * @return a <code>Connection</code> or null if no connection is available
	 */
	public synchronized Connection getConnection() throws SQLException
	{
		// Wait for a connection to be available
		while (this._freeConnections.isEmpty())
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				// Someone freed up a connection
			}
		}

		Connection conn = null;

		try
		{
			conn = this._freeConnections.removeFirst();
		}
		catch (NoSuchElementException e)
		{
			throw new SQLException("Out of connections");
		}

		return conn;
	}

	/**
	 * Releases a connection to the pool.
	 * 
	 * @param c the connection to release
	 */
	public synchronized void releaseConnection(Connection conn)
	{  
		boolean mustNotify = this._freeConnections.isEmpty();

		this._freeConnections.addFirst(conn);

		// Wake up one servlet waiting for a connection (if any)
		if (mustNotify)
		{
			this.notifyAll();
		}
	}
}
