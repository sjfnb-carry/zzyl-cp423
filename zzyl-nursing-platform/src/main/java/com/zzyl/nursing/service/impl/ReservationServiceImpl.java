package com.zzyl.nursing.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.UserThreadLocal;
import com.zzyl.common.utils.bean.BeanUtils;
import com.zzyl.nursing.domain.Reservation;
import com.zzyl.nursing.dto.ReservationDto;
import com.zzyl.nursing.mapper.ReservationMapper;
import com.zzyl.nursing.service.IReservationService;
import com.zzyl.nursing.vo.ReservationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 预约信息Service业务层处理
 *
 * @author alexis
 * @date 2025-09-13
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements IReservationService {
    @Autowired
    private ReservationMapper reservationMapper;

    /**
     * 查询预约信息
     *
     * @param id 预约信息主键
     * @return 预约信息
     */
    @Override
    public Reservation selectReservationById(Long id) {
        return reservationMapper.selectById(id);
    }

    /**
     * 查询预约信息列表
     *
     * @param reservation 预约信息
     * @return 预约信息
     */
    @Override
    public List<Reservation> selectReservationList(Reservation reservation) {
        Long userId = UserThreadLocal.getUserId();
        if (ObjUtil.isEmpty(userId)) {
            throw new ServiceException("请先登录");
        }
        reservation.setCreateBy(String.valueOf(userId));
        return reservationMapper.selectReservationList(reservation);
    }

    /**
     * 新增预约信息
     *
     * @param reservationDto 预约信息
     * @return 结果
     */
    @Override
    public int insertReservation(ReservationDto reservationDto) {
        //根据手机号查询数据库中是否有预约记录
        Reservation reservation = reservationMapper.selectOne(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getMobile, reservationDto.getMobile())
                .eq(Reservation::getTime, reservationDto.getTime()));
        if (ObjUtil.isNotEmpty(reservation)) {
            throw new ServiceException("此手机号已预约该时间");
        }
        reservation = new Reservation();
        BeanUtils.copyBeanProp(reservation, reservationDto);
        reservation.setStatus(0);
        reservation.setCreateBy(String.valueOf(UserThreadLocal.getUserId()));
        return reservationMapper.insert(reservation);
    }

    /**
     * 修改预约信息
     *
     * @param reservation 预约信息
     * @return 结果
     */
    @Override
    public int updateReservation(Reservation reservation) {
        return reservationMapper.updateById(reservation);
    }

    /**
     * 批量删除预约信息
     *
     * @param ids 需要删除的预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationByIds(Long[] ids) {
        return reservationMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除预约信息信息
     *
     * @param id 预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationById(Long id) {
        return reservationMapper.deleteById(id);
    }

    /**
     * 查询用户当天预约取消次数
     *
     * @return 返回用户当天预约取消的次数
     * @throws ServiceException 当取消次数超过3次时抛出异常
     */
    @Override
    public Integer searchCancelledCount() {
        // 计算当天的开始和结束时间
        long time = System.currentTimeMillis();
        LocalDateTime ldt = LocalDateTimeUtil.of(time);
        LocalDateTime startTime = ldt.toLocalDate().atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);

        // 从数据库查询当天状态为取消的预约记录
        List<Reservation> reservations = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, 2)
                .eq(Reservation::getUpdateBy, UserThreadLocal.getUserId())
                .between(Reservation::getUpdateTime, startTime, endTime));

        // 校验取消次数是否超过限制
        if (reservations.size() >= 3) {
            throw new ServiceException("预约取消次数过多,请明天再来");
        }

        return reservations.size();
    }


    /**
     * 根据时间统计预约信息
     *
     * @param time 时间戳
     * @return 预约信息列表
     */
    @Override
    public List<ReservationVo> countByTime(Long time) {
        // 将时间戳转换为LocalDateTime
        LocalDateTime ldt = LocalDateTimeUtil.of(time);
        // 计算当天的开始时间和结束时间
        LocalDateTime startTime = ldt.toLocalDate().atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);
        // 调用mapper查询指定时间段内的预约统计信息
        List<ReservationVo> list = reservationMapper.countByTime(startTime, endTime);
        return list;
    }


    /**
     * 取消预约
     *
     * @param id 预约记录的主键ID
     * @return 更新记录数，成功更新返回1，未找到记录返回0
     */
    @Override
    public Integer cancelReservation(Long id) {
        // 构造预约记录对象，设置取消状态
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setStatus(2);
        reservation.setUpdateBy(String.valueOf(UserThreadLocal.getUserId()));

        // 执行更新操作，将预约状态设置为已取消
        int num = reservationMapper.updateById(reservation);
        return num;

    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Reservation> uw = new LambdaUpdateWrapper<>();
        uw.set(Reservation::getStatus, 3)
                .eq(Reservation::getStatus, 0)
                .lt(Reservation::getTime, now);
        update(uw);
    }

}
