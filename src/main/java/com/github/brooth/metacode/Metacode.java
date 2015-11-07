package com.github.brooth.metacode;

public class Metacode {	

	public Metacode(Env env);

	public Servant get(Class<? extends Servant> servant) {
		Servant inst = servant.newInstance();	
		inst.prepare(env.getMetasitory());
		return inst;
	}

	public MasterServant<M, C> get(Class<? extends MasterServant> servant, Object master) {
		MasterServant inst = servant.newInstance(master); //you know...
		inst.prepare(env.getMetasitory());
		return inst;
	}

	public void apply(Class<? extends SolidServant> servant, Object master) {
		SolidServant inst = get(servant, master);
		inst.apply();
	}

	public static class Env {
		private String metaPackage;
		private Metasitory metasitory;
		private Map<Class<? extends Servant>, Provider<Servant>> servants;

		public static class Builder {
			private Env env;

			public Builder() {
				env = new Env();
				env.servants = new ArrayList<>();
				// todo: all base servants here
	 		}

			public Env build() {
				// todo: validate
				return env;
			}

			public Builder metaPackage(String value){
				config.metaPackage = value;
			}			
			
			public Builder metasitory(Metasitory value) {
				config.metasitory = value;
	 		}
			
			public Builder addServant(Class<? extends Servant> value) {
				servants.add(value);
			}
			
			public Builder setServants(...) {
				servants = value;
			}
		}
	}
}
