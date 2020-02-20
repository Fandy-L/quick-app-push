package com.chuang.qapp.service.push.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.chuang.qapp.common.QappMsgConstant;
import lombok.Data;

import java.util.Map;

/**
 * @author fandy.lin
 * 华为消息参数实体定义
 */
@Data
public class HuaweiMessage {
    @JSONField(name="hps")
    private MessageWapper messageWapper;

    public HuaweiMessage() {
        this.messageWapper = new MessageWapper();
    }

    /**
     * 实例化华为消息
     * @param title 标题
     * @param description 内容描述
     * @param page 请求页面路径
     * @param params 路径携带参数
     */
    public  HuaweiMessage(String title,String description,String page,Map<String,Object> params){
        this.messageWapper = new MessageWapper();
        messageWapper.getMsg().getMessageBody().getNotification().setTitle(title);
        messageWapper.getMsg().getMessageBody().getNotification().setDescription(description);
        messageWapper.getMsg().getMessageBody().getNotification().setPage(page);
        messageWapper.getMsg().getMessageBody().getNotification().setParams(params);
    }


}

@Data
class MessageWapper{
    private Message msg;

    public MessageWapper() {
        this.msg = new Message();
    }
}

@Data
class Message{
    //快应用设置为1
    int type = 1;
    @JSONField(name="body")
    MessageBody messageBody;

    public  Message(){
        this.messageBody = new MessageBody();
    }
}

@Data
class MessageBody{
    @JSONField(name="pushtype")
    private int pushType = QappMsgConstant.HUAWEI_PUSH_TYPE;
    @JSONField(name="pushbody")
    public Notification notification;

    public MessageBody() {
        this.notification = new Notification();
    }
}

@Data
class Notification{
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 访问页面路径(不可直接连接参数)
     */
    private String page;
    /**
     *访问参数
     */
    private Map<String,Object> params;

}






