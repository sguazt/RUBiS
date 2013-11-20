<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "ViewItem.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
	$itemId = NULL;
	if (isset($_POST['itemId']))
	{
    	$itemId = $_POST['itemId'];
	}
	else if (isset($_GET['itemId']))
	{
    	$itemId = $_GET['itemId'];
	}
	else
	{
		printError($scriptName, $startTime, "Viewing item", "You must provide an item identifier!<br>");
		exit();
	}
      
    getDatabaseLink($link);
    begin($link);
    $result = mysql_query("SELECT * FROM items WHERE items.id=$itemId");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM items WHERE items.id=$itemId' failed: " . mysql_error($link));
		die("ERROR: Query failed for item '$itemId': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
	{
      $result = mysql_query("SELECT * FROM old_items WHERE old_items.id=$itemId");
	  if (!$result)
	  {
		error_log("[".__FILE__."] Query 'SELECT * FROM old_items WHERE old_items.id=$itemId' failed: " . mysql_error($link));
		die("ERROR: Query failed: " . mysql_error($link));
	  }
	}
    if (mysql_num_rows($result) == 0)
    {
      commit($link);
      die("<h3>ERROR: Sorry, but this item '$itemId' does not exist.</h3><br>\n");
    }

    $row = mysql_fetch_array($result);
    $maxBidResult = mysql_query("SELECT MAX(bid) AS bid FROM bids WHERE item_id=".$row["id"], $link);
	if (!$maxBidResult)
	{
		error_log("[".__FILE__."] Query 'SELECT MAX(bid) AS bid FROM bids WHERE item_id=".$row["id"]."' failed: " . mysql_error($link));
		die("ERROR: Max bid query failed for item '".$row["id"]."': " . mysql_error($link));
	}
    $maxBidRow = mysql_fetch_array($maxBidResult);
    $maxBid = $maxBidRow["bid"];
    $buyNow = 0;
    if ($maxBid == 0)
    {
      $maxBid = $row["initial_price"];
      $buyNow = $row["buy_now"];
      $firstBid = "none";
      $nbOfBids = 0;
    }
    else
    {
      if ($row["quantity"] > 1)
      {
        $xRes = mysql_query("SELECT bid,qty FROM bids WHERE item_id=".$row["id"]." ORDER BY bid DESC LIMIT ".$row["quantity"], $link);
		if (!$xRes)
		{
			error_log("[".__FILE__."] Query 'SELECT bid,qty FROM bids WHERE item_id=".$row["id"]." ORDER BY bid DESC LIMIT ".$row["quantity"]."' failed: " . mysql_error($link));
			die("ERROR: Quantity query failed for item '".$row["id"]."': " . mysql_error($link));
		}
        $nb = 0;
        while ($xRow = mysql_fetch_array($xRes))
        {
          $nb = $nb + $xRow["qty"];
          if ($nb > $row["quantity"])
          {
            $maxBid = $xRow["bid"];
            break;
          }
        }
      }
      $firstBid = $maxBid;
      $nbOfBidsResult = mysql_query("SELECT COUNT(*) AS bid FROM bids WHERE item_id=".$row["id"], $link);
	  if (!$nbOfBidsResult)
	  {
		error_log("[".__FILE__."] Query 'SELECT COUNT(*) AS bid FROM bids WHERE item_id=".$row["id"]."' failed: " . mysql_error($link));
		die("ERROR: Nb of bids query failed: " . mysql_error($link));
	  }
      $nbOfBidsRow = mysql_fetch_array($nbOfBidsResult);
      $nbOfBids = $nbOfBidsRow["bid"];
      mysql_free_result($nbOfBidsResult);
    }

    printHTMLheader("RUBiS: Viewing ".$row["name"]);
    printHTMLHighlighted($row["name"]);
    print("<TABLE>\n".
          "<TR><TD>Currently<TD><b><BIG>$maxBid</BIG></b>\n");    

    // Check if the reservePrice has been met (if any)
    $reservePrice = $row["reserve_price"];
    if ($reservePrice > 0)
    {
	if ($maxBid >= $reservePrice)
	{
	  print("(The reserve price has been met)\n");
	}
	else
	{
          print("(The reserve price has NOT been met)\n");
	}
    }

    $sellerNameResult = mysql_query("SELECT users.nickname FROM users WHERE id=".$row["seller"], $link);
	if (!$sellerNameResult)
	{
		error_log("[".__FILE__."] Query 'SELECT users.nickname FROM users WHERE id=".$row["seller"]."' failed: " . mysql_error($link));
		die("ERROR: Seller name query failed for user '".$row["seller"]."': " . mysql_error($link));
	}
    $sellerNameRow = mysql_fetch_array($sellerNameResult);
    $sellerName = $sellerNameRow["nickname"];
    mysql_free_result($sellerNameResult);

    print("<TR><TD>Quantity<TD><b><BIG>".$row["quantity"]."</BIG></b>\n");
    print("<TR><TD>First bid<TD><b><BIG>$firstBid</BIG></b>\n");
    print("<TR><TD># of bids<TD><b><BIG>$nbOfBids</BIG></b> (<a href=\"/PHP/ViewBidHistory.php?itemId=".$row["id"]."\">bid history</a>)\n");
    print("<TR><TD>Seller<TD><a href=\"/PHP/ViewUserInfo.php?userId=".$row["seller"]."\">$sellerName</a> (<a href=\"/PHP/PutCommentAuth.php?to=".$row["seller"]."&itemId=".$row["id"]."\">Leave a comment on this user</a>)\n");
    print("<TR><TD>Started<TD>".$row["start_date"]."\n");
    print("<TR><TD>Ends<TD>".$row["end_date"]."\n");
    print("</TABLE>\n");

    // Can the user by this item now ?
    if ($buyNow > 0)
	print("<p><a href=\"/PHP/BuyNowAuth.php?itemId=".$row["id"]."\">".
              "<IMG SRC=\"/PHP/buy_it_now.jpg\" height=22 width=150></a>".
              "  <BIG><b>You can buy this item right now for only \$$buyNow</b></BIG><br><p>\n");

    print("<a href=\"/PHP/PutBidAuth.php?itemId=".$row["id"]."\"><IMG SRC=\"/PHP/bid_now.jpg\" height=22 width=90> on this item</a>\n");

    printHTMLHighlighted("Item description");
    print($row["description"]);
    print("<br><p>\n");

    commit($link);
    mysql_free_result($maxBidResult);
    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
