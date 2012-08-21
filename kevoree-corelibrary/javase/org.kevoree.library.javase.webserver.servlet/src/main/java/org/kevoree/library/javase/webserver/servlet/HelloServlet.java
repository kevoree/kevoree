package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.ComponentType;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 14:00
 */
@ComponentType
public class HelloServlet extends AbstractHttpServletPage {

    @Override
    public ServletContext getSharedServletContext() {
        return new FakeServletContext();
    }

    @Override
    public void initServlet() {
        this.legacyServlet = new SimpleHelloHttpServlet();
    }

    class SimpleHelloHttpServlet extends HttpServlet {
        public void doGet(HttpServletRequest req,
                          HttpServletResponse res)
                throws ServletException, IOException {
            PrintWriter out = res.getWriter();
            out.println("Hello, world!");
            out.flush();
          //  out.close();
        }
    }

}
