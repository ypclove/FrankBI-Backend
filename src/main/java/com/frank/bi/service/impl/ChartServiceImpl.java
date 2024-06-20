package com.frank.bi.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.constant.MqConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.manager.AiManager;
import com.frank.bi.manager.RedisLimiterManager;
import com.frank.bi.mapper.ChartMapper;
import com.frank.bi.model.dto.chart.GenChartByAiRequest;
import com.frank.bi.model.entity.Chart;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.enums.ChartStatusEnum;
import com.frank.bi.model.vo.BiResponse;
import com.frank.bi.service.AiFrequencyService;
import com.frank.bi.service.ChartService;
import com.frank.bi.service.UserService;
import com.frank.bi.utils.ChartUtils;
import com.frank.bi.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.frank.bi.constant.ChartConstant.*;

/**
 * @author Frank
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private UserService userService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * AI 生成图表（同步）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    @Override
    public BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                                   HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);
        // 1. 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 2. 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 2M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");
        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 2. 无需 Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        // 调用 AI
        String chartResult = aiManager.doChat(userInput.toString());

        // 3. 解析内容
        String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
        if (splits.length < GEN_ITEM_NUM) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        // 首次生成的图表数据内容
        String preGenChart = splits[GEN_CHART_IDX].trim();
        // 图表结论内容
        String genResult = splits[GEN_RESULT_IDX].trim();
        // 图表结构和数据内容
        String validGenChart = ChartUtils.getValidGenChart(preGenChart);

        System.out.println("preGenChart: ==========");
        System.out.println(preGenChart);
        System.out.println("genResult: ==========");
        System.out.println(genResult);
        System.out.println("validGenChart: ==========");
        System.out.println(validGenChart);

        // ThrowUtils.throwIf(StringUtils.isBlank(preGenChart), ErrorCode.PARAMS_ERROR, "AI 生成数据为空，请重试~");

        // 4. 插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setGenChart(preGenChart);
        // chart.setGenChart(validGenChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 5. 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(preGenChart);
        biResponse.setChartId(chart.getId());
        biResponse.setGenResult(genResult);
        System.out.println("biResponse=============");
        System.out.println(biResponse);

        // 6. 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "AI 调用次数减一失败");
        return biResponse;
    }

    /**
     * AI 生成图表（异步线程池）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                                        HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 1. 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 2. 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 2M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");
        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 3. 无需Prompt，直接调用现有模型
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 4. 先插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        // 任务队列已满
        if (threadPoolExecutor.getQueue().size() > threadPoolExecutor.getMaximumPoolSize()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前任务队列已满");
        }

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            // 等待-->执行中--> 成功/失败
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
            boolean updateChartById = this.updateById(updateChart);
            if (!updateChartById) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
                return;
            }
            // 调用 AI
            String chartResult = aiManager.doChat(userInput.toString());

            // 5. 解析内容
            String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
            if (splits.length < GEN_ITEM_NUM) {
                // throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
                handleChartUpdateError(chart.getId(), "AI生成错误");
                return;
            }
            // 生成前的内容
            String preGenChart = splits[GEN_CHART_IDX].trim();
            String genResult = splits[GEN_RESULT_IDX].trim();
            // 生成后端检验
            String validGenChart = ChartUtils.getValidGenChart(preGenChart);
            // 生成的最终结果-成功
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(preGenChart);
            // updateChartResult.setGenChart(validGenChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表，成功状态，失败");
            }
        }, threadPoolExecutor);

        try {
            completableFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 等待太久了，抛异常，超时时间
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            this.updateById(updateChartFailed);
            throw new RuntimeException(e);
        }

        // 6. 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());

        // 7. 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");
        return biResponse;
    }


    /**
     * AI 生成图表（异步消息队列）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    @Override
    public BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 1. 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 2. 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 2M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");
        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        // 4. 先插入数据到数据库
        Chart chart = new Chart();
        chartName = StringUtils.isBlank(chartName) ? ChartUtils.genDefaultChartName() : chartName;
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartName(chartName);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // 任务队列已满
        if (threadPoolExecutor.getQueue().size() > threadPoolExecutor.getMaximumPoolSize()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前任务队列已满");
        }
        String jsonStr = JSONUtil.toJsonPrettyStr(chart);
        rabbitTemplate.convertAndSend(MqConstant.BI_ASYNC_EXCHANGE, MqConstant.BI_ASYNC_ROUTING_KEY, jsonStr);

        // 5. 返回到前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());

        // 6. 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");
        return biResponse;
    }

    /**
     * 处理图表更新错误
     *
     * @param chartId     图表 Id
     * @param execMessage 执行消息
     */
    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setChartStatus(ChartStatusEnum.FAILED.getValue());
        updateChartResult.setExecMessage("图表更新失败！");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "，" + execMessage);
        }
    }
}




