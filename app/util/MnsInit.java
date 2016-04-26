package util;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.MNSClient;

import javax.inject.Singleton;

import static util.SysParCom.*;

/**
 * 阿里云mns初始化
 * Created by howen on 16/4/20.
 */
@Singleton
public class MnsInit {

    public static MNSClient mnsClient;

    public MnsInit(){
        CloudAccount account = new CloudAccount(MNS_KEY, MNS_SECRET, MNS_ENDPOINT);
        mnsClient = account.getMNSClient(); //this client need only initialize once
    }
}
