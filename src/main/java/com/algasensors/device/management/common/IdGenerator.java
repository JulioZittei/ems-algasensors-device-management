package com.algasensors.device.management.common;

import io.hypersistence.tsid.TSID;

import java.util.Optional;

public class IdGenerator {

    private static final TSID.Factory factory;

    static {
        Optional.ofNullable(System.getenv("tsid.node"))
                .ifPresent( node -> System.setProperty("tsid.node", node));

        Optional.ofNullable(System.getenv("tsid.node.count"))
                .ifPresent( nodeCount ->  System.setProperty("tsid.node.count", nodeCount));

        factory = TSID.Factory.builder().build();
    }

    private IdGenerator(){}

    public static TSID generateTSID() {
        return factory.generate();
    }
}
