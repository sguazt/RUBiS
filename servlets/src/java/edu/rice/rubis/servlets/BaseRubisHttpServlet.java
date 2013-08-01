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


import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


/**
 * Provides methods common to all RUBiS servlets.
 *
 * All the servlets inherit from this class.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public abstract class BaseRubisHttpServlet extends HttpServlet
{
	private Logger _logger = null;


	/**
	 * Load the driver and get a connection to the database
	 */
	@Override
	public void init() throws ServletException
	{
		this._logger = Logger.getLogger(RubisHttpServlet.class.getName());
		if (this._logger == null)
		{
			throw new ServletException("Cannot create an instance of Logger");
		}
	}

	/**
	 * Display an error message and log it to the server log.
	 * @param errorMsg the error message value
	 */
	protected void printError(String headerMsg, String errorMsg, ServletPrinter sp)
	{
		sp.printHTMLheader("<h1>RUBIS ERROR: " + headerMsg + "</h1>");
		sp.printHTML("<h2>Your request has not been processed due to the following error :</h2><br>");
		sp.printHTML(errorMsg + "<br>");
		sp.printHTMLfooter();

		this.getLogger().severe("[" + headerMsg + "] " + errorMsg);
	}

	protected Logger getLogger()
	{
		return this._logger;
	}
}
