<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "BrowseCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	$region = NULL;
	if (isset($_POST['region']))
	{
    	$region = $_POST['region'];
	}
	else if (isset($_GET['region']))
	{
    	$region = $_GET['region'];
	}
	$username = NULL;
	if (isset($_POST['nickname']))
	{
    	$username = $_POST['nickname'];
	}
	else if (isset($_GET['nickname']))
	{
    	$username = $_GET['nickname'];
	}
	$password = NULL;
	if (isset($_POST['password']))
	{
    	$password = $_POST['password'];
	}
	else if (isset($_GET['password']))
	{
    	$password = $_GET['password'];
	}

    getDatabaseLink($link);

    $userId = -1;
    if ((!is_null($username) && $username != "") || (!is_null($password) && $password !=""))
    { // Authenticate the user
      $userId = authenticate($username, $password, $link);
      if ($userId == -1)
      {
        printError($scriptName, $startTime, "Authentication", "You don't have an account on RUBiS!<br>You have to register first.<br>\n");
        exit();	
      }
    }

    printHTMLheader("RUBiS available categories");

    begin($link);
    $result = mysql_query("SELECT * FROM categories", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM categories' failed: " . mysql_error($link));
		die("ERROR: Query failed: " . mysql_error($link));
	}
    commit($link);
    if (mysql_num_rows($result) == 0)
      print("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
    else
      print("<h2>Currently available categories</h2><br>\n");

    while ($row = mysql_fetch_array($result))
    {
      if (!is_null($region))
        print("<a href=\"/PHP/SearchItemsByRegion.php?category=".$row["id"]."&categoryName=".urlencode($row["name"])."&region=$region\">".$row["name"]."</a><br>\n");
      else if ($userId != -1)
        print("<a href=\"/PHP/SellItemForm.php?category=".$row["id"]."&user=$userId\">".$row["name"]."</a><br>\n");
      else
        print("<a href=\"/PHP/SearchItemsByCategory.php?category=".$row["id"]."&categoryName=".urlencode($row["name"])."\">".$row["name"]."</a><br>\n");
    }
    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
