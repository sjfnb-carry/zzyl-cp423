package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.zzyl.nursing.domain.DeviceData;

/**
 * 设备数据表Mapper接口
 * 
 * @author alexis
 * @date 2025-09-15
 */
@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData>
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
     * 删除设备数据表
     * 
     * @param id 设备数据表主键
     * @return 结果
     */
    public int deleteDeviceDataById(Long id);

    /**
     * 批量删除设备数据表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceDataByIds(Long[] ids);
}
