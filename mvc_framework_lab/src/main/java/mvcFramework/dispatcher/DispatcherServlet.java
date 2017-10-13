package mvcFramework.dispatcher;

import mvcFramework.ControllerActionPair;
import mvcFramework.handlers.HandlerActionImpl;
import mvcFramework.handlers.HandlerMappingImpl;
import mvcFramework.interfaces.Dispatcher;
import mvcFramework.interfaces.HandlerAction;
import mvcFramework.interfaces.HandlerMapping;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet implements Dispatcher {

    private HandlerMapping handlerMapping;

    private HandlerAction handlerAction;

    public DispatcherServlet() {
        this.handlerMapping = new HandlerMappingImpl();
        this.handlerAction = new HandlerActionImpl();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isResource(request)) {
            this.sendResourceResponse(request, response);
            return;
        }

        this.handleRequest(request, response);
    }

    private void sendResourceResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = URLDecoder.decode(request.getRequestURI(), "UTF-8");
        String resourcePath = request.getServletContext().getResource("/").getPath();
        String directory = URLDecoder.decode(resourcePath, "UTF-8");
        File file = new File(directory + url);

        try (BufferedReader bfr = new BufferedReader(new FileReader(file))){
            while (true) {
                String line = bfr.readLine();
                if (line == null) {
                    break;
                }

                response.getWriter().print(line);
            }
        }
    }

    private boolean isResource(HttpServletRequest request) {
        boolean isResource = false;
        String url = request.getRequestURI();
        if (url.contains(".")) {
            isResource = true;
        }

        return isResource;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.handleRequest(request, response);
    }

    @Override
    public ControllerActionPair dispatchRequest(HttpServletRequest request) {
        ControllerActionPair controllerActionPair = null;
        try {
            controllerActionPair = this.handlerMapping.findController(request);
        } catch (IOException | ClassNotFoundException |
                InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return controllerActionPair;
    }

    @Override
    public String dispatchAction(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ControllerActionPair controllerActionPair) {
        String view = null;
        try {
            view = this.handlerAction.executeControllerAction(request, response, controllerActionPair);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException | NamingException e) {
            e.printStackTrace();
        }

        return view;
    }

    private void handleRequest(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {
        ControllerActionPair controllerActionPair = this.dispatchRequest(request);
        if (controllerActionPair == null) {
            response.sendError(404);
            return;
        }

        String view = this.dispatchAction(request, response,controllerActionPair);
        if (view.startsWith("redirect:")) {
            view = view.replace("redirect:", "");
            response.sendRedirect(view);
        } else {
            request.getRequestDispatcher("/" + view + ".jsp")
                    .forward(request, response);
        }
    }
}
