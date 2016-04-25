
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
            alert("支付失败,请重新尝试");
         },
        success: function(data) {
            console.log("data="+data);
            if(""!=data&&null!=data){
                if(data.message.code==200){ //成功

                    if("NATIVE"==tradeType){ //扫码支付

                        $("#codeImageUrl").attr("src", "/client/pay/getQRCode/" + data.qr_code_url);
                        //$(".weixin").unbind("click"); //移除click
                    }else{
                        //弹出微信支付界面 TODO
                        console.log("prepay_id="+data.prepay_id);
                        console.log("deeplink="+data.deeplink);
                        //window.location = data.deeplink;
                        alert("prepay_id="+data.prepay_id+"\n"+"deeplink="+data.deeplink);
                    }

                }else{
                    alert(data.message.message);
                }
            }
        }
    });


}



