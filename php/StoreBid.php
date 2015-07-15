<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreBid.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
	$userId = NULL;
	if (isset($_POST['userId']))
	{
    	$userId = $_POST['userId'];
	}
	else if (isset($_GET['userId']))
	{
    	$userId = $_GET['userId'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a user identifier!<br></h3>");
		exit();
	}
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
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide an item identifier !<br></h3>");
		exit();
	}
	$minBid = NULL;
	if (isset($_POST['minBid']))
	{
    	$minBid = $_POST['minBid'];
	}
	else if (isset($_GET['minBid']))
	{
    	$minBid = $_GET['minBid'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide an item identifier !<br></h3>");
		exit();
	}
	$bid = NULL;
	if (isset($_POST['bid']))
	{
    	$bid = $_POST['bid'];
	}
	else if (isset($_GET['bid']))
	{
    	$bid = $_GET['bid'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a minimum bid !<br></h3>");
		exit();
	}
	$maxBid = NULL;
	if (isset($_POST['maxBid']))
	{
    	$maxBid = $_POST['maxBid'];
	}
	else if (isset($_GET['maxBid']))
	{
    	$maxBid = $_GET['maxBid'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a maximum bid !<br></h3>");
		exit();
	}
	$maxQty = NULL;
	if (isset($_POST['maxQty']))
	{
    	$maxQty = $_POST['maxQty'];
	}
	else if (isset($_GET['maxQty']))
	{
    	$maxQty = $_GET['maxQty'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a maximum quantity !<br></h3>");
		exit();
	}
	$qty = NULL;
	if (isset($_POST['qty']))
	{
    	$qty = $_POST['qty'];
	}
	else if (isset($_GET['qty']))
	{
    	$qty = $_GET['qty'];
	}
	else
	{
		printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a quantity !<br></h3>");
		exit();
	}

    /* Check for invalid values */

    if ($qty > $maxQty)
    {
      printError("<h3>You cannot request $qty items because only $maxQty are proposed !<br></h3>");
      return ;
    }      
    if ($bid < $minBid)
    {
      printError("<h3>Your bid of \$$bid is not acceptable because it is below the \$$minBid minimum bid !<br></h3>");
      return ;
    }      
    if ($maxBid < $minBid)
    {
      printError("<h3>Your maximum bid of \$$maxBid is not acceptable because it is below the \$$minBid minimum bid !<br></h3>");
      return ;
    }      
    if ($maxBid < $bid)
    {
      printError("<h3>Your maximum bid of \$$maxBid is not acceptable because it is below your current bid of \$$bid !<br></h3>");
      return ;
    }      

    getDatabaseLink($link);
    begin($link);

    // Add bid to database and update values in item
    $now = date("Y:m:d H:i:s");
//    $result = mysql_query("LOCK TABLES bids WRITE, items WRITE", $link);
//	if (!$result)
//	{
//		error_log("[".__FILE__."] Failed to acquire locks on items and bids tables: " . mysql_error($link));
//		die("ERROR: Failed to acquire locks on items and bids tables: " . mysql_error($link));
//	}
    $result = mysql_query("SELECT max_bid FROM items WHERE id=$itemId", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT max_bid FROM items WHERE id=$itemId' failed: " . mysql_error($link) . ". DEADLOCK!!");
		die("ERROR: Failed to query the number of bids for item '$itemId' in database: " . mysql_error($link) . ". DEADLOCK!!");
	}
    $row = mysql_fetch_array($result);
    if ($maxBid > $row["max_bid"])
	{
      $result2 = mysql_query("UPDATE items SET max_bid=$maxBid WHERE id=$itemId", $link);
	  if (!$result2)
	  {
		error_log("[".__FILE__."] Failed to update maximum bid in database 'UPDATE items SET max_bid=$maxBid WHERE id=$itemId': " . mysql_error($link) . ". DEADLOCK!!");
		die("ERROR: Failed to update maximum bid '$maxBid' for item '$itemId' in database: " . mysql_error($link) . ". DEADLOCK!!");
	  }
	}

    $result = mysql_query("INSERT INTO bids VALUES (NULL, $userId, $itemId, $qty, $bid, $maxBid, '$now')", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to insert new bid in database 'INSERT INTO bids VALUES (NULL, $userId, $itemId, $qty, $bid, $maxBid, '$now')': " . mysql_error($link) . ". DEADLOCK!!");
		die("ERROR: Failed to insert new bid for user '$userId' and item '$itemId' in database: " . mysql_error($link) . ". DEADLOCK!!");
	}
    $result = mysql_query("UPDATE items SET nb_of_bids=nb_of_bids+1 WHERE id=$itemId", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to update number of bids in database 'UPDATE items SET nb_of_bids=nb_of_bids+1 WHERE id=$itemId': " . mysql_error($link) . ". DEADLOCK!!");
		die("ERROR: Failed to update number of bids for item '$itemId' in database: " . mysql_error($link) . ". DEADLOCK!!");
	}
//    $result = mysql_query("UNLOCK TABLES", $link);
//	if (!$result)
//	{
//		error_log("[".__FILE__."] Failed to unlock items and bids tables: " . mysql_error($link));
//		die("ERROR: Failed to unlock items and bids tables: " . mysql_error($link));
//	}
    commit($link);

    printHTMLheader("RUBiS: Bidding result");
    print("<center><h2>Your bid has been successfully processed.</h2></center>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
