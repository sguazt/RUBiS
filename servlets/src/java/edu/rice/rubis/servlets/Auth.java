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

public class Auth
{

  //private Context servletContext;
  private Connection conn = null;
  private ServletPrinter sp;

  public Auth(Connection connect, ServletPrinter printer)
  {
    conn = connect;
    sp = printer;
  }

  public int authenticate(String name, String password)
  {
    int userId = -1;
    ResultSet rs = null;
    PreparedStatement stmt = null;

    // Lookup the user
    try
    {
      stmt =
        conn.prepareStatement(
          "SELECT users.id FROM users WHERE nickname=? AND password=?");
      stmt.setString(1, name);
      stmt.setString(2, password);
      rs = stmt.executeQuery();
      if (!rs.first())
      {
        sp.printHTML(
          " User " + name + " does not exist in the database!<br><br>");
        return userId;
      }
      userId = rs.getInt("id");
    }
    catch (SQLException e)
    {
      return userId;
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close(); // close statement
      }
      catch (Exception ignore)
      {
      }
      return userId;
    }
  }
}
