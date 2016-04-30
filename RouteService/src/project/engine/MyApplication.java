package project.engine;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

public class MyApplication extends ResourceConfig {
	public MyApplication(){
	  	// Set jersey MVC model settings
    	packages("project.engine");
        register(JspMvcFeature.class);
        property(JspMvcFeature.TEMPLATES_BASE_PATH, "/WEB-INF/jsp");
	}
    
//	@Override
//    public Set<Class<?>> getClasses() {
//    	try {
//			//load db properties from the file
//			Properties prop = new Properties();
//			FileInputStream fis = new FileInputStream("/Recommendation_Engine/config/config.properties");
//			prop.load(fis);
//			//initial database settings
//			DBConnector.init_setting(prop);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//  
//       
//        Set<Class<?>> s = new HashSet<Class<?>>();
//        s.add(RequestHandler.class);
//        s.add(UserAgent.class);
//        return s;
//    }
}
