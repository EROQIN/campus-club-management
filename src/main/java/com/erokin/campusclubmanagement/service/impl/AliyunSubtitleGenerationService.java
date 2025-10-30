package com.erokin.campusclubmanagement.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;

import com.erokin.campusclubmanagement.config.speech.AliyunSpeechProperties;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.service.SubtitleGenerationService;
import com.erokin.campusclubmanagement.service.dto.GeneratedSubtitleSegment;

public class AliyunSubtitleGenerationService implements SubtitleGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AliyunSubtitleGenerationService.class);

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_QUEUEING = "QUEUEING";

    private final IAcsClient client;
    private final AliyunSpeechProperties properties;

    public AliyunSubtitleGenerationService(IAcsClient client, AliyunSpeechProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public List<GeneratedSubtitleSegment> generateSubtitles(String mediaUrl) {
        if (!StringUtils.hasText(mediaUrl)) {
            throw new BusinessException("无法生成字幕：视频文件地址为空。");
        }
        String appKey = properties.getAppKey();
        if (!StringUtils.hasText(appKey)) {
            throw new BusinessException("未配置阿里云语音识别 AppKey，请联系管理员。");
        }

        String taskId = submitTranscriptionTask(mediaUrl, appKey);
        return pollAndCollectResult(taskId);
    }

    private String submitTranscriptionTask(String mediaUrl, String appKey) {
        CommonRequest postRequest = new CommonRequest();
        postRequest.setDomain(properties.getDomain());
        postRequest.setVersion(properties.getApiVersion());
        postRequest.setAction("SubmitTask");
        postRequest.setProduct(properties.getProduct());
        postRequest.setMethod(MethodType.POST);

        JSONObject taskObject = new JSONObject();
        taskObject.put("appkey", appKey);
        taskObject.put("file_link", mediaUrl);
        if (StringUtils.hasText(properties.getVersion())) {
            taskObject.put("version", properties.getVersion());
        }
        taskObject.put("enable_words", properties.isEnableWords());

        postRequest.putBodyParameter("Task", taskObject.toJSONString());

        try {
            CommonResponse postResponse = client.getCommonResponse(postRequest);
            if (postResponse.getHttpStatus() != 200) {
                log.error("SubmitTask HTTP {} response: {}", postResponse.getHttpStatus(), postResponse.getData());
                throw new BusinessException("提交语音识别任务失败，请稍后再试。");
            }
            JSONObject result = JSONObject.parseObject(postResponse.getData());
            String statusText = result.getString("StatusText");
            if (!STATUS_SUCCESS.equalsIgnoreCase(statusText)) {
                log.error("SubmitTask failed: {}", result.toJSONString());
                throw new BusinessException("提交语音识别任务失败：" + result.getString("StatusText"));
            }
            return result.getString("TaskId");
        } catch (Exception e) {
            log.error("SubmitTask call failed: {}", e.getMessage(), e);
            throw new BusinessException("提交语音识别任务失败：" + e.getMessage());
        }
    }

    private List<GeneratedSubtitleSegment> pollAndCollectResult(String taskId) {
        CommonRequest getRequest = new CommonRequest();
        getRequest.setDomain(properties.getDomain());
        getRequest.setVersion(properties.getApiVersion());
        getRequest.setAction("GetTaskResult");
        getRequest.setProduct(properties.getProduct());
        getRequest.putQueryParameter("TaskId", taskId);
        getRequest.setMethod(MethodType.GET);

        Duration interval = properties.getPollInterval();
        long sleepMillis = Math.max(interval.toMillis(), 1000);
        int attempts = 0;
        int maxAttempts = Math.max(properties.getMaxPollAttempts(), 1);

        while (attempts++ < maxAttempts) {
            try {
                CommonResponse response = client.getCommonResponse(getRequest);
                if (response.getHttpStatus() != 200) {
                    log.error("GetTaskResult HTTP {} response: {}", response.getHttpStatus(), response.getData());
                    throw new BusinessException("查询语音识别结果失败，请稍后再试。");
                }
                JSONObject payload = JSONObject.parseObject(response.getData());
                String statusText = payload.getString("StatusText");
                if (STATUS_RUNNING.equalsIgnoreCase(statusText) || STATUS_QUEUEING.equalsIgnoreCase(statusText)) {
                    log.debug("Task {} still processing: {}", taskId, statusText);
                    sleepQuietly(sleepMillis);
                    continue;
                }
                if (!STATUS_SUCCESS.equalsIgnoreCase(statusText)) {
                    log.error("Task {} failed with status {} and payload {}", taskId, statusText, payload.toJSONString());
                    throw new BusinessException("语音识别失败：" + payload.getString("StatusText"));
                }
                return extractSegments(payload);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("Polling task {} failed: {}", taskId, e.getMessage(), e);
                throw new BusinessException("查询语音识别结果失败：" + e.getMessage());
            }
        }
        throw new BusinessException("查询语音识别结果超时，请稍后重试。");
    }

    private List<GeneratedSubtitleSegment> extractSegments(JSONObject payload) {
        JSONObject result = payload.getJSONObject("Result");
        if (result == null) {
            log.warn("Task result is missing Result field: {}", payload.toJSONString());
            return List.of();
        }
        JSONArray sentences = result.getJSONArray("Sentences");
        if (sentences == null || sentences.isEmpty()) {
            return List.of();
        }
        List<GeneratedSubtitleSegment> segments = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            JSONObject sentence = sentences.getJSONObject(i);
            int begin = sentence.getIntValue("BeginTime");
            int end = sentence.getIntValue("EndTime");
            String text = sentence.getString("Text");
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (end <= begin) {
                end = begin + 1000;
            }
            segments.add(new GeneratedSubtitleSegment(begin, end, text.trim()));
        }
        return segments;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Polling interrupted: {}", e.getMessage());
            throw new BusinessException("轮询语音识别结果被中断，请重试。");
        }
    }
}
