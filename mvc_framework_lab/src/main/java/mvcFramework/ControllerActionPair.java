package mvcFramework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ControllerActionPair {

    private Class controller;

    private Method actionMethod;

    private Map<String, Object> pathVariables;

    public ControllerActionPair(Class controller, Method actionMethod) {
        this.controller = controller;
        this.actionMethod = actionMethod;
        this.pathVariables = new HashMap<>();
    }

    public Class getController() {
        return this.controller;
    }

    public Method getActionMethod() {
        return this.actionMethod;
    }

    public void addPathVariable(String key, Object value) {
        this.pathVariables.put(key, value);
    }

    public Object getPathVariable(String key) {
        return this.pathVariables.get(key);
    }
}
