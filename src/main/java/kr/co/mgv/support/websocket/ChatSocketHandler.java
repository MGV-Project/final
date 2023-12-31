package kr.co.mgv.support.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.mgv.support.vo.ChatMessage;

public class ChatSocketHandler extends TextWebSocketHandler {
	// 자바객체 -> json, json -> 자바객체 변환
	private ObjectMapper objectMapper = new ObjectMapper();
	// 관리자 웹소켓세션객체를 저장하는 변수
	private WebSocketSession adminSession;
	// 사용자 웹소켓세션을 저장하는 변수[{userId:'sun', "session": 웹소켓세션}, {userId:'ohhgeo', "session": 웹소켓세션}, {userId:'hong', "session": 웹소켓세션},]
	private List<Map<String, Object>> userSessions = new ArrayList<>();
	// 현재 상담중인 사용자세션과 관리자세션객체를 저장
	private Map<String, Object> room = new HashMap<>();
	
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("웹소켓 연결요청이 접수됨..............");
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("웹소켓 연결이 종료됨..............");
		String userId = findUser(session);
		
		if (!"admin".equals(userId)) {
			removeSession(session);
			broadcast();
			
			ChatMessage responseMessage = new ChatMessage();
			responseMessage.setCmd("exit");
			responseMessage.setUserId(userId);
			
			sendMessage(adminSession, responseMessage);			
		} 
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.out.println("웹소켓 데이터 전송 중 오류가 발생함.............");
		String userId = findUser(session);
		
		if (!"admin".equals(userId)) {
			removeSession(session);
			broadcast();
			
			ChatMessage responseMessage = new ChatMessage();
			responseMessage.setCmd("exit");
			responseMessage.setUserId(userId);
			
			sendMessage(adminSession, responseMessage);			
		} 
	}
	
	protected void handleTextMessage(WebSocketSession session, TextMessage  message) throws Exception {
		System.out.println("수신 메세지 -> ["+message.getPayload()+"]");
		ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
		
		String cmd = chatMessage.getCmd();
		if("req".equals(cmd)) {
			reqChat(session, chatMessage);
		} else if ("ready".equals(cmd)) {
			readyChat(session, chatMessage);
		} else if ("start".equals(cmd)) {
			startChat(session, chatMessage);
		} else if ("msg".equals(cmd)) {
			message(session, chatMessage);
		} else if ("cancel".equals(cmd)) {
			cancelReq(session, chatMessage);
		} else if ("stop".equals(cmd)) {
			stopChat(session, chatMessage);
		}
		
	}
	
	// 요청
	//		상담요청 - {cmd:req, userId:sun} 메세지 처리
	// 응답
	//		대기자 명단 - {cmd:wait, waitings:[sun, ohhgeo, hong]}
	public void reqChat(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		String userId = chatMessage.getUserId();
		if (userId != null) {
			userSessions.add(Map.of("userId", userId, "session", session));
			broadcast();
		}
	}
	
	// 요청
	//		관리자 준비 요청 - {cmd:ready}
	// 응답
	//		대기자 명단 - {cmd:wait, waitings:[sun, ohhgeo, hong]}
	public void readyChat(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		adminSession = session;
		
		broadcast();
	}
	
	// 요청
	//		상담 시작요청 -{cmd:start: userId:sun}
	// 응답
	//		채팅방정보 - {cmd:start, roomId:xxxxx}
	public void startChat(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		String userId = chatMessage.getUserId();
		
		WebSocketSession userSession = findUserSession(userId);

		String uuid = UUID.randomUUID().toString();
		room = Map.of("roomId", uuid, "user", userSessions, "admin", adminSession);
		
		ChatMessage responseMessage = new ChatMessage();
		responseMessage.setCmd("start");
		responseMessage.setUserId(userId);
		responseMessage.setRoomId(uuid);
		
		sendMessage(adminSession, responseMessage);
		sendMessage(userSession, responseMessage);
	}
	
	// 요청
	//		상담종료 - {cmd:stop, roomId:xxxx, userId:sun}
	// 응답
	//		종료된 채팅방정보 - {cmd:stop, roomId:xxx}
	public void stopChat(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		String roomId = chatMessage.getRoomId();
		String userId = chatMessage.getUserId();
		
		ChatMessage responseMessage = new ChatMessage();
		responseMessage.setCmd("stop");
		responseMessage.setRoomId(roomId);
		responseMessage.setUserId(userId);
		
		WebSocketSession userSession = findUserSession(userId);
		
		sendMessage(adminSession, responseMessage);
		sendMessage(userSession, responseMessage);
		
		room = null;
		removeUserSession(userId);
		broadcast();
	}
	
	// 요청
	//		상담취소 - {cmd:cancel, userId:hong}
	// 응답
	//		대기자 명단 - {cmd:wait, waitings:[sun, ohhgeo, hong]}
	public void cancelReq(WebSocketSession session, ChatMessage chatMessage)  throws Exception {
		String userId = chatMessage.getUserId();
		removeUserSession(userId);
		
		broadcast();
	}
	
	// 요청
	//		메세지 전송 요청 - {cmd:msg, userId:sun, roomId:xxx,  text:"xxxxxxxxxxxxx"}
	// 응답
	//		메세지 응답 - {cmd:msg, roomId:xxx,  text:"xxxxxxxxxxxxx"}
	public void message(WebSocketSession session, ChatMessage chatMessage) throws Exception {
		String roomId = chatMessage.getRoomId();
		String userId = chatMessage.getUserId();
		String receiverId = chatMessage.getReceiverId();
		String text = chatMessage.getText();
		
		WebSocketSession userSession = null;
		if (userId.equals("admin")) {
			userSession = findUserSession(receiverId);
		} else {
			userSession = findUserSession(userId);
		}
		
		ChatMessage responseMessage = new ChatMessage();
		responseMessage.setUserId(userId);
		responseMessage.setCmd("msg");
		responseMessage.setRoomId(roomId);
		responseMessage.setText(text);		
		
		sendMessage(userSession, responseMessage);
		sendMessage(adminSession, responseMessage);
	}
	
	// 모든 고객들에게 대기자 목록과 자신의 위치를 보내는 메소드
	// 새고객이 접속할 때, 종료했을 때, 오류가 발생했을 때 모두 대기자 목록에 변화가 생기는 경우임으로 대기자 목록 및 자신의 위치정보를 모든 고객에게 발송
	private void broadcast() throws Exception {
		
		// 대기자 정보를 보내는 메세지 객체 생성
		// 대기자 목록을 저장한다.
		ChatMessage responseMessage = new ChatMessage();
		responseMessage.setCmd("wait");
		responseMessage.setWaitings(watingList());
		
		for (Map<String, Object> map : userSessions) {
			String userId = (String) map.get("userId");
			WebSocketSession userSession = (WebSocketSession) map.get("session");

			// 대기자 목록 중에서 고객의 위치를 조회한다. 메세지에 저장한다.
			int position = getPosition(userId);
			responseMessage.setPosition(position);
			
			// 고객에게 메세지를 보낸다.
			sendMessage(userSession, responseMessage);
		}
		
		// 관리자에게도 보낸다.
		sendMessage(adminSession, responseMessage);
	}
	
	private int getPosition(String currentId) {
		return watingList().indexOf(currentId);
	}
	
	private void sendMessage(WebSocketSession session, ChatMessage message) throws Exception {
		if (session != null) {
			try {
				String jsonMessage = new ObjectMapper().writeValueAsString(message);
				session.sendMessage(new TextMessage(jsonMessage));
			} catch (Exception ex) {}
		}
	}

	private WebSocketSession findUserSession(String userId) {
		for (Map<String, Object> map : userSessions) {
			String savedId = (String) map.get("userId");
			if (savedId.equals(userId)) {
				return (WebSocketSession) map.get("session");
			}
		}
		
		return null;
	}
	
	private String findUser(WebSocketSession session) {
		for (Map<String, Object> map : userSessions) {
			WebSocketSession savedSession = (WebSocketSession) map.get("session");
			String savedId = (String) map.get("userId");
			if (savedSession == session) {
				return savedId;
			}
		}
		
		return null;
	}
	
	private void removeUserSession(String userId) {
		for (Map<String, Object> map : userSessions) {
			String savedId = (String) map.get("userId");
			if (savedId.equals(userId)) {
				map.remove(userId);
			}
		}
	}
	
	private List<String> watingList() {
		return userSessions.stream()
				.map(item -> (String)item.get("userId"))
				.collect(Collectors.toList());
	}
	
	private void removeSession(WebSocketSession currentSession) throws Exception {
		Iterator<Map<String, Object>> iterator = userSessions.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> map = iterator.next();
			WebSocketSession session = (WebSocketSession) map.get("session");
			
			if (session == currentSession) {
				iterator.remove();
			}			
		}
		
	}
}












