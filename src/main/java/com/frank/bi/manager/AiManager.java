package com.frank.bi.manager;

import com.frank.bi.common.ErrorCode;
import com.frank.bi.config.AiModelConfig;
import com.frank.bi.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Frank
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient congMingClient;

    @Resource
    private AiModelConfig aiModelConfig;

    /**
     * AI 对话
     *
     * @param message 用户输入内容
     * @return AI 输出内容
     */
    public String doChat(String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(aiModelConfig.getModelId());
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = congMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }

    /**
     * 使用指定的模型进行 AI 对话
     *
     * @param modelId AI 模型 id
     * @param message 用户输入内容
     * @return AI 输出内容
     */
    public String doAiChat(Long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = congMingClient.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response.getData().getContent();
    }
}
