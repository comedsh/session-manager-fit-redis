<!DOCTYPE html>
<%@ page session="false" %>

<%@ page contentType="text/html; charset=utf-8" language="java" %>

<html lang="en">

    <head>
        <title>测试 session</title>
        <link href="favicon.ico" rel="icon" type="image/x-icon" />
        <link href="favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link href="tomcat.css" rel="stylesheet" type="text/css" />
    </head>

    <body>
    
    从 Session 中获得的值如下：<br>
    
<% 
    javax.servlet.http.HttpSession session = request.getSession();
    
    out.println( "key: hello; value: " + session.getAttribute("hello") );
    
    out.println( "<br>" );
    
    out.println( "key: list; values as below:");
    
    if( session.getAttribute("olist") != null ) {
    
	    for( String s : (java.util.List<String>) session.getAttribute("list") ){
	    	
	    	out.println("<br>");
	    	
	    	out.println( s );
	    	
	    }
	    
    }else{
    	
    	out.println("<br>");

    	out.println("what? list is null...");
    	
    }
    
    if( session.getAttribute("olist") != null ) {

    	for( org.ranran.domain.User u : (java.util.List<org.ranran.domain.User>) session.getAttribute("olist") ){
	
	    	out.println("<br>");
	    	
	    	out.println( u );
	    	
	    }
    	
    }else{
    	
    	out.println("<br>");

    	out.println("what? olist is null...");
    	
    }
%>
    </body>

</html>
