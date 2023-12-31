$(function() {
	
	const urlParams = new URLSearchParams(window.location.search);
	const locationNo = urlParams.get('locationNo');
	const theaterNo = urlParams.get('theaterNo');
	
	if (locationNo && theaterNo) {
		 selectTheater(locationNo, theaterNo);
	}
	
	// 폼에서 지역조회
	$("#theater").prop("disabled", true);
	
	let $selectLocation = $("#location").empty();
	$selectLocation.append(`<option value="" selected>지역선택</option>`)
	
	$.getJSON("/support/lost/getLocation", function(locations) {
		locations.forEach(function(loc) {
			let option = `<option value="${loc.no}" ${locationNo == loc.no ? 'selected' : ''}> ${loc.name}</option>`;
			$selectLocation.append(option);
		})
		
	})
	
	// 극장선택
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
	
	function selectTheater(locationNo, theaterNo) {
		
		let $selectTheater = $("#theater").empty();
		
		$selectTheater.append(`<option value="" selected disabled>극장선택</option>`)
		
		$.getJSON("/support/lost/getTheaterByLocationNo?locationNo="+ locationNo, function(theaters){
			theaters.forEach(function(thr) {
				let option = `<option value="${thr.no}" ${theaterNo == thr.no ? 'selected' : ''}> ${thr.name}</option>`;
				$selectTheater.append(option);
			})
			$("#theater").prop("disabled", false);
		})
	}
	
	// 폼 비번 4자리
    $(".pwnew").on("input", function() {
        // 입력값에서 숫자 이외의 문자 제거
        let numericValue = $(this).val().replace(/[^0-9]/g, '');

        // 4자리로 제한
        if (numericValue.length > 4) {
            numericValue = numericValue.slice(0, 4);
        }

        // 입력 필드에 반영
        $(this).val(numericValue);
    });
   
    // 폼 글자수 
	
	// 폼 알림창
	$("#btn-submit").on("click", function(event) {
		let checkbox = $('#chk').prop('checked');
		let location = $("#location").val();
		let theater = $("#theater").val();
		let guestname = $("input[name='guestName']").val();
		let guestemail = $("input[name='guestEmail']").val();
		let guestPassword = $("input[name='guestPassword']").val();
		let title = $("input[name='title']").val();
        let content = $("#textarea").val();
        
        if (checkbox == false) {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '개인정보 수집에 대한 동의가 필요합니다.'
            });
		} else if (location === null) {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '지역을 선택 해주세요.'
            });
        } else if (theater === null) {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '극장을 선택 해주세요.'
            });
        } else if (guestname === '') {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '이름을 입력 해주세요.'
            });
        } else if (guestemail === '') {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '이메일을 입력 해주세요.'
            });
        } else if (guestPassword === '') {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '비밀번호를 입력 해주세요.'
            });
        } else if (title === '') {
			event.preventDefault();
			 Swal.fire({
                icon: 'error',
                text: '제목을 입력 해주세요.'
            });
		} else if (content === '') {
	        event.preventDefault(); 
            Swal.fire({
                icon: 'error',
                text: '내용을 입력 해주세요.'
            });
        } else {
		        event.preventDefault(); 
			    Swal.fire({
			        icon: 'success',
			        text: '게시글이 등록되었습니다.',
			        showConfirmButton: true // 확인 버튼을 표시
			    }).then((result) => {
			        if (result.isConfirmed) {
			            $("#insertform").submit();
			        }
			    });
			}
    });
    
    // 삭제 버튼 띄우기
    $("#delete-btn").on("click", function(event) {
	    event.preventDefault();
		let no = $('[name=no]').val();
		
	    Swal.fire({
	        icon: 'warning',
	        title: '정말 삭제하시겠습니까?',
	        showCancelButton: true,
	        confirmButtonText: '네',
	        cancelButtonText: '아니오',
	    }).then((result) => {
	        if (result.isConfirmed) {
	              window.location.href = 'delete?no=' + no;      
	              
	        } else if (result.dismiss === Swal.DismissReason.cancel) {
	            
	        }
	    });
	});
	
	// 검색버튼 클릭했을 때
	$("#searchBtn").click(function() {
		$("input[name=page]").val(1);
		
		getLostList();
	});
	
	const params = new URLSearchParams(location.search);
	const defaultKeyword = params.get('keyword');
	if (defaultKeyword) {
		$("input[name=keyword]").val(defaultKeyword);
		getLostList();
	}
	
	// 폼 전송 이벤트
	$("#actionForm").on('submit', function(e) {
		e.preventDefault();
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
      let $pagination = $(".pagination");
      
      $.getJSON("/support/lost/list", {locationNo:locationNo, theaterNo:theaterNo, answered:answered, page:page,  keyword:keyword}, function(result) {
         
         // 총 건수 업데이트
         $('#totalCnt').text(result.pagination.totalRows);
         
         let lostList = result.lostList;
         let pagination = result.pagination;
         
         if (lostList.length === 0) {
            $tbody.append(`<tr><th colspan='5' style="text-align:center;">조회된 내역이 없습니다.</th></tr>`);
            $pagination.empty();
         } else {
            const tbodyHtml = lostList.map(function(lost, index) {
               let user = lost.user;
               let lostUser = '';
               if(user !== null){
                  lostUser = `
                        <input  type="hidden" name="id" value="${lost.user.id}"/>
                  `
               }
               return `
                  <tr>
                  `
                  +
                     lostUser
                     +
                        `<input  type="hidden" name="guestPassword" value="${lost.guestPassword}"/>
	                      <td>${lost.no}</td>
	                        <td>${lost.theater.name}</td>
	                        <td style="text-align:left;">
	                           <a class="text-black text-decoration-none"
	                              href="/support/lost/detail?no=${lost.no}"
	                              data-no="${lost.no}">
	                              ${lost.title }
	                           </a>
	                        </td>
	                        <td>${lost.answered == 'Y' ? '답변완료' : '미답변'}</td>
	                        <td>${lost.createDate}</td>
	                    </tr>
	                  `
            }).join("\n");
            
            $tbody.html(tbodyHtml);
            $pagination.html(renderPagination(pagination));
         }
      })
   }
   
   $("#table-lost tbody").on("click", "a", function(event) {
      event.preventDefault();
      const id = $(this).closest('tr').find("input[name=id]").val();
      const userId = $("input[name=userId]").val(); 
      const guestPassword =  $(this).closest('tr').find("input[name=guestPassword]").val(); 
      
      // 로그인한 사람과 게시글의 아이디가 서로 다를 때
      if(userId && id !== userId){
         Swal.fire({
                icon: 'warning',
                text: '다른 사용자의 분실물 문의 내용을 볼 수 없습니다.',
            });
      }
      
      // 비회원일때
      if(!userId){
		 // 회원이 작성한 글을 클릭했을 때
		 if (id != null) {
			Swal.fire({
                icon: 'warning',
                text: '비회원은 회원의 문의 내용을 볼 수 없습니다.',
            }); 
		 } else {
	         Swal.fire({
				  title: '게시글 확인',
				  icon: 'info',
				  text: '글 작성시 입력한 비밀번호를 입력해주세요.',
				  input: 'text',
				  customClass: {
				    validationMessage: 'my-validation-message'
				  },
				  showCancelButton: true,
				    confirmButtonText: '확인',
				    showLoaderOnConfirm: true,
				    cancelButtonText: '취소',
				    preConfirm: (value) => {
				      return new Promise((resolve) => {
				        if (value !== guestPassword) {
				          Swal.showValidationMessage(
				            '<i class="fa fa-info-circle"></i> 비밀번호가 일치하지 않습니다. 다시 입력해주세요.'
				          );
				          resolve();
				        } else {
				          resolve();
			         	  let lostNo = $(this).attr("data-no");
				          $("#actionForm input[name=no]").val(lostNo);
				         
				          document.querySelector("#actionForm").submit();
			      
				        }
				      });
				    }
				  });
		}
}
      
      if (id && userId && id === userId){
            
         let lostNo = $(this).attr("data-no");
         $("#actionForm input[name=no]").val(lostNo);
         
         document.querySelector("#actionForm").submit();
      }

   })
})









