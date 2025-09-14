package com.zzyl.nursing.service.impl;

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
import com.zzyl.nursing.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
     * 修改设备表
     *
     * @param device 设备表
     * @return 结果
     */
    @Override
    public int updateDevice(Device device) {
        return deviceMapper.updateById(device);
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


    @Override
    public List<ProductVo> allProduct() {
        String jsonStr = (String) redisTemplate.opsForValue().get(CacheConstants.IOT_PLATFORM_PRODUCT_LIST);
        if (StrUtil.isEmpty(jsonStr)) {
            return new ArrayList<>();
        }
        List<ProductVo> list = JSONUtil.toList(jsonStr, ProductVo.class);
        return list;
    }

    @Override
    public void register(DeviceDto dto) {
        //位置类型为0随身设备时，设置物理位置类型为-1
        if (dto.getLocationType() == 0) {
            dto.setDeviceDescription(String.valueOf(dto.getBindingLocation()));
            dto.setPhysicalLocationType(-1);
        }
        //1.判断设备名称是否重复 productName
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceName, dto.getDeviceName()));
        if (device != null) {
            throw new ServiceException("设备名称重复");
        }
        //2.检验设备标识码是否重复 nodeId
        device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getNodeId, dto.getNodeId()));
        if (device != null) {
            throw new ServiceException("设备标识码重复");
        }
        //3.校验同一位置是否绑定了同一类产品【数据库有唯一约束，可以走异常处理器】忽略
        LambdaQueryWrapper<Device> condition = new LambdaQueryWrapper<>();
        condition.eq(Device::getProductKey, dto.getProductKey())
                .eq(Device::getLocationType, dto.getLocationType())
                .eq(Device::getPhysicalLocationType, dto.getPhysicalLocationType())
                .eq(Device::getBindingLocation, dto.getBindingLocation());
        if (count(condition) > 0) {
            throw new BaseException("该老人/位置已绑定该产品，请重新选择");
        }
        //4.调用华为云的sdk，向iot中新增设备
        AddDeviceRequest request = new AddDeviceRequest();
        AddDevice body = new AddDevice();
        // 产品id
        body.withProductId(dto.getProductKey());
        // 设备名称
        body.withDeviceName(dto.getDeviceName());
        // 设备标识码
        body.withNodeId(dto.getNodeId());

        // 设备密钥
        AuthInfo authInfobody = new AuthInfo();
        String secret = UUID.randomUUID().toString().replace("-", "");
        authInfobody.withSecret(secret);

        body.setAuthInfo(authInfobody);

        request.withBody(body);
        AddDeviceResponse response = null;
        try {
            response = ioTDAClient.addDevice(request);
        } catch (Exception e) {
            throw new ServiceException("物联网接口 - 注册设备，调用失败");
        }
        int code = response.getHttpStatusCode();
        if (code != 201) {
            throw new ServiceException("设备注册失败");
        }
        //5.设备数据保存到数据库
        device = new Device();
        //填充数据
        BeanUtils.copyBeanProp(device, dto);
        device.setSecret(response.getAuthInfo().getSecret());
        device.setIotId(response.getDeviceId());
        if (dto.getLocationType() == 1 && dto.getPhysicalLocationType() == 0) {
            device.setHaveEntranceGuard(1);
        }
        deviceMapper.insert(device);

    }
}
