package controller;
//메인컨트롤러(중심)!
import java.io.*;//FileInputStream
import java.util.*;//Map,Properties
import javax.servlet.*;
import javax.servlet.http.*;
//추가->다른 패키지의 클래스나 인터페이스를 참조
import action.CommandAction;

public class ControllerAction extends HttpServlet {

	// 명령어와 명령어 처리클래스를 쌍으로 저장
	private Map commandMap = new HashMap();//commandPro.properties에 설정한 내용을 담기위한 Map

	// 서블릿을 실행시 서블릿의 초기화 작업->생성자
	public void init(ServletConfig config) throws ServletException {

		// 경로에 맞는 CommandPro.properties파일을 불러옴
		String props = config.getInitParameter("propertyConfig");
		System.out.println("불러온경로=" + props); // 콘솔에서 경로확인 //경로(주소값)

		// 명령어와 처리클래스의 매핑정보를 저장할
		// Properties객체 생성
		Properties pr = new Properties();
		FileInputStream f = null;// 파일불러올때

		try {
			// CommandPro.properties파일의 내용을 읽어옴
			f = new FileInputStream(props); //props->불러온 CommandPro.properties의 벨류값:파일의경로

			pr.load(f); // 파일의 정보를 Properties에 저장

		} catch (IOException e) {
			throw new ServletException(e);
		} finally {
			if (f != null) //값이 있으면 정상동작해서
				try {
					f.close(); //이 파일이 열려있으면 파일종료
				} catch (IOException ex) {
				}
		}

		// 객체를 하나씩 꺼내서 그 객체명으로 Properties
		// 객체에 저장된 객체를 접근
		Iterator keyiter = pr.keySet().iterator(); //for문같은 반복문
		//pr.keySet().iterator() -> (설정되어있는 키값들을 keyiter에 반복하면서 넣어주는과정)
		// keyiter => 키값들이 들어있게 됨
		
		while (keyiter.hasNext()) {
			// 요청한 명령어를 구하기위해
			String command = (String) keyiter.next(); //keyiter(key값)은 object이므로 String으로 강제형변환
			System.out.println("command=" + command); //"키값";-> "/list.do"
			// 요청한 명령어(키)에 해당하는 클래스명을 구함
			String className = pr.getProperty(command); //className-> 벨류값 : action.ListAction
			System.out.println("className=" + className); // action.ListAction 

			try {
				// 그 클래스의 객체를 얻어오기위해 메모리에 로드
				Class commandClass = Class.forName(className); //ListAction.java파일을 메모리에 로드!
				System.out.println("commandClass=" + commandClass); //ListAction.java의 주소값
				Object commandInstance = commandClass.newInstance(); //ListAction.java를 객체로 집어넣음 (객체로)
				// 실제로 요청한 명령어에 해당되는 객체가 생성
				System.out.println("commandInstance=" + commandInstance);

				// Map객체 commandMap에 저장
				commandMap.put(command, commandInstance); //ListAction.java객체를 벨류값으로 넣음
				System.out.println("commandMap=" + commandMap);

			} catch (ClassNotFoundException e) {
				throw new ServletException(e);
			} catch (InstantiationException e) {
				throw new ServletException(e);
			} catch (IllegalAccessException e) {
				throw new ServletException(e);
			}
		} // while
	}

	public void doGet(// get방식의 서비스 메소드
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		requestPro(request, response);
	}

	protected void doPost(// post방식의 서비스 메소드
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		requestPro(request, response);
	}
	
	// 시용자의 요청을 분석해서 해당 작업을 처리
	private void requestPro(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String view = null; // 요청명령어에 따라서 이동할 페이지를 저장
		// list.do = action.ListAction=>/list.jsp
		// writeForm.do=action.WriteFormAction->

		// ListAction com = null;
		// WriteFormAction com = nulll;
		CommandAction com = null; // 어떠한 자식클래스의 객체라도 부모형으로 자동형변환
		// CommandAction com = new ListAction();
			try {
				// 1.요청명령어 분리 //locahost8080/jspboard2/list.do로 접속했을때
				String command = request.getRequestURI();// /JspBoard2/list.do	
		
				System.out.println("request.getRequestURI()=>" + request.getRequestURI());// /JspBoard2/list.do
				System.out.println("request.getContextPath()=>" + request.getContextPath());// /JspBoard2
				// /JspBoard2/list.do
				// /JspBoard2
				if (command.indexOf(request.getContextPath()) == 0) {
				// "/JspBoard2/list.do".indexOf("/JspBoard2") == 0	 =>true
					command = command.substring(request.getContextPath().length()); 
				//	"/JspBoard2/list.do" . substring (/JspBoard2를 자르겠다) =>/list.do
					System.out.println("실질적인 command=>" + command);
					// /list.do
				}
				// 요청명령어->list.do->action.ListAction객체->requestPro()
				com = (CommandAction) commandMap.get(command); // list.do인 키값을 넣어서 벨류값으로 받아온다 //자식객체가들어옴(형변환되서)
				System.out.println("com=>" + com); // //com->벨류값( action.ListAction@~)
				view = com.requestPro(request, response);//인터페이스를 상속받은 클래스의 오버라이딩된 requestPro메서드호출!
				System.out.println("view=>" + view); //requestPro메서드의 리턴값 list.jsp가 view에 담김
			} catch (Throwable e) {
				throw new ServletException(e);// 서블릿 예외처리
			}
			// 위에서 요청명령어에 해당하는 view로 데이터를 공유시키면서 이동 ->forward
			RequestDispatcher dispatcher = request.getRequestDispatcher(view);//view->보낼경로를 설정//list.jsp
			
			dispatcher.forward(request, response); // Controller->View //실제로 forward(패쓰!) jsp페이지로 넘겨줘라
			
	}
}
