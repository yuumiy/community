//在profile.html中，通过button的class属性获取对象
$(function(){
    $(".follow-btn").click(follow);
});


function follow() {
    var btn = this;
    if($(btn).hasClass("btn-info")) {
        // 关注TA    $(btn).prev()获取前一个结点的值，就是用户id
        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType":3,"entityId":$(btn).prev().val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        // $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType":3,"entityId":$(btn).prev().val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        //$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}