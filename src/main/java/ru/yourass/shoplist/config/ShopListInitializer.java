package ru.yourass.shoplist.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ShopListInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }; 
    
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {ShopListConfig.class};
    }
    
    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    } 
}