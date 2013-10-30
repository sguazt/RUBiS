<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SearchItemsByCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
    $regionId = $_POST['region'];
    if ($regionId == null)
    {
      $regionId = $_GET['region'];
      if ($regionId == null)
      {
         printError($scriptName, $startTime, "Search Items By Region", "You must provide a region!<br>");
         exit();
      }
    }
      
    $categoryId = $_POST['category'];
    if ($categoryId == null)
    {
      $categoryId = $_GET['category'];
      if ($categoryId == null)
      {
         printError($scriptName, $startTime, "Search Items By Region", "You must provide a category identifier!<br>");
         exit();
      }
    }
      
    $page = $_POST['page'];
    if ($page == null)
    {
      $page = $_GET['page'];
      if ($page == null)
        $page = 0;
    }
      
    $nbOfItems = $_POST['nbOfItems'];
    if ($nbOfItems == null)
    {
      $nbOfItems = $_GET['nbOfItems'];
      if ($nbOfItems == null)
        $nbOfItems = 25;
    }

    printHTMLheader("RUBiS: Search items by region");
    print("<h2>Items in category $categoryName</h2><br><br>");
    
    getDatabaseLink($link);
    $result = mysql_query("SELECT items.id,items.name,items.initial_price,items.max_bid,items.nb_of_bids,items.end_date FROM items,users WHERE items.category=$categoryId AND items.seller=users.id AND users.region=$regionId AND end_date>=NOW() LIMIT ".$page*$nbOfItems.",$nbOfItems") or die("ERROR: Query failed");
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
      if (($maxBid == null) ||($maxBid == 0))
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
