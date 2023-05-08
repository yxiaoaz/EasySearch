<%@ page import="java.util.*" %>
<%@ page import="java.lang.*" %>

<html>
<body>
<%
if(request.getParameter("userInput")!="")
{
    out.println("The words you entered are:");
    out.println("<hr>");
    String[] tokens = request.getParameter("userInput").split(" ");
	for(int i=0;i<tokens.length;i++){
	    out.println(tokens[i]);
	    out.println("<br>");
	}
}
else
{
	out.println("You input nothing");
}

%>
</body>
</html>