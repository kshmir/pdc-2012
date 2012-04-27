package org.chinux.pdc;

import org.springframework.beans.factory.InitializingBean;

public class Service implements InitializingBean {

	private Runnable runner;

	public Service(final Runnable runner) {
		this.runner = runner;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		new Thread(runner).start();
	}
}
