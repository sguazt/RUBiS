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
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/** This class is a replacement for RubisHttpServlet that does not provide connection
 * pooling but relies on a datasource (such as the one provided by Tomcat) that
 * provides pooling.
 */
public abstract class RubisHttpServlet2 extends HttpServlet
{
  private DataSource ds = null;

  /** Initialize the datasource */
  public void init() throws ServletException
  {
    try
    {
      Context ctx = new InitialContext();
      if (ctx == null)
        throw new Exception("Boom - No Context");

      ds = (DataSource) ctx.lookup("java:comp/env/jdbc/RUBiS");
    }
    catch (NamingException e)
    {
      e.printStackTrace();
      throw new ServletException(e);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new ServletException(e);
    }
  }

  /**
  * Closes a <code>Connection</code>.
  * @param connection to close 
  */
  public void closeConnection(Connection connection)
  {
    try
    {
      connection.close();
    }
    catch (Exception e)
    {
    }
  }

  /**
   * Gets a connection from the pool (round-robin)
   *
   * @return a <code>Connection</code> or 
   * null if no connection is available
   */
  public synchronized Connection getConnection()
  {
    if (ds != null)
      try
      {
        return ds.getConnection();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        return null;
      }
    else
    {
      System.out.println("ERROR: No datasource available");
      return null;
    }
  }

  /**
  * Releases a connection to the pool.
  *
  * @param c the connection to release
  */
  public synchronized void releaseConnection(Connection c)
  {
    try
    {
      c.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {

  }

}
