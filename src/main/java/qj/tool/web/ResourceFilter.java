package qj.tool.web;

import qj.util.FileUtil;
import qj.util.IOUtil;
import qj.util.funct.F1;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

public class ResourceFilter implements Filter {

	private F1<HttpServletRequest, String> rootRedirect;
	LinkedList<F1<String, File>> locationFs = new LinkedList<>();

	public ResourceFilter(F1<HttpServletRequest,String> rootRedirect, String... locations) {
		this.rootRedirect = rootRedirect;

		for (final String loc : locations) {
			if (loc != null) {
				locationFs.add(locF(loc));
			}
		}
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req1, ServletResponse resp1,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) req1;
		HttpServletResponse resp = (HttpServletResponse) resp1;
		String uri = req.getRequestURI();
		
		String redirectLocation;
		if (uri.equals("/") && (redirectLocation = rootRedirect.e(req)) != null) {
			resp.sendRedirect(redirectLocation);
			return;
		}
		
		File file = getFile(uri);
		if (file != null) {
			serve((HttpServletResponse) resp1, file, uri);
		} else if (uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".jpg") || uri.endsWith(".png")) {
			resp.sendError(404);
		} else {
			chain.doFilter(req1, resp1);
		}
		
	}

	private void serve(HttpServletResponse resp, File file, String uri) throws IOException {
//		resp.setCharacterEncoding("UTF-8");
		
		resp.setContentType(
					uri.endsWith(".js" ) ? "application/javascript" : 
					uri.endsWith(".css") ? "text/css" :
					uri.endsWith(".svg") ? "image/svg+xml" :
					uri.endsWith(".ttf") ? "application/octet-stream" :
					uri.endsWith(".woff") ? "font/woff" :
					null
				);
		
		F1<String, String> contentFilter = getContentFilter(uri);
		if (contentFilter != null) {
			String content = contentFilter.e(IOUtil.toString(FileUtil.fileInputStream(file), "UTF-8"));
			resp.getOutputStream().write(content.getBytes(Charset.forName("UTF-8")));
		} else {
			ServletOutputStream out = resp.getOutputStream();
			try {
				IOUtil.connect(FileUtil.fileInputStream(file), out);
			} catch (IOException e) {
				if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
					;
				} else {
					throw e;
				}
			}
		}
	}
	
	LinkedList<ContentFilterHolder> filters = new LinkedList<>();
	@SuppressWarnings("UnusedDeclaration")
	public void addContentFilter(F1<String,Boolean> uriCheck, F1<String,String> contentFilter) {
		filters.add(new ContentFilterHolder(uriCheck, contentFilter));
	}
	public F1<String,String> getContentFilter(String uri) {
		for (ContentFilterHolder filter : filters) {
			if (filter.uriCheck.e(uri)) {
				return filter.contentFilter;
			}
		}
		return null;
	}

	public static F1<String, Boolean> js = uri -> uri.endsWith(".js" );
	static class ContentFilterHolder {
		F1<String,Boolean> uriCheck;
		F1<String,String> contentFilter;
		public ContentFilterHolder(
				F1<String, Boolean> uriCheck,
				F1<String, String> contentFilter
				) {
			this.uriCheck = uriCheck;
			this.contentFilter = contentFilter;
		}
	}

	private File getFile(String uri) {
		if (uri.contains("//") || uri.contains("..") || uri.contains("./") || uri.contains("\\")) {
			return null;
		}
		
		for (F1<String, File> locF : locationFs) {
			File file = locF.e(uri);
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	public F1<String, File> locF(final String loc) {
		return uri -> {
			File file = new File(loc + uri);
			if (file.exists() && file.isFile()) {
				return file;
			}
			return null;
		};
	}
	
}
