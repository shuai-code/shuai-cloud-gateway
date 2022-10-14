package org.shuai.cloud.gateway.support;

import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author Yangs
 */
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    /**
     * 接收一个毫秒时间戳, 转成ZonedDateTime格式, 时区是0时区
     */
    @Override
    public ZonedDateTime convert(String source) {
        ZonedDateTime dateTime;
        try {
            long epoch = Long.parseLong(source);
            dateTime = Instant.ofEpochMilli(epoch).atOffset(ZoneOffset.ofTotalSeconds(0)).toZonedDateTime();
        } catch (NumberFormatException e) {
            dateTime = ZonedDateTime.parse(source);
        }
        return dateTime;
    }
}
