package com.zzyl.nursing.service;

import java.util.List;

import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.DeviceData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.dto.DeviceDataPageReqDto;

/**
 * 设备数据表Service接口
 * 
 * @author alexis
 * @date 2025-09-15
 */
public interface IDeviceDataService extends IService<DeviceData>
{
    /**
     * 查询设备数据表
     * 
     * @param id 设备数据表主键
     * @return 设备数据表
     */
    public DeviceData selectDeviceDataById(Long id);

    /**
     * 查询设备数据表列表
     * 
     * @param deviceData 设备数据表
     * @return 设备数据表集合
     */
    public List<DeviceData> selectDeviceDataList(DeviceData deviceData);

    /**
     * 新增设备数据表
     * 
     * @param deviceData 设备数据表
     * @return 结果
     */
    public int insertDeviceData(DeviceData deviceData);

    /**
     * 修改设备数据表
     * 
     * @param deviceData 设备数据表
     * @return 结果
     */
    public int updateDeviceData(DeviceData deviceData);

    /**
     * 批量删除设备数据表
     * 
     * @param ids 需要删除的设备数据表主键集合
     * @return 结果
     */
    public int deleteDeviceDataByIds(Long[] ids);

    /**
     * 删除设备数据表信息
     * 
     * @param id 设备数据表主键
     * @return 结果
     */
    public int deleteDeviceDataById(Long id);


    TableDataInfo<DeviceData> selectDeviceDataList(DeviceDataPageReqDto deviceDataPageReqDto);
}
