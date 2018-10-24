package org.sapia.corus.client.common;

import java.util.concurrent.TimeUnit;

/**
 * An instance of this class wraps a thread and offers convenience methods "around" it,
 * namely allowing to stop the thread synchronously.
 * 
 * @author yduchesne
 *
 */
public class ThreadWrapper {

	private Thread delegate;
	
	/**
	 * @param delegate a {@link Thread} to wrap.
	 */
	public ThreadWrapper(Thread delegate) {
		this.delegate = delegate;
	}

	/**
	 * @return the {@link Thread} that this instance wraps.
	 */
	public Thread getDelegate() {
		return delegate;
	}

	/**
	 * @return this instance.
	 * @throws InterruptedException if the calling thread is interrupted while performing this operation.
	 */
	public ThreadWrapper stop() throws InterruptedException {
		delegate.interrupt();
		while (delegate.isAlive()) {
			delegate.join();
		}
		return this;
	}
	
	/**
	 * @param timeoutMillis the total number of millis to wait for.
	 * @param maxRetries the max number of times to attempt interrupting the thread that this instance wraps.
	 * @return this instance.
	 * @throws InterruptedException if the calling thread is interrupted while performing this operation.
	 * @throws IllegalStateException if the wrapped thread could not be stopped within the given amount 
	 * 		   of retries or total timeout.
	 */
	public ThreadWrapper stop(long timeoutMillis, int maxRetries) throws InterruptedException {
		delegate.interrupt();
		int retryCount = 0;
		Delay delay = new Delay(timeoutMillis, TimeUnit.MILLISECONDS);
		while (delegate.isAlive() && retryCount < maxRetries && delay.isNotOver()) {
			delegate.join(delay.remainingMillisNotZero());
			retryCount++;
		}
		if (delegate.isAlive()) {
			throw new IllegalStateException("Could not stop thread within specified tie");
		}
		return this;
	} 
	
	/**
	 * @return this instance.
	 */
	public ThreadWrapper start() {
		if (!delegate.isAlive() && !delegate.isInterrupted()) {
			delegate.start();
		} 
		return this;
	}
	
	/**
	 * @param delegate a {@link Thread} to wrap.
	 * @return a new instance of this class, wrapping the given thread.
	 */
	public static ThreadWrapper wrap(Thread delegate) {
		return new ThreadWrapper(delegate);
	}
}
