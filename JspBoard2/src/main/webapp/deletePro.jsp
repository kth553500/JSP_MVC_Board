<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
 <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


  <!-- response.sendRedirect("list.jsp");
         http-equiv="Refresh" =>새로고침 옵션
         content="초단위(몇초동안 멈춘후);url=이동할페이지"  -->

<c:if test="${check==1 }">
<meta http-equiv="Refresh"
           content="0;url=/JspBoard2/list.do?pageNum=${pageNum}">
</c:if>

<c:if test="${check==0}">
	 <script>
	      alert("비밀번호가 맞지않습니다.\n다시 비밀번호를 확인요망!");
	      history.go(-1);
	 </script>
</c:if>
 
 
 