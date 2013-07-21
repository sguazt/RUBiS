/*
 * Copyright (C) 2013  Marco Guazzone (marco.guazzone@gmail.com)
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


/**
 * Interface to setup and manage database connections.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public interface DatabaseConnectionManager
{
//	/// Return the JDBC driver class name
//	String getDriver();

//	/// Return the JDBC database URL
//	String getUrl();

//	/// Return the username to connect to the database
//	String getUsername();

//	/// Return the password used to authenticate the username to the database
//	String getPassword();

	/// Initialize the connection(s) to the database
	void init() throws SQLException;

	/// Destroy the connection(s) to the database
	void destroy();

	/// Obtain a connection to the database
	Connection getConnection() throws SQLException;

	/// Release the given connection to the database
	void releaseConnection(Connection conn) throws SQLException;
}
