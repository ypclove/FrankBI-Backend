package com.frank.bi.controller;


import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.model.entity.AiFrequency;
import com.frank.bi.model.entity.AiFrequencyOrder;
import com.frank.bi.model.entity.AlipayInfo;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.enums.PayOrderEnum;
import com.frank.bi.model.vo.AlipayInfoVO;
import com.frank.bi.service.AiFrequencyOrderService;
import com.frank.bi.service.AiFrequencyService;
import com.frank.bi.service.AlipayInfoService;
import com.frank.bi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Frank
 */
@Slf4j
@Controller
@RequestMapping("/alipay")
public class AliPayController {

    @Resource
    private UserService userService;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AiFrequencyOrderService aiFrequencyOrderService;

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    /**
     * 支付接口
     *
     * @param alipayAccountNo 支付账户编号
     */
    @Transactional
    @GetMapping("/pay")
    public void pay(String alipayAccountNo) {
        try {
            if (StringUtils.isBlank(alipayAccountNo)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            AlipayInfo alipayInfo = getTotalAmount(alipayAccountNo);
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

            // 配置需要的公用请求参数：异步通知的地址
            // request.setNotifyUrl("");
            request.setReturnUrl("http://localhost:8000/result/success");

            // 组装当前业务方法的请求参数
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", alipayInfo.getAlipayAccountNo());
            bizContent.put("total_amount", alipayInfo.getTotalAmount());
            bizContent.put("subject", "智能AI使用次数");
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toString());

            // 执行请求，调用支付宝接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                log.info("调用成功，返回结果===>{}", response.getBody());
                // response.setContentType("text/html;charset=" + AliPayConstant.CHARSET);
                // String form = response.getBody();
                // response.getWriter().write(form);
                // response.getWriter().flush();
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, response.getMsg());
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成二维码
     *
     * @param orderId 订单 Id
     * @param request HttpServletRequest
     * @return 订单信息
     */
    @PostMapping("/payCode")
    @ResponseBody
    public BaseResponse<AlipayInfoVO> payCode(long orderId, HttpServletRequest request) {
        if (orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long alipayAccountNo = alipayInfoService.getPayNo(orderId, loginUser.getId());
        // TODO：将 URL 放在 application.yml 中或者常量类 AliPayConstant 中
        // String url = String.format("http://xxxxxx:8103/api/alipay/pay?alipayAccountNo=%s", alipayAccountNo);
        String url = String.format("http://192.168.11.219:8103/api/alipay/pay?alipayAccountNo=%s", alipayAccountNo);
        String generateQrCode = QrCodeUtil.generateAsBase64(url, new QrConfig(400, 400), "png");
        AlipayInfoVO alipayInfoVO = new AlipayInfoVO();
        alipayInfoVO.setAlipayAccountNo(String.valueOf(alipayAccountNo));
        alipayInfoVO.setQrCode(generateQrCode);
        alipayInfoVO.setOrderId(orderId);
        return ResultUtils.success(alipayInfoVO);
    }

    /**
     * 查询交易结果
     *
     * @param alipayAccountNo 支付账户编号
     * @throws AlipayApiException AlipayApiException
     */
    @Transactional
    @PostMapping("/tradeQuery")
    public void tradeQuery(String alipayAccountNo) throws AlipayApiException {
        AlipayInfo alipayInfo = getTotalAmount(alipayAccountNo);
        Long orderId = alipayInfo.getOrderId();
        AiFrequencyOrder orderId1 = getOrder(orderId);
        if (orderId1.getOrderStatus() == 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "你已经支付过了");
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", alipayAccountNo);

        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (!response.isSuccess()) {
            log.error("查询交易结果失败");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用失败");
        }
        // 获取支付结果
        String resultJson = response.getBody();
        // 转 map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        alipayInfo.setPayStatus(Integer.valueOf(PayOrderEnum.COMPLETE.getValue()));
        alipayInfo.setAlipayId(trade_no);
        boolean updateComplete = alipayInfoService.updateById(alipayInfo);
        if (!updateComplete) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiFrequencyOrder order = getOrder(orderId);
        order.setOrderStatus(Integer.valueOf(PayOrderEnum.COMPLETE.getValue()));
        aiFrequencyOrderService.updateById(order);
        // 获取充值次数
        Long total = order.getPurchaseQuantity();
        Long userId = order.getUserId();
        AiFrequency aiFrequency = getHartFrequency(userId);

        if (aiFrequency == null) {
            AiFrequency frequency = new AiFrequency();
            frequency.setUserId(userId);
            frequency.setTotalFrequency(Integer.valueOf(PayOrderEnum.WAIT_PAY.getValue()));
            frequency.setRemainFrequency(Math.toIntExact(total));
            boolean save = aiFrequencyService.save(frequency);
            if (!save) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付成功回调发生错误");
            }
        }
        Integer remainFrequency = aiFrequency.getRemainFrequency();
        int i = Math.toIntExact(total);
        aiFrequency.setRemainFrequency(remainFrequency + i);
        boolean updateFrequency = aiFrequencyService.updateById(aiFrequency);
        if (!updateFrequency) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "支付成功回调发生错误");
        }
        log.info("调用成功，结果：{}", response.getBody());
        // return ResultUtils.success(resultJson);
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param alipayAccountNo 支付账户编号
     */
    @PostMapping("/query/payNo")
    @ResponseBody
    public void queryPayResultFromAlipay(String alipayAccountNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        try {
            bizContent.put("out_trade_no", alipayAccountNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        assert response != null;
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        // 获取支付结果
        String resultJson = response.getBody();
        // 转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        // 支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
    }

    /**
     * 获取支付宝流水账号
     *
     * @param alipayAccountNo 支付账户编号
     * @return 支付宝流水信息
     */
    public AlipayInfo getTotalAmount(String alipayAccountNo) {
        QueryWrapper<AlipayInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("alipayAccountNo", alipayAccountNo);
        AlipayInfo aliPayOne = alipayInfoService.getOne(wrapper);
        if (aliPayOne == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有这个记录");
        }
        return aliPayOne;
    }

    /**
     * 获取 Ai 次数订单
     *
     * @param orderId 订单 Id
     * @return Ai 次数订单
     */
    public AiFrequencyOrder getOrder(Long orderId) {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        wrapper.eq("orderId", orderId);
        AiFrequencyOrder frequencyOrderServiceById = aiFrequencyOrderService.getById(orderId);
        if (frequencyOrderServiceById == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查无此纪录");
        }
        return frequencyOrderServiceById;
    }

    /***
     * 获取 Ai 调用次数
     * @param userId 用户 Id
     * @return Ai 调用次数
     */
    public AiFrequency getHartFrequency(long userId) {
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency one = aiFrequencyService.getOne(wrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return one;
    }
}
