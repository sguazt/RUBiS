<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoreBuyNow.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
    $userId = $_POST['userId'];
    if ($userId == null)
    {
      $userId = $_GET['userId'];
      if ($userId == null)
      {
         printError($scriptName, $startTime, "StoreBuyNow", "<h3>You must provide a user identifier!<br></h3>");
         exit();
      }
    }
      
    $itemId = $_POST['itemId'];
    if ($itemId == null)
    {
      $itemId = $_GET['itemId'];
      if ($itemId == null)
      {
         printError($scriptName, $startTime, "StoreBuyNow", "<h3>You must provide an item identifier !<br></h3>");
         exit();
      }
    }
      
    $maxQty = $_POST['maxQty'];
    if ($maxQty == null)
    {
      $maxQty = $_GET['maxQty'];
      if ($maxQty == null)
      {
         printError($scriptName, $startTime, "StoreBuyNow", "<h3>You must provide a maximum quantity !<br></h3>");
         exit();
      }
    }

    $qty = $_POST['qty'];
    if ($qty == null)
    {
      $qty = $_GET['qty'];
      if ($qty == null)
      {
         printError($scriptName, $startTime, "StoreBuyNow", "<h3>You must provide a quantity !<br></h3>");
         exit();
      }
    }

    /* Check for invalid values */

    if ($qty > $maxQty)
    {
      printError("<h3>You cannot request $qty items because only $maxQty are proposed !<br></h3>");
      return ;
    }      

    getDatabaseLink($link);
    begin($link);

    mysql_query("LOCK TABLES buy_now WRITE, items WRITE", $link) or die("ERROR: Failed to acquire locks on items and buy_now tables.");
    $result = mysql_query("SELECT * FROM items WHERE items.id=$itemId") or die("ERROR: Query failed");
    if (mysql_num_rows($result) == 0)
    {
      printError($scriptName, $startTime, "BuyNow", "<h3>Sorry, but this item does not exist.</h3><br>");
      commit($link);
      exit();
    }
    $row = mysql_fetch_array($result);
    $newQty = $row["quantity"]-$qty;
    if ($newQty == 0)
      mysql_query("UPDATE items SET end_date=NOW(),quantity=$newQty WHERE id=$itemId") or die("ERROR: Failed to update item");
    else
      mysql_query("UPDATE items SET quantity=$newQty WHERE id=$itemId") or die("ERROR: Failed to update item");
    // Add BuyNow to database
    $now = date("Y:m:d H:i:s");
    mysql_query("INSERT INTO buy_now VALUES (NULL, $userId, $itemId, $qty, '$now')", $link) or die("ERROR: Failed to insert new BuyNow in database.");
    mysql_query("UNLOCK TABLES", $link) or die("ERROR: Failed to unlock items and buy_now tables.");

    printHTMLheader("RUBiS: BuyNow result");
    if ($qty == 1)
      print("<center><h2>Your have successfully bought this item.</h2></center>\n");
    else
      print("<center><h2>Your have successfully bought these items.</h2></center>\n");
    
    commit($link);
    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
