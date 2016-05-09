
var appId = "";
var timeStamp = "";
var nonceStr = "";
var pg = "";
var signType = "";
var paySign = "";

//微信统一下单
function payUnifiedorder(tradeType,orderId,orderCreateAt,token,securityCode){
    var d = new Date();
    var n = d.getTime();
    if(n-orderCreateAt>=86400000){
        alert("您的订单已经超时自动取消");
        return ;
    }
     //去支付
    var form = $('<form action="/client/weixin/pay/unifiedorder/redirect" method="post">' +
                '<input type="hidden" name="orderId" value="'+orderId+'"/>' +
                '<input type="hidden" name="token" value="'+token+'"/>' +
                '<input type="hidden" name="securityCode" value="'+securityCode+'"/>' +
                '<input type="hidden" name="tradeType" value="'+tradeType+'"/>' +
                '</form>');

    $.ajax({
        type: 'POST',
        url: "/client/weixin/pay/unifiedorder/redirect",
        data: form.serialize(),
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
                        $("#codeImageDiv").show();
                        $("#codeImageUrl").attr("src", "/client/weixin/pay/getQRCode/" + data.qr_code_url);

                        $("#paySucDiv").show();

                    }else if("JSAPI"==tradeType){ //微信公众号支付

                        if (typeof WeixinJSBridge == "undefined"){
                           if( document.addEventListener ){
                               document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                           }else if (document.attachEvent){
                               document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                               document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                           }
                        }else{
                            appId=data.paramMap.appId;
                            timeStamp=data.paramMap.timeStamp,         //时间戳，自1970年以来的秒数
                            nonceStr=data.paramMap.nonceStr, //随机串
                            package=data.paramMap.package,
                            signType=data.paramMap.signType,         //微信签名方式：
                            paySign=data.paramMap.paySign  //微信签名
                            onBridgeReady();
                        }

                    }else{
                        //弹出微信支付界面
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

};

function onBridgeReady(){
console.log("appId="+appId+",timeStamp="+timeStamp+",nonceStr="+nonceStr+",package="+package+",signType="+signType+",paySign="+paySign)
   WeixinJSBridge.invoke(
       'getBrandWCPayRequest', {
              "appId":appId,     //公众号名称，由商户传入
              "timeStamp":timeStamp,         //时间戳，自1970年以来的秒数
              "nonceStr":nonceStr, //随机串
              "package":package,
              "signType":signType,         //微信签名方式：
              "paySign":paySign  //微信签名

//           "appId" ： "wx2421b1c4370ec43b",     //公众号名称，由商户传入
//           "timeStamp"：" 1395712654",         //时间戳，自1970年以来的秒数
//           "nonceStr" ： "e61463f8efa94090b1f366cccfbbb444", //随机串
//           "package" ： "prepay_id=u802345jgfjsdfgsdg888",
//           "signType" ： "MD5",         //微信签名方式：
//           "paySign" ： "70EA570631E4BB79628FBCA90534C63FF7FADD89" //微信签名
       },
       function(res){
           if(res.err_msg == "get_brand_wcpay_request：ok" ) {}     // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
       }
   );
};

function payOrderquery(orderId,token,securityCode){
     //去支付
    var form = $('<form action="/client/weixin/pay/orderquery/redirect" method="post">' +
                '<input type="hidden" name="orderId" value="'+orderId+'"/>' +
                '<input type="hidden" name="token" value="'+token+'"/>' +
                '<input type="hidden" name="securityCode" value="'+securityCode+'"/>' +
                '</form>');
   form.submit();

}

