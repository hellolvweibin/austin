package com.java3y.austin.service.api.impl.service;

import cn.monitor4all.logRecord.annotation.OperationLog;
import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.service.api.domain.BatchSendRequest;
import com.java3y.austin.service.api.domain.SendRequest;
import com.java3y.austin.service.api.domain.SendResponse;
import com.java3y.austin.service.api.impl.domain.SendTaskModel;
import com.java3y.austin.service.api.service.SendService;
import com.java3y.austin.support.pipeline.ProcessContext;
import com.java3y.austin.support.pipeline.ProcessController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 发送接口
 *
 * @author 3y
 */
@Service
public class SendServiceImpl implements SendService {

    @Autowired
    private ProcessController processController;

    /**
     * 发送消息
     */
    @Override
    @OperationLog(bizType = "SendService#send", bizId = "#sendRequest.messageTemplateId", msg = "#sendRequest")
    public SendResponse send(SendRequest sendRequest) {

        //建造者模式构造发送消息任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(sendRequest.getMessageTemplateId())
                .messageParamList(Collections.singletonList(sendRequest.getMessageParam()))
                .build();

        //构造上下文模型
        ProcessContext context = ProcessContext.builder()
                .code(sendRequest.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success()).build();

        //流程控制器执行发送流程
        ProcessContext process = processController.process(context);

        //根据上下文的返回内容构造发送接口返回值
        return new SendResponse(process.getResponse().getStatus(), process.getResponse().getMsg());
    }

    /**
     * 批量发送
     */
    @Override
    @OperationLog(bizType = "SendService#batchSend", bizId = "#batchSendRequest.messageTemplateId", msg = "#batchSendRequest")
    public SendResponse batchSend(BatchSendRequest batchSendRequest) {
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                .messageTemplateId(batchSendRequest.getMessageTemplateId())
                .messageParamList(batchSendRequest.getMessageParamList())
                .build();

        ProcessContext context = ProcessContext.builder()
                .code(batchSendRequest.getCode())
                .processModel(sendTaskModel)
                .needBreak(false)
                .response(BasicResultVO.success()).build();

        ProcessContext process = processController.process(context);

        return new SendResponse(process.getResponse().getStatus(), process.getResponse().getMsg());
    }


}
