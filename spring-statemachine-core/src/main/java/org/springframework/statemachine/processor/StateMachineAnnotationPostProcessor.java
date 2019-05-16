/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanPostProcessor} implementation that processes method-level
 * annotations such as {@link OnTransition}.
 *
 * @author Mark Fisher
 * @author Marius Bogoevici
 * @author Janne Valkealahti
 *
 */
public class StateMachineAnnotationPostProcessor implements BeanPostProcessor, BeanFactoryAware, InitializingBean,
		Lifecycle, ApplicationListener<ApplicationEvent> {

	private final static Log log = LogFactory.getLog(StateMachineAnnotationPostProcessor.class);

	/** Factory from BeanFactoryAware */
	private volatile ConfigurableListableBeanFactory beanFactory;

	/** Post processors map - annotation -> method post processor */
	private final Map<Class<? extends Annotation>, MethodAnnotationPostProcessor<?>> postProcessors =
			new HashMap<Class<? extends Annotation>, MethodAnnotationPostProcessor<?>>();

	/**
	 * Application events for post processed beans (if bean instance of ApplicationListener) will
	 * be dispatched from here via callback in this class.
	 */
	private final Set<ApplicationListener<ApplicationEvent>> listeners = new HashSet<ApplicationListener<ApplicationEvent>>();

	/**
	 * Lifecycle callbacks for post processed bean (if bean instance of Lifecycle) will
	 * be dispatched from here via callback in this class.
	 */
	private final Set<Lifecycle> lifecycles = new HashSet<Lifecycle>();

	/** Flag for Lifecycle in this class */
	private volatile boolean running = true;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isAssignable(ConfigurableListableBeanFactory.class, beanFactory.getClass(),
				"a ConfigurableListableBeanFactory is required");
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		postProcessors.put(OnTransition.class, new StateMachineActivatorAnnotationPostProcessor(beanFactory));
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		final Class<?> beanClass = getBeanClass(bean);

		if (!isStereotype(beanClass)) {
			// we only post-process stereotype components
			return bean;
		}

		ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Annotation[] annotations = AnnotationUtils.getAnnotations(method);

				for (Annotation annotation : annotations) {

					Annotation metaAnnotation = null;
					for (Class<? extends Annotation> ppa : postProcessors.keySet()) {
						Annotation a = AnnotationUtils.getAnnotation(annotation,ppa);
						if (a != null && annotation.getClass().equals(a.getClass())) {
							metaAnnotation = a;
						} else {
							metaAnnotation = a;
						}
					}

					MethodAnnotationPostProcessor postProcessor = metaAnnotation != null ? postProcessors
							.get(metaAnnotation.annotationType()) : null;

					if (postProcessor != null && shouldCreateHandler(annotation)) {
						Object result = postProcessor.postProcess(beanClass, bean, beanName, method, metaAnnotation, annotation);

						if (result != null && result instanceof StateMachineHandler) {
							String endpointBeanName = generateBeanName(beanName, method, annotation.annotationType());

							if (result instanceof BeanNameAware) {
								((BeanNameAware) result).setBeanName(endpointBeanName);
							}
							beanFactory.registerSingleton(endpointBeanName, result);
							if (result instanceof BeanFactoryAware) {
								((BeanFactoryAware) result).setBeanFactory(beanFactory);
							}
							if (result instanceof InitializingBean) {
								try {
									((InitializingBean) result).afterPropertiesSet();
								} catch (Exception e) {
									throw new BeanInitializationException("failed to initialize annotated component", e);
								}
							}
							if (result instanceof Lifecycle) {
								lifecycles.add((Lifecycle) result);
								if (result instanceof SmartLifecycle && ((SmartLifecycle) result).isAutoStartup()) {
									((SmartLifecycle) result).start();
								}
							}
							if (result instanceof ApplicationListener) {
								listeners.add((ApplicationListener) result);
							}
						}
					}
				}
			}
		});
		return bean;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		for (ApplicationListener<ApplicationEvent> listener : listeners) {
			try {
				listener.onApplicationEvent(event);
			} catch (ClassCastException e) {
				if (log.isWarnEnabled() && event != null) {
					log.warn("ApplicationEvent of type [" + event.getClass()
							+ "] not accepted by ApplicationListener [" + listener + "]");
				}
			}
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void start() {
		for (Lifecycle lifecycle : this.lifecycles) {
			if (!lifecycle.isRunning()) {
				lifecycle.start();
			}
		}
		this.running = true;
	}

	@Override
	public void stop() {
		for (Lifecycle lifecycle : this.lifecycles) {
			if (lifecycle.isRunning()) {
				lifecycle.stop();
			}
		}
		this.running = false;
	}

	private boolean shouldCreateHandler(Annotation annotation) {
		return true;
	}

	/**
	 * Gets the bean class. Will check if bean is a proxy and
	 * find a class from there as target class, otherwise
	 * we just get bean class.
	 *
	 * @param bean the bean
	 * @return the bean class
	 */
	private Class<?> getBeanClass(Object bean) {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		return (targetClass != null) ? targetClass : bean.getClass();
	}

	/**
	 * Checks if class is a stereotype meaning if there is
	 * a Component annotation present.
	 *
	 * @param beanClass the bean class
	 * @return true, if is stereotype
	 */
	private boolean isStereotype(Class<?> beanClass) {
		List<Annotation> annotations = new ArrayList<Annotation>(Arrays.asList(beanClass.getAnnotations()));
		Class<?>[] interfaces = beanClass.getInterfaces();
		for (Class<?> iface : interfaces) {
			annotations.addAll(Arrays.asList(iface.getAnnotations()));
		}
		for (Annotation annotation : annotations) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationType.equals(Component.class) || annotationType.isAnnotationPresent(Component.class)) {
				return true;
			}
		}
		return false;
	}

	private String generateBeanName(String originalBeanName, Method method, Class<? extends Annotation> annotationType) {
		String baseName = originalBeanName + "." + method.getName() + "." + ClassUtils.getShortNameAsProperty(annotationType);
		String name = baseName;
		int count = 1;
		while (beanFactory.containsBean(name)) {
			name = baseName + "#" + (++count);
		}
		return name;
	}

}
