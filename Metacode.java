//-----------------------------------------------
//                  Main
//-----------------------------------------------
package com.github.brooth.metacode;

public interface Metacode<M> {
	Class<M> getMasterClass(); 
}

public interface Servant {	
	Criteria criteria();
	void prepare(List<Metacode> metacodes);
}

/**
 * needs a master to apply to and handles one type of metacode
 */
public abstract class MasterServant<M, C> {
	protected M master;
	protected List<C> metacodes;

	protected MasterServant(M master) {
		this.master = master;
	}

	void prepare(List<Metacode> metacodes) {
		this.metacodes = (List<C>) metacodes;
	}
}

/**
 * ready to apply servant. no needs for args or return result
 * like logger, injector, findviws and more
 */
public abstract class SolidServant<M, C> extends MasterServant<M, C> {
	void abstract apply();
}

public class Metacode {	

	public void setup(Env env);

	protected void prepare(Servant servant) {
		List<Metacode> metacods = repository.search(inst.criteria());
		inst.prepare(metacodes);
	}

	public Servant get(Class<? extends Servant> servant) {
		Servant inst = servant.newInstance();	
		prepare(inst);
		return inst;
	}

	public MasterServant<M, C> get(Class<? extends MasterServant> servant, Object master) {
		MasterServant inst = servant.newInstance(master); //you know...
		prepare(inst);
		return inst;
	}

	public void apply(Class<? extends SolidServant> servant, Object master) {
		SolidServant inst = get(servant, master);
		inst.apply();
	}

	public static class Env {
		private String metaPackage;
		private Metasitory metasitory;
		private List<Servant> servants;

		public static class Builder {
			private Env config;

			public Builder() {
				this.config = new Env();
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
