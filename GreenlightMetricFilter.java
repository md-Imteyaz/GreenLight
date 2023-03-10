package com.gl.platform.config;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

public class GreenlightMetricFilter implements MetricFilter {

	@Override
	public boolean matches(String name, Metric metric) {
		return name.startsWith("jvm") || name.startsWith("HikariPool-1.pool");
	}

}
