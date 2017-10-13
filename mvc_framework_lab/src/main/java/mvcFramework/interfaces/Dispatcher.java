package mvcFramework.interfaces;

import mvcFramework.ControllerActionPair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Dispatcher {

    ControllerActionPair dispatchRequest(HttpServletRequest request);

    String dispatchAction(HttpServletRequest request,
                          HttpServletResponse response,
                          ControllerActionPair controllerActionPair);
}
