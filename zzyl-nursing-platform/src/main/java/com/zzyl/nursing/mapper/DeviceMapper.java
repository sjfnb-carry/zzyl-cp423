package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.zzyl.nursing.domain.Device;

/**
 * 设备表Mapper接口
 * 
 * @author alexis
 * @date 2025-09-14
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device>
{
    /**
     * 查询设备表
     * 
     * @param id 设备表主键
     * @return 设备表
     */
    public Device selectDeviceById(Long id);

    /**
     * 查询设备表列表
     * 
     * @param device 设备表
     * @return 设备表集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 新增设备表
     * 
     * @param device 设备表
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改设备表
     * 
     * @param device 设备表
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 删除设备表
     * 
     * @param id 设备表主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    /**
     * 批量删除设备表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    /**
     * 根据物联网ID查询关联的老人的护理人ID列表（通过老人关联）
     * 
     * @param iotId 物联网设备ID
     * @return 养老人员ID列表
     */
    List<Long> selectNursingIdsByIotIdWithElder(String iotId);

    /**
     * 根据物联网ID查询关联的老人的护理人ID列表（通过床位关联）
     * 
     * @param iotId 物联网设备ID
     * @return 养老人员ID列表
     */
    List<Long> selectNursingIdsByIotIdWithBed(String iotId);
}
