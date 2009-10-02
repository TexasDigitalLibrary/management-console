package org.duracloud.servicesadminclient;

import java.io.File;
import java.io.InputStream;

import java.util.Map;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 */
public class ServicesAdminClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private RestHttpHelper rester;

    private ServiceSerializer serializer;

    private String baseURL;

    public HttpResponse postServiceBundle(String fileName, InputStream stream,
                                          long length)
            throws Exception {
        log.debug("FILENAME: " + fileName + "\nSTREAM: " + stream);
        return getRester().multipartFileStreamPost(getInstallURL(),
                                                   fileName,
                                                   stream, length);
    }

    public HttpResponse postServiceBundle(File file) throws Exception {
        log.debug("FILE: " + file);
        return getRester().multipartFilePost(getInstallURL(), file);
    }

    public HttpResponse deleteServiceBundle(String bundleId) throws Exception {
        log.debug("BUNDLE-ID: " + bundleId);

        ComputeServiceBean bean = new ComputeServiceBean(bundleId);
        String requestContent = getSerializer().serialize(bean);
        Map<String, String> headers = null;

        return getRester().post(getUninstallURL(), requestContent, headers);
    }

    public HttpResponse getServiceListing() throws Exception {
        log.debug("Listing");
        return getRester().get(getListURL());
    }

    public Map<String, String> getServiceConfig(String configId)
            throws Exception {
        HttpResponse response = getRester().get(getConfigureURL(configId));
        // TODO: process erroneous responses

        String body = response.getResponseBody();
        log.debug("config for '" + configId + "': " + body);
        return SerializationUtil.deserializeMap(body);
    }

    public void postServiceConfig(String configId, Map<String, String> config)
            throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(postServiceConfigText(configId, config));
        }
        String body = SerializationUtil.serializeMap(config);
        Map<String, String> headers = null;

        log.debug("POST url: " + getConfigureURL(configId));

        HttpResponse response =
                getRester().post(getConfigureURL(configId), body, headers);
        // TODO: process erroneous responses
    }

    private String postServiceConfigText(String configId,
                                         Map<String, String> config) {
        StringBuffer sb = new StringBuffer();
        sb.append("Posting config for id: '" + configId + "'");
        for (String key : config.keySet()) {
            sb.append("\t[" + key + "|" + config.get(key) + "]\n");
        }
        return sb.toString();
    }

    private String getInstallURL() {
        return this.baseURL + "/services/install";
    }

    private String getUninstallURL() {
        return this.baseURL + "/services/uninstall";
    }

    private String getListURL() {
        return this.baseURL + "/services/list";
    }

    private String getConfigureURL(String configId) {
        return this.baseURL + "/services/configure/" + configId;
    }

    public RestHttpHelper getRester() {
        return rester;
    }

    public void setRester(RestHttpHelper rester) {
        this.rester = rester;
    }

    public ServiceSerializer getSerializer() {
        if (serializer == null) {
            serializer = new XMLServiceSerializerImpl();
        }
        return serializer;
    }

    public void setSerializer(ServiceSerializer serializer) {
        this.serializer = serializer;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

}
