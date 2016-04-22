
//微信统一下单
function payUnifiedorder(tradeType,orderId,orderCreateAt){
    var d = new Date();
    var n = d.getTime();
    if(n-orderCreateAt>=86400000){
        alert("您的订单已经超时自动取消");
        return ;
    }
    $.ajax({
        type: 'GET',
        url: "/client/pay/unifiedorder/"+tradeType+"/"+orderId,
        contentType: "application/json; charset=utf-8",
        dataType: 'json',
        error : function(request) {
            console.log("data="+request);
            alert("提交订单失败,请检测是否已登录");
         },
        success: function(data) {
            console.log("data="+data);
            if(""!=data&&null!=data){
                if(data.message.code==200){ //成功
                //弹出微信支付界面 TODO
                    window.location = data.deeplink;
                }else{
                    alert(data.message.message);
                }
            }
        }
    });


}



