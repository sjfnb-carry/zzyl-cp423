package com.zzyl.nursing.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.domain.Room;
import com.zzyl.nursing.mapper.RoomMapper;
import com.zzyl.nursing.service.IRoomService;
import com.zzyl.nursing.vo.BedVo;
import com.zzyl.nursing.vo.DeviceInfo;
import com.zzyl.nursing.vo.RoomVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 房间Service业务层处理
 *
 * @author ruoyi
 * @date 2024-04-26
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 查询房间
     *
     * @param id 房间主键
     * @return 房间
     */
    @Override
    public Room selectRoomById(Long id) {
        return getById(id);
    }

    /**
     * 查询房间列表
     *
     * @param room 房间
     * @return 房间
     */
    @Override
    public List<Room> selectRoomList(Room room) {
        return roomMapper.selectRoomList(room);
    }

    /**
     * 新增房间
     *
     * @param room 房间
     * @return 结果
     */
    @Override
    public int insertRoom(Room room) {
        return save(room) ? 1 : 0;
    }

    /**
     * 修改房间
     *
     * @param room 房间
     * @return 结果
     */
    @Override
    public int updateRoom(Room room) {
        return updateById(room) ? 1 : 0;
    }

    /**
     * 批量删除房间
     *
     * @param ids 需要删除的房间主键
     * @return 结果
     */
    @Override
    public int deleteRoomByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 根据楼层 id 获取房间视图对象列表
     *
     * @param floorId
     * @return
     */
    @Override
    public List<RoomVo> getRoomsByFloorId(Long floorId) {
        return roomMapper.selectByFloorId(floorId);
    }


    /**
     * 获取所有房间（负责老人）
     *
     * @param floorId
     * @return
     */
    @Override
    public List<RoomVo> getRoomsWithNurByFloorId(Long floorId) {
        return roomMapper.selectByFloorIdWithNur(floorId);
    }

    /**
     * 获取房间数据
     *
     * @param roomId 房间 id
     * @return 房间数据
     */
    @Override
    public RoomVo getRoomDataById(Integer roomId) {
        return roomMapper.selectFloorAndRoomAndPriceByRoomId(roomId);
    }

    /**
     * 根据楼层ID获取房间及其设备信息
     *
     * @param floorId 楼层ID
     * @return 房间信息列表，包含房间内的设备数据
     */
    @Override
    public List<RoomVo> getRoomsWithDeviceByFloorId(Long floorId) {
        // 查询指定楼层下的所有房间及其设备信息
        List<RoomVo> roomVos = roomMapper.selectByFloorIdWithDevice(floorId);

        // 遍历所有房间，为每个设备填充最新的设备数据
        for (RoomVo roomVo : roomVos) {
            // 处理房间内的设备数据
            List<DeviceInfo> RoomDeviceVos = roomVo.getDeviceVos();
            if (CollectionUtil.isNotEmpty(RoomDeviceVos)) {
                for (DeviceInfo deviceVo : RoomDeviceVos) {
                    String iotId = deviceVo.getIotId();
                    // 从Redis中获取设备的最新数据
                    String str = (String) redisTemplate.opsForHash().get(CacheConstants.IOT_DEVICE_LAST_DATA, iotId);
                    if (StrUtil.isNotEmpty(str)) {
                        List<DeviceData> list = JSONUtil.toList(str, DeviceData.class);
                        deviceVo.setDeviceDataVos(list);
                    }

                }
            }
            // 处理床位上的设备数据
            for (BedVo bedVo : roomVo.getBedVoList()) {
                List<DeviceInfo> BedDeviceVos = bedVo.getDeviceVos();
                if (CollectionUtil.isNotEmpty(BedDeviceVos)) {
                    for (DeviceInfo deviceVo : BedDeviceVos) {
                        String iotId = deviceVo.getIotId();
                        // 从Redis中获取设备的最新数据
                        String str = (String) redisTemplate.opsForHash().get(CacheConstants.IOT_DEVICE_LAST_DATA, iotId);
                        if (StrUtil.isNotEmpty(str)) {
                            List<DeviceData> list = JSONUtil.toList(str, DeviceData.class);
                            deviceVo.setDeviceDataVos(list);
                        }

                    }
                }
            }
        }
        return roomVos;
    }

}
