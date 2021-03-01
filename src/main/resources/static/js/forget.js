$(function(){
    $(".feach-btn").click(feach);
});

function feach() {
    var btn = this;
    var count = 60;
    const countDown = setInterval(() => {
        if(count===60){
        $.post(
            CONTEXT_PATH + "/user/sendCode?p="+Math.random(),
            {"email":$("#email").val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code != 0) {
                    alert(data.msg);
                }
            }
        );
        }
        if (count === 0) {
        $(btn).text('重新发送').removeAttr('disabled');
        $(btn).css({
            background: '#ff9400',
            color: '#fff',
        });
        clearInterval(countDown);
    } else {
        $(btn).attr('disabled', true);
        $(btn).css({
            background: '#d8d8d8',
            color: '#707070',
        });
        $(btn).text(count + '秒后可重新获取');
    }
    count--;
}, 1000);
}
