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

/** This servlets records a comment in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreComment?itemId=aa&userId=bb&minComment=cc&maxQty=dd&comment=ee&maxComment=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable comment for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user comment
 *          ff is the maximum comment the user wants
 *          gg is the quantity asked by the user
 * /<pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class StoreComment extends RubisHttpServlet
{


  public int getPoolSize()
  {
    return Config.StoreCommentPoolSize;
  }

/**
 * Close both statement and connection to the database.
 */
  private void closeConnection(PreparedStatement stmt, Connection conn)
  {
    try
    {
      if (stmt != null)
        stmt.close(); // close statement
      if (conn != null)
        releaseConnection(conn);
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
	this.printError("Store Comment", errorMsg, sp);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    Integer toId; // to user id
    Integer fromId; // from user id
    Integer itemId; // item id
    String comment; // user comment
    Integer rating; // user rating
    ServletPrinter sp = null;
    PreparedStatement stmt = null;
    Connection conn = null;

    sp = new ServletPrinter(response, "StoreComment");

    /* Get and check all parameters */

    String value = request.getParameter("to");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a 'to user' identifier !<br></h3>", sp);
      return;
    }
    else
      toId = new Integer(value);

    value = request.getParameter("from");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a 'from user' identifier !<br></h3>", sp);
      return;
    }
    else
      fromId = new Integer(value);

    value = request.getParameter("itemId");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide an item identifier !<br></h3>", sp);
      return;
    }
    else
      itemId = new Integer(value);

    value = request.getParameter("rating");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a rating !<br></h3>", sp);
      return;
    }
    else
      rating = new Integer(value);

    comment = request.getParameter("comment");
    if ((comment == null) || (comment.equals("")))
    {
      printError("<h3>You must provide a comment !<br></h3>", sp);
      return;
    }

    try
    {
      conn = getConnection();
      conn.setAutoCommit(false); // faster if made inside a Tx
      // Try to create a new comment
      try
      {
        String now = TimeManagement.currentDateToString();
        stmt =
          conn.prepareStatement(
            "INSERT INTO comments VALUES (NULL, \""
              + fromId
              + "\", \""
              + toId
              + "\", \""
              + itemId
              + "\", \""
              + rating
              + "\", \""
              + now
              + "\",\""
              + comment
              + "\")");

        stmt.executeUpdate();
        stmt.close();
      }
      catch (SQLException e)
      {
        conn.rollback();
        this.printError("Error while storing the comment: " + e, sp);
         closeConnection(stmt, conn);
        return;
      }
      // Try to find the user corresponding to the 'to' ID
      try
      {
        ResultSet urs;
        stmt = conn.prepareStatement("SELECT rating FROM users WHERE id=?");
        stmt.setInt(1, toId.intValue());
        urs = stmt.executeQuery();
        if (urs.first())
        {
          int userRating = urs.getInt("rating");
          userRating = userRating + rating.intValue();

          stmt = conn.prepareStatement("UPDATE users SET rating=? WHERE id=?");
          stmt.setInt(1, userRating);
          stmt.setInt(2, toId.intValue());
          stmt.executeUpdate();
        }
      }
      catch (SQLException e)
      {
        conn.rollback();
        this.printError("Error while updating user's rating: " + e, sp);
        closeConnection(stmt, conn);
        return;
      }
      sp.printHTMLheader("RUBiS: Comment posting");
      sp.printHTML(
        "<center><h2>Your comment has been successfully posted.</h2></center>");

      sp.printHTMLfooter();
      conn.commit();
      closeConnection(stmt, conn);
    }
    catch (Exception e)
    {
      this.printError("Exception getting comment list: " + e, sp);
      try
      {
        conn.rollback();
        closeConnection(stmt, conn);
      }
      catch (Exception se)
      {
        this.printError("Transaction rollback failed: " + e, sp);
        closeConnection(stmt, conn);
      }
    }
  }

  /**
   * Clean up the connection pool.
   */
  public void destroy()
  {
    super.destroy();
  }
}
