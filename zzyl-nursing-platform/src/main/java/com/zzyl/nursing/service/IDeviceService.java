package com.zzyl.nursing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huaweicloud.sdk.iotda.v5.model.ServiceCapability;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.vo.DeviceDetailVo;
import com.zzyl.nursing.vo.ProductVo;

import java.util.List;
import java.util.Map;

/**
 * 设备表Service接口
 *
 * @author alexis
 * @date 2025-09-14
 */
public interface IDeviceService extends IService<Device> {
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
     * @param dto 设备表
     * @return 结果
     */
    public void updateDevice(DeviceDto dto);

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
     *
     * @return 所有产品
     */
    List<ProductVo> allProduct();

    /**
     * 设备注册
     *
     * @param dto 设备信息传输对象
     */
    void register(DeviceDto dto);

    /**
     * 根据IoT ID获取设备详情
     *
     * @param iotId IoT设备唯一标识
     * @return 设备详情信息
     */
    DeviceDetailVo getDeviceDetail(String iotId);

    /**
     * 根据IoT ID查询设备服务属性
     *
     * @param iotId IoT设备唯一标识
     * @return 服务属性列表
     */
    List<Map<String, Object>> queryServiceProperties(String iotId);

    /**
     * 根据IoT ID删除设备
     *
     * @param iotId IoT设备唯一标识
     */
    void deleteDeviceByIotId(String iotId);

    /**
     * 根据产品Key查询服务功能定义
     *
     * @param productKey 产品唯一标识
     * @return 服务功能定义列表
     */
    List<ServiceCapability> queryProduct(String productKey);

    /**
     * 根据IoT设备ID查询关联的护理人ID列表
     *
     * @param iotId IoT设备ID
     * @return 与指定IoT设备关联的护理记录ID列表
     */
    List<Long> selectNursingIdsByIotIdWithElder(String iotId);


    /**
     * 根据IoT设备ID查询关联的床位的老人的护理人ID列表
     *
     * @param iotId IoT设备ID
     * @return 与指定IoT设备关联的床位ID列表
     */
    List<Long> selectNursingIdsByIotIdWithBed(String iotId);
}
