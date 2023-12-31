package kr.co.mgv.admin.theater.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.mgv.movie.service.MovieService;
import kr.co.mgv.movie.vo.Movie;
import kr.co.mgv.theater.dto.ScreenDto;
import kr.co.mgv.theater.service.TheaterService;
import kr.co.mgv.theater.vo.Screen;
import kr.co.mgv.theater.vo.Theater;
import kr.co.mgv.theater.vo.TheaterFacility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@Secured({"ROLE_ADMIN"})
@RequestMapping("/admin/theater")
@RequiredArgsConstructor
public class AdminTheaterController {

	private final TheaterService theaterService;
	private final MovieService movieService;
	
    @GetMapping("/list")
    public String list() {
        return "view/admin/theater/list";
    }
    
    @GetMapping("/schedule/register")
    public String form() {
        return "view/admin/theater/schedule/form";
    }
    
    @GetMapping("/schedule/delete")
    public String deleteForm() {
    	return "view/admin/theater/schedule/deleteForm";
    }
    
    @GetMapping("/regist")
    public String registForm() {
    	return "view/admin/theater/form/regist";
    }
    
    @GetMapping("/screen/regist")
    public String registScreenForm() {
    	return "view/admin/theater/screen/regist";
    }
    
    @PostMapping("/screen/regist")
    @ResponseBody
    public Map<String, Object> registScreen(@RequestBody ScreenDto screenDto) {
    	try {
    		theaterService.registScreen(screenDto);
    		return Map.of("status", "success");
    	}catch (Exception e) {
    		log.info("error massage->{}",e.getMessage());
    		return Map.of("status", "fail", "message", "등록 중 오류가 발생하였습니다");
    	}
    }
    
    @PostMapping("/screen/modify")
    @ResponseBody
    public Map<String, Object> modifyScreen(@RequestBody ScreenDto screenDto) {
    	try {
    		theaterService.modifyScreen(screenDto);
    		return Map.of("status", "success");
    	}catch (Exception e) {
    		log.info("error massage->{}",e.getMessage());
    		return Map.of("status", "fail", "message", "등록 중 오류가 발생하였습니다");
    	}
    }
    
    @GetMapping("/screen/modify")
    public String modifyScreenForm() {
    	return "view/admin/theater/screen/modify";
    }
    
    @GetMapping("/screen/screenList")
    @ResponseBody
    public List<Screen> getScreenList(int theaterNo) {
    	return theaterService.getScreenlist(theaterNo);
    }
    
    @GetMapping("/modify")
    public String modifyForm(Model model,@RequestParam int theaterNo) {
    	
    	List<TheaterFacility> facilities = theaterService.getFacilities();
    	model.addAttribute("facilities", facilities);
    	model.addAttribute("theaterNo", theaterNo);
    	
    	return "view/admin/theater/form/modify";
    }
    
    @PostMapping("/modify")
    @ResponseBody
    public Map<String, Object> modifyTheater(@RequestBody Theater theater) {
    	try{
    		theaterService.modifyTheater(theater);
    		return Map.of("status", "success");
    	}catch (Exception e) {
    		return Map.of("status", "fail", "message", "수정 중 오류가 발생하였습니다");
    	}
    }
    
    @PostMapping("/regist")
    @ResponseBody
    public Map<String, Object>  registTheater(@RequestBody Theater theater) {
    	try{
    		theaterService.registTheater(theater);
    		return Map.of("status", "success");
    	}catch (Exception e) {
    		return Map.of("status", "fail", "message", "등록 중 오류가 발생하였습니다");
		}
    }
    
    @GetMapping("/registform")
    @ResponseBody
    public List<TheaterFacility> getFacilities(){
    	return theaterService.getFacilities();
    }
    
    @GetMapping("/detail")
    @ResponseBody
    public Theater getTheaterDetail(@RequestParam int theaterNo) {
     	Theater theater = theaterService.getTheaterDetail(theaterNo);
     	return theater;
    }
    
    @GetMapping("/schedule/list")
    public String scheduleList(Model model,@RequestParam int theaterNo) {
    	model.addAttribute("theaterNo", theaterNo);
    	return "view/admin/theater/schedule/list";
    }
    
    @GetMapping("/theaterList")
    @ResponseBody
    public List<Theater> getTheaters(@RequestParam int locationNo){
    	List<Theater> theaterList = theaterService.getTheaterlist(locationNo);
    	return theaterList;
    }
    
    @GetMapping("/screenList")
    @ResponseBody
    public List<Screen> getscreenList(@RequestParam int theaterNo){
    	List<Screen> screens = theaterService.getScreenlist(theaterNo);
    	return screens;
    }
    
    @GetMapping("/movieList")
    @ResponseBody
    public List<Movie> getMovieList(){
    	return movieService.getAllMovies();
    }
    
}
