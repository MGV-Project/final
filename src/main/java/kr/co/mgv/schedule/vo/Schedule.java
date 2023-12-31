package kr.co.mgv.schedule.vo;

import java.util.Date;

import org.apache.ibatis.type.Alias;

import kr.co.mgv.movie.vo.Movie;
import kr.co.mgv.theater.vo.Screen;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Alias("schedule")
public class Schedule {

	private Movie movie;
	private Screen screen;
	private String id;
	private Date date;
	private int turn;
	private Date start;
	private Date end;
	private String info;
	private int remainingSeats;
	private int seats;

}
