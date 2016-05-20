
var appId = "";
var timeStamp = "";
var nonceStr = "";
var package = "";
var signType = "";
var paySign = "";
var orderId="";
var token="";
var securityCode="";
//微信统一下单
function payUnifiedorder(tradeType,orderId,orderCreateAt,token,securityCode){
    var d = new Date();
    var n = d.getTime();
    if(n-orderCreateAt>=86400000){
        alert("您的订单已经超时自动取消");
        return ;
    }
    if("NATIVE"==tradeType){
        if(1==$("#codeImgHidden").val()){
            $("#codeImageDiv").show();
            return false;
        }

    }else{
        $("#codeImgHidden").val(0);
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
          //  alert("支付失败,请重新尝试");
         },
        success: function(data) {
            console.log("data="+data);
            if(""!=data&&null!=data){
                if(data.message.code==200){ //成功

                    if("NATIVE"==tradeType){ //扫码支付
                        $("#codeImageDiv").show();
                        $("#codeImageUrl").attr("src", "/client/weixin/pay/getQRCode/" + data.qr_code_url);
                        $("#codeImgHidden").val(1);

                    }else if("JSAPI"==tradeType){ //微信公众号支付

                            appId=data.paramMap.appId;
                            timeStamp=data.paramMap.timeStamp,         //时间戳，自1970年以来的秒数
                            nonceStr=data.paramMap.nonceStr, //随机串
                            package=data.paramMap.package,
                            signType=data.paramMap.signType,         //微信签名方式：
                            paySign=data.paramMap.paySign  //微信签名

                        var weixinJsApiForm = $('<form action="/client/weixin/pay/jsapi" method="post">' +
                            '<input type="hidden" name="appId" value="'+appId+'"/>' +
                            '<input type="hidden" name="timeStamp" value="'+timeStamp+'"/>' +
                            '<input type="hidden" name="nonceStr" value="'+nonceStr+'"/>' +
                            '<input type="hidden" name="pg" value="'+package+'"/>' +
                            '<input type="hidden" name="signType" value="'+signType+'"/>' +
                            '<input type="hidden" name="paySign" value="'+paySign+'"/>' +
                            '<input type="hidden" name="orderId" value="'+orderId+'"/>' +
                            '<input type="hidden" name="token" value="'+token+'"/>' +
                            '<input type="hidden" name="securityCode" value="'+securityCode+'"/>' +
                            '</form>');
                            weixinJsApiForm.submit();

                    }else{
                        //弹出微信支付界面
                        console.log("prepay_id="+data.prepay_id);

                    }

                }else{
                    alert(data.message.message);
                }
            }
        }
    });

};


//调用微信H5支付
function callpay(app,ts,ns,pg,st,ps,o,t,sc){
    appId=app;
    timeStamp=ts;         //时间戳，自1970年以来的秒数
    nonceStr=ns; //随机串
    package=pg;
    signType=st;        //微信签名方式：
    paySign=ps; //微信签名
    orderId=o;
    token=t;
    securityCode=sc;

    if (typeof WeixinJSBridge == "undefined"){
       if( document.addEventListener ){
           document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
       }else if (document.attachEvent){
           document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
           document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
       }
    }else{
        onBridgeReady();
    }
}


function onBridgeReady(){
   console.log("appId="+appId+",timeStamp="+timeStamp+",nonceStr="+nonceStr+",package="+package+",signType="+signType+",paySign="+paySign+",orderId="+orderId+",token="+token+",securityCode="+securityCode);
   WeixinJSBridge.invoke(
       'getBrandWCPayRequest', {
              "appId":appId,     //公众号名称，由商户传入
              "timeStamp":timeStamp,         //时间戳，自1970年以来的秒数
              "nonceStr":nonceStr, //随机串
              "package":package,
              "signType":signType,         //微信签名方式：
              "paySign":paySign  //微信签名
       },
       function(res){
           console.log(res.err_msg);
         //  alert(res.err_msg);
           if(res.err_msg == "get_brand_wcpay_request:ok" ) {// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
              //查询支付结果
              payOrderquery("JSAPI",orderId,token,securityCode);
           }else{
               if (navigator.userAgent.match(/MicroMessenger/i)||navigator.userAgent.match(/android/i)) {
                  history.back();
               }else{
                   payOrderquery("JSAPI",orderId,token,securityCode); //ios浏览器返回会重新加载所以不返回跳查订单界面
               }
           }

       }
   );
};

//订单查询
function payOrderquery(tradeType,orderId,token,securityCode){
    var form = $('<form action="/client/weixin/pay/orderquery/redirect" method="post">' +
                '<input type="hidden" name="orderId" value="'+orderId+'"/>' +
                '<input type="hidden" name="token" value="'+token+'"/>' +
                '<input type="hidden" name="securityCode" value="'+securityCode+'"/>' +
                '<input type="hidden" name="tradeType" value="'+tradeType+'"/>' +
                '</form>');
   form.submit();
}

$(document).on("click", "#cancelPaySpan", function() {
    $("#codeImageDiv").hide();
});