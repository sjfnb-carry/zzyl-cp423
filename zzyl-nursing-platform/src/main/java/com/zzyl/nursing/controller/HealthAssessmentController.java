package com.zzyl.nursing.controller;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.PDFUtil;
import com.zzyl.common.utils.file.FileUtils;
import com.zzyl.common.utils.poi.ExcelUtil;
import com.zzyl.nursing.domain.HealthAssessment;
import com.zzyl.nursing.dto.HealthAssessmentDto;
import com.zzyl.nursing.service.IHealthAssessmentService;
import com.zzyl.oss.AliyunOSSOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 健康评估Controller
 *
 * @author sjf
 * @date 2025-09-08
 */
@Api(tags = "健康评估管理")
@RestController
@RequestMapping("/nursing/healthAssessment")
public class HealthAssessmentController extends BaseController {
    @Autowired
    private IHealthAssessmentService healthAssessmentService;
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    /**
     * 查询健康评估列表
     */
    @ApiOperation("查询健康评估列表")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:list')")
    @GetMapping("/list")
    public TableDataInfo<List<HealthAssessment>> list(@ApiParam("健康评估查询条件") HealthAssessment healthAssessment) {
        startPage();
        List<HealthAssessment> list = healthAssessmentService.selectHealthAssessmentList(healthAssessment);
        return getDataTable(list);
    }

    /**
     * 导出健康评估列表
     */
    @ApiOperation("导出健康评估列表")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:export')")
    @Log(title = "健康评估", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@ApiParam(value = "健康评估查询条件") HttpServletResponse response, HealthAssessment healthAssessment) {
        List<HealthAssessment> list = healthAssessmentService.selectHealthAssessmentList(healthAssessment);
        ExcelUtil<HealthAssessment> util = new ExcelUtil<HealthAssessment>(HealthAssessment.class);
        util.exportExcel(response, list, "健康评估数据");
    }

    /**
     * 获取健康评估详细信息
     */
    @ApiOperation("获取健康评估详细信息")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:query')")
    @GetMapping(value = "/{id}")
    public R<HealthAssessment> getInfo(@ApiParam("健康评估ID") @PathVariable("id") Long id) {
        return R.ok(healthAssessmentService.selectHealthAssessmentById(id));
    }

    /**
     * 新增健康评估
     */
    @ApiOperation("新增健康评估")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:add')")
    @Log(title = "健康评估", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@ApiParam("健康评估信息") @RequestBody HealthAssessmentDto healthAssessmentDto) {
        return AjaxResult.success(healthAssessmentService.insertHealthAssessment(healthAssessmentDto));
    }

    /**
     * 修改健康评估
     */
    @ApiOperation("修改健康评估")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:edit')")
    @Log(title = "健康评估", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@ApiParam("健康评估信息") @RequestBody HealthAssessment healthAssessment) {
        return toAjax(healthAssessmentService.updateHealthAssessment(healthAssessment));
    }

    /**
     * 删除健康评估
     */
    @ApiOperation("删除健康评估")
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:remove')")
    @Log(title = "健康评估", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam("健康评估ID数组") @PathVariable Long[] ids) {
        return toAjax(healthAssessmentService.deleteHealthAssessmentByIds(ids));
    }

    /**
     * 健康文档上传接口
     *
     * @param file     上传的文件对象，包含文件数据和原始文件名
     * @param idCardNo 用户身份证号码，用于关联健康文档
     * @return AjaxResult 包含上传结果的对象，成功时包含文件访问URL、文件名等信息，失败时包含错误信息
     * @throws Exception 文件上传或处理过程中可能抛出的异常
     */
    @ApiOperation("健康文档上传")
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file, @RequestParam String idCardNo) throws Exception {
        try {
            if (file.isEmpty()) {
                throw new ServiceException("提交报告不能为空");
            }
            // 上传文件到阿里云OSS并获取访问URL
            String url = aliyunOSSOperator.upload(file.getBytes(), Objects.requireNonNull(file.getOriginalFilename()));
            //读取PDF文件内容，并存入redis中
            String content = PDFUtil.pdfToString(file.getInputStream());
            if (content != null) {
                redisTemplate.opsForHash().put(CacheConstants.HEALTH_REPORT, idCardNo, content);
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", url);
            ajax.put("newFileName", FileUtils.getName(url));
            ajax.put("originalFilename", file.getOriginalFilename());

            redisTemplate.opsForSet().add(CacheConstants.GARBAGE_FILE, url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

}
