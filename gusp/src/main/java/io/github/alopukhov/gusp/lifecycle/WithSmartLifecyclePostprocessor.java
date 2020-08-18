package io.github.alopukhov.gusp.lifecycle;

import io.github.alopukhov.gusp.annotations.WithSmartLifecycle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Process {@link WithSmartLifecycle} annotations and creates corresponding {@link ServiceSmartLifecycle} adapters.
 * If registered manually consider using static method. E.g.
 * <pre>
 *     &#064;Configuration
 *     public class AcmeConf {
 *         &#064;Bean
 *         public <u>static</u> WithSmartLifecyclePostprocessor withSmartLifecyclePostprocessor() {
 *             return new WithSmartLifecyclePostprocessor();
 *         }
 *     }
 * </pre>
 *
 * @see io.github.alopukhov.gusp.annotations.EnableGusp
 */
public class WithSmartLifecyclePostprocessor implements BeanDefinitionRegistryPostProcessor {
    private static final String ANNOTATION_NAME = WithSmartLifecycle.class.getName();
    private static final String BEAN_NAME_ATTRIBUTE = "beanName";
    private static final String NON_SINGLETON_MSG = "Non singleton bean [%s] annotated with " + WithSmartLifecycle.class.getSimpleName();
    public static final String DEFAULT_BEAN_NAME_SUFFIX = "-service-smart-lifecycle-support";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] names = registry.getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition definition = registry.getBeanDefinition(name);
            processBeanDefinition(name, definition, registry);
        }
    }

    private void processBeanDefinition(String beanName, BeanDefinition definition, BeanDefinitionRegistry registry) {
        if (definition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedDefinition = ((AnnotatedBeanDefinition) definition);
            AnnotatedTypeMetadata metadata = annotatedDefinition.getFactoryMethodMetadata() == null?
                    annotatedDefinition.getMetadata() : annotatedDefinition.getFactoryMethodMetadata();
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ANNOTATION_NAME);
            if (annotationAttributes == null) {
                return;
            }
            validateAnnotatedBeanDefinition(beanName, definition);
            processAnnotationAttributes(beanName, annotationAttributes, registry);
        }
    }

    private void validateAnnotatedBeanDefinition(String beanName, BeanDefinition definition) throws BeansException{
        if (!definition.isSingleton()) {
            throw new BeanDefinitionValidationException(String.format(NON_SINGLETON_MSG, beanName));
        }
    }

    private void processAnnotationAttributes(String beanName, Map<String, Object> annotationAttributes, BeanDefinitionRegistry registry) {
        String suggestedBeanName = (String) annotationAttributes.get(BEAN_NAME_ATTRIBUTE);
        String lifecycleBeanName = suggestedBeanName.isEmpty()? (beanName + DEFAULT_BEAN_NAME_SUFFIX) : suggestedBeanName;
        BeanDefinition beanDefinition = createBeanDefinition(beanName, annotationAttributes);
        registry.registerBeanDefinition(lifecycleBeanName, beanDefinition);
    }

    private BeanDefinition createBeanDefinition(String serviceBeanName, Map<String, Object> annotationAttributes) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServiceSmartLifecycle.class)
                .addConstructorArgReference(serviceBeanName)
                .setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
        HashMap<String, Object> attributes = new HashMap<>(annotationAttributes);
        attributes.remove(BEAN_NAME_ATTRIBUTE);
        attributes.forEach(beanDefinitionBuilder::addPropertyValue);
        return beanDefinitionBuilder.getBeanDefinition();
    }
}
