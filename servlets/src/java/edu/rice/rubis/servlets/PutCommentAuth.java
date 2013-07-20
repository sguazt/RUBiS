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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlets display the page authentifying the user 
 * to allow him to put a comment on another user.
 * It must be called this way :
 * <pre>
 * http://..../PutCommentAuth?to=xx&itemId=yy
 *     where xx is the id of the user that will receive the comment
 *           yy is the item id to which this comment is related to
 * /<pre>
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class PutCommentAuth extends BaseRubisHttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		ServletPrinter sp = null;
		sp = new ServletPrinter(response, "PubCommentAuth");

		String to = request.getParameter("to");
		String item = request.getParameter("itemId");
		if ((to == null) || (to.equals("")) || (item == null) || (item.equals("")))
		{
			this.printError("No item or user identifier received. Cannot process the request", sp);
			return;
		}

		sp.printHTMLheader("RUBiS: User authentification for comment");
		sp.printFile(Config.HTMLFilesPath + "/put_comment_auth_header.html");
		sp.printHTML("<input type=hidden name=\"to\" value=\"" + to + "\">");
		sp.printHTML("<input type=hidden name=\"itemId\" value=\"" + item + "\">");
		sp.printFile(Config.HTMLFilesPath + "/auth_footer.html");
		sp.printHTMLfooter();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		this.doGet(request, response);
	}

	private void printError(String errorMsg, ServletPrinter sp)
	{
		this.printError("Put Comment Auth", errorMsg, sp);
	}
}
