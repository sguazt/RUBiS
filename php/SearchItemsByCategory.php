<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SearchItemsByCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
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
		printError($scriptName, $startTime, "Search Items By Category", "You must provide a category name!<br>");
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
		printError($scriptName, $startTime, "Search Items By Category", "You must provide a category identifier!<br>");
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

    printHTMLheader("RUBiS: Items in category $categoryName");
    print("<h2>Items in category $categoryName</h2><br><br>");
    
    getDatabaseLink($link);
    begin($link);
    $result = mysql_query("SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items WHERE category=$categoryId AND end_date>=NOW() LIMIT ".$page*$nbOfItems.",$nbOfItems", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items WHERE category=$categoryId AND end_date>=NOW() LIMIT ".$page*$nbOfItems.",$nbOfItems' failed: " . mysql_error($link));
		die("ERROR: Query failed for category '$categoryId', page '$page' and nbOfItems '$nbOfItems': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
    {
      if ($page == 0)
        print("<h2>Sorry, but there are no items available in this category !</h2>");
      else
      {
        print("<h2>Sorry, but there are no more items available in this category !</h2>");
        print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId".
              "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfItems=$nbOfItems\">Previous page</a>\n</CENTER>\n");
      }
      mysql_free_result($result);
      commit($link);
      mysql_close($link);
      printHTMLfooter($scriptName, $startTime);   
      exit();
    }
    else
    {
      print("<TABLE border=\"1\" summary=\"List of items\">".
            "<THEAD>".
            "<TR><TH>Designation<TH>Price<TH>Bids<TH>End Date<TH>Bid Now".
            "<TBODY>");
    }

    while ($row = mysql_fetch_array($result))
    {
      $maxBid = $row["max_bid"];
      if ($maxBid == 0)
	$maxBid = $row["initial_price"];

      print("<TR><TD><a href=\"/PHP/ViewItem.php?itemId=".$row["id"]."\">".$row["name"].
            "<TD>$maxBid".
            "<TD>".$row["nb_of_bids"].
            "<TD>".$row["end_date"].
            "<TD><a href=\"/PHP/PutBidAuth.php?itemId=".$row["id"]."\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90></a>");
    }
    print("</TABLE>");  

    if ($page == 0)
      print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId".
           "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfItems=$nbOfItems\">Next page</a>\n</CENTER>\n");
    else
      print("<p><CENTER>\n<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfItems=$nbOfItems\">Previous page</a>\n&nbsp&nbsp&nbsp".
            "<a href=\"/PHP/SearchItemsByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfItems=$nbOfItems\">Next page</a>\n\n</CENTER>\n");

    commit($link);
    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
