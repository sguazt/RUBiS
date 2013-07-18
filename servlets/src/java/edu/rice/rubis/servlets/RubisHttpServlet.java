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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Deque;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;


/**
 * Provides the method to initialize connection to the database.
 *
 * All the servlets that uses DB connection should inherit from this class.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author Jeremy Philippe
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public abstract class RubisHttpServlet extends BaseRubisHttpServlet
{
	/** Controls connection pooling */
	private static final boolean _enablePooling = false;
	/** Stack of available connections (pool) */
	private Deque<Connection> _freeConnections = null;
	private int _poolSize;
	private Properties _dbProperties = null;


	/** Get the pool size for this class. */
	public abstract int getPoolSize();

	/** Load the driver and get a connection to the database */
	@Override
	public void init() throws ServletException
	{
		super.init();

		InputStream in = null;
		this._poolSize = getPoolSize();
		try
		{
			// Get the properties for the database connection
			this._dbProperties = new Properties();
			in = new FileInputStream(Config.DatabaseProperties);
			this._dbProperties.load(in);

			// load the driver
			Class.forName(this._dbProperties.getProperty("datasource.classname"));
			this._freeConnections = new ArrayDeque<Connection>();
			this.initializeConnections();
		}
		catch (FileNotFoundException f)
		{
			throw new UnavailableException("Couldn't find file mysql.properties: " + f + "<br>");
		}
		catch (IOException io)
		{
			throw new UnavailableException("Cannot open read mysql.properties: " + io + "<br>");
		}
		catch (ClassNotFoundException c)
		{
			throw new UnavailableException("Couldn't load database driver: " + c + "<br>");
		}
		catch (SQLException s)
		{
			throw new UnavailableException("Couldn't get database connection: " + s + "<br>");
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
				}
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}

	/**
	 * Clean up database connections.
	 */
	public void destroy()
	{
		try
		{
			this.finalizeConnections();
		}
		catch (SQLException e)
		{
			// ignore
		}
	}

	/**
	 * Initialize the pool of connections to the database. The caller must ensure
	 * that the driver has already been loaded else an exception will be thrown.
	 * 
	 * @exception SQLException if an error occurs
	 */
	protected synchronized void initializeConnections() throws SQLException
	{
		if (this._enablePooling)
		{
			for (int i = 0; i < this._poolSize; i++)
			{
				// Get connections to the database
				this._freeConnections.addFirst(
					DriverManager.getConnection(this._dbProperties.getProperty("datasource.url"),
												this._dbProperties.getProperty("datasource.username"),
												this._dbProperties.getProperty("datasource.password")));
			}
		}
	}

	/**
	 * Closes a <code>Connection</code>.
	 * 
	 * @param connection to close
	 */
	protected void closeConnection(Connection connection)
	{
		try
		{
			connection.close();
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	/**
	 * Gets a connection from the pool (round-robin)
	 * 
	 * @return a <code>Connection</code> or null if no connection is available
	 */
	protected synchronized Connection getConnection()
	{
		if (this._enablePooling)
		{
			try
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
						this.getLogger().info("Connection pool wait interrupted: " + e);
					}
				}

				return this._freeConnections.removeFirst();
			}
			catch (NoSuchElementException e)
			{
				this.getLogger().severe("Out of connections: " + e);
				return null;
			}
		}
		else
		{
			try
			{
				return DriverManager.getConnection(this._dbProperties.getProperty("datasource.url"),
												   this._dbProperties.getProperty("datasource.username"),
												   this._dbProperties.getProperty("datasource.password"));
			}
			catch (SQLException ex) 
			{
				this.getLogger().severe("Database connection failed: " + ex);
				return null; 
			}
		}
	}

	/**
	 * Releases a connection to the pool.
	 * 
	 * @param c the connection to release
	 */
	protected synchronized void releaseConnection(Connection c)
	{  
		if (this._enablePooling)
		{
			boolean mustNotify = this._freeConnections.isEmpty();
			this._freeConnections.addFirst(c);
			// Wake up one servlet waiting for a connection (if any)
			if (mustNotify)
			{
				this.notifyAll();
			}
		}
		else
		{
			this.closeConnection(c);
		}
	}

	/**
	 * Release all the connections to the database.
	 * 
	 * @exception SQLException if an error occurs
	 */
	protected synchronized void finalizeConnections() throws SQLException
	{
		if (this._enablePooling)
		{
			while (!this._freeConnections.isEmpty())
			{
				Connection c = this._freeConnections.removeFirst();
				c.close();
			}
		}
	}
}
