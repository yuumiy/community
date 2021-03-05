$(function(){
	$("form").submit(check_data);
	$("input").focus(clear_error);
});

function check_data() {
	var pwd1 = $("#password").val();
	var pwd2 = $("#confirmPassword").val();
	if(pwd1 != pwd2) {
		$("#confirmPassword").addClass("is-invalid");
		return false;
	}
	return true;
}

function clear_error() {
	$(this).removeClass("is-invalid");
}