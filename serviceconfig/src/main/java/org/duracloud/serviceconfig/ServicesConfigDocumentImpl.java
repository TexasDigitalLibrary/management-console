package org.duracloud.serviceconfig;

import java.io.InputStream;
import java.util.List;

/**
 * @see org.duracloud.serviceconfig.ServicesConfigDocument
 */
public class ServicesConfigDocumentImpl implements ServicesConfigDocument {

    private String version;
    private List<ServiceInfo> serviceInfos;

    public String getVersion() {
        return version;
    }

    public List<ServiceInfo> getServiceInfoList(InputStream xml) {
        return null;
    }

    public List<UserConfig> getUserConfigList(InputStream xml) {
        return null;
    }

    public String getServiceListAsXML(List<ServiceInfo> serviceList) {
        // TODO: Convert Service Infos list to XML
        return null;
    }

    public String getUserConfigAsXML(List<UserConfig> userConfig) {
        // TODO: Convert User Config List to XML
        return null;
    }

}