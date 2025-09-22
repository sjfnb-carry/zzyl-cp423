package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.domain.Bed;
import com.zzyl.nursing.domain.Floor;
import com.zzyl.nursing.domain.Room;
import com.zzyl.nursing.dto.AlertDataDto;
import com.zzyl.nursing.mapper.BedMapper;
import com.zzyl.nursing.mapper.FloorMapper;
import com.zzyl.nursing.mapper.RoomMapper;
import com.zzyl.nursing.vo.AlertDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.AlertDataMapper;
import com.zzyl.nursing.domain.AlertData;
import com.zzyl.nursing.service.IAlertDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报警数据Service业务层处理
 *
 * @author alexis
 * @date 2025-09-20
 */
@Service
public class AlertDataServiceImpl extends ServiceImpl<AlertDataMapper, AlertData> implements IAlertDataService {
    @Autowired
    private AlertDataMapper alertDataMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private FloorMapper floorMapper;

    /**
     * 查询报警数据
     *
     * @param id 报警数据主键
     * @return 报警数据
     */
    @Override
    public AlertData selectAlertDataById(Long id) {
        return alertDataMapper.selectById(id);
    }

    /**
     * 查询报警数据列表
     *
     * @param alertDataDto 报警数据
     * @return 报警数据
     */
    @Override
    public TableDataInfo<AlertDataVo> selectAlertDataList(AlertDataDto alertDataDto) {
        Long currentUserId = SecurityUtils.getUserId();
        IPage<AlertData> page = new Page<>(alertDataDto.getPageNum(), alertDataDto.getPageSize());
        List<AlertDataVo> alertDataVos = alertDataMapper.selectAlertDataList(page, alertDataDto);
        List<AlertDataVo> collect = alertDataVos.stream().filter(alertDataVo -> {
            return alertDataVo.getUserId().equals(currentUserId);
        }).map(alertDataVo -> {
            AlertDataVo build = AlertDataVo.builder()
                    .id(alertDataVo.getId())
                    .iotId(alertDataVo.getIotId())
                    .deviceName(alertDataVo.getDeviceName())
                    .nickname(alertDataVo.getNickname())
                    .productKey(alertDataVo.getProductKey())
                    .productName(alertDataVo.getProductName())
                    .functionId(alertDataVo.getFunctionId())
                    .accessLocation(alertDataVo.getRemark())
                    .locationType(alertDataVo.getLocationType())
                    .physicalLocationType(alertDataVo.getPhysicalLocationType())
                    .deviceDescription(alertDataVo.getDeviceDescription())
                    .dataValue(alertDataVo.getDataValue())
                    .alertRuleId(alertDataVo.getAlertRuleId())
                    .alertReason(alertDataVo.getAlertReason())
                    .type(alertDataVo.getType())
                    .status(alertDataVo.getStatus())
                    .userId(alertDataVo.getUserId())
                    .createTime(alertDataVo.getCreateTime())
                    .processingTime(alertDataVo.getUpdateTime())
                    .processorName(alertDataVo.getProcessorName())
                    .processingResult(alertDataVo.getProcessingResult())
                    .build();
            return build;
        }).collect(Collectors.toList());
        TableDataInfo<AlertDataVo> tableDataInfo = new TableDataInfo<>();
        tableDataInfo.setRows(collect);
        tableDataInfo.setTotal(page.getTotal());
        tableDataInfo.setMsg("请求成功");
        tableDataInfo.setCode(200);
        return tableDataInfo;
    }



    /**
     * 新增报警数据
     *
     * @param alertData 报警数据
     * @return 结果
     */
    @Override
    public int insertAlertData(AlertData alertData) {
        return alertDataMapper.insert(alertData);
    }

    /**
     * 修改报警数据
     *
     * @param params 报警数据
     * @return 结果
     */
    @Transactional
    @Override
    public int updateAlertData(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String processingResult = params.get("processingResult").toString();
        String processingTime = params.get("processingTime").toString();
        LocalDateTime parse = ZonedDateTime.parse(processingTime)
                .withZoneSameInstant(ZoneId.of("Asia/Shanghai"))
                .toLocalDateTime();
        AlertData alertDataDb = alertDataMapper.selectById(id);
        if(alertDataDb == null){
            throw new RuntimeException("数据不存在");
        }
        LocalDateTime createTime = alertDataDb.getCreateTime();
        // 获取时间范围
        LocalDateTime startTime = createTime.minusMinutes(2);
        LocalDateTime endTime = createTime.plusMinutes(2);
        List<AlertData> alertDataList = alertDataMapper.selectList(new LambdaQueryWrapper<AlertData>()
                .eq(AlertData::getIotId, alertDataDb.getIotId())
                .eq(AlertData::getFunctionId, alertDataDb.getFunctionId())
                .eq(AlertData::getDataValue,alertDataDb.getDataValue())
                .between(AlertData::getCreateTime,startTime,endTime));
        alertDataList.forEach(alertData -> {
            alertData.setProcessingResult(processingResult);
            alertData.setProcessingTime(parse);
            alertData.setStatus(1);
        });
       return updateBatchById(alertDataList) ? 1:0;
    }

    /**
     * 批量删除报警数据
     *
     * @param ids 需要删除的报警数据主键
     * @return 结果
     */
    @Override
    public int deleteAlertDataByIds(Long[] ids) {
        return alertDataMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除报警数据信息
     *
     * @param id 报警数据主键
     * @return 结果
     */
    @Override
    public int deleteAlertDataById(Long id) {
        return alertDataMapper.deleteById(id);
    }
}
