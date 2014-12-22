package qj.blog.classreloading.example5.servlet;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.JadeTemplate;
import qj.util.Cols;
import qj.util.FileUtil;
import qj.util.StringUtil;
import qj.util.funct.P2;
import qj.util.math.Range;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;

public class JadeServlet extends HttpServlet {
	public String version = null;
	private String webLoc;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html; charset=UTF-8");

		if (StringUtil.countHappens('.', req.getServerName()) == 1) {
			resp.sendRedirect("http://www." + req.getServerName()); // Fix ui-sref urls
			return;
		}
		
		String requestURI = req.getRequestURI();
		if (requestURI.equals("/")) {
			requestURI = "/spa.jade";
		}
		File file = new File(webLoc + "/spa" + requestURI);
		
		if (!file.exists()) {
			resp.sendRedirect("/#!" + requestURI.replaceFirst("/$", "")); // Fix ui-sref urls
			return;
		}
		
		JadeTemplate template = Jade4J.getTemplate(file.getPath());
		if ("/spa.jade".equals(requestURI)) {
			StringWriter buffer = new StringWriter();
			template.process(new JadeModel(Cols.map(
					"version", version
					)), buffer);

			String target = "<!--spa-js-->";
			
			String scriptLocations = allJs();
			
			String content = buffer.toString();
			resp.getWriter().write(
					StringUtil.replace(scriptLocations, Range.fromlength(content.indexOf(target), target.length()), content)
			);
		} else {
			template.process(new JadeModel(null), resp.getWriter());
		}
	}

	private String allJs() {
		LinkedList<String> col = new LinkedList<>();
		P2<File,String> collect = (file, path) -> {
			if (file.getName().endsWith(".js")) {
				if (StringUtil.isEmpty(path)) {
					col.add("/" + file.getName());
				} else {
					col.add("/" + path.replaceAll("\\\\", "/") + "/" + file.getName());
				}
			}
		};
		FileUtil.eachFile(new File(webLoc + "/spa"), collect);

		return Cols.join(Cols.yield(col, s -> "<script src=\"/spa" + s + "?v=" + version + "\"></script>"), "");
	}

	public void init(String webLoc) {
		this.webLoc = webLoc;
	}
}
