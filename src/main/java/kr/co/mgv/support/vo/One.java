package kr.co.mgv.support.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.co.mgv.theater.vo.Location;
import kr.co.mgv.theater.vo.Theater;
import kr.co.mgv.user.vo.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class One {

	private int no;
	private String title;
	private String content;
	private String answered;
	private String deleted;
	private String guestName;
	private String guestEmail; 
	private int guestPassword;
	private String fileName;
	@JsonFormat(pattern = "yyyy.MM.dd")
	private Date updateDate;
	@JsonFormat(pattern = "yyyy.MM.dd")
	private Date createDate;
	private User user;
	private Location location;
	private Theater theater;
	private SupportCategory category;
	
	@Builder
	public One(int no, String title, String content, String answered, String deleted, String guestName,
			String guestEmail, int guestPassword, String fileName, Date updateDate, Date createDate, User user,
			Location location, Theater theater, SupportCategory category) {
		super();
		this.no = no;
		this.title = title;
		this.content = content;
		this.answered = answered;
		this.deleted = deleted;
		this.guestName = guestName;
		this.guestEmail = guestEmail;
		this.guestPassword = guestPassword;
		this.fileName = fileName;
		this.updateDate = updateDate;
		this.createDate = createDate;
		this.user = user;
		this.location = location;
		this.theater = theater;
		this.category = category;
	}
	
	public String getHtmlContent() {
		if (content == null) {
			return null;
		}
		return content.replace(System.lineSeparator(), "<br />");
	}
	
	
}








