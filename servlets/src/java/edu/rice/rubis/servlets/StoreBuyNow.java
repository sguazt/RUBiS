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

/** This servlet records a BuyNow in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreBuyNow?itemId=aa&userId=bb&minBuyNow=cc&maxQty=dd&BuyNow=ee&maxBuyNow=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable BuyNow for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user BuyNow
 *          ff is the maximum BuyNow the user wants
 *          gg is the quantity asked by the user
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class StoreBuyNow extends RubisHttpServlet
{

  public int getPoolSize()
  {
    return Config.StoreBuyNowPoolSize;
  }

/**
 * Close both statement and connection.
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
	this.printError("Store Buy Now", errorMsg, sp);
  }

  /**
   * Call the <code>doPost</code> method.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    doPost(request, response);
  }

  /**
   * Store the BuyNow to the database and display resulting message.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception IOException if an error occurs
   * @exception ServletException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    Integer userId; // item id
    Integer itemId; // user id
    //     float   minBuyNow; // minimum acceptable BuyNow for this item
    //     float   BuyNow;    // user BuyNow
    //     float   maxBuyNow; // maximum BuyNow the user wants
    int maxQty; // maximum quantity available for this item
    int qty; // quantity asked by the user
    ServletPrinter sp = null;
    PreparedStatement stmt = null;
    Connection conn = null;

    sp = new ServletPrinter(response, "StoreBuyNow");

    /* Get and check all parameters */

    String value = request.getParameter("userId");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a user identifier !<br></h3>", sp);
      return;
    }
    else
      userId = new Integer(value);

    value = request.getParameter("itemId");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide an item identifier !<br></h3>", sp);
      return;
    }
    else
      itemId = new Integer(value);

    value = request.getParameter("maxQty");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a maximum quantity !<br></h3>", sp);
      return;
    }
    else
    {
      Integer foo = new Integer(value);
      maxQty = foo.intValue();
    }

    value = request.getParameter("qty");
    if ((value == null) || (value.equals("")))
    {
      printError("<h3>You must provide a quantity !<br></h3>", sp);
      return;
    }
    else
    {
      Integer foo = new Integer(value);
      qty = foo.intValue();
    }

    /* Check for invalid values */
    if (qty > maxQty)
    {
      printError(
        "<h3>You cannot request "
          + qty
          + " items because only "
          + maxQty
          + " are proposed !<br></h3>", sp);
      return;
    }
    String now = TimeManagement.currentDateToString();
    // Try to find the Item corresponding to the Item ID
    try
    {
      int quantity;
      conn = getConnection();
      conn.setAutoCommit(false);
      stmt =
        conn.prepareStatement(
          "SELECT quantity, end_date FROM items WHERE id=?");
      stmt.setInt(1, itemId.intValue());
      ResultSet irs = stmt.executeQuery();
      if (!irs.first())
      {
        conn.rollback();
        printError("This item does not exist in the database.", sp);
        closeConnection(stmt, conn);
        return;
      }
      quantity = irs.getInt("quantity");
      quantity = quantity - qty;
      stmt.close();
      if (quantity == 0)
      {
        stmt =
          conn.prepareStatement(
            "UPDATE items SET end_date=?, quantity=? WHERE id=?");
        stmt.setString(1, now);
        stmt.setInt(2, quantity);
        stmt.setInt(3, itemId.intValue());
        stmt.executeUpdate();
        stmt.close();
      }
      else
      {
        stmt = conn.prepareStatement("UPDATE items SET quantity=? WHERE id=?");
        stmt.setInt(1, quantity);
        stmt.setInt(2, itemId.intValue());
        stmt.executeUpdate();
        stmt.close();
      }
    }
    catch (SQLException e)
    {
      this.printError("Failed to execute Query for the item: " + e, sp);
      try
      {
        conn.rollback();
        closeConnection(stmt, conn);
      }
      catch (Exception se)
      {
        printError("Transaction rollback failed: " + e, sp);
        closeConnection(stmt, conn);
      }
      return;
    }
    try
    {
      stmt =
        conn.prepareStatement(
          "INSERT INTO buy_now VALUES (NULL, \""
            + userId
            + "\", \""
            + itemId
            + "\", \""
            + qty
            + "\", \""
            + now
            + "\")");
      stmt.executeUpdate();
      
      conn.commit();
      sp.printHTMLheader("RUBiS: BuyNow result");
      if (qty == 1)
        sp.printHTML(
          "<center><h2>Your have successfully bought this item.</h2></center>\n");
      else
        sp.printHTML(
          "<center><h2>Your have successfully bought these items.</h2></center>\n");
    }
    catch (Exception e)
    {
      this.printError("Error while storing the BuyNow: " + e, sp);
      try
      {
        conn.rollback();
        closeConnection(stmt, conn);
      }
      catch (Exception se)
      {
        this.printError("Transaction rollback failed: " + e, sp);
      }
      return;
    }
    closeConnection(stmt, conn);
    sp.printHTMLfooter();
  }

  /**
  * Clean up the connection pool.
  */
  public void destroy()
  {
    super.destroy();
  }

}
