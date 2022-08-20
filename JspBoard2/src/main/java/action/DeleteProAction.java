package action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lys.board.BoardDAO;

public class DeleteProAction implements CommandAction {

	@Override
	public String requestPro(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		// TODO Auto-generated method stub
		 
		     String pageNum=request.getParameter("pageNum");
		     String passwd=request.getParameter("passwd"); //직접입력
		     int num=Integer.parseInt(request.getParameter("num")); 
		     System.out.println("deletePro.jsp의 num="+num
		    		      +",passwd="+passwd+",pageNum="+pageNum);
		     
		     BoardDAO dbPro=new BoardDAO();
		     int check=dbPro.deleteArticle(num,passwd);
		     
		     request.setAttribute("check", check);
		     request.setAttribute("pageNum", pageNum);
		     
		return "/deletePro.jsp";
	}
	

}
