package scot.alba.webpostits;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/marketplace")
public class PostitsServlet extends javax.servlet.http.HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // In the event this servlet is called it immediately forwards to the index.jsp
        String url = "/index.jsp";
        getServletContext().getRequestDispatcher(url)
                .forward(req, resp);
    }

}
