package mvcFramework.model;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class Model {

    private HttpServletRequest request;

    private Map<String, Object> attributes;

    public Model(HttpServletRequest request) {
        this.request = request;
        this.attributes = new HashMap<>();
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
        this.request.setAttribute(key, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
