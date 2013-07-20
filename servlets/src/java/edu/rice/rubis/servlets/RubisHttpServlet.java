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
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletException;
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
	private DatabaseConnectionManager _dbMngr = null;


	/** Load the driver and get a connection to the database */
	@Override
	public void init() throws ServletException
	{
		super.init();

		InputStream in = null;
		try
		{
			Properties dbProperties = null;

			// Create the DB manager
			switch (Config.DatabaseConnectionStrategy)
			{
				case Config.UNPOOLED_DRIVERMANAGER_DB_CONNECTION_STRATEGY:
					// Get the properties for the database connection
					dbProperties = new Properties();
					in = new FileInputStream(Config.DatabaseProperties);
					dbProperties.load(in);
					// Setup the connection manager according to the read properties
					this._dbMngr = new UnpooledDriverManagerDatabaseConnectionManager(dbProperties.getProperty("datasource.classname"),
																					  dbProperties.getProperty("datasource.url"),
																					  dbProperties.getProperty("datasource.username"),
																					  dbProperties.getProperty("datasource.password"));
					break;
				case Config.POOLED_DRIVERMANAGER_DB_CONNECTION_STRATEGY:
					// Get the properties for the database connection
					dbProperties = new Properties();
					in = new FileInputStream(Config.DatabaseProperties);
					dbProperties.load(in);
					// Setup the connection manager according to the read properties
					this._dbMngr = new PooledDriverManagerDatabaseConnectionManager(dbProperties.getProperty("datasource.classname"),
																					dbProperties.getProperty("datasource.url"),
																					dbProperties.getProperty("datasource.username"),
																					dbProperties.getProperty("datasource.password"),
																					this.getPoolSize());
					break;
				case Config.DATASOURCE_DB_CONNECTION_STRATEGY:
					this._dbMngr = new DataSourceDatabaseConnectionManager();
					break;
			}
			this._dbMngr.init();
		}
		catch (FileNotFoundException fnfe)
		{
			throw new ServletException("Couldn't find file mysql.properties: " + fnfe);
		}
		catch (IOException ioe)
		{
			throw new ServletException("Cannot open read mysql.properties: " + ioe);
		}
		catch (SQLException se)
		{
			throw new ServletException("Couldn't get database connection: " + se);
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
	@Override
	public void destroy()
	{
		this._dbMngr.destroy();

		super.destroy();
	}

	/** Get the pool size for this class. */
	protected int getPoolSize()
	{
		return 4; // Default pool size
	}

	protected Connection getConnection() throws ServletException
	{
		try
		{
			return this._dbMngr.getConnection();
		}
		catch (SQLException se)
		{
			throw new ServletException(se);
		}
	} 

	protected void releaseConnection(Connection conn)
	{
		this._dbMngr.releaseConnection(conn);
	}
}
