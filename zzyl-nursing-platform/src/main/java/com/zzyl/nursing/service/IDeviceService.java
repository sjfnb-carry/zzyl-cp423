package com.zzyl.nursing.service;

import java.util.List;
import com.zzyl.nursing.domain.Device;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.vo.ProductVo;

/**
 * 设备表Service接口
 * 
 * @author alexis
 * @date 2025-09-14
 */
public interface IDeviceService extends IService<Device>
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
     * 批量删除设备表
     * 
     * @param ids 需要删除的设备表主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    /**
     * 删除设备表信息
     * 
     * @param id 设备表主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    /**
     * 同步产品列表
     */
    void syncProductList();

    /**
     * 查询所有产品列表
     * @return 所有产品
     */
    List<ProductVo> allProduct();

    void register(DeviceDto dto);
}
