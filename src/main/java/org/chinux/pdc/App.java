package org.chinux.pdc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
	public static void main(final String[] args) {
		new ClassPathXmlApplicationContext("META-INF/beans.xml");
	}
}
