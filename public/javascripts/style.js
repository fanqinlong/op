
			$(function(){
				$(".btn").click(function(){$(".fm").submit()});
				$(".fm").validate({
					rules:{
						'email':{
							required:true,
							email:true
						},
						'password':{
							required:true,
							rangelength:[6,18]
						}
					},
					messages:{
						email:{
							required:'����������ʼ���ַ',
							email:'�����ʼ���ʽ����'
						},
						password:{
							required:"����������",
							rangelength:"���볤��Ϊ6~18λ"
						}

					}
    		});
    	});
		
		function change(){
			$("#fir").fadeOut(1000);
			$("#denglu_page").addClass("hide");
			$("#send").fadeIn(1000);
			$("#zuce_page").removeClass("hide");	

		}
		function change_page(){
			$("#fir").fadeIn();
			$("#denglu_page").removeClass("hide");
			$("#send").fadeOut();	
			$("#zuce_page").addClass("hide");
		}
	