package com.zzyl.nursing.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.dto.DeviceDataPageReqDto;
import com.zzyl.nursing.mapper.DeviceDataMapper;
import com.zzyl.nursing.service.IDeviceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 设备数据表Service业务层处理
 *
 * @author alexis
 * @date 2025-09-15
 */
@Service
public class DeviceDataServiceImpl extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService {
    @Autowired
    private DeviceDataMapper deviceDataMapper;

    /**
     * 查询设备数据表
     *
     * @param id 设备数据表主键
     * @return 设备数据表
     */
    @Override
    public DeviceData selectDeviceDataById(Long id) {
        return deviceDataMapper.selectById(id);
    }

    /**
     * 查询设备数据表列表
     *
     * @param deviceData 设备数据表
     * @return 设备数据表
     */
    @Override
    public List<DeviceData> selectDeviceDataList(DeviceData deviceData) {
        return deviceDataMapper.selectDeviceDataList(deviceData);
    }

    /**
     * 新增设备数据表
     *
     * @param deviceData 设备数据表
     * @return 结果
     */
    @Override
    public int insertDeviceData(DeviceData deviceData) {
        return deviceDataMapper.insert(deviceData);
    }

    /**
     * 修改设备数据表
     *
     * @param deviceData 设备数据表
     * @return 结果
     */
    @Override
    public int updateDeviceData(DeviceData deviceData) {
        return deviceDataMapper.updateById(deviceData);
    }

    /**
     * 批量删除设备数据表
     *
     * @param ids 需要删除的设备数据表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataByIds(Long[] ids) {
        return deviceDataMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除设备数据表信息
     *
     * @param id 设备数据表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataById(Long id) {
        return deviceDataMapper.deleteById(id);
    }

    @Override
    public AjaxResult selectDeviceDataList(DeviceDataPageReqDto dto) {
        IPage<DeviceData> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<DeviceData> qw = new LambdaQueryWrapper<>();
        qw.eq(StrUtil.isNotEmpty(dto.getDeviceName()), DeviceData::getDeviceName, dto.getDeviceName())
                .eq(StrUtil.isNotEmpty(dto.getFunctionId()), DeviceData::getFunctionId, dto.getFunctionId())
                .between(DeviceData::getAlarmTime, dto.getStartTime(), dto.getEndTime());
        IPage<DeviceData> deviceDataIPage = deviceDataMapper.selectPage(page, qw);
        HashMap<String, Object> map = new HashMap<>();
        map.put("page", dto.getPageNum());
        map.put("pageSize", dto.getPageSize());
        map.put("pages", deviceDataIPage.getPages());
        map.put("records", deviceDataIPage.getRecords());
        map.put("total", deviceDataIPage.getTotal());
        AjaxResult ajaxResult = new AjaxResult(0, "操作成功");
        ajaxResult.put("data", map);
        ajaxResult.put("operationTime", LocalDateTime.now());
        return ajaxResult;
    }


}
