$(() => {
	$(".inner-wrap .tab-list").on("click", "a", function(){
		$(".tab-cont-wrap .tab-cont").removeClass("on");
		$(".inner-wrap .tab-list li").removeClass("on");
		let target = $(this).attr("href");
		$(target).addClass("on");
		$(this).parent().addClass("on");
		return false;
	})
	
});