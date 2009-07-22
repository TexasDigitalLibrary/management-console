package org.duraspace.duradav.core;

import java.util.Date;

public class Content extends Resource {

    private final ContentPath contentPath;

    private final Body body;

    private final long length;

    private final String mediaType;

    public Content(ContentPath path,
                   Date modifiedDate,
                   Body body,
                   long length,
                   String mediaType) {
        super(path, modifiedDate, false);
        this.contentPath = path;
        this.body = body;
        this.length = length;
        this.mediaType = mediaType;
    }

    public ContentPath getContentPath() {
        return contentPath;
    }

    public Body getBody() {
        return body;
    }

    public long getLength() {
        return length;
    }

    public String getMediaType() {
        return mediaType;
    }

}