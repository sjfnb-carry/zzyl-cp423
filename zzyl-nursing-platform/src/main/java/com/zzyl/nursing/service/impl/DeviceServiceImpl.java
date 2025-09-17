package com.zzyl.nursing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.*;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.mapper.DeviceMapper;
import com.zzyl.nursing.service.IDeviceService;
import com.zzyl.nursing.vo.DeviceDetailVo;
import com.zzyl.nursing.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备表Service业务层处理
 *
 * @author alexis
 * @date 2025-09-14
 */
@Slf4j
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private IoTDAClient ioTDAClient;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 查询设备表
     *
     * @param id 设备表主键
     * @return 设备表
     */
    @Override
    public Device selectDeviceById(Long id) {
        return deviceMapper.selectById(id);
    }

    /**
     * 查询设备表列表
     *
     * @param device 设备表
     * @return 设备表
     */
    @Override
    public List<Device> selectDeviceList(Device device) {
        return deviceMapper.selectDeviceList(device);
    }

    /**
     * 新增设备表
     *
     * @param device 设备表
     * @return 结果
     */
    @Override
    public int insertDevice(Device device) {
        return deviceMapper.insert(device);
    }


    /**
     * 更新设备信息
     *
     * @param dto 设备信息传输对象，包含要更新的设备信息
     */
    @Override
    @Transactional
    public void updateDevice(DeviceDto dto) {
        // 将DTO对象转换为设备实体对象
        Device device = new Device();
        BeanUtils.copyBeanProp(device, dto);

        // 处理位置类型为0的特殊情况
        if (dto.getLocationType() == 0) {
            device.setDeviceDescription(String.valueOf(dto.getBindingLocation()));
            device.setPhysicalLocationType(-1);
        }

        // 更新本地数据库中的设备信息
        deviceMapper.updateById(device);

        // 修改华为云平台上的设备信息
        // 从数据库查询设备的IotId
        Device deviceDb = deviceMapper.selectOne(new LambdaQueryWrapper<Device>().eq(Device::getNodeId, dto.getNodeId()));

        // 构造华为云设备更新请求
        UpdateDeviceRequest request = new UpdateDeviceRequest();
        request.withDeviceId(deviceDb.getIotId());
        UpdateDevice body = new UpdateDevice();
        AuthInfoWithoutSecret authInfobody = new AuthInfoWithoutSecret();
        authInfobody.withSecureAccess(true);
        body.withAuthInfo(authInfobody);
        body.withDeviceName(dto.getDeviceName());
        request.withBody(body);

        // 发送更新请求到华为云平台
        UpdateDeviceResponse response = ioTDAClient.updateDevice(request);
        if (response.getHttpStatusCode() != 200) {
            throw new ServiceException("修改设备信息失败");
        }

    }


    /**
     * 批量删除设备表
     *
     * @param ids 需要删除的设备表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceByIds(Long[] ids) {
        return deviceMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除设备表信息
     *
     * @param id 设备表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceById(Long id) {
        return deviceMapper.deleteById(id);
    }

    /**
     * 同步产品列表
     * 从IoT平台获取产品列表数据并存储到Redis缓存中
     *
     * @throws ServiceException 当同步产品列表失败时抛出异常
     */
    @Override
    public void syncProductList() {
        // 构造获取产品列表的请求，设置每次获取50条记录
        ListProductsRequest request = new ListProductsRequest();
        request.setLimit(50);
        ListProductsResponse response = ioTDAClient.listProducts(request);
        log.info("同步产品结束，返回结果：{}", response);
        int code = response.getHttpStatusCode();
        // 检查HTTP响应状态码，非200表示请求失败
        if (code != 200) {
            throw new ServiceException("同步产品列表失败");
        }
        List<ProductSummary> products = response.getProducts();
        // 将产品列表数据序列化为JSON字符串并存入Redis缓存
        redisTemplate.opsForValue().set(CacheConstants.IOT_PLATFORM_PRODUCT_LIST, JSONUtil.toJsonStr(products));
    }


    /**
     * 获取所有产品信息
     * 从Redis缓存中获取产品列表数据，如果缓存中没有数据则返回空列表
     *
     * @return List<ProductVo> 产品信息列表
     */
    @Override
    public List<ProductVo> allProduct() {
        // 从Redis中获取产品列表的JSON字符串
        String jsonStr = (String) redisTemplate.opsForValue().get(CacheConstants.IOT_PLATFORM_PRODUCT_LIST);
        // 如果缓存中没有数据，返回空列表
        if (StrUtil.isEmpty(jsonStr)) {
            return new ArrayList<>();
        }
        // 将JSON字符串转换为ProductVo对象列表
        List<ProductVo> list = JSONUtil.toList(jsonStr, ProductVo.class);
        return list;
    }


    /**
     * 注册设备信息
     * <p>
     * 该方法用于注册一个新的设备，包括对设备名称、标识码等唯一性校验，
     * 向华为云 IoT 平台注册设备，并将设备信息保存到本地数据库。
     * </p>
     *
     * @param dto 设备传输对象，包含设备的基本信息，如设备名称、标识码、产品Key等
     */
    @Override
    public void register(DeviceDto dto) {
        // 当位置类型为0（随身设备）时，设置物理位置类型为-1，并记录绑定位置信息到设备描述中
        if (dto.getLocationType() == 0) {
            dto.setDeviceDescription(String.valueOf(dto.getBindingLocation()));
            dto.setPhysicalLocationType(-1);
        }

        // 1. 判断设备名称是否重复
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceName, dto.getDeviceName()));
        if (device != null) {
            throw new ServiceException("设备名称重复");
        }

        // 2. 检验设备标识码是否重复
        device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getNodeId, dto.getNodeId()));
        if (device != null) {
            throw new ServiceException("设备标识码重复");
        }

        // 3. 校验同一位置是否绑定了同一类产品（数据库有唯一约束，异常由全局处理器处理）
        LambdaQueryWrapper<Device> condition = new LambdaQueryWrapper<>();
        condition.eq(Device::getProductKey, dto.getProductKey())
                .eq(Device::getLocationType, dto.getLocationType())
                .eq(Device::getPhysicalLocationType, dto.getPhysicalLocationType())
                .eq(Device::getBindingLocation, dto.getBindingLocation());
        if (count(condition) > 0) {
            throw new BaseException("该老人/位置已绑定该产品，请重新选择");
        }

        // 4. 调用华为云SDK向IoT平台注册设备
        AddDeviceRequest request = new AddDeviceRequest();
        AddDevice body = new AddDevice();

        // 设置产品ID
        body.withProductId(dto.getProductKey());
        // 设置设备名称
        body.withDeviceName(dto.getDeviceName());
        // 设置设备标识码
        body.withNodeId(dto.getNodeId());

        // 设置设备密钥
        AuthInfo autoInfoBody = new AuthInfo();
        String secret = UUID.randomUUID().toString().replace("-", "");
        autoInfoBody.withSecret(secret);
        body.setAuthInfo(autoInfoBody);

        request.withBody(body);
        AddDeviceResponse response = null;
        try {
            response = ioTDAClient.addDevice(request);
        } catch (Exception e) {
            throw new ServiceException("物联网接口 - 注册设备，调用失败");
        }

        // 判断设备注册是否成功
        int code = response.getHttpStatusCode();
        if (code != 201) {
            throw new ServiceException("设备注册失败");
        }

        // 5. 将设备信息保存到本地数据库
        device = new Device();
        // 复制dto中的属性到device实体中
        BeanUtils.copyBeanProp(device, dto);
        // 设置从华为云返回的设备密钥和IoT ID
        device.setSecret(response.getAuthInfo().getSecret());
        device.setIotId(response.getDeviceId());

        // 如果是特定类型的位置设备，则标记为有门禁权限
        if (dto.getLocationType() == 1 && dto.getPhysicalLocationType() == 0) {
            device.setHaveEntranceGuard(1);
        }

        deviceMapper.insert(device);
    }


    /**
     * 获取设备详细信息
     *
     * @param iotId 设备ID，不能为空
     * @return DeviceDetailVo 设备详细信息对象
     * @throws ServiceException 当设备ID为空、设备不存在或调用华为云接口失败时抛出
     */
    @Override
    public DeviceDetailVo getDeviceDetail(String iotId) {
        if (iotId == null) {
            throw new ServiceException("设备ID不能为空");
        }
        DeviceDetailVo deviceDetailVo = new DeviceDetailVo();
        //查询华为云获取设备信息
        ShowDeviceRequest request = new ShowDeviceRequest();
        request.withDeviceId(iotId);
        ShowDeviceResponse response = null;
        try {
            response = ioTDAClient.showDevice(request);
        } catch (Exception e) {
            log.error("调用华为云接口获取设备详情失败: ", e);
            throw new ServiceException("获取华为云中设备详细失败");
        }
        if (response.getHttpStatusCode() != 200) {
            throw new ServiceException("获取华为云中设备详细失败, 状态码: " + response.getHttpStatusCode());
        }
        if (StrUtil.isNotEmpty(response.getActiveTime())) {
            //2019-03-03T08:10:111Z
            deviceDetailVo.setActiveTime(LocalDateTime.parse(response.getActiveTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));

        }
        deviceDetailVo.setDeviceStatus(response.getStatus());
        //查询本地数据库获取设备信息
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>().eq(Device::getIotId, iotId));
        if (device == null) {
            throw new ServiceException("设备不存在");
        }
        //合并华为云和数据库的设备信息
        BeanUtils.copyBeanProp(deviceDetailVo, device);
        return deviceDetailVo;
    }


    /**
     * 查询设备服务属性
     *
     * @param iotId 设备ID，不能为空
     * @return 设备属性列表，每个元素包含eventTime、functionId、value三个字段
     * @throws ServiceException 当设备ID为空或获取设备属性失败时抛出异常
     */
    @Override
    public List<Map<String, Object>> queryServiceProperties(String iotId) {
        if (StrUtil.isEmpty(iotId)) {
            throw new ServiceException("设备ID不能为空");
        }
        ShowDeviceShadowRequest request = new ShowDeviceShadowRequest();
        request.withDeviceId(iotId);
        ShowDeviceShadowResponse response = ioTDAClient.showDeviceShadow(request);
        if (response.getHttpStatusCode() != 200) {
            throw new ServiceException("获取设备属性失败");
        }
        List<DeviceShadowData> shadow = response.getShadow();
        // 处理设备影子数据，提取属性信息
        if (CollectionUtil.isNotEmpty(shadow)) {
            DeviceShadowProperties reported = shadow.get(0).getReported();
            String eventTime = reported.getEventTime();
            Map<String, Object> properties = (Map<String, Object>) reported.getProperties();
            if (CollectionUtil.isNotEmpty(properties)) {
                Set<Map.Entry<String, Object>> entries = properties.entrySet();
                List<Map<String, Object>> collect = entries.stream().map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("eventTime", LocalDateTime.parse(eventTime, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")));
                    map.put("functionId", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                }).collect(Collectors.toList());
                return collect;
            }
        }

        return List.of();
    }


    @Override
    public void deleteDeviceByIotId(String iotId) {
        //删除华为云
        DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.withDeviceId(iotId);
        DeleteDeviceResponse response = ioTDAClient.deleteDevice(request);
        if (response.getHttpStatusCode() != 204) {
            throw new ServiceException("删除设备失败");
        }
        //删除数据库
        deviceMapper.delete(new LambdaQueryWrapper<Device>().eq(Device::getIotId, iotId));

    }

}
