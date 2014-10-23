package com.sikulix.remoteserver.server;

import com.sikulix.remoteserver.service.CommandLineService;
import com.sikulix.remoteserver.service.FileService;
import com.sikulix.remoteserver.service.ImageService;
import com.sikulix.remoteserver.utils.ObjectMapperProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ServerConfiguration extends ResourceConfig {
    public ServerConfiguration() {
        super(ObjectMapperProvider.class, CommandLineService.class, ImageService.class, MultiPartFeature.class,
                JacksonFeature.class, FileService.class);
    }
}