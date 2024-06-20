package com.frank.bi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.frank.bi.annotation.AuthCheck;
import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.DeleteRequest;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.constant.CommonConstant;
import com.frank.bi.constant.MqConstant;
import com.frank.bi.constant.UserConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.manager.RedisLimiterManager;
import com.frank.bi.model.dto.aiassistant.*;
import com.frank.bi.model.entity.AiAssistant;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.enums.AiAssistantStatusEnum;
import com.frank.bi.service.AiAssistantService;
import com.frank.bi.service.AiFrequencyService;
import com.frank.bi.service.UserService;
import com.frank.bi.utils.SqlUtils;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank
 */
@Slf4j
@RestController
@RequestMapping("/aiAssistant")
@Api(tags = "AiAssistantController")
public class AiAssistantController {

    @Resource
    private UserService userService;

    @Resource
    private AiAssistantService aiAssistantService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private final static Gson GSON = new Gson();

    /**
     * 创建 Ai 对话
     *
     * @param aiAssistantAddRequest Ai 对话请求
     * @param request               HttpServletRequest
     * @return Ai 对话的 Id
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增对话")
    public BaseResponse<Long> addAiAssistant(@RequestBody AiAssistantAddRequest aiAssistantAddRequest,
                                             HttpServletRequest request) {
        if (aiAssistantAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantAddRequest, aiAssistant);
        User loginUser = userService.getLoginUser(request);
        aiAssistant.setUserId(loginUser.getId());
        boolean result = aiAssistantService.save(aiAssistant);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long aiAssistantId = aiAssistant.getId();
        return ResultUtils.success(aiAssistantId);
    }

    /**
     * 删除 Ai 对话
     *
     * @param deleteRequest 删除请求
     * @param request       HttpServletRequest
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除对话")
    public BaseResponse<Boolean> deleteAiAssistant(@RequestBody DeleteRequest deleteRequest,
                                                   HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAiAssistant.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean isDelete = aiAssistantService.removeById(id);
        return ResultUtils.success(isDelete);
    }

    /**
     * 更新（仅管理员）
     *
     * @param aiAssistantUpdateRequest 更新 Ai 对话
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "管理员更新对话信息")
    public BaseResponse<Boolean> updateAiAssistant(@RequestBody AiAssistantUpdateRequest aiAssistantUpdateRequest) {
        if (aiAssistantUpdateRequest == null || aiAssistantUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantUpdateRequest, aiAssistant);
        long id = aiAssistantUpdateRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        boolean isUpdate = aiAssistantService.updateById(aiAssistant);
        return ResultUtils.success(isUpdate);
    }

    /**
     * 根据 id 获取 Ai 对话
     *
     * @param id Ai 对话 Id
     * @return Ai 对话
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据Id获取对话")
    public BaseResponse<AiAssistant> getAiAssistantById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = aiAssistantService.getById(id);
        if (aiAssistant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(aiAssistant);
    }

    /**
     * 分页获取 Ai 对话列表
     *
     * @param aiAssistantQueryRequest 分页获取 Ai 对话
     * @return Ai 对话列表
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取Ai对话")
    public BaseResponse<Page<AiAssistant>> listAiAssistantByPage(@RequestBody AiAssistantQueryRequest aiAssistantQueryRequest) {
        long current = aiAssistantQueryRequest.getCurrent();
        long size = aiAssistantQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiAssistant> assistantPage = aiAssistantService.page(new Page<>(current, size), getQueryWrapper(aiAssistantQueryRequest));
        return ResultUtils.success(assistantPage);
    }

    /**
     * 分页获取当前用户创建的 Ai 对话列表
     *
     * @param aiAssistantQueryRequest Ai 对话查询请求
     * @param request                 HttpServletRequest
     * @return Ai 对话列表
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取我的Ai对话列表")
    public BaseResponse<Page<AiAssistant>> listMyAiAssistantByPage(@RequestBody AiAssistantQueryRequest aiAssistantQueryRequest,
                                                                   HttpServletRequest request) {
        if (aiAssistantQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        aiAssistantQueryRequest.setUserId(loginUser.getId());
        long current = aiAssistantQueryRequest.getCurrent();
        long size = aiAssistantQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiAssistant> aiAssistantPage = aiAssistantService.page(new Page<>(current, size), getQueryWrapper(aiAssistantQueryRequest));
        return ResultUtils.success(aiAssistantPage);
    }

    /**
     * 管理员编辑 Ai 对话
     *
     * @param aiAssistantEditRequest Ai 对话编辑请求
     * @param request                HttpServletRequest
     * @return 编辑是否成功
     */
    @PostMapping("/edit")
    @ApiOperation(value = "管理员编辑Ai对话")
    public BaseResponse<Boolean> editAiAssistant(@RequestBody AiAssistantEditRequest aiAssistantEditRequest,
                                                 HttpServletRequest request) {
        if (aiAssistantEditRequest == null || aiAssistantEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantEditRequest, aiAssistant);
        User loginUser = userService.getLoginUser(request);
        long id = aiAssistantEditRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAiAssistant.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean isUpdate = aiAssistantService.updateById(aiAssistant);
        return ResultUtils.success(isUpdate);
    }

    /**
     * 封装查询条件
     *
     * @param aiAssistantQueryRequest Ai 对话查询条件
     * @return 查询结果
     */
    private QueryWrapper<AiAssistant> getQueryWrapper(AiAssistantQueryRequest aiAssistantQueryRequest) {

        QueryWrapper<AiAssistant> queryWrapper = new QueryWrapper<>();
        if (aiAssistantQueryRequest == null) {
            return queryWrapper;
        }

        Long id = aiAssistantQueryRequest.getId();
        String questionGoal = aiAssistantQueryRequest.getQuestionGoal();
        String questionName = aiAssistantQueryRequest.getQuestionName();
        String questionType = aiAssistantQueryRequest.getQuestionType();
        String questionStatus = aiAssistantQueryRequest.getQuestionStatus();
        String questionResult = aiAssistantQueryRequest.getQuestionResult();
        Long userId = aiAssistantQueryRequest.getUserId();
        String sortField = aiAssistantQueryRequest.getSortField();
        String sortOrder = aiAssistantQueryRequest.getSortOrder();
        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionGoal), "questionGoal", questionGoal);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionName), "questionName", questionName);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionStatus), "questionStatus", questionStatus);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionResult), "questionResult", questionResult);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionType), "questionType", questionType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * AI 对话助手
     *
     * @param genChatByAiRequest Ai 对话的上传文件请求
     * @param request            HttpServletRequest
     * @return AI 对话助手
     */
    @PostMapping("/chat")
    @ApiOperation("AI 对话")
    public BaseResponse<?> aiAssistant(@RequestBody GenChatByAiRequest genChatByAiRequest,
                                       HttpServletRequest request) {
        String questionName = genChatByAiRequest.getQuestionName();
        String questionGoal = genChatByAiRequest.getQuestionGoal();
        String questionType = genChatByAiRequest.getQuestionType();
        User loginUser = userService.getLoginUser(request);

        // 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 校验
        if (StringUtils.isBlank(questionName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题名称为空");
        }
        if (ObjectUtils.isEmpty(questionType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题类型为空");
        }
        if (StringUtils.isBlank(questionGoal)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题分析目标为空");
        }

        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("Ai_Rate_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        AiAssistant aiAssistant = new AiAssistant();
        aiAssistant.setQuestionName(questionName);
        aiAssistant.setQuestionGoal(questionGoal);
        aiAssistant.setQuestionType(questionType);
        aiAssistant.setQuestionStatus(AiAssistantStatusEnum.WAIT.getValue());
        aiAssistant.setUserId(loginUser.getId());
        // 插入到数据库
        boolean save = aiAssistantService.save(aiAssistant);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存失败");

        String json = GSON.toJson(aiAssistant);
        rabbitTemplate.convertAndSend(MqConstant.AI_QUESTION_EXCHANGE, MqConstant.AI_QUESTION_ROUTING_KEY, json);

        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return ResultUtils.success(aiAssistant);
    }
}
