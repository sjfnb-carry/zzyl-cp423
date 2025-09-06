package com.zzyl.common.utils;

import java.time.LocalDate;

/**
 * 身份证工具类
 */
public class IdCardNoUtils {
    /**
     * 根据身份证号计算年龄
     *
     * @param idCardNo 身份证号码
     * @return 年龄
     */
    public static int getAgeByIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.length() != 18) {
            throw new IllegalArgumentException("无效的身份证号码");
        }

        // 获取出生年月日
        String birthYearStr = idCardNo.substring(6, 10);
        String birthMonthStr = idCardNo.substring(10, 12);
        String birthDayStr = idCardNo.substring(12, 14);

        // 当前日期
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        int currentDay = currentDate.getDayOfMonth();

        // 出生日期
        int birthYear = Integer.parseInt(birthYearStr);
        int birthMonth = Integer.parseInt(birthMonthStr);
        int birthDay = Integer.parseInt(birthDayStr);

        // 计算年龄
        int age = currentYear - birthYear;

        // 如果当前月份小于出生月份，或者当前月份等于出生月份但当前日期小于出生日期，则年龄减1
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--;
        }

        return age;
    }

    /**
     * 根据身份证号计算性别
     *
     * @param idCardNo 身份证号码
     * @return 性别，"男" 或 "女"
     */
    public static String getGenderByIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.length() != 18) {
            throw new IllegalArgumentException("无效的身份证号码");
        }

        // 获取倒数第二位数字
        char genderChar = idCardNo.charAt(16);
        int genderDigit = Character.getNumericValue(genderChar);

        // 奇数为男性，偶数为女性
        return genderDigit % 2 == 1 ? "男" : "女";
    }
}
