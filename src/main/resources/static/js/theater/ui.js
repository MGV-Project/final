$(() => {
	let saveCnt = 0;
	refrashtheater();
	getNoticeList();
	
	function getNoticeList() {
		
		let categoryNo =23;
		let page
		let locationNo
		let theaterNo=$("p.name").attr("data-theater-no");
		let keyword 
		const $noticeList = $(".board-list tbody").empty()
		$.getJSON("/support/notice/list", {catNo:categoryNo, locationNo:locationNo, theaterNo:theaterNo,page:page,  keyword:keyword}, function(result) {
			if(result.noticeList.length==0){
				const htmlcontent = `
						<tr>
							<td colspan='4'> 공지사항이 없습니다.</td>
						</tr>
				`;
					$noticeList.append(htmlcontent);
				}
			
			result.noticeList.forEach(function(notice,index){
				
				
				const htmlcontent = `
						<tr>
							<td>${notice.theater.name}</td>
							<th scope="row"><a
								href="/support/notice/detail?locationNo=&no=${notice.no}&catNo=23&page=1&keyword="
								title="${notice.title} 상세보기"> ${notice.title} </a></th>
							<td>${notice.location.name}</td>
							<td>${dayjs(notice.createDate).format("YYYY.MM.DD")}</td>
						</tr>
				`;
				if(index<5){
					$noticeList.append(htmlcontent);
				}
			})
			
		})
	}
	// 극장리스트
	function refrashtheater(){
		$('#location').val($('#selectBoxId option:first').val());
		$('#theater').val($('#selectBoxId option:first').val());
		
		let $favtheater = $("#theater")
		$favtheater.html('<option value="" selected disabled>극장선택</option>');
		$.getJSON("/theater/theaterList", function(locations) {
			locations.forEach(function(location) {
				let contents = '';
				// 로케이션 네임으로 입력할 요소를 찾아서
				let $theatersarea = $('.sel-city:contains(' + location.name + ') + div ul');
				let favtheatercontent = ``;
				let currentlocationno = $("#location").val(); 
				location.theaters.forEach(function(theater) {
					// html컨텐츠 만들고
					contents += `
					<li data-brch-no="${theater.no}">
					<a href="/theater/detail?brchNo=${theater.no}" 
					title="${theater.name} 상세보기">${theater.name}</a></li>
					`
					favtheatercontent = `<option class="${currentlocationno == location.no ? '':'d-none'}" data-location="${location.no}" value="${theater.no}">${theater.name}</option>`
					$favtheater.append(favtheatercontent);
				})
				// 찾은 요소에 대입
				$theatersarea.html(contents);
			})
			// 선호극장
			$.ajax({
				url: "/theater/favorite",
				type: "GET",
				success: function(theaters) {
	
					// 여기에서 원하는 동작 수행
					// 홈의 선호극장
					let $container = $(".user-theater .theater-circle");
					$container.empty();
					$('.theater-choice-list .bg').empty();
					saveCnt = theaters.length; 
					if (theaters.length != 0) {
						theaters.forEach(function(theater, index) {
							let htmlContent = `<li><a href="/theater/detail?brchNo=${theater.no}"
											title="${theater.name} 상세보기">${theater.name}</a></li>
								
									`
							$container.append(htmlContent);
							if (theater.no == $("p.name").attr("data-theater-no")) {
								$(".btn-util .block button").addClass("on");
							}
	
							let htmlContents = `<span class="favorit-theater"><i class="iconset ico-favo-theater"></i></span>`
							$(".theater-place .theater-list li[data-brch-no=" + theater.no + "]").prepend(htmlContents);
							
							makecircle(index, theater.no, theater.name);
	
						})
	
					}
				}
			})
		})
	}
	
	// 선호극장관리 극장리스트 변경
	$("#location").on("change",function(){
		let locationno = $(this).val();
		$("#theater option").addClass("d-none");
		if(locationno != ''){
			$("#theater option[data-location="+locationno+"]").removeClass("d-none");
		}
	})
	
	// 추가버튼
	$("#button-add-fav").off().on("click",function(){
		let $selectedtheater = $("#theater").find('option:selected')
		let idx  = $('#favorBrchReg .bg').has('.circle').length;
			// 지점 선택 확인
		if ($selectedtheater.val() == '') {
			Swal.fire({
	            icon: 'error',
	            text: '지점을 선택해 주세요.',
	        })
			return;
		}

		// 중복 지점 확인
		if ($('#favorBrchReg div[data-brch-no="'+$selectedtheater.val()+'"]').length != 0) {
			Swal.fire({
	            icon: 'error',
	            text: '이미 선택하신 지점 입니다.',
	        })
			return;
		}

		// 최대 선택 갯수 확인
		if (idx == 3) {
			Swal.fire({
	            icon: 'error',
	            text: '최대 3개 까지 선택 하실 수 있습니다.'
	        })
			return;
		}

		// 값 설정
		makecircle(idx, $selectedtheater.val(), $selectedtheater.text());
	})
	
	// 삭제 버튼 클릭
	$('#favorBrchReg .theater-choice-list').off().on('click', '.del', function() {

		var $lObj = $('#favorBrchReg .bg').has($('#favorBrchReg .del').last());
		var $tObj = $('#favorBrchReg .bg').has(this);

		$lObj.after($tObj);
		$tObj.empty();
	});
	
	// 취소버튼 클릭
	$("button.close-layer").off().on("click", function(){
		refrashtheater();
		togglefavmodal();
		
	})
		
	// 등록
	$('#favorBrchReg .btn-group-fixed').off().on('click', '.purple', function() {
		let param, arr = new Array();

		let $obj = $('#favorBrchReg .theater-choice-list .circle');

		// 값 설정
		$.each($obj, function(i, obj) {
			arr.push(param = new Object());

			param.theaterNo = $(this).data('brchNo');
			param.rank  = i+1;
			param.theaterName = $(obj).find('p').html();
		});

		
		// 저장값이 있었다 전체삭제 했을 경우
		if ($obj.length == 0 && saveCnt > 0) {

			$.each($('#favorBrchReg .theater-choice-list .bg'), function(i) {
				arr.push(param = new Object());

				param.theaterNo = '';
				param.theaterName = '';
				param.rank  = i+1;
			});
		}
		// 등록요청
		$.ajax({
            type: 'POST',
			contentType   : 'application/json',
			url           : '/theater/favorite',
			data          : JSON.stringify(arr),
			success       : function (data, textStatus, jqXHR) {
				if (data == 'undefined') {
					Swal.fire({
						icon: 'error',
						text: '로그인된 사용자가 아닙니다. 로그인 해주세요.',
					}).then((result) => {
						if (result.isConfirmed) {
							$('.btn-login').click();
							$('#favorBrchReg .close-layer').click();
						}

					})
				} else {

					Swal.fire({
						icon: 'success',
						text: '등록 되었습니다.',
					})

					$('#favorBrchReg .close-layer').click();
				}
			}
		}).fail(handleAjaxError);
	});	
	

	
		
	// 선호극장 모달 채우기
	function makecircle(index, theaterno, theatername){
		let $bg = $($("#divFavorBrch").html());
		$bg.find('p').html(theatername);
		$bg.attr('data-brch-no', theaterno);

		$('.theater-choice-list .bg').eq(index).html($bg);
	} 
	
	// 극장상세 선호극장 on/off
	$("#favorBrch").on('click',function() {
		const $theaterSelector = $("p.name");
		let param = new Object();
		param.theaterNo = $theaterSelector.attr("data-theater-no");
		param.theaterName = $theaterSelector.text();
		param.rank  = saveCnt+1;
		if(saveCnt === 3){
			Swal.fire({
				icon: 'error',
				text: '최대 3개 까지 선택 하실 수 있습니다.'
			})
			return false;
		}
		$.ajax({
			type: 'POST',
			contentType: 'application/json',
			url: '/theater/favorite/registfavorite',
			data: JSON.stringify(param),
			success: function(data, textStatus, jqXHR) {
				if (data == 'undefined') {
					Swal.fire({
						icon: 'error',
						text: '로그인된 사용자가 아닙니다. 로그인 해주세요.',
					}).then((result) => {
						if (result.isConfirmed) {
							$('.btn-login').click();
						}

					})
				} 
				if (data == 'success') {

					Swal.fire({
						icon: 'success',
						text: '등록 되었습니다.',
					})
					$("#favorBrch").addClass("on");
				}
				if (data == 'delete') {

					Swal.fire({
						icon: 'warning',
						text: '취소 되었습니다.',
					})
					$("#favorBrch").removeClass("on");
				}
			}
			}).fail(handleAjaxError);
		
		
	})
	





	$(".theater-place button").on("click", function() {
		$(this).closest("li").addClass("on");
		$(this).closest("li").siblings().removeClass("on");
	})

	$(".area-depth1 li").hover(function() {
		$(this).addClass("on");
	}, function() {
		$(this).removeClass("on");
	})

	// 선호 극장 모달 on/off
	function togglefavmodal(){
		$("#favor_theater_setting").toggleClass("on"),
		$("#favorBrchReg").toggleClass("target")
		$(".bg-modal").toggle();
	}

	// 선호극장 모달 on
	$(".user-theater .control-fav").on("click", function(event) {
		event.preventDefault()
		togglefavmodal();
	});

	// 선호극장 모달 off
	$(".btn-modal-close").on("click", function(event) {
		event.preventDefault()
		$("button.close-layer").click();
	});
	
	function handleAjaxError() {
		Swal.fire({
			icon: 'error',
			text: "오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
		});
	}
	
	/*카카오맵 약도*/
	$(".location-map-btn button").click(function() {

		let container = document.getElementById('map');
		let options = {
			center: new kakao.maps.LatLng(33.450701, 126.570667),
			level: 3
		};

		let map = new kakao.maps.Map(container, options);

		// 주소-좌표 변환 객체를 생성합니다
		var geocoder = new kakao.maps.services.Geocoder();
		let address = $("#theater-address").text()
		let locationname = $("p.name").text();
		// 주소로 좌표를 검색합니다
		geocoder.addressSearch(address, function(result, status) {

			// 정상적으로 검색이 완료됐으면 
			if (status === kakao.maps.services.Status.OK) {

				var coords = new kakao.maps.LatLng(result[0].y, result[0].x);

				// 결과값으로 받은 위치를 마커로 표시합니다
				var marker = new kakao.maps.Marker({
					map: map,
					position: coords
				});
				marker.setMap(map);

				// 인포윈도우로 장소에 대한 설명을 표시합니다
				var customOverlay = new kakao.maps.CustomOverlay({
					map: map,
					position: coords,
					content: `<div class="card card-body p-2" style="width:150px;text-align:center;">
		            			<span>${locationname}</span>
		            			<div class="px-auto py-2">
		            				<a class="btn btn-primary btn-sm " href="https://map.kakao.com/link/to/${locationname},${result[0].y},${result[0].x}" style="width:100px; color:white" target="_blank"> 빠른 길찾기</a>
		            			</div>
		           			</div>`,
					yAnchor: 1.5
				});


				// 지도의 중심을 결과값으로 받은 위치로 이동시킵니다
				map.setCenter(coords);
			}
		});
	})









	









})