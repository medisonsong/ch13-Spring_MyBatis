package kr.spring.board.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.spring.board.service.BoardService;
import kr.spring.board.vo.BoardVO;
import kr.spring.util.PageUtil;

@Controller
public class BoardController {
	//BoardService 주입받기
	@Autowired
	private BoardService boardService; 
	//로그 처리(로그 대상 지정)
	private static final Logger log = LoggerFactory.getLogger(BoardController.class);
	
	//자바빈(VO) 초기화
	@ModelAttribute
	public BoardVO initCommand() {
		return new BoardVO();
	}
	
	//list.do로 리다이렉트
	@RequestMapping("/")
	public String main() {
		return "redirect:/list.do";
	}
	
	//글쓰기 폼호출
	@GetMapping("/insert.do")
	public String form() {
		
		return "insertForm";
	}
	
	//글쓰기
	@PostMapping("/insert.do")
	public String submitForm(@Valid BoardVO boardVO, BindingResult result) {
		log.debug("<<BoardVO>> : " + boardVO);
		
		//유효성 체크 결과 오류가 있으면 폼 호출
		if(result.hasErrors()) {
			return form();
		}
		//글 등록
		boardService.insertBoard(boardVO);
		
		return "redirect:/list.do";
	}
	
	//목록처리
	@RequestMapping("/list.do")
	public ModelAndView getList(@RequestParam(value="pageNum",defaultValue="1") int currentPage) {
		
		//총 레코드 수
		int count = boardService.selectBoardCount();
		//페이지 처리
		PageUtil page = new PageUtil(currentPage,count,10,10,"list.do"); 
		
		//목록 호출
		List<BoardVO> list = null;
		if(count > 0) {
			Map<String,Integer> map = new HashMap<String,Integer>(); //마이바티스-인자 하나만 넘기지만 인자가 여러개기 때문에 map으로 묶음
			
			map.put("start", page.getStartRow());
			map.put("end", page.getEndRow());
			
			list = boardService.selectBoardList(map);
		}
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("selectList");
		mav.addObject("count", count);
		mav.addObject("list", list);
		mav.addObject("page", page.getPage());
		
		return mav;
	}
	
	//페이지 상세보기
	@RequestMapping("/detail.do")
	public ModelAndView detail(@RequestParam int num) {
		BoardVO board = boardService.selectBoard(num); //한 건의 레코드 읽어오기
		
		return new ModelAndView("selectDetail","board",board); //호출페이지(뷰이름),속성명,속성값
	}
	
	//수정폼 호출
	@GetMapping("/update.do")
	public String formUpdate(@RequestParam int num, Model model) {
		model.addAttribute("boardVO", boardService.selectBoard(num)); //자바빈 model에 저장(+request에도)
		
		return "updateForm"; //반환타입(String) 문자열로 view호출
	}
	
	//수정 작업
	@PostMapping("/update.do")
	public String submitUpdate(@Valid BoardVO boardVO, BindingResult result) {
		
		//유효성 체크 결과 오류가 있으면 폼을 호출
		if(result.hasErrors()) {
			return "updateForm";
		}
		//DB에 저장된 비밀번호 구하기
		BoardVO db_board = boardService.selectBoard(boardVO.getNum()); //한건의 데이터 읽어오기
		
		//비밀번호 체크
		if(!db_board.getPasswd().equals(boardVO.getPasswd())) {
			result.rejectValue("passwd", "invalidPassword");
			return "updateForm";
		}
		//글 수정
		boardService.updateBoard(boardVO);
		
		return "redirect:/list.do";
	}
	
	//삭제폼 호출
	@GetMapping("/delete.do")
	public String formDelete(@RequestParam int num, Model model) {
		BoardVO boardVO = new BoardVO();
		boardVO.setNum(num);
		model.addAttribute("boardVO", boardService.selectBoard(num));
		
		return "deleteForm";
	}
	
	//삭제 작업
	@PostMapping("/delete.do")
	public String submitDelete(@Valid BoardVO boardVO, BindingResult result) {
		log.debug("<<BoardVO>> : " + boardVO);
		
		//유효성 체크 결과 오류가 있으면 폼을 호출
		//비밀번호만 전송 여부 체크
		if(result.hasFieldErrors("passwd")) {
			return "deleteForm";
		}
		//DB에 저장된 비밀번호 구하기
		BoardVO db_board = boardService.selectBoard(boardVO.getNum());
		//비밀번호 일치 여부 체크
		if(!db_board.getPasswd().equals(boardVO.getPasswd())) {
			result.rejectValue("passwd", "invalidPassword");
			return "deleteForm";
		}
		//글 삭제
		boardService.deleteBoard(boardVO.getNum());
		
		return "redirect:/list.do";
	}
	
}
