package kr.co.mgv.store.vo;

import kr.co.mgv.user.vo.User;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Data
@Alias("GiftTicket")
public class GiftTicket {

    private long no;
    private User user;
    private int bookingNo;
    private String isUsed;
    private Date createDate;
    private Date updateDate;
}
