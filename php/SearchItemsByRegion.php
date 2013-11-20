<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SearchItemsByCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
	$regionId = NULL;
	if (isset($_POST['region']))
	{
    	$regionId = $_POST['region'];
	}
	else if (isset($_GET['region']))
	{
    	$regionId = $_GET['region'];
	}
	else
	{
		printError($scriptName, $startTime, "Search Items By Region", "You must provide a region!<br>");
		exit();
	}
	$categoryId = NULL;
	if (isset($_POST['category']))
	{
    	$categoryId = $_POST['category'];
	}
	else if (isset($_GET['category']))
	{
    	$categoryId = $_GET['category'];
	}
	else
	{
		printError($scriptName, $startTime, "Search Items By Region", "You must provide a category identifier!<br>");
		exit();
	}
	$categoryName = NULL;
	if (isset($_POST['categoryName']))
	{
    	$categoryName = $_POST['categoryName'];
	}
	else if (isset($_GET['categoryName']))
	{
    	$categoryName = $_GET['categoryName'];
	}
	else
	{
		printError($scriptName, $startTime, "Search Items By Region", "You must provide a category name!<br>");
		exit();
	}
	$page = NULL;
	if (isset($_POST['page']))
	{
    	$page = $_POST['page'];
	}
	else if (isset($_GET['page']))
	{
    	$page = $_GET['page'];
	}
	else
	{
		$page = 0;
	}
	$nbOfItems = NULL;
	if (isset($_POST['nbOfItems']))
	{
    	$nbOfItems = $_POST['nbOfItems'];
	}
	else if (isset($_GET['nbOfItems']))
	{
    	$nbOfItems = $_GET['nbOfItems'];
	}
	else
	{
		$nbOfItems = 25;
	}

    printHTMLheader("RUBiS: Search items by region");
    print("<h2>Items in category $categoryName</h2><br><br>");
    
    getDatabaseLink($link);
    $result = mysql_query("SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items,users WHERE items.category=$categoryId AND items.seller=users.id AND users.region=$regionId AND end_date>=NOW() LIMIT ".$page*$nbOfItems.",$nbOfItems");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items,users WHERE items.category=$categoryId AND items.seller=users.id AND users.region=$regionId AND end_date>=NOW() LIMIT ".$page*$nbOfItems."' failed: " . mysql_error($link));
		die("ERROR: Query failed for category '$categoryId', region '$regionId', page '$page' and nbOfItems '$nbOfItems': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
    {
      if ($page == 0)
        print("<h3>Sorry, but there is no item in this category for this region.</h3><br>\n");
      else
      {
        print("<h2>Sorry, but there are no more items available in this category for this region!</h2>");
        print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByRegion.php?category=$categoryId&region=$regionId".
              "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfItems=$nbOfItems\">Previous page</a>\n</CENTER>\n");
      }
      mysql_free_result($result);
      mysql_close($link);
      printHTMLfooter($scriptName, $startTime);
      exit();
    }
    else
      print("<TABLE border=\"1\" summary=\"List of items\">".
            "<THEAD>".
            "<TR><TH>Designation<TH>Price<TH>Bids<TH>End Date<TH>Bid Now".
            "<TBODY>");

    while ($row = mysql_fetch_array($result))
    {
      $maxBid = $row["max_bid"];
      if ((is_null($maxBid)) ||($maxBid == 0))
	$maxBid = $row["initial_price"];

      print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=".$row["id"]."\">".$row["name"].
            "<TD>$maxBid".
            "<TD>".$row["nb_of_bids"].
            "<TD>".$row["end_date"].
            "<TD><a href=\"/PHP/PutBidAuth.php?itemId=".$row["id"]."\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90></a>");
    }
    print("</TABLE>");  
    if ($page == 0)
      print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByRegion.php?category=$categoryId&region=$regionId".
           "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfItems=$nbOfItems\">Next page</a>\n</CENTER>\n");
    else
      print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByRegion.php?category=$categoryId&region=$regionId".
            "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfItems=$nbOfItems\">Previous page</a>\n&nbsp&nbsp&nbsp".
            "<a href=\"/PHP/SearchItemsByRegion.php?category=$categoryId&region=$regionId".
            "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfItems=$nbOfItems\">Next page</a>\n\n</CENTER>\n");

    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
