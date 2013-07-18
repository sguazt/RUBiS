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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** This servlet records a bid in the database and display
 * the result of the transaction.
 * It must be called this way :
 * <pre>
 * http://..../StoreBid?itemId=aa&userId=bb&minBid=cc&maxQty=dd&bid=ee&maxBid=ff&qty=gg 
 *   where: aa is the item id 
 *          bb is the user id
 *          cc is the minimum acceptable bid for this item
 *          dd is the maximum quantity available for this item
 *          ee is the user bid
 *          ff is the maximum bid the user wants
 *          gg is the quantity asked by the user
 * </pre>
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class StoreBid extends RubisHttpServlet
{


  public int getPoolSize()
  {
    return Config.StoreBidPoolSize;
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
	this.printError("Store Bid", errorMsg, sp);
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
   * Store the bid to the database and display resulting message.
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
    float minBid; // minimum acceptable bid for this item
    float bid; // user bid
    float maxBid; // maximum bid the user wants
    int maxQty; // maximum quantity available for this item
    int qty; // quantity asked by the user
    ServletPrinter sp = null;
    PreparedStatement stmt = null;
    Connection conn = null;

    sp = new ServletPrinter(response, "StoreBid");

    /* Get and check all parameters */

    String value = request.getParameter("userId");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide a user identifier!", sp);
      return;
    }
    else
      userId = new Integer(value);

    value = request.getParameter("itemId");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide an item identifier!", sp);
      return;
    }
    else
      itemId = new Integer(value);

    value = request.getParameter("minBid");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide a minimum bid!", sp);
      return;
    }
    else
    {
      Float foo = new Float(value);
      minBid = foo.floatValue();
    }

    value = request.getParameter("bid");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide a bid!", sp);
      return;
    }
    else
    {
      Float foo = new Float(value);
      bid = foo.floatValue();
    }

    value = request.getParameter("maxBid");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide a maximum bid!", sp);
      return;
    }
    else
    {
      Float foo = new Float(value);
      maxBid = foo.floatValue();
    }

    value = request.getParameter("maxQty");
    if ((value == null) || (value.equals("")))
    {
      this.printError("You must provide a maximum quantity!", sp);
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
      this.printError("You must provide a quantity!", sp);
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
      this.printError("You cannot request " + qty + " items because only " + maxQty + " are proposed!", sp);
      return;
    }
    if (bid < minBid)
    {
      this.printError("Your bid of $" + bid + " is not acceptable because it is below the $" + minBid + " minimum bid!", sp);
      return;
    }
    if (maxBid < minBid)
    {
      this.printError("Your maximum bid of $" + maxBid + " is not acceptable because it is below the $" + minBid + " minimum bid!", sp);
      return;
    }
    if (maxBid < bid)
    {
      this.printError("Your maximum bid of $" + maxBid + " is not acceptable because it is below your current bid of $" + bid + "!", sp);
      return;
    }
    try
    {
      conn = getConnection();
      conn.setAutoCommit(false);
      String now = TimeManagement.currentDateToString();
      stmt =
        conn.prepareStatement(
          "INSERT INTO bids VALUES (NULL, \""
            + userId
            + "\", \""
            + itemId
            + "\", \""
            + qty
            + "\", \""
            + bid
            + "\", \""
            + maxBid
            + "\", \""
            + now
            + "\")");
      stmt.executeUpdate();
      stmt.close();
      // update the number of bids and the max bid for the item
      PreparedStatement update = null;
      try
      {
        stmt =
          conn.prepareStatement(
            "SELECT nb_of_bids, max_bid FROM items WHERE id=?");
        stmt.setInt(1, itemId.intValue());
        ResultSet rs = stmt.executeQuery();
        if (rs.first())
        {
          
          int nbOfBids = rs.getInt("nb_of_bids");
          nbOfBids++;
          float oldMaxBid = rs.getFloat("max_bid");
          if (bid > oldMaxBid)
          {
            oldMaxBid = bid;
            update =
              conn.prepareStatement(
                "UPDATE items SET max_bid=?, nb_of_bids=? WHERE id=?");
            update.setFloat(1, maxBid);
            update.setInt(2, nbOfBids);
            update.setInt(3, itemId.intValue());
            update.executeUpdate();
            update.close();
          }
          else
          {
            update =
              conn.prepareStatement("UPDATE items SET nb_of_bids=? WHERE id=?");
            update.setInt(1, nbOfBids);
            update.setInt(2, itemId.intValue());
            update.executeUpdate();
            update.close();
          }

        }
        else
        {
          conn.rollback();
          this.printError("Couldn't find the item.", sp);
          closeConnection(stmt, conn);
          return;
        }
      }
      catch (Exception ex)
      {
        conn.rollback();
        this.printError("Failed to update nb of bids and max bid: " + ex, sp);
        if (update != null) 
          update.close();
        closeConnection(stmt, conn);
        return;
      }
      sp.printHTMLheader("RUBiS: Bidding result");
      sp.printHTML(
        "<center><h2>Your bid has been successfully processed.</h2></center>\n");
      conn.commit();
      closeConnection(stmt, conn);
    }
    catch (Exception e)
    {
      this.printError("Error while storing the bid: " + e, sp);
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
