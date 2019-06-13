/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.client.nameresolver;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.config.GrpcChannelProperties;
import net.devh.boot.grpc.client.config.GrpcChannelsProperties;

/**
 * A {@link NameResolver} factory that uses the the properties to rewrite the target uri.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public class ConfigMappedNameResolverFactory extends NameResolver.Factory {

    private final GrpcChannelsProperties config;
    private final NameResolver.Factory delegate;
    private final Function<String, URI> defaultUriMapper;

    /**
     * Creates a new ConfigMappedNameResolverFactory with the given config that resolves the remapped target uri using
     * the grpc's registered name resolvers.
     *
     * @param config The config used to remap the target uri.
     * @param defaultUriMapper The function to use when no uri is configured for a certain endpoint. This can be useful
     *        if the address can be derived from the client name.
     */
    @SuppressWarnings("deprecation")
    // TODO: Update this to grpc-java 1.21 in v2.6.0
    public ConfigMappedNameResolverFactory(final GrpcChannelsProperties config,
            Function<String, URI> defaultUriMapper) {
        this(config, NameResolverProvider.asFactory(), defaultUriMapper);
    }

    /**
     * Creates a new ConfigMappedNameResolverFactory with the given config that resolves the remapped target uri using
     * the given delegate.
     *
     * @param config The config used to remap the target uri.
     * @param delegate The delegate used to resolve the remapped target uri.
     * @param defaultUriMapper The function to use when no uri is configured for a certain endpoint. This can be useful
     *        if the address can be derived from the client name.
     */
    public ConfigMappedNameResolverFactory(final GrpcChannelsProperties config, final NameResolver.Factory delegate,
            Function<String, URI> defaultUriMapper) {
        this.config = requireNonNull(config, "config");
        this.delegate = requireNonNull(delegate, "delegate");
        this.defaultUriMapper = requireNonNull(defaultUriMapper, "defaultUriMapper");
    }

    @Nullable
    @Override
    @Deprecated
    // TODO: Update this to grpc-java 1.21 in v2.6.0
    public NameResolver newNameResolver(final URI targetUri, final io.grpc.NameResolver.Helper helper) {
        final String clientName = targetUri.toString();
        final GrpcChannelProperties clientConfig = this.config.getChannel(clientName);
        URI remappedUri = clientConfig.getAddress();
        if (remappedUri == null) {
            remappedUri = this.defaultUriMapper.apply(clientName);
            if (remappedUri == null) {
                throw new IllegalStateException("No targetUri provided for '" + clientName + "'"
                        + " and defaultUri mapper returned null.");
            }
        }
        log.debug("Remapping target URI for {} to {} via {}", clientName, remappedUri, this.delegate);
        return this.delegate.newNameResolver(remappedUri, helper);
    }

    @Override
    public String getDefaultScheme() {
        // The config does not use schemes at all
        return "";
    }

    @Override
    public String toString() {
        return "ConfigMappedNameResolverFactory [config=" + this.config + ", delegate=" + this.delegate + "]";
    }

}
