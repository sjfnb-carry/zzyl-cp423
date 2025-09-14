package com.zzyl.framework.web.exception;

import com.zzyl.common.constant.HttpStatus;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.text.Convert;
import com.zzyl.common.exception.DemoModeException;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.common.utils.html.EscapeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器
 *
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String value = Convert.toStr(e.getValue());
        if (StringUtils.isNotEmpty(value)) {
            value = EscapeUtil.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e) {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 演示模式异常
     */
    @ExceptionHandler(DemoModeException.class)
    public AjaxResult handleDemoModeException(DemoModeException e) {
        return AjaxResult.error("演示模式，不允许操作");
    }


    private static final Map<String, String> UNIQUE_KEY_MAP = new HashMap<>();

    static {
        UNIQUE_KEY_MAP.put("bed.bed_number", "床位编号");
        UNIQUE_KEY_MAP.put("elder.name_id_card_no", "老人姓名和身份证号的组合");
        UNIQUE_KEY_MAP.put("floor.name", "楼层名称");
        UNIQUE_KEY_MAP.put("nursing_elder.nursing_id", "护理员和老人的绑定关系");
        UNIQUE_KEY_MAP.put("nursing_level.name", "护理等级名称");
        UNIQUE_KEY_MAP.put("nursing_plan.plan_name", "护理计划名称");
        UNIQUE_KEY_MAP.put("nursing_project.name", "护理项目名称");
        UNIQUE_KEY_MAP.put("room.code", "房间编号");
        UNIQUE_KEY_MAP.put("room_type.name", "房型名称");
        UNIQUE_KEY_MAP.put("sys_dict_type.dict_type", "字典类型");
        //device.binding_location_location_type_physical_location_type_product_id
        UNIQUE_KEY_MAP.put("device.binding_location_location_type_physical_location_type_product_id", "该老人/位置已绑定该产品，请重新选择");
    }

    /**
     * 捕获并处理数据库的 DuplicateKeyException 异常。
     *
     * @param e DuplicateKeyException 异常对象
     * @return 封装了友好错误信息的 AjaxResult
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public AjaxResult handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库操作违反唯一约束: {}", e.getMessage(), e);

        String value = "未知值";
        String rawKeyName = "未知字段";
        String friendlyFieldName = "该字段";

        // 尝试从异常信息中提取重复的值和键名
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            // 正则表达式用于从 "Duplicate entry '...' for key '...'" 中提取信息
            Pattern pattern = Pattern.compile("Duplicate entry '(.*?)' for key '(.*?)'");
            Matcher matcher = pattern.matcher(e.getCause().getMessage());

            if (matcher.find()) {
                value = matcher.group(1);      // 捕获重复的值
                rawKeyName = matcher.group(2); // 捕获原始的键名（例如：'bed.bed_number'）
            }
        }

        // 从MAP中查找对应的中文描述
        friendlyFieldName = UNIQUE_KEY_MAP.getOrDefault(rawKeyName, "");
        // 构建并返回对用户友好的错误信息
        String errorMessage = String.format("%s: [%s] 已存在，请勿重复添加。", friendlyFieldName, value);
        return AjaxResult.error(errorMessage);
    }


}
