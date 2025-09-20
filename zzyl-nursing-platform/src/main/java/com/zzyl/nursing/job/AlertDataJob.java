package com.zzyl.nursing.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.nursing.domain.AlertData;
import com.zzyl.nursing.domain.AlertRule;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.service.IAlertDataService;
import com.zzyl.nursing.service.IAlertRuleService;
import com.zzyl.nursing.service.IDeviceService;
import com.zzyl.system.service.ISysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 报警数据处理任务类
 */
@Slf4j
@Component
public class AlertDataJob {
    @Autowired
    private IAlertRuleService alertRuleService;
    @Autowired
    private IDeviceService deviceService;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private IAlertDataService alertDataService;
    @Autowired
    private ISysRoleService sysRoleService;

    /**
     * 处理报警数据的主流程方法。
     * <p>
     * 该方法会依次执行以下步骤：
     * 1. 查询所有启用状态的报警规则；
     * 2. 遍历每条报警规则，查询其关联的设备列表；
     * 3. 针对每个设备获取其最新的上报数据；
     * 4. 根据规则中指定的功能ID匹配设备数据，并调用处理逻辑。
     * </p>
     * 注意：此方法不接收参数，也无返回值。
     */
    public void processAlertData() {
        log.info("第一步：查询所有报警规则");
        List<AlertRule> alertRules = alertRuleService.list(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getStatus, 1));//1 启用 0 禁用
        if (CollectionUtil.isEmpty(alertRules)) {
            log.info("\t没有启用的报警规则");
            return;
        }
        log.info("第二步：遍历报警规则，根据规则查询其关联的产品设备");
        for (AlertRule rule : alertRules) {
            log.info("\t处理报警规则:{}", rule.getAlertRuleName());
            String iotId = rule.getIotId();
            List<Device> deviceList = null;
            // 根据iotId是否为"-1"决定查询方式
            if ("-1".equals(iotId)) {
                deviceList = deviceService.list(new LambdaQueryWrapper<Device>()
                        .eq(Device::getProductKey, rule.getProductKey()));
            } else {
                deviceList = deviceService.list(new LambdaQueryWrapper<Device>().eq(Device::getIotId, iotId));
            }
            if (CollectionUtil.isEmpty(deviceList)) {
                log.info("\t没有关联的产品设备");
                return;
            }
            log.info("第三步：处理产品设备数据");
            for (Device device : deviceList) {
                log.info("\t处理当前的设备:{}", device.getDeviceName());
                // 获取当前设备的最后上报数据
                String str = (String) redisTemplate.opsForHash().get(CacheConstants.IOT_DEVICE_LAST_DATA, device.getIotId());
                if (StrUtil.isNotEmpty(str)) {
                    List<DeviceData> deviceData = JSONUtil.toList(str, DeviceData.class);
                    String functionId = rule.getFunctionId();
                    // 遍历设备数据，匹配规则中的functionId
                    for (DeviceData data : deviceData) {
                        if (functionId.equals(data.getFunctionId())) {
                            //处理数据
                            log.info("\t处理数据:{}", data);
                            handlerData(rule, data);
                        }
                    }
                }
            }

        }


    }


    /**
     * 处理设备数据，判断是否满足报警规则并触发相应操作。
     *
     * @param rule 报警规则对象，包含报警条件、生效时间段、持续周期等配置信息
     * @param data 设备上报的数据，包括数据值、报警时间、设备标识等
     */
    private void handlerData(AlertRule rule, DeviceData data) {
        //1. 判断数据上报时间是否超过一分钟
        if (data.getAlarmTime().isBefore(LocalDateTime.now().minusMinutes(1))) {
            log.info("数据上报时间【{}】超过一分钟，不处理", data.getAlarmTime());
            return;
        }

        //2. 判断上报时间是否在报警生效时间段范围内
        String alertEffectivePeriod = rule.getAlertEffectivePeriod();
        log.info("判断数据上报时间是否在报警生效时间段内:{}", alertEffectivePeriod);
        if (StrUtil.isNotEmpty(alertEffectivePeriod) && !isInEffectivePeriod(alertEffectivePeriod, data.getAlarmTime())) {
            log.info("数据上报时间不在报警生效时间段内，不处理");
            return;
        }

        //3. 判断当前设备数据是否触发报警条件（根据运算符比较数据值与设定阈值）
        String gongshi = data.getDataValue() + rule.getOperator() + rule.getValue();
        boolean result = evaluateExpression(gongshi);
        log.info("\t,计算公式是:{},数据是否触发警告:{}", gongshi, result);

        // 构造用于记录连续报警次数的 Redis key
        String countKey = CacheConstants.ALERT_TRIGGER_COUNT.replace("{IotId}", data.getIotId())
                .replace("{FunctionId}", data.getFunctionId())
                .replace("{RuleId}", String.valueOf(rule.getId()));

        if (!result) {
            // 如果未触发报警，清除之前的累计计数
            log.info("\t数据没有触发警告，删除连续累计报警数据:{}", countKey);
            redisTemplate.delete(countKey);
            return;
        }

        //4. 判断设备是否处于报警沉默周期中，若在沉默周期内则跳过处理
        String silentKey = CacheConstants.ALERT_SILENT.replace("{IotId}", data.getIotId())
                .replace("{FunctionId}", data.getFunctionId())
                .replace("{RuleId}", String.valueOf(rule.getId()));
        if (redisTemplate.hasKey(silentKey)) {
            log.info("\t数据在沉默周期内，不处理");
            return;
        }

        //5. 增加连续报警计数，并判断是否达到持续周期阈值
        Long count = redisTemplate.opsForValue().increment(countKey);
        if ((count != null && count >= rule.getDuration())) {
            log.info("\t数据连续报警次数【{}】,达到阈值:【{}】，触发报警", count, rule.getDuration());

            //6. 触发报警逻辑
            //6.1 删除Redis中的连续报警计数
            redisTemplate.delete(countKey);

            //6.2 设置报警沉默周期，防止频繁报警
            redisTemplate.opsForValue().set(silentKey, data, rule.getAlertSilentPeriod(), TimeUnit.MINUTES);

            //6.3 将报警信息写入数据库
            log.info("\t存储报警数据到数据库");
            //获取报警数据的接收人ids
            List<Long> userIds = getAlertReceiver(rule, data);
            if (CollectionUtil.isEmpty(userIds)) {
                log.info("\t没有接收人，不处理");
                return;
            }
            List<AlertData> alertDataList = userIds.stream().map(userid -> {
                AlertData alertData = new AlertData();
                alertData.setIotId(data.getIotId());
                alertData.setProductName(data.getProductName());
                alertData.setProductKey(data.getProductKey());
                alertData.setDeviceName(data.getDeviceName());
                alertData.setFunctionId(data.getFunctionId());
                alertData.setAccessLocation(data.getAccessLocation());
                alertData.setLocationType(data.getLocationType());
                alertData.setPhysicalLocationType(data.getPhysicalLocationType());
                alertData.setDeviceDescription(data.getDeviceDescription());
                alertData.setDataValue(data.getDataValue());
                alertData.setAlertRuleId(rule.getId());
                alertData.setProcessingTime(LocalDateTime.now());
                alertData.setType(rule.getAlertDataType());
                //0待处理 1已处理
                alertData.setStatus(0);

                alertData.setUserId(userid);

                String functionName = rule.getFunctionName();//心率
                String operator = rule.getOperator();//运算符 >=<
                Double value = rule.getValue();//阈值
                Integer duration = rule.getDuration();//持续周期
                //心率连续三次>=200
                alertData.setAlertReason(functionName + operator + value + "持续" + duration + "次");

                return alertData;
            }).collect(Collectors.toList());

            alertDataService.saveBatch(alertDataList);
        }
    }

    /**
     * 根据告警规则和设备数据获取告警接收人列表
     *
     * @param rule 告警规则对象
     * @param data 设备数据对象
     * @return 告警接收人用户ID列表
     */
    private List<Long> getAlertReceiver(AlertRule rule, DeviceData data) {
        List<Long> userIds = new ArrayList<>();

        // 根据设备位置类型确定告警接收人
        if (rule.getAlertDataType() == 0) {
            if (data.getLocationType() == 0) {
                //0：随身设备 老人异常数据
                List<Long> ids = deviceService.selectNursingIdsByIotIdWithElder(data.getIotId());
                userIds.addAll(ids);
            } else if (data.getLocationType() == 1 && data.getPhysicalLocationType() == 2) {
                //1：固定设备 床位异常数据
                List<Long> ids = deviceService.selectNursingIdsByIotIdWithBed(rule.getIotId());
                userIds.addAll(ids);
            }
        } else {
            //通知维修工
            List<Long> ids = sysRoleService.getUserIdsByRoleNameOrRoleKey("维修工");
            userIds.addAll(ids);
        }

        //查询超级管理员
        List<Long> superIds = sysRoleService.getUserIdsByRoleNameOrRoleKey("超级管理员");
        userIds.addAll(superIds);

        return userIds;
    }


    /**
     * 判断指定时间是否在有效时间段内
     *
     * @param alertEffectivePeriod 有效时间段，格式如：00:00:00~23:59:59
     * @param dataTime             数据上报时间
     * @return 是否在有效时间段内
     */
    private boolean isInEffectivePeriod(String alertEffectivePeriod, LocalDateTime dataTime) {
        String[] timeRange = alertEffectivePeriod.split("~");
        if (timeRange.length != 2) {
            return true; // 格式不正确，默认在有效期内
        }

        String startTimeStr = timeRange[0];
        String endTimeStr = timeRange[1];

        // 获取当前时间的时分秒部分
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.parse(now.toLocalDate() + "T" + startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(now.toLocalDate() + "T" + endTimeStr);

        // 如果结束时间小于开始时间，说明跨天了
        if (endTime.isBefore(startTime)) {
            endTime = endTime.plusDays(1);
        }

        LocalDateTime dataDateTime = LocalDateTime.of(dataTime.toLocalDate(), dataTime.toLocalTime());

        // 判断数据时间是否在生效时间段内
        return !dataDateTime.isBefore(startTime) && !dataDateTime.isAfter(endTime);
    }


    /**
     * 计算数学关系表达式的结果
     *
     * @param expression 表达式字符串，如 "100 >= 55"
     * @return 表达式计算结果
     */
    private static boolean evaluateExpression(String expression) {
        try {
            expression = expression.replaceAll("(?<![<>])=", "==");
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            Object result = engine.eval(expression);
            return (Boolean) result;
        } catch (Exception e) {
            log.error("表达式计算失败: {}", expression, e);
            return false;
        }
    }

}
