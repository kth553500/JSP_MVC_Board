<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
   <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<title>게시판</title>
<link href="style.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="script.js?ver=1"></script>
</head>
 <%-- <%
  int num = (Integer)request.getAttribute("num"); //${num}
 %> --%>
<body bgcolor="#e0ffff">  
<center><b>글쓰기</b>
<br><!-- onsubmit 이벤트="return 호출할 함수명(~)" -->
<form method="post" name="writeform" action="/JspBoard2/writePro.do"
          onsubmit="return writeSave()">
          
      <!-- 입력하지 않고 매개변수로 전달해서 테이블에 저장(hidden) 4개 --> 
      <input type="hidden" name="num" value="${num }" >
      <input type="hidden" name="ref" value="${ref }" >
      <input type="hidden" name="re_step" value="${re_step}" >
      <input type="hidden" name="re_level" value="${re_level }" >
      
<table width="400" border="1" cellspacing="0" cellpadding="0"  bgcolor="#e0ffff" align="center">
   <tr>
    <td align="right" colspan="2" bgcolor="#b0e0e6">
	    <a href="/JspBoard2/list.do"> 글목록</a> 
   </td>
   </tr>
   <tr>
    <td  width="70"  bgcolor="#b0e0e6" align="center">이 름</td>
    <td  width="330">
       <input type="text" size="10" maxlength="10" name="writer"></td>
  </tr>
  <tr>
    <td  width="70"  bgcolor="#b0e0e6" align="center" >제 목</td>
    <td  width="330">
  
    	<c:if test="${num==0 }"><!-- list.jsp에서 글쓰기 눌렀을때 신규글일때는 초기값0만 오게설정 -->
     	 	 <input type="text" size="40" maxlength="50" name="subject">
       </c:if>
       <c:if test="${num!=0 }"><!-- 양수(답변글이라면) content.jsp에서 글상세보기를눌러서 컬럼정보를 가져오므로 
       											양수만뜰수밖에 없다. -->
     	  	  <input type="text" size="40" maxlength="50" name="subject"
     	  	    	value="[re]">
       </c:if>
     </td>
  </tr>
  <tr>
    <td  width="70"  bgcolor="#b0e0e6" align="center">Email</td>
    <td  width="330">
       <input type="text" size="40" maxlength="30" name="email" ></td>
  </tr>
  <tr>
    <td  width="70"  bgcolor="#b0e0e6" align="center" >내 용</td>
    <td  width="330" >
     <textarea name="content" rows="13" cols="40"></textarea> </td>
  </tr>
  <tr>
    <td  width="70"  bgcolor="#b0e0e6" align="center" >비밀번호</td>
    <td  width="330" >
     <input type="password" size="8" maxlength="12" name="passwd"> 
	 </td>
  </tr>
<tr>      
<!-- a링크, action속성값, 이벤트처리를 통해서 이동하는 경우 전부 Jsp->do-->
 <td colspan=2 bgcolor="#b0e0e6" align="center"> 
  <input type="submit" value="글쓰기" >  
  <input type="reset" value="다시작성">
  <input type="button" value="목록보기" OnClick="window.location='/JspBoard2/list.do'">
</td></tr></table>    
</form>      
</body>
</html>      