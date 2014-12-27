package qj.tool.web;

import qj.util.funct.F0;
import qj.util.funct.F1;
import qj.util.funct.P0;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public class ReloadingWebContext extends ReloadingContext {
	F1<HttpServletRequest, Boolean> shouldReload;

	@SuppressWarnings("UnusedDeclaration")
	public ReloadingWebContext(String contextClass, final String... classpaths) {
		super(contextClass, classpaths);

		shouldReload = req -> req.getMethod().equalsIgnoreCase("GET") && "true".equals(req.getParameter("rc"));
	}

	public ReloadingWebContext(String contextClass, F0<ClassLoader> classLoaderF, F1<HttpServletRequest, Boolean> shouldReload) {
		super(contextClass, classLoaderF);
		this.shouldReload = shouldReload;
	}

	public HttpServlet stubServlet(final String servletName) {
		return new HttpServlet() {
			protected void service(HttpServletRequest req,
			                       HttpServletResponse resp) throws ServletException,
					IOException {

				if (shouldReload != null && shouldReload.e(req)) {
					reload();
				}
				((HttpServlet)get(servletName + "Servlet")).service(req, resp);
			}
		};
	}

}
