<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "PutCommentAuth.php";
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
		printError($scriptName, $startTime, "Authentification for comment", "You must provide an item identifier!<br>");
		exit();
	}
	$to = NULL;
	if (isset($_POST['to']))
	{
    	$to = $_POST['to'];
	}
	else if (isset($_GET['to']))
	{
    	$to = $_GET['to'];
	}
	else
	{
		printError($scriptName, $startTime, "Authentification for comment", "You must provide a user identifier!<br>");
		exit();
	}

    printHTMLheader("RUBiS: User authentification for comment");
    include("put_comment_auth_header.html");
    print("<input type=hidden name=\"to\" value=\"$to\">");
    print("<input type=hidden name=\"itemId\" value=\"$itemId\">");
    include("auth_footer.html");

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
