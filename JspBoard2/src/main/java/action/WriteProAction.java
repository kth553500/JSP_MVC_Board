package action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//추가
import lys.board.*; //BoardDAO,BoardDTO
import java.sql.Timestamp; //DB에서의 필드의 날짜자료형 때문에

public class WriteProAction implements CommandAction {

	@Override
	public String requestPro(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		// TODO Auto-generated method stub
		//1.writePro.jsp의 자바처리 구문
	     request.setCharacterEncoding("utf-8");//한글처리
	     BoardDTO article = new BoardDTO();
	     article.setNum(Integer.parseInt(request.getParameter("num"))); //hidden
	     article.setWriter(request.getParameter("writer"));
	     article.setEmail(request.getParameter("email"));
	     article.setSubject(request.getParameter("subject"));
	     article.setPasswd(request.getParameter("passwd"));
	     article.setRef(Integer.parseInt(request.getParameter("ref"))); //hidden
	     article.setRe_step(Integer.parseInt(request.getParameter("re_step"))); //hidden
	     article.setRe_level(Integer.parseInt(request.getParameter("re_level"))); //hidden
	     //조회수 ->자동으로 0을 default
	     article.setContent(request.getParameter("content"));
	     //readcount(0)(생략),오늘날짜,원격주소ip
	     Timestamp temp=new Timestamp(System.currentTimeMillis());//컴퓨터계산(날짜,시간)
	     article.setReg_date(temp);//오늘 날짜 수동저장=>~.getReg_date() ->now()
	     article.setIp(request.getRemoteAddr());//원격 ip주소 저장
	     
	     BoardDAO dbPro=new BoardDAO();
	     dbPro.insertArticle(article);
	     
	     //3.공유->페이지이동
		return "/writePro.jsp"; // /list.do로 처리->list.jsp
	}

}
