package com.java3y.austin.handler.pending;


import cn.hutool.core.collection.CollUtil;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.handler.deduplication.DeduplicationRuleService;
import com.java3y.austin.handler.discard.DiscardMessageService;
import com.java3y.austin.handler.handler.HandlerHolder;
import com.java3y.austin.handler.shield.ShieldService;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Task 执行器
 * 0.丢弃消息
 * 1.屏蔽消息
 * 2.通用去重功能
 * 3.发送消息
 *
 * @author 3y
 */
@Data
@Accessors(chain = true)
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable {

    @Autowired
    private HandlerHolder handlerHolder;

    /**
     * 去重功能
     */
    @Autowired
    private DeduplicationRuleService deduplicationRuleService;

    /**
     * 丢弃模版消息
     */
    @Autowired
    private DiscardMessageService discardMessageService;

    /**
     * 屏蔽服务
     */
    @Autowired
    private ShieldService shieldService;

    private TaskInfo taskInfo;


    @Override
    public void run() {

        // 0. 丢弃消息
        if (discardMessageService.isDiscard(taskInfo)) {
            return;
        }
        // 1. 屏蔽消息 TODO 待啃
        shieldService.shield(taskInfo);

        // 2.平台通用去重 TODO 待啃
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            deduplicationRuleService.duplication(taskInfo);
        }

        // 3. 真正发送消息 TODO 待啃
        // 如果接收者不为空，通过获得渠道名称，获得对应的Handler来执行任务
        if (CollUtil.isNotEmpty(taskInfo.getReceiver())) {
            handlerHolder.route(taskInfo.getSendChannel()).doHandler(taskInfo);
        }

    }
}
