package com.zzyl.nursing.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.core.domain.model.LoginUser;
import com.zzyl.common.utils.IDCardUtils;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.dto.NursingTaskDto;
import com.zzyl.nursing.dto.NursingTaskExecutionDto;
import com.zzyl.nursing.mapper.NursingTaskMapper;
import com.zzyl.nursing.service.*;
import com.zzyl.nursing.vo.NursingPlanVo;
import com.zzyl.nursing.vo.NursingProjectPlanVo;
import com.zzyl.nursing.vo.NursingTaskDetailVo;
import com.zzyl.nursing.vo.NursingTaskVo;
import com.zzyl.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 护理任务Service业务层处理
 *
 * @author alexis
 * @date 2024-11-17
 */
@Slf4j
@Service
public class NursingTaskServiceImpl extends ServiceImpl<NursingTaskMapper, NursingTask> implements INursingTaskService {
    @Autowired
    private NursingTaskMapper nursingTaskMapper;

    @Autowired
    private ICheckInService checkInService;

    @Autowired
    private ICheckInConfigService checkInConfigService;

    @Autowired
    private INursingLevelService nursingLevelService;

    @Autowired
    private INursingPlanService nursingPlanService;

    @Autowired
    private INursingElderService nursingElderService;

    @Autowired
    private INursingProjectService nursingProjectService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IElderService elderService;


    /**
     * 根据ID查询护理任务详情
     *
     * @param id 护理任务ID
     * @return 护理任务详情对象
     */
    @Override
    public NursingTaskDetailVo selectNursingTaskById(Long id) {
        NursingTaskDetailVo nursingTask = nursingTaskMapper.selectDetailById(id);

        //获取老人年龄
        Elder elder = elderService.getOne(new LambdaQueryWrapper<Elder>().eq(Elder::getId, nursingTask.getElderId()));
        String idCardNo = elder.getIdCardNo();
        int age = IDCardUtils.getAgeByIdCard(idCardNo);
        nursingTask.setAge(age);

        //获取护理人姓名列表
        String nursingId = nursingTask.getNursingId();
        List<String> nursingName = getNursingName(nursingId);
        nursingTask.setNursingName(nursingName);

        //获取执行人
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String updater = loginUser.getUser().getNickName();
        nursingTask.setUpdater(updater);
        return nursingTask;
    }


    /**
     * 查询护理任务列表
     *
     * @param nursingTaskDto 护理任务
     * @return 护理任务
     */
    @Override
    public List<NursingTaskVo> selectNursingTaskList(NursingTaskDto nursingTaskDto) {
        List<NursingTaskVo> nursingTasks = nursingTaskMapper.selectNursingTaskList(nursingTaskDto);
        nursingTasks.forEach(nursingTaskVo -> {
            String nursingId = nursingTaskVo.getNursingId();
            List<String> name = getNursingName(nursingId);
            nursingTaskVo.setNursingName(name);
        });
        return nursingTasks;
    }

    /**
     * 取消护理任务
     *
     * @param params 包含任务取消参数的Map，必须包含：
     *               - "taskId": 任务ID（Long类型）
     *               - "reason": 取消原因（String类型）
     */
    @Override
    public void cancel(Map<String, Object> params) {
        // 提取任务ID和取消原因
        Long id = Long.valueOf(params.get("taskId").toString());
        String reason = params.get("reason").toString();

        // 构造护理任务对象并更新状态
        NursingTask nursingTask = new NursingTask();
        nursingTask.setId(id);
        nursingTask.setCancelReason(reason);
        nursingTask.setStatus(3);// 已关闭
        nursingTaskMapper.updateById(nursingTask);

    }


    /**
     * 执行护理任务处理
     *
     * @param dto 护理任务执行数据传输对象，包含任务ID、预计服务时间、标记、任务图片等信息
     */
    @Override
    public void doTask(NursingTaskExecutionDto dto) {
        // 构建护理任务对象并更新数据库记录
        NursingTask nursingTask = NursingTask.builder().id(dto.getTaskId())
                .realServerTime(dto.getEstimatedServerTime())
                .mark(dto.getMark())
                .taskImage(dto.getTaskImage())
                .status(2).build();
        nursingTaskMapper.updateById(nursingTask);


    }


    /**
     * 更新护理任务的预估服务时间
     *
     * @param params 包含更新参数的Map，必须包含taskId(任务ID)和estimatedServerTime(预估服务时间)两个键值对
     */
    @Override
    public void updateTime(Map<String, Object> params) {
        // 从参数中提取任务ID和预估服务时间
        Long taskId = Long.valueOf(params.get("taskId").toString());
        Object estimatedServerTime = params.get("estimatedServerTime");
        LocalDateTime time = LocalDateTime.parse(estimatedServerTime.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 构建护理任务对象并更新数据库
        NursingTask nursingTask = NursingTask.builder().id(taskId).estimatedServerTime(time).build();
        nursingTaskMapper.updateById(nursingTask);


    }


    /**
     * 根据护理人员ID获取对应的昵称列表
     *
     * @param nursingId 护理人员ID字符串，多个ID用逗号分隔
     * @return 护理人员昵称列表，如果输入为空则返回null
     */
    private List<String> getNursingName(String nursingId) {
        if (StringUtils.isNotEmpty(nursingId)) {
            // 解析护理人员ID列表
            String[] nursingIds = nursingId.split(",");
            List<Long> nursingIdList = Arrays.stream(nursingIds)
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            // 根据ID列表查询对应的用户昵称
            return nursingIdList.stream().map(id -> sysUserService.selectUserById(id).getNickName()).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 新增护理任务
     *
     * @param nursingTask 护理任务
     * @return 结果
     */
    @Override
    public int insertNursingTask(NursingTask nursingTask) {
        return save(nursingTask) ? 1 : 0;
    }

    /**
     * 修改护理任务
     *
     * @param nursingTask 护理任务
     * @return 结果
     */
    @Override
    public int updateNursingTask(NursingTask nursingTask) {
        return updateById(nursingTask) ? 1 : 0;
    }

    /**
     * 批量删除护理任务
     *
     * @param ids 需要删除的护理任务主键
     * @return 结果
     */
    @Override
    public int deleteNursingTaskByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除护理任务信息
     *
     * @param id 护理任务主键
     * @return 结果
     */
    @Override
    public int deleteNursingTaskById(Long id) {
        return removeById(id) ? 1 : 0;
    }


    /**
     * 生成月度护理任务
     *
     * @param elder 老人信息
     */
    @Transactional
    @Override
    public void generateMonthlyTask(Elder elder) {
        // 校验
        if (elder == null || elder.getId() == null) {
            log.info("老人不能为空");
            return;
        }

        // 获取入住信息
        CheckIn checkIn = checkInService.getOne(Wrappers.<CheckIn>lambdaQuery().eq(CheckIn::getElderId, elder.getId()));

        // 获取入住及入住配置信息
        CheckInConfig checkInConfig = checkInConfigService.getOne(Wrappers.<CheckInConfig>lambdaQuery().eq(CheckInConfig::getCheckInId, checkIn.getId()));
        if (ObjectUtil.isEmpty(checkInConfig)) {
            log.info("入住配置不能为空");
            return;
        }
        // 开始时间
        LocalDateTime startTime = LocalDateTime.now();
        // 判断费用开始时间是否等于当天时间
        if (checkInConfig.getFeeStartDate().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            startTime = checkInConfig.getFeeStartDate();
        }

        // 护理等级
        NursingLevel nursingLevel = nursingLevelService.selectNursingLevelById(checkInConfig.getNursingLevelId());
        // 护理计划
        NursingPlanVo nursingPlanVo = nursingPlanService.selectNursingPlanById(nursingLevel.getLplanId());
        // 查询老人对应的护理员列表
        List<NursingElder> nursingElderList = nursingElderService.list(Wrappers.<NursingElder>lambdaQuery().eq(NursingElder::getElderId, elder.getId()));
        String nursingIds;
        if (CollUtil.isNotEmpty(nursingElderList)) {
            List<Long> list = nursingElderList.stream().map(NursingElder::getNursingId).collect(Collectors.toList());
            // 多个id用逗号分隔，转换为字符串
            nursingIds = StringUtils.join(list, ",");
        } else {
            nursingIds = "";
        }

        // 查询所有的护理项目
        List<NursingProject> list = nursingProjectService.list();
        // 转换为map key是id,value是name
        Map<Long, String> nursingProjectMap = list.stream().collect(Collectors.toMap(NursingProject::getId, NursingProject::getName));

        // 组装护理任务
        List<NursingTask> nursingTasks = new ArrayList<>();

        LocalDateTime finalStartTime = startTime;
        nursingPlanVo.getProjectPlans().forEach(planVo -> {
            // 执行频次
            Integer executeFrequency = Integer.valueOf(String.valueOf(planVo.getExecuteFrequency()));
            // 执行时间
            String executeTime = planVo.getExecuteTime();
            LocalTime localTime = LocalTime.parse(executeTime);
            // 开始执行时间
            LocalDateTime firstExecutionTime = LocalDateTime.of(finalStartTime.toLocalDate(), localTime);
            // 计算相差天数
            LocalDateTime monthEndTime = LocalDateTime.of(finalStartTime.getYear(), checkInConfig.getFeeStartDate().getMonth(), finalStartTime.toLocalDate().lengthOfMonth(), 23, 59);
            // 间隔天数
            long diffDays = monthEndTime.toLocalDate().toEpochDay() - finalStartTime.toLocalDate().toEpochDay() + 1;
            if ("0".equals(planVo.getExecuteCycle())) {
                // 日
                generateTaskByDay(firstExecutionTime, diffDays, nursingTasks, executeFrequency, elder, planVo, nursingIds, nursingProjectMap);
            } else if ("1".equals(planVo.getExecuteCycle())) {
                // 周
                generateTaskByWeek(firstExecutionTime, diffDays, nursingTasks, executeFrequency, elder, planVo, monthEndTime, nursingIds, nursingProjectMap);
            } else {
                // 月
                generateTaskByMonth(firstExecutionTime, monthEndTime, nursingTasks, executeFrequency, elder, planVo, nursingIds, nursingProjectMap);
            }
        });

        if (CollUtil.isEmpty(nursingTasks)) {
            return;
        }
        saveBatch(nursingTasks);
    }


    /**
     * 按月创建任务
     *
     * @param firstExecutionTime
     * @param monthEndTime
     * @param nursingTasks
     * @param executeFrequency
     * @param elder
     * @param v
     */
    private void generateTaskByMonth(LocalDateTime firstExecutionTime, LocalDateTime monthEndTime, List<NursingTask> nursingTasks, Integer executeFrequency, Elder elder, NursingProjectPlanVo v, String nursingIds, Map<Long, String> nursingProjectMap) {
        LocalDateTime executionTime = firstExecutionTime;
        Integer diffDay = (monthEndTime.plusSeconds(1).getDayOfMonth() - executionTime.getDayOfMonth()) / executeFrequency;
        for (int x = 0; x < executeFrequency; x++) {
            // 根据时间差和执行顺序计算每个任务的具体时间
            LocalDateTime seconds = executionTime.plusDays(diffDay * x);
            // 初始化护理任务对象
            NursingTask nursingTask = getNursingTask(elder, v, nursingIds, seconds, nursingProjectMap);
            // 将生成的任务添加到任务列表中
            nursingTasks.add(nursingTask);
        }
    }

    /**
     * 根据周为单位创建护理任务
     * <p>
     * 此方法旨在根据给定的开始时间、差异天数、执行频率等参数，为指定的老人生成护理任务列表
     * 它考虑了跨年情况以及每月结束时间的限制，避免生成越过月末的有效任务
     *
     * @param firstExecutionTime 首次执行时间，用于计算后续任务的时间点
     * @param diffDays           任务之间的时间差，以天为单位，此方法专注于周级别，故此值应与7的倍数有关
     * @param nursingTasks       护理任务列表，方法将新生成的任务添加到此列表中
     * @param executeFrequency   执行频率，决定每周内任务执行的次数
     * @param elder              老人信息对象，任务相关的老人信息由此对象提供
     * @param v                  护理项目计划的视图对象，包含项目ID等信息
     * @param monthEndTime       每月结束时间的限制，确保任务不会跨月生成
     * @param nursingIds         护理员ID列表，以字符串形式传递，用于分配任务给护理员
     */
    private void generateTaskByWeek(LocalDateTime firstExecutionTime, long diffDays, List<NursingTask> nursingTasks, Integer executeFrequency, Elder elder, NursingProjectPlanVo v, LocalDateTime monthEndTime, String nursingIds, Map<Long, String> nursingProjectMap) {
        int i;
        // 以7天为步长遍历差异天数，创建每周的任务
        for (i = 0; i < diffDays - 7; i = i + 7) {
            // 计算每周结束时间
            LocalDateTime dayEndTime = LocalDateTime.of(firstExecutionTime.plusDays(i + 7).toLocalDate(), LocalTime.of(23, 59));
            // 计算本周的执行起始时间
            LocalDateTime executionTime = firstExecutionTime.plusDays(i);
            // 根据执行频率计算时间差，用于确定本周内各任务的时间点
            Integer diffDay = (dayEndTime.plusSeconds(1).getDayOfYear() - executionTime.getDayOfYear()) / executeFrequency;
            // 根据执行频率生成本周的任务
            for (int x = 0; x < executeFrequency; x++) {
                // 根据时间差和执行顺序计算每个任务的具体时间
                LocalDateTime seconds = executionTime.plusDays(diffDay * x);
                // 初始化护理任务对象
                NursingTask nursingTask = getNursingTask(elder, v, nursingIds, seconds, nursingProjectMap);
                // 将生成的任务添加到任务列表中
                nursingTasks.add(nursingTask);
            }
        }

        // 处理边界情况，当i超过diffDays-7且小于diffDays时
        if (i > diffDays - 7 && i < diffDays) {
            // 计算到达diffDays时的结束时间
            LocalDateTime dayEndTime = LocalDateTime.of(firstExecutionTime.plusDays(i + 7).toLocalDate(), LocalTime.of(23, 59));
            // 如果结束时间与开始时间年份不同，则返回，避免跨年任务
            if (ObjectUtil.notEqual(dayEndTime.getYear(), firstExecutionTime.getYear())) {
                return;
            }
            // 计算执行时间
            LocalDateTime executionTime = firstExecutionTime.plusDays(i);
            // 计算时间差，确定任务时间点
            Integer diffDay = (dayEndTime.plusSeconds(1).getDayOfYear() - executionTime.getDayOfYear()) / executeFrequency;
            // 根据执行频率生成任务，直到月末
            for (int x = 0; x < executeFrequency; x++) {
                // 计算每个任务的时间
                LocalDateTime seconds = executionTime.plusDays(diffDay * x);
                // 如果任务时间超过月末结束时间，则停止生成
                if (seconds.isAfter(monthEndTime)) {
                    break;
                }
                // 初始化护理任务对象
                NursingTask nursingTask = getNursingTask(elder, v, nursingIds, seconds, nursingProjectMap);
                // 将生成的任务添加到任务列表中
                nursingTasks.add(nursingTask);
            }
        }

    }

    /**
     * 按日创建任务
     * @param firstExecutionTime
     * @param diffDays
     * @param nursingTasks
     * @param executeFrequency
     * @param elder
     * @param v
     */
    /**
     * 根据天数创建护理任务
     * <p>
     * 该方法根据首次执行时间、任务间隔天数、护理任务列表、执行频率、老人信息和护理项目计划数据，
     * 创建一系列的护理任务。任务的创建以天为单位，直到达到任务间隔天数为止。
     *
     * @param firstExecutionTime 首次执行时间，标志着任务创建的起始时间
     * @param diffDays           任务间隔天数，确定需要创建任务的天数范围
     * @param nursingTasks       护理任务列表，创建的任务将被添加到此列表中
     * @param executeFrequency   执行频率，决定一天内任务执行的次数
     * @param elder              老人对象，包含执行任务的老人的信息
     * @param v                  护理项目计划数据对象，包含护理项目的相关信息
     */
    private void generateTaskByDay(LocalDateTime firstExecutionTime, long diffDays, List<NursingTask> nursingTasks, Integer executeFrequency, Elder elder, NursingProjectPlanVo v, String nursingIds, Map<Long, String> nursingProjectMap) {
        // 遍历每一天，从首次执行时间开始，直到达到任务间隔天数
        for (int i = 0; i < diffDays; i++) {
            // 计算每一天的任务执行起始时间
            LocalDateTime executionTime = firstExecutionTime.plusDays(i);
            // 确定当天任务执行的结束时间（当天的23:59）
            LocalDateTime dayEndTime = LocalDateTime.of(executionTime.toLocalDate(), LocalTime.of(23, 59));
            // 计算当天内每次执行任务之间的小时差，用于确定执行时间点
            Integer diffHour = (dayEndTime.plusSeconds(1).getHour() - executionTime.getHour()) / executeFrequency;
            // 根据执行频率创建任务
            for (int x = 0; x < executeFrequency; x++) {
                // 计算每个任务的具体执行时间
                LocalDateTime seconds = executionTime.plusHours(diffHour * x);
                // 创建新的护理任务对象
                NursingTask nursingTask = getNursingTask(elder, v, nursingIds, seconds, nursingProjectMap);
                // 将生成的任务添加到任务列表中
                nursingTasks.add(nursingTask);
            }
        }
    }

    /**
     * 获取护理任务对象
     *
     * @param elder
     * @param v
     * @param nursingIds
     * @param seconds
     * @return
     */
    private NursingTask getNursingTask(Elder elder, NursingProjectPlanVo v, String nursingIds, LocalDateTime seconds, Map<Long, String> nursingProjectMap) {
        NursingTask nursingTask = new NursingTask();
        // 设置任务状态
        nursingTask.setStatus(1);
        // 设置护理员ID列表
        nursingTask.setNursingId(nursingIds);
        // 老人姓名
        nursingTask.setElderName(elder.getName());
        // 设置床位号
        nursingTask.setBedNumber(elder.getBedNumber());
        // 设置预估服务时间
        nursingTask.setEstimatedServerTime(seconds);
        // 设置项目ID
        nursingTask.setProjectId(Integer.valueOf(v.getProjectId()));
        // 设置老人ID
        nursingTask.setElderId(elder.getId());
        // 匹配护理项目
        nursingTask.setProjectName(nursingProjectMap.get(Long.valueOf(nursingTask.getProjectId())));
        // 设置任务类型
        nursingTask.setTaskType(2);
        return nursingTask;
    }
}
