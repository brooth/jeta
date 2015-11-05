
public interface Servant<M> {	

	Criteria criteria(Class<M> master);
	
	void prepare(List<Metacode> metacodes);
}

/*
 * needs a master to apply to and handles one type of metacode
 * todo: BaseMasterServant with masterEqDeep criteria. also for solid
 */
public abstract class MasterServant<M, C> implements Servant<M> {
	protected M master;
	protected List<C> metacodes;

	protected MasterServant(M master) {
		this.master = master;
	}

	void prepare(List<Metacode> metacodes) {
		this.metacodes = (List<C>) metacodes;
	}
}

/*
 * ready to apply servant. doesn't need extra args. doesn't return result
 * like logger, injector, findviws and more
 */
public abstract class SolidServant<M, C> extends MasterServant<M, C> {
	void abstract apply();
}

