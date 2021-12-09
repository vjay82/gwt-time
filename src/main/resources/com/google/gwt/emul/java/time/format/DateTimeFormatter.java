package java.time.format;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class DateTimeFormatter {

	public static DateTimeFormatter ofPattern(String pattern) {
		return new DateTimeFormatter(pattern);
	}

	public static DateTimeFormatter ofPattern(PredefinedFormat predefinedFormat) {
		return new DateTimeFormatter(predefinedFormat);
	}

	public static DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ofPattern(PredefinedFormat.ISO_8601);

	protected DateTimeFormat dateTimeFormat;

	public DateTimeFormatter(String pattern) {
		dateTimeFormat = DateTimeFormat.getFormat(pattern);
	}

	public DateTimeFormatter(PredefinedFormat predefinedFormat) {
		dateTimeFormat = DateTimeFormat.getFormat(predefinedFormat);
	}

	public String format(LocalTime lt) {
		return format(LocalDate.now().atTime(lt));
	}

	public String format(LocalDate ld) {
		return format(ld.atStartOfDay());
	}

	public String format(LocalDateTime ldt) {
		return format(ldt.atZone(ZoneId.systemDefault()));
	}

	public String format(ZonedDateTime ldt) {
		return format(ldt.toInstant());
	}

	public String format(Instant instant) {
		Date date = new Date(instant.toEpochMilli());
		return dateTimeFormat.format(date);
	}

	public Date parse(String text) {
		return dateTimeFormat.parse(text);
	}
}
