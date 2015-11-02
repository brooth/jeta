//-----------------------------------------------
//                  Main
//-----------------------------------------------
package com.github.brooth.metacode;

public interface Metacode<T> {
	Class<T> getHostClass(); 
}

public final class MetaEntity<T> {
	private final T host;
	private final Metacode<T> metacode;
}

public interface Metacode {	
	public void setup(Metaconig config);
	public MetaEntity of(Class host);

	public static class Metaconig {
		private String metaPackage;
		private Metasitory metasitory;

		public static class Builder {
			private Metaconig config;

			public Builder() {
				this.config = new Metaconfig();
	 		}

			public Builder metaPackage(String value){
				config.metaPackage = value;
			}			
			
			public Builder repository(Metasitory value) {
				config.metasitory = value;
	 		}
		}
	}
}
