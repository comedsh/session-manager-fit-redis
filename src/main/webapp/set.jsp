<!DOCTYPE html>
<%@ page session="false" %>

<%@ page contentType="text/html; charset=utf-8" language="java" %>

<%

// #1
request.getSession().setAttribute("hello", "world");

// #2
java.util.List<String> list = new java.util.ArrayList<String>(5);
list.add("a");
list.add("b");
list.add("c");

request.getSession().setAttribute("list", list);

java.util.List<org.ranran.domain.User> olist = new java.util.ArrayList<org.ranran.domain.User>(5);
olist.add( new org.ranran.domain.User("user1") );
olist.add( new org.ranran.domain.User("user2") );
olist.add( new org.ranran.domain.User("user3") );

request.getSession().setAttribute("olist", olist);


// #3, dirty check, to check if the direty values get updated between session and redis?
list.remove("c");

olist.remove(2);

%>
<html lang="en">
    <head>
        <title>初始化 session</title>
        <link href="favicon.ico" rel="icon" type="image/x-icon" />
        <link href="favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link href="tomcat.css" rel="stylesheet" type="text/css" />
    </head>

    <body>
    
    初始设置的值如下：<br>
    
<% 
    javax.servlet.http.HttpSession session = request.getSession();
    
    out.println( "key: hello; value: " + session.getAttribute("hello") );
    
    out.println( "<br>" );
    
    out.println( "key: list; values as below:");
    
    for( String s : (java.util.List<String>) session.getAttribute("list") ){

    	out.println("<br>");
    	
    	out.println( s );
    	
    }

    for( org.ranran.domain.User u : (java.util.List<org.ranran.domain.User>) session.getAttribute("olist") ){

    	out.println("<br>");
    	
    	out.println( u );
    	
    }

    
%>
    </body>

</html>
