$(function () {
	
	const $request = $("input[name=request]");
	let join;
	$("#request-button").click(function(event) {
          event.preventDefault();
          let type= $(this).attr("data-button-type");
          if (type == 'plus') {
             join = 'Y';  
             $("#request-button").text('신청취소').attr('data-button-type', 'minus').removeClass('btn-outline-success').addClass('btn-success')      
             
         } else if (type == 'minus') {
            join = 'N';
            $("#request-button").text('파티신청').attr('data-button-type', 'plus').removeClass('btn-success').addClass('btn-outline-success')        
         }
          $request.attr('th:value', join);  
          
       
      
      /*
          // AJAX 요청
          $.ajax({
              url: '/board/party/changeRequest',
              method: "POST",
              data: $("#request-btn-form").serialize(),
              success: function(response) {
                  // 성공 시 업데이트된 내용을 특정 부분에 적용
                  if (type == 'plus') {
                  $("#like-button").text('신청취소').attr('data-button-type', 'minus').removeClass('btn-outline-success').addClass('btn-success')      
               } else if (type == 'minus') {
                  $("#like-button").text('파티신청').attr('data-button-type', 'plus').removeClass('btn-success').addClass('btn-outline-success')        
               }
              }
          });
       */
      });
})