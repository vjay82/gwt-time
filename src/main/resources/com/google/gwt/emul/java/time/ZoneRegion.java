package java.time;

import static java.time.ZoneOffset.UTC;

import java.time.zone.ZoneRules;

public class ZoneRegion extends ZoneId {

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
		if ("Europe/Berlin".equals(id)) {
			return ZoneOffset.ofHours(1).getRules();
		}
		if ("UTC".equals(id)) {
			return UTC.getRules();
		}
		throw new IllegalArgumentException("Not Implemented yet.");
	}
}
