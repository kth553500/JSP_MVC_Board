package lys.board;

//DBConnectionMgr(DB접속,관리),BoardDTO(매개변수,반환형,데이터를 담는 역할)

import java.sql.*;
import java.util.*;

public class BoardDAO {//MemberDAO

	private DBConnectionMgr pool=null;//1.연결객체선언
	//공통
	private Connection con=null;
	private PreparedStatement pstmt=null;
	private ResultSet rs=null;//select
	private String sql="";//실행시킬 SQL구문 저장
	
	//2.생성자를 통해서 연결=->의존관계
	public BoardDAO() {
		try {
			pool=DBConnectionMgr.getInstance();
			System.out.println("pool=>"+pool);
		}catch(Exception e) {
		   System.out.println("DB접속 오류=>"+e);	
		}
	}
	
	//3.메서드 작성(페이징 처리를 위한 메서드 작성)=>총레코드수(=총게시물수=총회원수)
	//select count(*) from board;  select count(*) from member
	public int getArticleCount() {//getmemberCount()->MemberDAO에서 작성
		int x=0;
		try {
			con=pool.getConnection();
			System.out.println("con=>"+con);
			sql="select count(*) from board";
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next()) {//보여주는 결과가 있다면
				x=rs.getInt(1);//변수명=rs.get자료형(필드명 또는 인덱스번호)=>필드명X
			}
		}catch(Exception e) {
			System.out.println("getArticleCount()에러발생=>"+e);
		}finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return x; //총레코드수
	}
	
	
	////**********////////(1).검색어에 해당되는 총레코드수를 구하는 메서드(검색분야, 검색어가 추가됨***)
	public int getArticleSearchCount(String search, String searchtext) {
		int x=0;
		try {
			con=pool.getConnection();
			System.out.println("con=>"+con);
			//-------------------------------------------------------
			if(search==null || search=="") { //검색분야 선택X
				sql="select count(*) from board";
			} else { //검색분야(제목,작성자,제목+본문)
				if(search.equals("subject_content")) {//제목+본문
					sql="select count(*) from board where subject like '%" + 
							searchtext + "%' or content like '%" + searchtext + "%'";
				}else {//제목 or 작성자 ->매개변수를 이용해서 하나의 sql통합
					//search의 value가 제목도 될수있고, 작성자도 될수있기떄문에 search로 썻음
					sql="select count(*) from board where "+ search +" like '%"+
							searchtext+"%'";
				}
			}
			System.out.println("getArticleSearchCount 검색 sql=>" + sql);
			//---------------------------------------------------
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next()) {//보여주는 결과가 있다면
				x=rs.getInt(1);//변수명=rs.get자료형(필드명 또는 인덱스번호)=>필드명X
			}
		}catch(Exception e) {
			System.out.println("getArticleSearchCount()에러발생=>"+e);
		}finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return x; //검색어에 해당되는 총레코드수 //검색했을경우와 안했을 경우에 대한 총레코드수
	}
	//////////////////////////////
	
	

	//2.글목록보기에 대한 메서드구현->레코드 한개이상->한 페이지당 10개씩 끊어서 보여준다.
	//1)레코드의 시작번호    2)불러올 레코드의 갯수(ex 10,20,30)
	//public List<MemberDTO> getMemberList(int start,int end){
	public List<BoardDTO> getArticles(int start,int end){
		 List<BoardDTO> articleList=null;//ArrayList articleList=null;//(0)
		 
		 try {
			 con=pool.getConnection();
			 /*
			  * 그룹번호가 가장 최신의 글을 중심으로 정렬하되,만약에 level이 같은 경우에는
			  * step값으로 오름차순을 통해서 몇번째 레코드번호를 기준해서 몇개까지 정렬할것인가
			  * 를 지정해주는 SQL구문
			  */
			 sql="select * from board order by ref desc,re_step limit ?,?";
			 pstmt=con.prepareStatement(sql);
			 pstmt.setInt(1, start-1);//mysql은 레코드순번이 내부적으로 0부터 시작
			 pstmt.setInt(2, end);//불러와서 담을 갯수(ex 10)
			 rs=pstmt.executeQuery();
			 if(rs.next()) {//보여주는 결과가 있다면
				 articleList=new ArrayList(end);//10=>end갯수만큼 데이터를 담을 공간생성
				 do {
					 BoardDTO article=new BoardDTO();//new MemberDTO();필드별로 담기위해서
					 article.setNum(rs.getInt("num"));
					 article.setWriter(rs.getString("writer"));//부적합한 열입니다.->필드오타
					 article.setEmail(rs.getString("email"));
					 article.setSubject(rs.getString("subject"));//글제목
					 article.setPasswd(rs.getString("passwd"));
					 article.setReg_date(rs.getTimestamp("reg_date"));//작성날짜
					 article.setReadcount(rs.getInt("readcount"));//조회수 default->0
					 article.setRef(rs.getInt("ref"));//그룹번호->신규글과 답변글을 묶어주는 역할
					 article.setRe_step(rs.getInt("re_step"));//답변글이 나오는 순서
					 article.setRe_level(rs.getInt("re_level"));//들여쓰기(답변의 깊이) depth
					 article.setContent(rs.getString("content"));//글내용
					 article.setIp(rs.getString("ip"));//ip->request.getRemoteAddr()
					 //추가
					 articleList.add(article);//생략하면 데이터가 저장X->for문 에러유발(NullPointerException)
				 }while(rs.next());
			 }
		 }catch(Exception e) {
			System.out.println("getArticles() 에러유발=>"+e); 
		 }finally {
			 pool.freeConnection(con, pstmt, rs);
		 }
		 return articleList;//NullPointerException 조심
	}
	
	///////*******///////(2)검색어에 따른 레코드의 범위지정에 대한 메서드(검색분야가 추가)
	public List<BoardDTO> getBoardArticles(int start,int end,String search,String searchtext){
		 List<BoardDTO> articleList=null;//ArrayList articleList=null;//(0)
		 
		 try {
			 con=pool.getConnection();
			//-----------------------------------------------------------------------
			 if(search==null || search=="") {
				 sql="select * from board order by ref desc,re_step limit ?,?";
			 } else {
				 if(search.equals("subject_content")) {//제목+본문
						sql="select * from board where subject like '%" + 
								searchtext + "%' or content like '%" + searchtext + "%' order by ref desc,re_step limit ?,?";
					}else {//제목 or 작성자 ->매개변수를 이용해서 하나의 sql통합
						sql="select * from board where "+ search +" like '%"+
								searchtext+"%' order by ref desc,re_step limit ?,?";
					}
			 }
			 System.out.println("getBoardArticles()의 sql=>" + sql);
			 //-----------------------------------------------------------------------
			 pstmt=con.prepareStatement(sql);
			 pstmt.setInt(1, start-1);//mysql은 레코드순번이 내부적으로 0부터 시작
			 pstmt.setInt(2, end);//불러와서 담을 갯수(ex 10)
			 rs=pstmt.executeQuery();
			 if(rs.next()) {//보여주는 결과가 있다면
				 articleList=new ArrayList(end);//10=>end갯수만큼 데이터를 담을 공간생성
				 do {
					 BoardDTO article=new BoardDTO();//new MemberDTO();필드별로 담기위해서
					 article.setNum(rs.getInt("num"));
					 article.setWriter(rs.getString("writer"));//부적합한 열입니다.->필드오타
					 article.setEmail(rs.getString("email"));
					 article.setSubject(rs.getString("subject"));//글제목
					 article.setPasswd(rs.getString("passwd"));
					 article.setReg_date(rs.getTimestamp("reg_date"));//작성날짜
					 article.setReadcount(rs.getInt("readcount"));//조회수 default->0
					 article.setRef(rs.getInt("ref"));//그룹번호->신규글과 답변글을 묶어주는 역할
					 article.setRe_step(rs.getInt("re_step"));//답변글이 나오는 순서
					 article.setRe_level(rs.getInt("re_level"));//들여쓰기(답변의 깊이) depth
					 article.setContent(rs.getString("content"));//글내용
					 article.setIp(rs.getString("ip"));//ip->request.getRemoteAddr()
					 //추가
					 articleList.add(article);//생략하면 데이터가 저장X->for문 에러유발(NullPointerException)
				 }while(rs.next());
			 }
		 }catch(Exception e) {
			System.out.println("getBoardArticles() 에러유발=>"+e); 
		 }finally {
			 pool.freeConnection(con, pstmt, rs);
		 }
		 return articleList; //검색을 했을경우와 안했을 경우로 쿼리에대한 쿼리문에대한 결과가 달라짐
		 //NullPointerException 조심 
	}
	
	///////////***************////////////////////////////////////////////
	//(3) 페이징 처리 계산 정리해주는 메서드 
	public Hashtable pageList(String pageNum,int count) {
	//1. 페이징 처리결과를 저장할 Hashtable 객체를 선언
		Hashtable<String, Integer>pgList = new Hashtable<String,Integer>();
		
		
		int pageSize=5;//numPerPage=>페이지당 보여주는 게시물수(=레코드수) 10
	    int blockSize=5;//pagePerBlock=>블럭당 보여주는 페이지수 10
	  
	  //게시판을 맨 처음 실행시키면 무조건 1페이지부터 출력->가장 최근의 글이 나오기때문에 
	  if(pageNum==null){
		  pageNum="1";//default(무조건 1페이지는 선택하지 않아도 보여줘야 되기때문에)
	  }
	  int currentPage=Integer.parseInt(pageNum);//"1"->1(=nowPage)(현재페이지)
	  //                  (1-1)*10+1=1,(2-1)*10+1=11,(3-1)*10+1=21
	  int startRow=(currentPage-1)*pageSize+1;//시작 레코드번호
	  int endRow=currentPage*pageSize;//1*10=10,2*10=20,3*10=30
	  
	  int number=0;//beginPerPage->페이지별로 시작하는 맨 처음에 나오는 게시물번호
	  System.out.println("현재 레코드수(count)->" + count);
	  
	  //           122-(1-1)*10=122,121,120,119,,,
	  //           122-(2-1)*10=122-10=
	  number=count-(currentPage-1)*pageSize;
	  System.out.println("페이지별 number=>"+number);
	  
	  //총페이지수, 시작, 종료페이지 계산-> list.jsp에서 이미 코딩
	 //모델1에서의 list.jsp에서 복사해온다. //페이징처리부분
	  //1.총페이지수 구하기 122/10 + 12.2 +  1.0(122%10=1) =>13
		int pageCount = count/pageSize + (count%pageSize==0? 0 : 1);
		
		//2.시작페이지
		int startPage = 0;
		if(currentPage%blockSize!=0){//1~9, 11~19, 21~29(10의배수X)
			startPage = currentPage/blockSize*blockSize+1; //경계선 때문
		} else { //10%10 = 0 (10, 20, 30, 40...)
							//((10/10)-1)*10+1 => 1, 2=>>2
			startPage = ((currentPage/blockSize)-1)*blockSize+1;
		}
		
		//3.종료페이지
		int endPage = startPage + blockSize-1; //1+10-1 =>10 , 2+10-1=>11
		System.out.println("startPage=>" + startPage+ ",endPage=>" + endPage);
		
		//블럭별로 구분해서 링크 걸어서 출력()
		if(endPage > pageCount) endPage = pageCount; //마지막=총페이지수
		
		
		//페이징처리에 대한 계산결과->Hashtable,HashMap->
		//ListAction에 전달 -> 메모리에 저장,공유->list.jsp로 전달
		pgList.put("pageSize", pageSize); // <-> psList.get(키명)
		pgList.put("blockSize", blockSize);
		pgList.put("currentPage",currentPage );
		pgList.put("startRow", startRow);
		pgList.put("endRow", endRow);
		pgList.put("count", count);
		pgList.put("number", number);
		pgList.put("startPage", startPage);
		pgList.put("endPage", endPage);
		pgList.put("pageCount", pageCount);
		
		return pgList; //ListAction에게 리턴해준다.
	}
	
	
	
	
	//-----게시판의 글쓰기및 답변글쓰기
	//insert into board values(?,?,,,,)
	public void insertArticle(BoardDTO article) {//~(MemberDTO mem)
		//1.article->신규글인지 답변글(기존 게시물번호)인지 확인
		int num=article.getNum();//0 신규글<->0이 아닌경우->양수(1이상~)(old)
		int ref=article.getRef();
		int re_step=article.getRe_step();
		int re_level=article.getRe_level();
		
		int number=0;//데이터를 저장하기위한 게시물번호(=New)
		System.out.println("insertArticle 메서드의 내부 num=>"+num);//0 신규글
		System.out.println("ref=>"+ref+",re_step=>"+re_step+",re_level=>"+re_level);
		
		try {
			con=pool.getConnection();
			sql="select max(num) from board";
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			if(rs.next()) {//보여주는 결과가 있다면
				number=rs.getInt(1)+1;//최대값+1
			}else {
				number=1;//테이블에 한개의 데이터가 없다면 최초부여값 1
			}
			//답변글이라면(양수이면서 1이상인경우)
			if(num!=0) {
				//같은 그룹번호를 가지고 있으면서 나보다 step값이 큰 게시물을 찾아서 그 step하나증가
			   sql="update board set re_step=re_step+1 where ref=? and re_step > ?";
			   pstmt=con.prepareStatement(sql);
			   pstmt.setInt(1, ref);
			   pstmt.setInt(2, re_step);
			   int update=pstmt.executeUpdate();
			   System.out.println("댓글수정유무(update)=>"+update);//1.성공  0 실패
			   //답변글
			   re_step=re_step+1;
			   re_level=re_level+1;
			}else {//신규글이라면 num=0
				ref=number;//num=1  ref=1(1,2,3,4,,,,)
				re_step=0;//답변순서 X
				re_level=0;//답변자체 X
			}
			//12개->num,reg_date,readcount(생략)->default
			//작성날짜=>sysdate, now()(mysql)
			//SQLSyn~sql구문 오류
			sql="insert into board(writer,email,subject,passwd,reg_date,";
			sql+=" ref,re_step,re_level,content,ip)values(?,?,?,?,?,?,?,?,?,?)";
			//sql+=" ref,re_step,re_level,content,ip)values(?,?,?,?,now(),?,?,?,?,?)";
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, article.getWriter());//웹상에서 이미 데이터 저장된 상태(setWriter(~)
			pstmt.setString(2, article.getEmail());
			pstmt.setString(3, article.getSubject());
			pstmt.setString(4, article.getPasswd());
			pstmt.setTimestamp(5,article.getReg_date());//5번째에 ? 대신에 now()
			//------ref,re_step,re_level에 대한 계산이 적용된 상태에서 저장
			pstmt.setInt(6,ref);//최대값+1
			pstmt.setInt(7,re_step);//0
			pstmt.setInt(8,re_level);//0
			//-----------------------------------------------
			pstmt.setString(9, article.getContent());//내용
			pstmt.setString(10, article.getIp());//request.getRemoteAddr();
			int insert=pstmt.executeUpdate();
			System.out.println("게시판의 글쓰기 성공유무(insert)=>"+insert);//1 or 0
		}catch(Exception e) {
			System.out.println("insertArticle()메서드 에러유발=>"+e);
		}finally {
			pool.freeConnection(con, pstmt, rs);//rs가 왜필요?
		}
	}
	//글상세보기
	//<a href="content.jsp?num=3&pageNum=1">
	//형식) select * from board where num=3;
	//형식) update board set readcount=readcount+1 where num=3;
	//public MemberDTO getMember(String id){~}
	public BoardDTO getArticle(int num) {
		
     BoardDTO article=null;
		 
		 try {
			 con=pool.getConnection();
		
			 sql="update board set readcount=readcount+1 where num=?";
			 pstmt=con.prepareStatement(sql);
			 pstmt.setInt(1, num);
			 int update=pstmt.executeUpdate();
			 System.out.println("조회수 증가유무(update)=>"+update);//1
			 
			 sql="select * from board where num=?";
			 pstmt=con.prepareStatement(sql);
			 pstmt.setInt(1, num);
			 rs=pstmt.executeQuery();
			 
			 if(rs.next()) {//보여주는 결과가 있다면
				    article=this.makeArticleFromResult();
				    /*
					 article=new BoardDTO();//new MemberDTO();필드별로 담기위해서
					 article.setNum(rs.getInt("num"));
					 article.setWriter(rs.getString("writer"));//부적합한 열입니다.->필드오타
					 article.setEmail(rs.getString("email"));
					 article.setSubject(rs.getString("subject"));//글제목
					 article.setPasswd(rs.getString("passwd"));
					 article.setReg_date(rs.getTimestamp("reg_date"));//작성날짜
					 article.setReadcount(rs.getInt("readcount"));//조회수 default->0
					 article.setRef(rs.getInt("ref"));//그룹번호->신규글과 답변글을 묶어주는 역할
					 article.setRe_step(rs.getInt("re_step"));//답변글이 나오는 순서
					 article.setRe_level(rs.getInt("re_level"));//들여쓰기(답변의 깊이) depth
					 article.setContent(rs.getString("content"));//글내용
					 article.setIp(rs.getString("ip"));//ip->request.getRemoteAddr()
				  */
			 }
		 }catch(Exception e) {
			System.out.println("getArticle() 에러유발=>"+e); 
		 }finally {
			 pool.freeConnection(con, pstmt, rs);
		 }
		 return article;//content.jsp에서 받아서 출력->NullPointerException
	}
	//접근지정자가 private가 되는 경우 외부에서 호출되면 안되고 내부에서만 호출되는 메서드 작성
	private BoardDTO makeArticleFromResult() throws Exception {
		 BoardDTO article=new BoardDTO();//new MemberDTO();필드별로 담기위해서
		 article.setNum(rs.getInt("num"));
		 article.setWriter(rs.getString("writer"));//부적합한 열입니다.->필드오타
		 article.setEmail(rs.getString("email"));
		 article.setSubject(rs.getString("subject"));//글제목
		 article.setPasswd(rs.getString("passwd"));
		 article.setReg_date(rs.getTimestamp("reg_date"));//작성날짜
		 article.setReadcount(rs.getInt("readcount"));//조회수 default->0
		 article.setRef(rs.getInt("ref"));//그룹번호->신규글과 답변글을 묶어주는 역할
		 article.setRe_step(rs.getInt("re_step"));//답변글이 나오는 순서
		 article.setRe_level(rs.getInt("re_level"));//들여쓰기(답변의 깊이) depth
		 article.setContent(rs.getString("content"));//글내용
		 article.setIp(rs.getString("ip"));
		 return article;
	}
	
	//글수정
	//1)수정할 데이터를 찾을 메서드 필요->select * from board where num=?
	public BoardDTO updateGetArticle(int num) {
		
		BoardDTO article=null;
		 try {
			 con=pool.getConnection();
			 sql="select * from board where num=?";
			 pstmt=con.prepareStatement(sql);
			 pstmt.setInt(1, num);
			 rs=pstmt.executeQuery();
			 if(rs.next()) {//보여주는 결과가 있다면
					 article=makeArticleFromResult();//반환형을 통해서 객체를 얻어온다.
			 }
		 }catch(Exception e) {
			System.out.println("updateGetArticle() 에러유발=>"+e); 
		 }finally {
			 pool.freeConnection(con, pstmt, rs);
		 }
		 return article;
	}
	//2)수정시켜주는 메서드 작성->본인인지 확인절차=>회원탈퇴(암호를 비교->탈퇴)와 동일하다.
	public int updateArticle(BoardDTO article) {//insertArticle(BoardDTO article)
		String dbpasswd="";//DB상에서 찾은 암호를 저장
		int x=-1;//게시물의 수정유무
		
		try {
			con=pool.getConnection();
			sql="select passwd from board where num=?";//sql구문먼저 확인(오타)
			pstmt=con.prepareStatement(sql);
			pstmt.setInt(1, article.getNum());//index ~ 에러유발->작성X
			rs=pstmt.executeQuery();
			//암호를 찾았다면
			if(rs.next()) {
				dbpasswd=rs.getString("passwd");
				System.out.println("dbpasswd=>"+dbpasswd);
				//dbpasswd(DB상에 저장된 암호)==passwd(웹상에서 입력한 암호)
				System.out.println("updateArticle()내부 암호확인중");
				System.out.println("article.getPasswd()=>"+article.getPasswd());
				
				if(dbpasswd.equals(article.getPasswd())) {//본인인증
					sql="update board set writer=?,email=?,subject=?,passwd=?,";
					sql+=" content=? where num=?";
					pstmt=con.prepareStatement(sql);
					pstmt.setString(1, article.getWriter());
					pstmt.setString(2, article.getEmail());
					pstmt.setString(3, article.getSubject());
					pstmt.setString(4, article.getPasswd());
					pstmt.setString(5, article.getContent());
					pstmt.setInt(6, article.getNum());
					
					int update=pstmt.executeUpdate();
					System.out.println("게시판의 글수정 성공유무)=>"+update);
					x=1;//수정성공 성공
				}else {//암호가 틀린경우
					x=0;//수정 실패
				}
			}else {//암호가 존재하지 않은경우
				x=-1;
			}
		}catch(Exception e) {
			System.out.println("updateArticle()실행 오류=>"+e);
		}finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return x;
	}
	//                 select passwd from board where num=?
	//게시물 삭제->delete from board where num=?
	public int deleteArticle(int num,String passwd) {//회원탈퇴 기능과 동일
		String dbpasswd="";//DB상에서 찾은 암호를 저장
		int x=-1;//게시물의 삭제유무
		
		try {
			con=pool.getConnection();
			sql="select passwd from board where num=?";//sql구문먼저 확인(오타)
			pstmt=con.prepareStatement(sql);
			pstmt.setInt(1, num);//index ~ 에러유발->작성X
			rs=pstmt.executeQuery();
			//암호를 찾았다면
			if(rs.next()) {
				dbpasswd=rs.getString("passwd");
				System.out.println("dbpasswd=>"+dbpasswd);//다 개발후에는 삭제
				//dbpasswd(DB상에 저장된 암호)==passwd(웹상에서 입력한 암호)
				if(dbpasswd.equals(passwd)) {//본인인증
					sql="delete from board where num=?";
					pstmt=con.prepareStatement(sql);
					pstmt.setInt(1, num);
					int delete=pstmt.executeUpdate();
					System.out.println("게시판의 글삭제 성공유무)=>"+delete);//1 성공, or 0 실패
					x=1;//삭제성공 
				}else {//암호가 틀린경우
					x=0;//삭제 실패
				}
			}
		}catch(Exception e) {
			System.out.println("deleteArticle()실행 오류=>"+e);//Log객체
		}finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return x;
	}

}
