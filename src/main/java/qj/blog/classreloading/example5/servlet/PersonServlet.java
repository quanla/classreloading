package qj.blog.classreloading.example5.servlet;

import com.google.gson.Gson;
import qj.blog.classreloading.example5.dao.PersonDAO;
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
public class PersonServlet extends HttpServlet {


	public F0<Connection> connF;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		new Gson().toJson(PersonDAO.selectAll(connF.e()), resp.getWriter());
	}
}
