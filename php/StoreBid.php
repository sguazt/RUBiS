<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreBid.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
    $userId = $_POST['userId'];
    if ($userId == null)
    {
      $userId = $_GET['userId'];
      if ($userId == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a user identifier!<br></h3>");
         exit();
      }
    }
      
    $itemId = $_POST['itemId'];
    if ($itemId == null)
    {
      $itemId = $_GET['itemId'];
      if ($itemId == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide an item identifier !<br></h3>");
         exit();
      }
    }
      
    $minBid = $_POST['minBid'];
    if ($minBid == null)
    {
      $minBid = $_GET['minBid'];
      if ($minBid == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide an item identifier !<br></h3>");
         exit();
      }
    }

    $bid = $_POST['bid'];
    if ($bid == null)
    {
      $bid = $_GET['bid'];
      if ($bid == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a minimum bid !<br></h3>");
         exit();
      }
    }

    $maxBid = $_POST['maxBid'];
    if ($maxBid == null)
    {
      $maxBid = $_GET['maxBid'];
      if ($maxBid == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a maximum bid !<br></h3>");
         exit();
      }
    }

    $maxQty = $_POST['maxQty'];
    if ($maxQty == null)
    {
      $maxQty = $_GET['maxQty'];
      if ($maxQty == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a maximum quantity !<br></h3>");
         exit();
      }
    }

    $qty = $_POST['qty'];
    if ($qty == null)
    {
      $qty = $_GET['qty'];
      if ($qty == null)
      {
         printError($scriptName, $startTime, "StoreBid", "<h3>You must provide a quantity !<br></h3>");
         exit();
      }
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
    mysql_query("LOCK TABLES bids WRITE, items WRITE", $link) or die("ERROR: Failed to acquire locks on items and bids tables.");
    $result = mysql_query("SELECT max_bid FROM items WHERE id=$itemId", $link) or die("ERROR: Failed to update number of bids in database. DEADLOCK!!");
    $row = mysql_fetch_array($result);
    if ($maxBid > $row["max_bid"])
      mysql_query("UPDATE items SET max_bid=$maxBid WHERE id=$itemId", $link) or die("ERROR: Failed to update maximum bid in database. DEADLOCK!!");

    mysql_query("INSERT INTO bids VALUES (NULL, $userId, $itemId, $qty, $bid, $maxBid, '$now')", $link) or die("ERROR: Failed to insert new bid in database. DEADLOCK!!");
    mysql_query("UPDATE items SET nb_of_bids=nb_of_bids+1 WHERE id=$itemId", $link) or die("ERROR: Failed to update number of bids in database. DEADLOCK!!");
    mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock items and bids tables.");
    commit($link);

    printHTMLheader("RUBiS: Bidding result");
    print("<center><h2>Your bid has been successfully processed.</h2></center>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
