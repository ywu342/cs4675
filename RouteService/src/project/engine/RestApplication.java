package project.engine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import project.utility.DBConnector;

@ApplicationPath("/")
public class RestApplication extends Application {
    // All request scoped resources and providers
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(RequestHandler.class);
        return classes;
    }

    // all singleton resources and providers
    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        return singletons;
    }
}