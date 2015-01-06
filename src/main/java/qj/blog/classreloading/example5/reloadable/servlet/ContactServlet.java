package qj.blog.classreloading.example5.reloadable.servlet;

import com.google.gson.Gson;
import qj.blog.classreloading.example5.reloadable.dao.ContactDAO;
import qj.blog.classreloading.example5.reloadable.model.Contact;
import qj.util.funct.F0;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

/**
 * Created by Quan on 22/12/2014.
 */
public class ContactServlet extends HttpServlet {

	public F0<Connection> connF;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        switch (action) {
            case "getAll":
                getAll(resp);
                break;
            case "add":
                add(req, resp);
                break;
            case "remove":
                remove(req, resp);
                break;

        }
	}

    private void getAll(HttpServletResponse resp) throws IOException {
        new Gson().toJson(ContactDAO.selectAll(connF.e()), resp.getWriter());
    }

    private void add(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Gson gson = new Gson();
        Contact contact = gson.fromJson(req.getReader(), Contact.class);

        ContactDAO.insert(contact, connF.e());

        gson.toJson(contact, resp.getWriter());
    }

    private void remove(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = Long.valueOf(req.getParameter("id"));

        ContactDAO.delete(id, connF.e());

        resp.getWriter().write(0);
    }
}
