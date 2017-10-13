package mvcFramework.handlers;

import mvcFramework.ControllerActionPair;
import mvcFramework.annotations.controller.Controller;
import mvcFramework.annotations.request.GetMapping;
import mvcFramework.annotations.request.PostMapping;
import mvcFramework.interfaces.HandlerMapping;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class HandlerMappingImpl implements HandlerMapping {

    @Override
    public ControllerActionPair findController(HttpServletRequest request)
            throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        String urlPath = URLDecoder.decode(request.getRequestURI(), "UTF-8");
        String projectPath = request.getServletContext().getRealPath("/WEB-INF/classes");
        List<Class> controllers = this.findAllControllers(projectPath);
        ControllerActionPair controllerActionPair = null;

        for (Class controller : controllers) {
            Method[] methods = controller.getDeclaredMethods();
            for (Method method : methods) {
                String methodPath = this.findMethodPath(request, method);
                if (methodPath == null) {
                    continue;
                }

                boolean isPathMatching = this.isPathMatching(urlPath, methodPath);
                if (isPathMatching) {
                    controllerActionPair = new ControllerActionPair(controller, method);
                    this.addPathVariables(controllerActionPair, urlPath, methodPath);
                }
            }
        }

        return controllerActionPair;
    }

    private void addPathVariables(ControllerActionPair controllerActionPair,
                                  String urlPath, String methodPath) {
        String[] urlTokens = urlPath.split("/");
        String[] methodTokens = methodPath.split("/");
        for (int i = 0; i < methodTokens.length; i++) {
            String methodToken = methodTokens[i];
            if (methodToken.startsWith("{") && methodToken.endsWith("}")) {
                String key = methodToken.replaceAll("[{}]", "");
                Object value = urlTokens[i];
                controllerActionPair.addPathVariable(key, value);
            }
        }
    }

    private boolean isPathMatching(String urlPath, String methodPath) {
        boolean isPathMatching = true;
        String[] uriTokens = urlPath.split("/");
        String[] methodTokens = methodPath.split("/");

        if (uriTokens.length != methodTokens.length) {
            isPathMatching = false;
            return isPathMatching;
        }

        for (int i = 0; i < methodTokens.length; i++) {
            String methodToken = methodTokens[i];
            if (methodToken.startsWith("{") && methodToken.endsWith("}")) {
                continue;
            }

            String urlToken = uriTokens[i];
            if (!urlToken.equals(methodToken)) {
                isPathMatching = false;
                break;
            }
        }

        return isPathMatching;
    }

    private String findMethodPath(HttpServletRequest request, Method method)
            throws IllegalAccessException, InstantiationException {
        String requestMethod = request.getMethod();
        String path = null;
        switch (requestMethod) {
            case "GET":
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping getMapping = method.getDeclaredAnnotation(GetMapping.class);
                    path = getMapping.value();
                }
                break;
            case "POST":
                if (method.isAnnotationPresent(PostMapping.class)) {
                    PostMapping postMapping = method.getDeclaredAnnotation(PostMapping.class);
                    path = postMapping.value();
                }
                break;
        }

        return path;
    }

    private List<Class> findAllControllers(String projectDirectory) throws
            ClassNotFoundException {
        List<Class> controllerClasses = new ArrayList<>();
        File directory = new File(projectDirectory);
        Queue<File> dirsFile = new ArrayDeque<>();
        dirsFile.offer(directory);

        while (!dirsFile.isEmpty()) {
            File dir = dirsFile.poll();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        dirsFile.offer(file);
                        continue;
                    }

                    if (!file.isFile()) {
                        continue;
                    }

                    if (!file.getName().endsWith(".class")) {
                        continue;
                    }

                    Class currentClass = this.getClass(file);
                    if (currentClass != null &&
                            currentClass.isAnnotationPresent(Controller.class)) {
                        controllerClasses.add(currentClass);
                    }
                }
            }
        }

        return controllerClasses;
    }

    private Class getClass(File file) throws ClassNotFoundException {
        String absolutePath = file.getAbsolutePath();
        String[] pathTokens = absolutePath.split("classes\\\\");
        String className = pathTokens[1].replaceAll("\\\\", ".")
                .replaceAll("\\.class", "");
        Class currentClass = null;
        if (!className.endsWith("DispatcherServlet")) {
            currentClass = Class.forName(className);
        }

        return currentClass;
    }
}
