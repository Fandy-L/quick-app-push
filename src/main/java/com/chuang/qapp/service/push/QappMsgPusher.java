package com.chuang.qapp.service.push;

import com.chuang.qapp.entity.QappPushResult;
import com.chuang.qapp.entity.dto.PushMsgInfDTO;
import com.chuang.qapp.entity.mysql.push.QappPushApp;

public interface QappMsgPusher {
     QappPushResult allPush(PushMsgInfDTO reqDTO, QappPushApp QappPushApp);
}
