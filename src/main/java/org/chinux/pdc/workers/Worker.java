
public interface Worker {
	
	public void processData(final T event);
	
	public  T DoWork(T dataEvent);

}
