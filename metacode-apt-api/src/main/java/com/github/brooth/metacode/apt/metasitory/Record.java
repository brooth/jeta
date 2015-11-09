package com.github.brooth.metacode.apt.metasitory;

/**
 *
 */
public final class Record {
	private final String host;
	private final String metacode;
	private final String[] annotations;

	public Record(String host, String metacode, String[] annotations) {
		this.host = host;
		this.metacode = metacode;
		this.annotations = annotations;
	}

	public String getHost() {
		return host;
	}

	public String getMetacode() {
		return metacode;
	}

	public String[] getAnnotations() {
		return annotations;
	}
}
