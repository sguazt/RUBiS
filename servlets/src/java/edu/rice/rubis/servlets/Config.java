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

/**
 * This class contains the configuration for the servlets
 * like the path of HTML files, etc ...
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */

public class Config
{
	public static final int UNPOOLED_DRIVERMANAGER_DB_CONNECTION_STRATEGY = 0;
	public static final int POOLED_DRIVERMANAGER_DB_CONNECTION_STRATEGY = 1;
	public static final int DATASOURCE_DB_CONNECTION_STRATEGY = 2;

	private static final String J2eeContainerPath = "/opt/apache-tomcat";
	public static final String HTMLFilesPath = J2eeContainerPath + "/webapps/rubis_servlets";
	public static final String DatabaseProperties = J2eeContainerPath + "/webapps/rubis_servlets/WEB-INF/classes/META-INF/mysql.properties";

	public static final int DatabaseConnectionStrategy = DATASOURCE_DB_CONNECTION_STRATEGY;
	public static final int AboutMePoolSize = 10;
	public static final int BrowseCategoriesPoolSize = 6;
	public static final int BrowseRegionsPoolSize = 6;
	public static final int BuyNowPoolSize = 4;
	public static final int PutBidPoolSize = 8;
	public static final int PutCommentPoolSize = 2;
	public static final int RegisterItemPoolSize = 2;
	public static final int RegisterUserPoolSize = 2;
	public static final int SearchItemsByCategoryPoolSize = 15;
	public static final int SearchItemsByRegionPoolSize = 20;
	public static final int StoreBidPoolSize = 8;
	public static final int StoreBuyNowPoolSize = 4;
	public static final int StoreCommentPoolSize = 2;
	public static final int ViewBidHistoryPoolSize = 4;
	public static final int ViewItemPoolSize = 20;
	public static final int ViewUserInfoPoolSize = 4;
}
