package org.example.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.example.domainservice.ReviewServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.server.host}")
    private String grpcServerHost;

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress(grpcServerHost, grpcServerPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public ReviewServiceGrpc.ReviewServiceBlockingStub reviewServiceStub(ManagedChannel channel) {
        return ReviewServiceGrpc.newBlockingStub(channel);
    }
}
