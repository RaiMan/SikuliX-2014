package com.sikulix.remoteserver.server;

import com.sikulix.remoteserver.service.CommandLineService;
import com.sikulix.remoteserver.service.FileTransferService;
import com.sikulix.remoteserver.service.ImageService;
import com.sikulix.remoteserver.utils.ObjectMapperProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ServerConfiguration extends ResourceConfig {
    public ServerConfiguration() {
        super(ObjectMapperProvider.class, CommandLineService.class, FileTransferService.class, ImageService.class,
                MultiPartFeature.class, JacksonFeature.class);
    }
}