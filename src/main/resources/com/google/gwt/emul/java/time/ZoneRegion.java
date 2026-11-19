package java.time;

import static java.time.ZoneOffset.UTC;

import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.List;

public class ZoneRegion extends ZoneId {

	private static final int UNKNOWN_ZONE = Integer.MIN_VALUE;

	public final String id;

	public ZoneRegion(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public ZoneRules getRules() {
		if ("UTC".equals(id)) {
			return UTC.getRules();
		}
		if (offsetSeconds(id, 0) == UNKNOWN_ZONE) {
			throw new DateTimeException("Unknown time-zone ID: " + id);
		}
		return new BrowserZoneRules(id);
	}

	/**
	 * Offset in seconds of the IANA zone at the given epoch millisecond, taken from the browser's Intl time-zone database.
	 * Returns {@link #UNKNOWN_ZONE} if the browser does not know the zone.
	 */
	private static native int offsetSeconds(String zone, double epochMillis) /*-{
		var cache = $wnd.__gwtTimeFormatters || ($wnd.__gwtTimeFormatters = {});
		var format = cache[zone];
		if (!format) {
			try {
				format = cache[zone] = new Intl.DateTimeFormat("en-US", {timeZone: zone, hourCycle: "h23", era: "short", year: "numeric", month: "numeric", day: "numeric", hour: "numeric", minute: "numeric", second: "numeric"});
			} catch (e) {
				return -2147483648;
			}
		}
		var wall = {};
		var parts = format.formatToParts(new Date(epochMillis));
		for (var i = 0; i < parts.length; i++) wall[parts[i].type] = parts[i].value;
		var year = Number(wall.year);
		if (wall.era.charAt(0) == "B") year = 1 - year;
		var utc = new Date(0);
		utc.setUTCFullYear(year, Number(wall.month) - 1, Number(wall.day));
		utc.setUTCHours(Number(wall.hour) % 24, Number(wall.minute), Number(wall.second), 0);
		return Math.round((utc.getTime() - Math.floor(epochMillis / 1000) * 1000) / 1000);
	}-*/;

	private static final class BrowserZoneRules implements ZoneRules {

		private static final long DAY_MILLIS = 86400000L;

		private final String zone;

		BrowserZoneRules(String zone) {
			this.zone = zone;
		}

		private int offsetAt(long epochMillis) {
			return offsetSeconds(zone, epochMillis);
		}

		@Override
		public boolean isFixedOffset() {
			return false;
		}

		@Override
		public ZoneOffset getOffset(Instant instant) {
			return ZoneOffset.ofTotalSeconds(offsetAt(instant.toEpochMilli()));
		}

		@Override
		public ZoneOffset getOffset(LocalDateTime localDateTime) {
			List<ZoneOffset> valid = getValidOffsets(localDateTime);
			if (!valid.isEmpty()) {
				return valid.get(0);
			}
			// Inside a gap: use the offset in force before the transition.
			return ZoneOffset.ofTotalSeconds(offsetAt(localDateTime.toEpochSecond(UTC) * 1000 - DAY_MILLIS));
		}

		@Override
		public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
			long local = localDateTime.toEpochSecond(UTC);
			int before = offsetAt(local * 1000 - DAY_MILLIS);
			int after = offsetAt(local * 1000 + DAY_MILLIS);
			List<ZoneOffset> valid = new ArrayList<>(2);
			for (int candidate : before == after ? new int[] { before } : new int[] { before, after }) {
				if (offsetAt((local - candidate) * 1000) == candidate) {
					valid.add(ZoneOffset.ofTotalSeconds(candidate));
				}
			}
			return valid;
		}

		@Override
		public ZoneOffset getStandardOffset(Instant instant) {
			// Standard offset = the smaller of the January and July offsets of that year.
			int year = LocalDateTime.ofEpochSecond(instant.getEpochSecond(), 0, UTC).getYear();
			long january = LocalDateTime.of(year, 1, 1, 0, 0).toEpochSecond(UTC) * 1000;
			long july = LocalDateTime.of(year, 7, 1, 0, 0).toEpochSecond(UTC) * 1000;
			return ZoneOffset.ofTotalSeconds(Math.min(offsetAt(january), offsetAt(july)));
		}

		@Override
		public Duration getDaylightSavings(Instant instant) {
			return Duration.ofSeconds(getOffset(instant).getTotalSeconds() - getStandardOffset(instant).getTotalSeconds());
		}

		@Override
		public boolean isDaylightSavings(Instant instant) {
			return !getDaylightSavings(instant).isZero();
		}

		@Override
		public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
			return offset != null && getValidOffsets(localDateTime).contains(offset);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof BrowserZoneRules && ((BrowserZoneRules) other).zone.equals(zone);
		}

		@Override
		public int hashCode() {
			return zone.hashCode();
		}
	}
}