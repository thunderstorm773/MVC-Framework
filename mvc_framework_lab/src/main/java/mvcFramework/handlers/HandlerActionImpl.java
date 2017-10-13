package mvcFramework.handlers;

import mvcFramework.ControllerActionPair;
import mvcFramework.annotations.parameters.ModelAttribute;
import mvcFramework.annotations.parameters.PathVariable;
import mvcFramework.annotations.parameters.RequestParam;
import mvcFramework.interfaces.HandlerAction;
import mvcFramework.model.Model;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HandlerActionImpl implements HandlerAction {

    @Override
    public String executeControllerAction(HttpServletRequest request,
                                          HttpServletResponse response,
                                          ControllerActionPair controllerActionPair)
            throws InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, NamingException {

        Class controller = controllerActionPair.getController();
        Method method = controllerActionPair.getActionMethod();
        List<Object> arguments = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            Object argument = null;
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                argument = this.getParameterValue(parameter, request);
            }

            if (parameter.isAnnotationPresent(PathVariable.class)) {
                argument = this.getPathVariableValue(parameter, controllerActionPair);
            }

            if (parameter.getType().isAssignableFrom(Model.class)) {
                Constructor constructor = parameter.getType().getDeclaredConstructor(HttpServletRequest.class);
                Model model = (Model) constructor.newInstance(request);
                argument = model;
            }

            if (parameter.isAnnotationPresent(ModelAttribute.class)) {
                argument = this.getModelAttributeValue(parameter, request);
            }

            if (parameter.getType().isAssignableFrom(HttpSession.class)) {
                argument = this.getSession(request);
            }

            if (parameter.getType().isAssignableFrom(HttpServletResponse.class)) {
                argument = response;
            }

            arguments.add(argument);
        }

        Context context = new InitialContext();
        String controllerName = controller.getSimpleName();
        Object controllerInstance = context.lookup("java:global/" + controllerName);

        String view = (String) method.invoke(controllerInstance, arguments.toArray());
        return view;
    }

    private HttpSession getSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session;
    }

    private <T> Object getModelAttributeValue(Parameter parameter,
                                         HttpServletRequest request)
            throws IllegalAccessException, InstantiationException {
        Class bindingModelClass = parameter.getType();
        Object bindingModelObject = bindingModelClass.newInstance();
        Field[] fields = bindingModelClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null) {
                T convertedFieldValue = this.convertArgument(field, fieldValue);
                field.set(bindingModelObject, convertedFieldValue);
            }
        }

        return bindingModelObject;
    }

    private <T> T getPathVariableValue(Parameter parameter,
                                       ControllerActionPair controllerActionPair) {
        PathVariable pathVariableAnnotation = parameter.getDeclaredAnnotation(PathVariable.class);
        String pathVariableKey = pathVariableAnnotation.value();
        Object argument = controllerActionPair.getPathVariable(pathVariableKey);
        String argumentStr = argument != null ? String.valueOf(argument) : null;
        T convertedArgument = this.convertArgument(parameter, argumentStr);
        return convertedArgument;
    }

    private <T> T getParameterValue(Parameter parameter,
                                    HttpServletRequest request) {
        RequestParam requestParamAnnotation = parameter.getDeclaredAnnotation(RequestParam.class);
        String parameterName = requestParamAnnotation.value();
        Object argument = request.getParameter(parameterName);
        String argumentStr = argument != null ? String.valueOf(argument) : null;
        T convertedArgument = this.convertArgument(parameter, argumentStr);
        return convertedArgument;
    }

    private <T> T convertArgument(Parameter parameter, String pathVariable) {
        Object object = pathVariable;
        String parameterType = parameter.getType().getSimpleName();
        switch (parameterType) {
            case "Integer":
                object = Integer.valueOf(pathVariable);
                break;
            case "int":
                object = Integer.parseInt(pathVariable);
                break;
            case "Long":
                object = Long.valueOf(pathVariable);
                break;
            case "long":
                object = Long.parseLong(pathVariable);
                break;
            case "double":
                object = Double.parseDouble(pathVariable);
                break;
            case "Double":
                object = Double.valueOf(pathVariable);
                break;
            case "BigDecimal":
                object = new BigDecimal(pathVariable);
                break;
        }

        return (T) object;
    }

    private <T> T convertArgument(Field field, String pathVariable) {
        Object object = pathVariable;
        String parameterType = field.getType().getSimpleName();
        switch (parameterType) {
            case "Integer":
                object = Integer.valueOf(pathVariable);
                break;
            case "int":
                object = Integer.parseInt(pathVariable);
                break;
            case "Long":
                object = Long.valueOf(pathVariable);
                break;
            case "long":
                object = Long.parseLong(pathVariable);
                break;
            case "double":
                object = Double.parseDouble(pathVariable);
                break;
            case "Double":
                object = Double.valueOf(pathVariable);
                break;
            case "BigDecimal":
                object = new BigDecimal(pathVariable);
                break;
        }

        return (T) object;
    }
}
