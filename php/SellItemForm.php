<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "SellItemForm.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();
    
	$category = NULL;
	if (isset($_POST['category']))
	{
    	$category = $_POST['category'];
	}
	else if (isset($_GET['category']))
	{
    	$category = $_GET['category'];
	}
	else
	{
		printError($scriptName, $startTime, "SellItemForm", "You must provide a category identifier!<br>");
		exit();
	}
	$user = NULL;
	if (isset($_POST['user']))
	{
    	$user = $_POST['user'];
	}
	else if (isset($_GET['user']))
	{
    	$user = $_GET['user'];
	}
	else
	{
		printError($scriptName, $startTime, "SellItemForm", "You must provide a user identifier!<br>");
		exit();
	}

    printHTMLheader("RUBiS: Sell your item");
    include("sellItemForm.html");
    print("<input type=hidden name=\"userId\" value=\"$user\">");
    print("<input type=hidden name=\"categoryId\" value=\"$category\">");

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
