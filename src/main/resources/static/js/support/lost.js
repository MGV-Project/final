$(function() {
	
	$("#theater").prop("disabled", true);
	
	let $selectLocation = $("#location").empty();
	$selectLocation.append(`<option value="" selected disabled>지역선택</option>`)
	
	$.getJSON("/support/lost/getLocation", function(locations) {
		locations.forEach(function(loc) {
			let option = `<option value="${loc.no}"> ${loc.name}</option>`;
			$selectLocation.append(option);
		})
		
	})
	
	$("#location").change(function() {
		$("#theater").prop("disabled", false);
		
		let locationNo = $(this).val();
		let $selectTheater = $("#theater").empty();
		
		$selectTheater.append(`<option value="" selected disabled>극장선택</option>`)
		
		$.getJSON("/support/lost/getTheaterByLocationNo?locationNo="+ locationNo, function(theaters){
			theaters.forEach(function(thr) {
				let option = `<option value="${thr.no}"> ${thr.name}</option>`;
				$selectTheater.append(option);
			})
		})
		
	});
	
	// 검색버튼 클릭했을 때
	$("#searchBtn").click(function() {
		$("input[name=page]").val(1);
		
		getLostList();
	});
	
	// 페이저번호클릭했 떄
	$('.pagination').on('click', '.page-number-link', function(event) {
		event.preventDefault();
		let page = $(this).attr("data-page");
		
		// 모든 페이지 번호 링크에서 active 클래스 제거
        $('.page-number-link').removeClass('active');

        // 클릭한 페이지 번호에만 active 클래스 추가
        $(this).addClass('active'); 
		
		$("input[name=page]").val(page);
		
		getLostList();
	})	
	
	function getLostList() {
		// form의 값 조회
		let locationNo = $("select[name=locationNo]").val();
		let theaterNo = $("select[name=theaterNo]").val();
		let answered = $("select[name=answered]").val();
		let page = $("input[name=page]").val();
		let keyword = $("input[name=keyword]").val();
		
		let $tbody = $(".lostList ").empty()
		let $pagination = $(".pagination").empty();
		
		$.getJSON("/support/lost/list", {locationNo:locationNo, theaterNo:theaterNo, answered:answered, page:page,  keyword:keyword}, function(result) {
			
			// 총 건수 업데이트
			$('#totalCnt').text(result.pagination.totalRows);
			
			let lostList = result.lostList;
			let pagination = result.pagination;
			
			if (lostList.length === 0) {
				$tbody.append(`
					<tr><th colspan='5' style="text-align:center;">조회된 내역이 없습니다.</th></tr>
					`
					);
			} else {
				lostList.forEach(function(lost, index) {
					let content = `
						<tr>
						 	<td >${lost.no}</td>
				            <td >${lost.theater.name}</td>
				            <td style="text-align:left;">${lost.title}</td>
				            <!-- 
				            <th scope="row">
				            	<a href="" class="btn-layer-open moveBtn">분실물 문의</a>
				            </th>
				            -->
				            <td >${lost.answered == 'Y' ? '답변완료' : '미답변'}</td>
				            <td >${lost.updateDate}</td>
			           </tr>
					`
					$tbody.append(content);
				})
				
				for (let i = pagination.beginPage; i <= pagination.endPage; i++) {
				let content = `
					 <li class="page-item">
                    	<a href="list?page=${i}" 
                      	 	class="page-link page-number-link ${i == pagination.page ? 'active' : ''}"
                       		data-page="${i}">${i}</a>
               		 </li>
				`;
				$pagination.append(content);	
				}
			}
		})
		
		
	}
	
})








