package org.duraspace.duradav.error;

import javax.servlet.http.HttpServletResponse;

import org.duraspace.duradav.core.Path;

/**
 * HTTP 404 (Not Found).
 */
public class NotFoundException
        extends WebdavException {

    public static final String STATUS_LINE = "HTTP/1.1 404 Not Found";

    private static final long serialVersionUID = 1L;

    public NotFoundException(Path path) {
        super(path, HttpServletResponse.SC_NOT_FOUND);
    }
}