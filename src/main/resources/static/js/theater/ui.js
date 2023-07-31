$(() => {
	
	/*상세정보 탭 이동*/
	$(".inner-wrap .tab-list").on("click", "a", function(){
		$(".tab-cont-wrap .tab-cont").removeClass("on");
		$(".inner-wrap .tab-list li").removeClass("on");
		let target = $(this).attr("href");
		$(target).addClass("on");
		$(this).parent().addClass("on");
		return false;
	})
	/*<!-- 카카오맵 -->*/				
	var mapContainer = document.getElementById('map'), // 지도를 표시할 div 
	mapOption = {
	    center: new kakao.maps.LatLng(37.5642135, 127.0016985), // 지도의 중심좌표
	    level: 3 // 지도의 확대 레벨
	};  
	
	//지도를 생성합니다    
	var map = new kakao.maps.Map(mapContainer, mapOption); 
	
	//주소-좌표 변환 객체를 생성합니다
	var places = new kakao.maps.services.Places();
	var geocoder = new kakao.maps.services.Geocoder();
	
	
	//주소로 좌표를 검색합니다
	geocoder.addressSearch('서울특별시 서초구 서초대로 77길 3 (서초동) 아라타워 8층', function(result, status) {
	
	// 정상적으로 검색이 완료됐으면 undifined
	
	 if (status === kakao.maps.services.Status.OK) {
	
		 
		var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
		var lat = result[0].y; // 위도
		var lng = result[0].x; // 경도
	    // 결과값으로 받은 위치를 마커로 표시합니다
	    var marker = new kakao.maps.Marker({
	        map: map,
	        position: coords
	    });
	
	    // 인포윈도우로 장소에 대한 설명을 표시합니다
	    var infowindow = new kakao.maps.InfoWindow({
	        content: '<div style="width:300px;text-align:center;padding:6px;"><%=theater.getAddress()%><button id="map-btn" class="border-0" style=" border-radius: 15px;">길찾기</button></div>'
	    });
	    infowindow.open(map, marker);
	
	    // 지도의 중심을 결과값으로 받은 위치로 이동시킵니다
	    map.setCenter(coords);
	    var name = '<%=theater.getName()%>'; 
	    var mapbtn = document.getElementById('map-btn');
		mapbtn.addEventListener("click", function() {
			window.open("https://map.kakao.com/link/to/"+name+","+lat+","+lng,
					"빠른길찾기",
			        "width=800, height=1200, top=50, left=50"
			        )
		});
	} 
	});
	/*<!-- 카카오맵 -->*/
});