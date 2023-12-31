package kr.co.mgv.board.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.mgv.board.form.AddTboardForm;
import kr.co.mgv.board.form.ReportForm;
import kr.co.mgv.board.list.TBoardCommentList;
import kr.co.mgv.board.list.TheaterBoardList;
import kr.co.mgv.board.service.MovieBoardService;
import kr.co.mgv.board.service.TheaterBoardService;
import kr.co.mgv.board.vo.BoardLocation;
import kr.co.mgv.board.vo.BoardTheater;
import kr.co.mgv.board.vo.ReportReason;
import kr.co.mgv.board.vo.TBoardComment;
import kr.co.mgv.board.vo.TBoardLike;
import kr.co.mgv.board.vo.TboardReport;
import kr.co.mgv.board.vo.TheaterBoard;
import kr.co.mgv.user.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/board/theater")
@RequiredArgsConstructor
@Slf4j
public class TheaterBoardController {
	
	private final TheaterBoardService theaterBoardService;
	private final MovieBoardService movieBoardService;

    @GetMapping("/list")
    public String theaterList(@RequestParam(name = "sort", required = false, defaultValue = "id") String sort,
			@RequestParam(name = "rows", required = false, defaultValue = "10") int rows,
			@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			@RequestParam(name = "opt", required = false, defaultValue = "") String opt,
			@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(name = "theaterNo", required = false) Integer theaterNo,
			@RequestParam(name = "locationNo", required = false) Integer locationNo,
			@AuthenticationPrincipal User user,
			Model model) {
    	
    	Map<String, Object> param = new HashMap<String, Object>();
		param.put("sort", sort);
		param.put("rows", rows);
		param.put("page", page);
		if (theaterNo != null) {
			param.put("theaterNo", theaterNo);
		}
		if (locationNo != null) {
			param.put("locationNo", locationNo);
			List<BoardTheater> theaters = theaterBoardService.getTheatersByLocationNo(locationNo);
			model.addAttribute("theaters", theaters);
		}
		if(StringUtils.hasText(opt) && StringUtils.hasText(keyword)) {
			param.put("opt", opt);
			param.put("keyword", keyword);
		}
    	
		// service로 극장게시물 목록, 극장,지역 목록, 페이지네이션 조회하기 
		TheaterBoardList result = theaterBoardService.getTBoards(param);
		
		if(user != null) {
			// 로그인한 사용자의 신고 기록 가져오기
			List<TboardReport> reportList = theaterBoardService.getReportById(user.getId());
			
			// 사용자가 신고한 게시물 제외
			List<TheaterBoard> theaterBoardsToShow = new ArrayList<>();
			
			for(TheaterBoard board : result.getTheaterBoards()) {
				boolean isReported = reportList.stream()
						.anyMatch(report -> report.getBoard().getNo() == board.getNo());
				if(!isReported) {
					theaterBoardsToShow.add(board);
				}
			}
			result.setTheaterBoards(theaterBoardsToShow);
			model.addAttribute("reports", theaterBoardsToShow);
		}
		
		// model에 조회한 리스트 담기
		model.addAttribute("result", result);
		
		
        return "view/board/theater/list";
    }

    
	@GetMapping("/theaterByLocationNo")
	@ResponseBody
	public List<BoardTheater> getTheaterbyLocationNo(@RequestParam("locationNo") int locationNo) {
		List<BoardTheater> theaters = theaterBoardService.getTheatersByLocationNo(locationNo);
		return theaters;
	}
	
	// 상세페이지 관련
	@GetMapping("/read")
	public String read(@RequestParam("no") int no,
					   @RequestParam("page") int page,
					   @RequestParam(name = "rows", required = false, defaultValue = "10") Integer rows,
					   @RequestParam("sort") String sort,
					   @RequestParam(name = "theaterNo", required = false) Integer theaterNo,
					   @RequestParam(name = "locationNo", required = false) Integer locationNo,
					   @RequestParam("opt") String opt,
					   @RequestParam("keyword") String keyword,
					   RedirectAttributes redirectAttributes) {
		
		theaterBoardService.increseRead(no);
		
		redirectAttributes.addAttribute("no", no);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("sort", sort);
        if(rows != null) {
        	redirectAttributes.addAttribute("rows", rows);		
        }
        redirectAttributes.addAttribute("theaterNo", theaterNo);
        redirectAttributes.addAttribute("locationNo", locationNo);
        redirectAttributes.addAttribute("opt", opt);
        redirectAttributes.addAttribute("keyword", keyword);
		
		return "redirect:/board/theater/detail";
	}
	
	@GetMapping("/detail")
	public String theaterDetail(@RequestParam("no") int no,
								Model model,
								@AuthenticationPrincipal User user) {
		
		if(user != null) {
			TBoardLike like = new TBoardLike();
			like.setUser(user);
			TheaterBoard board = TheaterBoard.builder()
									.no(no)
									.build();
			like.setBoard(board);
			
			TBoardLike savedLike = theaterBoardService.getLike(like);
			model.addAttribute("like", savedLike);
		}
		
		// 게시물 번호로 게시물 조회
		TheaterBoard theaterBoard = theaterBoardService.getTheaterBoardByNo(no);
		model.addAttribute("board", theaterBoard);
		// 모댓글 목록
		List<TBoardComment> comments = theaterBoardService.getComments(no);
		model.addAttribute("comments", comments);
		// 자손댓글 목록
		List<TBoardComment> childComments = theaterBoardService.getChildComments(no);
		model.addAttribute("childComments", childComments);
		// 신고 이유
		List<ReportReason> reportReasons = movieBoardService.getReportReason();
		model.addAttribute("reasons", reportReasons);
		
		return "view/board/theater/detail";
	}

	@PostMapping("/changelike")
	@ResponseBody
	public ResponseEntity<Void> addLike(@RequestParam("no") int no,
										@RequestParam("id") String id,
										@RequestParam("likeCount") int likeCount,
										@RequestParam("writerId") String writerId) throws IOException {
		
		TBoardLike like = new TBoardLike();
    	User user = User.builder()
				.id(id)
				.build();
    	like.setUser(user);
    	TheaterBoard board = TheaterBoard.builder()
						.no(no)
						.build();
		like.setBoard(board); 
		TBoardLike savedLike = theaterBoardService.getLike(like);
		
	 	if (savedLike != null && "Y".equals(savedLike.getCancel())) {
    		savedLike.setCancel("N");
    		theaterBoardService.updateTboardLike(savedLike, writerId);
    	} else if(savedLike != null && "N".equals(savedLike.getCancel())) {
    		savedLike.setCancel("Y");
    		theaterBoardService.updateTboardLike(savedLike, writerId);
    	} else if(savedLike == null) {
    		theaterBoardService.insertBoardLike(like);
    	}
	 	theaterBoardService.updateBoardLike(no, likeCount);
	 	
	 	return ResponseEntity.ok().build();
	}

	// 게시물 등록/수정/삭제 관련
    @GetMapping("/add")
    public String theaterBoardForm(Model model) {
    	
    	// /theaterByLocationNo 사용
    	List<BoardLocation> locations = theaterBoardService.getLocations();
    	model.addAttribute("locations", locations);
    	
        return "view/board/theater/form";
    }
    
    
    @PostMapping("/add")
    public String addTheaterBoard(@AuthenticationPrincipal User user, AddTboardForm form) {
        log.info("입력한 정보 -> {}", form);

        theaterBoardService.addTboard(form, user);
        return "redirect:/board/theater/list";
    }



    
    @GetMapping("/modify")
    public String theaterBoardModifyForm(@RequestParam("no") int no,
			   Model model) {
	
	List<BoardLocation> locations = theaterBoardService.getLocations();
	model.addAttribute("locations", locations);
    	
	TheaterBoard savedBoard = theaterBoardService.getTheaterBoardByNo(no);
	model.addAttribute("board", savedBoard);
	
	return "view/board/theater/modifyForm";
	}
    
    @PostMapping("/modify")
    public String modifyBoard(@RequestParam("no") int no, AddTboardForm form) {
    	
    	theaterBoardService.updateTBoard(form, no);
    	
    	return "redirect:/board/theater/detail?no=" + no;
    }
    
    @GetMapping("/delete")
    public String deleteBoard(@RequestParam("no") int no) {
    	
    	theaterBoardService.deleteBoard(no);
    	
    	return "redirect:/board/theater/list";
    }
	
	// 댓글 관련
	@PostMapping("/addComment")
	@ResponseBody
	public ResponseEntity<TBoardCommentList> addComment(@RequestParam("no") int no, 
										            @RequestParam("id") String id, 
										            @RequestParam(name="parentNo", required = false) Integer parentNo, 
										            @RequestParam(name="greatNo", required = false) Integer greatNo, 
										            @RequestParam("content") String content,
										            @RequestParam("writerId") String writerId) throws IOException{
		TBoardComment comment = new TBoardComment();
		comment.setContent(content);
		
		TheaterBoard tBoard = theaterBoardService.getTheaterBoardByNo(no);
		comment.setBoard(tBoard);
		
		if (parentNo != null) {
			TBoardComment parentComment = TBoardComment.builder().no(parentNo).build();
			comment.setParent(parentComment);
		}
		if (greatNo != null) {
			TBoardComment greatComment = TBoardComment.builder().no(greatNo).build();
			comment.setGreat(greatComment);
		}
		
		User user = User.builder().id(id).build();
		comment.setUser(user);
		
		theaterBoardService.TBoardCommentInsert(comment, writerId);
		TheaterBoard board = theaterBoardService.getTheaterBoardByNo(no);
		int commentCount = board.getCommentCount() + 1;
		theaterBoardService.updateBoardComment(no, commentCount);
		
		List<TBoardComment> parents = theaterBoardService.getComments(no);
		List<TBoardComment> childs = theaterBoardService.getChildComments(no);
		TBoardCommentList list = TBoardCommentList
								 .builder()
								 .parentComments(parents)
								 .childComments(childs)
								 .build(); 
		
		return ResponseEntity.ok().body(list);
	}
	
	@PostMapping("/addReComment")
	@ResponseBody
	public ResponseEntity<TBoardCommentList> addReComment (@RequestParam("no") int no, 
											    	   @RequestParam("id") String id, 
											    	   @RequestParam(name="parentNo", required = false) Integer parentNo, 
											    	   @RequestParam(name="greatNo", required = false) Integer greatNo, 
											    	   @RequestParam("content") String content,
											    	   @RequestParam("greatCommentId") String greatCommentId,
											    	   @RequestParam("writerId") String writerId) throws IOException {
		TBoardComment comment = new TBoardComment();
		comment.setContent(content);
		
		TheaterBoard tBoard = theaterBoardService.getTheaterBoardByNo(no);
		comment.setBoard(tBoard);
		
		if (parentNo != null) {
			TBoardComment parentComment = TBoardComment.builder().no(parentNo).build();
			comment.setParent(parentComment);
		}
		User writer = User.builder().id(greatCommentId).build();
		if (greatNo != null) {
			TBoardComment greatComment = TBoardComment.builder().no(greatNo).user(writer).build();
			comment.setGreat(greatComment);
		}
		
		User user = User.builder().id(id).build();
		comment.setUser(user);
		
		theaterBoardService.TBoardCommentInsert(comment, writerId);
		TheaterBoard board = theaterBoardService.getTheaterBoardByNo(no);
		int commentCount = board.getCommentCount() + 1;
		theaterBoardService.updateBoardComment(no, commentCount);
		
		List<TBoardComment> parents = theaterBoardService.getComments(no);
		List<TBoardComment> childs = theaterBoardService.getChildComments(no);
		TBoardCommentList list = TBoardCommentList
								 .builder()
								 .parentComments(parents)
								 .childComments(childs)
								 .build(); 
		
		return ResponseEntity.ok().body(list);
	}
	
	@PostMapping("/deleteGreatComment")
	@ResponseBody
	public ResponseEntity<Integer> deleteGreatComment(@RequestBody Map<String, Integer> request){
		int no = request.get("no");
		int commentNo = request.get("greatCommentNo");
		
		if(no == 0 || commentNo == 0) {
			return ResponseEntity.badRequest().build();// 값이 없는 경우 잘못된 요청 응답 반환
		}
		
		// table의 commentCount 구하기
		TheaterBoard board = theaterBoardService.getTheaterBoardByNo(no);
		// commentNo를 조상으로 갖고 있는 자손 댓글의 수 구하기
		int childCount = theaterBoardService.getTotalChildCount(commentNo);
		// update할 commentCount 구하기
		int commentCount = board.getCommentCount() - (childCount + 1);
		
		// commentCount update
		board.setCommentCount(commentCount);
		theaterBoardService.updateBoardComment(no, commentCount);
		
		// 자손 댓글 삭제
		theaterBoardService.childCommentsDelete(commentNo);
		
		// 해당 댓글 삭제
		theaterBoardService.greatCommentDelete(commentNo);
		
		return ResponseEntity.ok().body(commentCount);
	}
	
	@PostMapping("/deleteReComment")
	@ResponseBody
	public ResponseEntity<Integer> deleteReComment(@RequestBody Map<String, Integer> request){
		int no = request.get("no");
		int commentNo = request.get("commentNo");
		
		if(no == 0 || commentNo == 0) {
			return ResponseEntity.badRequest().build();// 값이 없는 경우 잘못된 요청 응답 반환
		}
		
		// table의 commentCount 구하기
		TheaterBoard board = theaterBoardService.getTheaterBoardByNo(no);
		// update할 commentCount 구하기
		int commentCount = board.getCommentCount() -1;
		
		// commentCount update
		board.setCommentCount(commentCount);
		theaterBoardService.updateBoardComment(no, commentCount);
		
		// 해당 댓글 삭제
		theaterBoardService.greatCommentDelete(commentNo);
		
		return ResponseEntity.ok().body(commentCount);
	}
	
	// 신고관련 
	@PostMapping("/report")
	public String reportBoard(ReportForm form, @AuthenticationPrincipal User user) {
    	log.info("입력 내용 -> {}", form.getReasonNo());
    	log.info("입력 내용 -> {}", form.getBoardNo());
    	log.info("입력 내용 -> {}", form.getReasonContent());
    	log.info("입력 내용 -> {}", user.getId());
		theaterBoardService.insertReport(form, user);
		
		TheaterBoard board = theaterBoardService.getTheaterBoardByNo(form.getBoardNo());
		int reportCount = board.getReportCount()+1;
		theaterBoardService.updateReportCount(form.getBoardNo(), reportCount);
		
		if (reportCount == 5) {
			String report = "Y";
			theaterBoardService.updateReport(form.getBoardNo(), report);
		}
		return "redirect:/board/theater/list";
	}

}
